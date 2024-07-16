package io.diagrid.spring.core.keyvalue.repository;


import java.lang.annotation.Annotation;

import org.springframework.data.repository.config.RepositoryBeanDefinitionRegistrarSupport;
import org.springframework.data.repository.config.RepositoryConfigurationExtension;

/**
 * Dapr specific {@link RepositoryBeanDefinitionRegistrarSupport} implementation.
 *
 */
public class DaprRepositoriesRegistrar extends RepositoryBeanDefinitionRegistrarSupport {

	@Override
	protected Class<? extends Annotation> getAnnotation() {
		return EnableDaprRepositories.class;
	}

	@Override
	protected RepositoryConfigurationExtension getExtension() {
		return new DaprRepositoryConfigurationExtension();
	}
}
