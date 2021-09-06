- Start Date: 2021-08-02
- RFC PR:
- Implementation PR(s):

# Metadata validation

## Summary

DataHub is designed to collate externally managed metadata, as such it doesn't impose any rules on metadata changes
(besides minimum data requirements to create entities/aspects). To start using DataHub for orchestration purposes we
require the ability to flexibly validate/modify a change before it is applied.

## Motivation

We're looking to replace an in-house Data Catalog with DataHub. The catalog we're replacing has the ability to
reject/ignore metadata changes based on a set of validation rules. The expected outcome would be that we would be able
to specify rules that different entities/aspects would be required to follow such that we aren't required to fork
DataHub.

## Requirements

- Validate metadata changes before they are applied based on properties of the incoming aspect change proposal. We
  require the ability to ignore certain changes based on the previous value.
- Easily create/change validation surrounding a metadata change.
- Not maintain a fork of DataHub. We could replicate our desired behaviour by just modifying DataHub in a fork, however
  one driver of replacing an internal catalog with DataHub is such that we no longer are required to maintain a service
  that handles orchestration logic.

## Detailed design

The metadata-io package currently has a
function ([updateLambda](https://github.com/linkedin/datahub/blob/352a0abf8d7e4dd5d5664a8c7cdf3d77bf6f1c51/metadata-io/src/main/java/com/linkedin/metadata/entity/ebean/EbeanEntityService.java#L236))
that is run before each aspect change proposal. It accepts the previous version of an aspect (or null if it's the first
version) and the proposed aspect change. It currently does nothing with this information and returns the proposed new
aspect value.

We could extend this behaviour such that users can choose to overload it.

### Proposed implementation

1. On metadata-service start up, load a jar from a specified location (defined in config).
2. The user defined jar would contain a class that implements a [specified interface](#contract-example) that the
   metadata service can understand. (This contract could belong in a separate contracts module that is deployed to an
   artefact repository).
3. Within the metadata-io package instantiate this class via reflection (define the class name in config), that is then
   injected into the EbeanEntityService.
4. On each update call pass the previous aspect version and the proposed new version into the custom function.
5. If a validation fails bubble up the details to the caller.

If no custom jar is detected on start up keep the existing noop behaviour.

### Developer Lifecycle Example

Suppose we require the ability to ignore metadata updates where a property contains a specific string. The following
steps indicates the development lifecycle:

1. Create a new project that will contain the custom validation logic.
2. Add a dependency to a DataHub contract library that contains the expected contracts.
3. Implement the class and write the custom validation rules.
4. Build and deploy the jar to a location the running metadata-service will have access to.
5. Restart the metadata-service and provide the path to the new jar via config.
6. All new updates will pass through the new code path checking whether that aspect change contains the disallowed
   string.

### Contract Example

```java
public class ValidationResult {
    Optional<RecordTemplate> modifiedValue; // Empty if validation fails
    List<Issue> issues; // A list of why the validation failed, empty if success
}
```

```java
public interface UpdateValidation {
    ValidationResult validate(Optional<RecordTemplate> previousRecord, RecordTemplate newRecord);
}
```

The previous class/interface would be defined in a package that is published to an artefact repository that both the
metadata-io module and the custom validation logic references.

## Drawbacks

* If the contract between DataHub and the custom validation code changes, it would be a breaking change for other users
  of this feature.
* If wanting to apply lots of validation rules to many entities, it might introduce complex branching logic that could
  be difficult to maintain. Foresee having a large switch statement to route various validations based on different
  entity/aspect types.
* Difficult to see what validation is being applied to what aspect at runtime.
* Deployment of DataHub would be more complicated than it is currently, before starting the metadata service the jar
  must exist in the expected location.
* Would need to investigate the effects on performance if using Reflection to load the class and call on each update.

## Alternatives

* Modify DataHub to provide workflow functionality, where users can upload
  a [BPMN](https://en.wikipedia.org/wiki/Business_Process_Model_and_Notation) diagram into DataHub and offer similar
  functionality to workflow engines like Camunda. Downsides include introducing much more complexity into DataHub.

* Instead of loading validation logic by jars, create a validation service that DataHub calls out to. Requires DataHub
  clients to host and maintain a separate service. Also could impact performance by adding a separate http hop per
  update.

## Rollout / Adoption Strategy

Introducing the proposed design above wouldn't be a breaking change as if no custom jars are found on start up then this
feature is ignored.

## Unresolved questions

* How should validation errors be propagated back to the user?
* How can we determine what aspect type the RecordTemplate corresponds to?
* Is Reflection the optimal way to load in validations?
* How would we version the custom jars?