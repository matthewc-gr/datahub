- Start Date: 2021-08-02
- RFC PR:
- Implementation PR(s):

# Metadata orchestration

## Summary

DataHub is designed to collate externally managed metadata, as such it doesn't impose any rules on metadata changes (
besides minimum data requirements to create entities/aspects). To start using DataHub for orchestration purposes we
require the ability to flexibly validate/modify a change before it is applied.

## Motivation

We're looking to replace an in-house Data Catalog with DataHub. The catalog we're replacing has the ability to
reject/ignore metadata changes based on a set of validation rules. The expected outcome would be that we would be able
to specify rules that different entities/aspects would be required to follow such that we aren't required to fork
DataHub.

## Requirements

- Validate metadata changes before they are applied based on properties of the Entity/Aspect. We would require the
  ability to ignore certain changes based on the previous value.
- Easily create/change the logic surrounding a metadata change.
- Not maintain a fork of DataHub. We could replicate our desired behaviour by just modifying DataHub, however one driver
  of replacing an internal catalog with DataHub is such that we no longer are required to maintain a Catalog internally.

### Extensibility

This design should be extensible such that other DataHub developers can add their own custom behaviour without impacting
existing logic.

## Detailed design

Option 1 (Sidecar jar):

Todo: System diagram

DataHub has an unused
function ([updateLambda](https://github.com/linkedin/datahub/blob/352a0abf8d7e4dd5d5664a8c7cdf3d77bf6f1c51/metadata-io/src/main/java/com/linkedin/metadata/entity/ebean/EbeanEntityService.java#L236))
that is run before each Aspect change proposal. 

One option would be to extend this function such that we can load in this function from a separate jar.  

> This is the bulk of the RFC.

> Explain the design in enough detail for somebody familiar with the framework to understand, and for somebody familiar
> with the implementation to implement. This should get into specifics and corner-cases, and include examples of how the
> feature is used. Any new terminology should be defined here.

## How we teach this

> What names and terminology work best for these concepts and why? How is this idea best presented? As a continuation
> of existing DataHub patterns, or as a wholly new one?

> What audience or audiences would be impacted by this change? Just DataHub backend developers? Frontend developers?
> Users of the DataHub application itself?

> Would the acceptance of this proposal mean the DataHub guides must be re-organized or altered? Does it change how
> DataHub is taught to new users at any level?

> How should this feature be introduced and taught to existing audiences?

## Drawbacks

> Why should we *not* do this? Please consider the impact on teaching DataHub, on the integration of this feature with
> other existing and planned features, on the impact of the API churn on existing apps, etc.

> There are tradeoffs to choosing any path, please attempt to identify them here.

## Alternatives

> What other designs have been considered? What is the impact of not doing this?

> This section could also include prior art, that is, how other frameworks in the same domain have solved this problem.

## Rollout / Adoption Strategy

> If we implemented this proposal, how will existing users / developers adopt it? Is it a breaking change? Can we write
> automatic refactoring / migration tools? Can we provide a runtime adapter library for the original API it replaces?

## Future Work

> Describe any future projects, at a very high level, that will build off this proposal. This does not need to be
> exhaustive, nor does it need to be anything you work on. It just helps reviewers see how this can be used in the
> future, so they can help ensure your design is flexible enough.

## Unresolved questions

> Optional, but suggested for first drafts. What parts of the design are still TBD?