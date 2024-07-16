package io.diagrid.spring.core.keyvalue.repository;


import java.util.Map;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.data.config.ParsingUtils;
import org.springframework.data.keyvalue.core.KeyValueTemplate;
import org.springframework.data.keyvalue.core.QueryEngine;
// import org.springframework.data.keyvalue.core.QueryEngineFactory;
import org.springframework.data.keyvalue.core.SortAccessor;
import org.springframework.data.keyvalue.repository.config.KeyValueRepositoryConfigurationExtension;
import org.springframework.data.repository.config.RepositoryConfigurationExtension;
import org.springframework.data.repository.config.RepositoryConfigurationSource;
import org.springframework.lang.Nullable;

import io.diagrid.spring.core.keyvalue.DaprKeyValueAdapter;

/**
 * {@link RepositoryConfigurationExtension} for Dapr-based repositories.
 *
 */
@SuppressWarnings("unchecked")
public class DaprRepositoryConfigurationExtension extends KeyValueRepositoryConfigurationExtension {

	@Override
	public String getModuleName() {
		return "Dapr";
	}

	@Override
	protected String getModulePrefix() {
		return "dapr";
	}

	@Override
	protected String getDefaultKeyValueTemplateRef() {
		return "daprKeyValueTemplate";
	}

	@Override
	protected AbstractBeanDefinition getDefaultKeyValueTemplateBeanDefinition(
			RepositoryConfigurationSource configurationSource) {

		BeanDefinitionBuilder adapterBuilder = BeanDefinitionBuilder.rootBeanDefinition(DaprKeyValueAdapter.class);
		//adapterBuilder.addConstructorArgValue(getMapTypeToUse(configurationSource));

		SortAccessor<?> sortAccessor = getSortAccessor(configurationSource);
		QueryEngine<?, ?, ?> queryEngine = getQueryEngine(sortAccessor, configurationSource);

		if (queryEngine != null) {
			adapterBuilder.addConstructorArgValue(queryEngine);
		} else if (sortAccessor != null) {
			adapterBuilder.addConstructorArgValue(sortAccessor);
		}

		BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(KeyValueTemplate.class);
		builder
				.addConstructorArgValue(ParsingUtils.getSourceBeanDefinition(adapterBuilder, configurationSource.getSource()));
		builder.setRole(BeanDefinition.ROLE_SUPPORT);

		return ParsingUtils.getSourceBeanDefinition(builder, configurationSource.getSource());
	}


	@Nullable
	private static SortAccessor<?> getSortAccessor(RepositoryConfigurationSource source) {

		Class<? extends SortAccessor<?>> sortAccessorType = (Class<? extends SortAccessor<?>>) getAnnotationAttributes(
				source).get("sortAccessor");

		if (sortAccessorType != null && !sortAccessorType.isInterface()) {
			return BeanUtils.instantiateClass(sortAccessorType);
		}

		return null;
	}

	@Nullable
	private static QueryEngine<?, ?, ?> getQueryEngine(@Nullable SortAccessor<?> sortAccessor,
			RepositoryConfigurationSource source) {

		// Class<? extends QueryEngineFactory> queryEngineFactoryType = (Class<? extends QueryEngineFactory>) getAnnotationAttributes(
		// 		source).get("queryEngineFactory");

		// if(queryEngineFactoryType == null || queryEngineFactoryType.isInterface()) {
		// 	return null;
		// }

		// if (sortAccessor != null) {
		// 	Constructor<? extends QueryEngineFactory> constructor = ClassUtils
		// 			.getConstructorIfAvailable(queryEngineFactoryType, SortAccessor.class);
		// 	if (constructor != null) {
		// 		return BeanUtils.instantiateClass(constructor, sortAccessor).create();
		// 	}
		// }

		// return BeanUtils.instantiateClass(queryEngineFactoryType).create();
        return null;
	}

	private static Map<String, Object> getAnnotationAttributes(RepositoryConfigurationSource source) {

		AnnotationMetadata annotationSource = (AnnotationMetadata) source.getSource();

		if (annotationSource == null) {
			throw new IllegalArgumentException("AnnotationSource not available");
		}

		Map<String, Object> annotationAttributes = annotationSource
				.getAnnotationAttributes(EnableDaprRepositories.class.getName());

		if (annotationAttributes == null) {
			throw new IllegalStateException("No annotation attributes for @EnableDaprRepositories");
		}

		return annotationAttributes;
	}
}