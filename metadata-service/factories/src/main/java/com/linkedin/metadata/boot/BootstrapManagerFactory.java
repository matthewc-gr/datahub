package com.linkedin.metadata.boot;

import com.google.common.collect.ImmutableList;
import com.linkedin.gms.factory.entity.EntityServiceFactory;
import com.linkedin.metadata.boot.steps.IngestDataPlatformInstancesStep;
import com.linkedin.metadata.boot.steps.IngestDataPlatformsStep;
import com.linkedin.metadata.boot.steps.IngestPoliciesStep;
import com.linkedin.metadata.entity.EntityService;
import io.ebean.EbeanServer;
import javax.annotation.Nonnull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Scope;


@Configuration
@Import({EntityServiceFactory.class})
public class BootstrapManagerFactory {

  @Autowired
  @Qualifier("entityService")
  private EntityService _entityService;

  @Autowired(required = false)
  @Qualifier("ebeanServer")
  private EbeanServer _server;

  @Bean(name = "bootstrapManager")
  @Scope("singleton")
  @Nonnull
  protected BootstrapManager createInstance() {
    final IngestPoliciesStep ingestPoliciesStep = new IngestPoliciesStep(_entityService);
    final IngestDataPlatformsStep ingestDataPlatformsStep = new IngestDataPlatformsStep(_entityService);
    return _server == null
      ? new BootstrapManager(ImmutableList.of(ingestPoliciesStep, ingestDataPlatformsStep))
      : new BootstrapManager(ImmutableList.of(ingestPoliciesStep, ingestDataPlatformsStep, new IngestDataPlatformInstancesStep(_entityService, _server)));
  }
}
