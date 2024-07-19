/*
 * Copyright 2024 The Dapr Authors
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
limitations under the License.
*/

package io.diagrid.spring.core.keyvalue.repository;

import io.diagrid.spring.core.keyvalue.PostgreSQLDaprKeyValueAdapter;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.data.config.ParsingUtils;
import org.springframework.data.keyvalue.core.KeyValueTemplate;
import org.springframework.data.keyvalue.core.QueryEngine;
import org.springframework.data.keyvalue.core.SortAccessor;
import org.springframework.data.keyvalue.repository.config.KeyValueRepositoryConfigurationExtension;
import org.springframework.data.repository.config.RepositoryConfigurationExtension;
import org.springframework.data.repository.config.RepositoryConfigurationSource;
import org.springframework.lang.Nullable;

import java.util.Map;

/**
 * {@link RepositoryConfigurationExtension} for Dapr-based repositories.
 */
@SuppressWarnings("unchecked")
public class DaprRepositoryConfigurationExtension extends KeyValueRepositoryConfigurationExtension {

  @Nullable
  private static SortAccessor<?> getSortAccessor(RepositoryConfigurationSource source) {

    Class<? extends SortAccessor<?>> sortAccessorType = (Class<? extends SortAccessor<?>>) getAnnotationAttributes(
        source).get("sortAccessor");

    if (sortAccessorType != null && !sortAccessorType.isInterface()) {
      return BeanUtils.instantiateClass(sortAccessorType);
    }

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

    BeanDefinitionBuilder adapterBuilder =
        BeanDefinitionBuilder.rootBeanDefinition(PostgreSQLDaprKeyValueAdapter.class);
    //adapterBuilder.addConstructorArgValue(getMapTypeToUse(configurationSource));

    SortAccessor<?> sortAccessor = getSortAccessor(configurationSource);

    if (sortAccessor != null) {
      adapterBuilder.addConstructorArgValue(sortAccessor);
    }

    BeanDefinitionBuilder builder = BeanDefinitionBuilder.rootBeanDefinition(KeyValueTemplate.class);
    builder
        .addConstructorArgValue(ParsingUtils.getSourceBeanDefinition(adapterBuilder, configurationSource.getSource()));
    builder.setRole(BeanDefinition.ROLE_SUPPORT);

    return ParsingUtils.getSourceBeanDefinition(builder, configurationSource.getSource());
  }
}