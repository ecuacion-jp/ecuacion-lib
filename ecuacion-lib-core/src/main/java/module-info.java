/*
 * Copyright © 2012 ecuacion.jp (info@ecuacion.jp)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Provides basic functions used as common utilities.
 * 
 * <p>Classes and Utilities for {@code bean validation} are included 
 * sinse it is used in wide variation of projects.</p>
 */
module jp.ecuacion.lib.core {
  exports jp.ecuacion.lib.core.annotation;
  exports jp.ecuacion.lib.core.beanvalidation.bean;
  exports jp.ecuacion.lib.core.beanvalidation.validator;
  exports jp.ecuacion.lib.core.constant;
  exports jp.ecuacion.lib.core.exception.checked;
  exports jp.ecuacion.lib.core.exception.unchecked;
  exports jp.ecuacion.lib.core.logging;
  exports jp.ecuacion.lib.core.spi;
  exports jp.ecuacion.lib.core.spi.impl;
  exports jp.ecuacion.lib.core.util;

  uses jp.ecuacion.lib.core.spi.MessagesLibCoreProvider;
  uses jp.ecuacion.lib.core.spi.MessagesUtilPoiProvider;
  
  provides jp.ecuacion.lib.core.spi.MessagesLibCoreProvider
      with jp.ecuacion.lib.core.spi.impl.internal.MessagesLibCoreProviderImpl;
  
  requires transitive jakarta.validation;
  requires jakarta.mail;
  requires jakarta.annotation;
  requires org.apache.commons.exec;
  requires org.slf4j;
  requires org.apache.commons.lang3;
  requires org.hibernate.validator;
  
  // for test
  uses jp.ecuacion.lib.core.spi.MessagesTestProvider;
  provides jp.ecuacion.lib.core.spi.MessagesTestProvider
      with jp.ecuacion.lib.core.spi.impl.internal.MessagesTestProviderImpl;
  uses jp.ecuacion.lib.core.spi.Test92NoneProvider;
  provides jp.ecuacion.lib.core.spi.Test92NoneProvider
      with jp.ecuacion.lib.core.spi.impl.internal.Test92NoneProviderImpl;
  uses jp.ecuacion.lib.core.spi.Test92LangProvider;
  provides jp.ecuacion.lib.core.spi.Test92LangProvider
      with jp.ecuacion.lib.core.spi.impl.internal.Test92LangProviderImpl;
  uses jp.ecuacion.lib.core.spi.Test92NoneAndLangProvider;
  provides jp.ecuacion.lib.core.spi.Test92NoneAndLangProvider
      with jp.ecuacion.lib.core.spi.impl.internal.Test92NoneAndLangProviderImpl;
  uses jp.ecuacion.lib.core.spi.Test92NoneAndLangCountryProvider;
  provides jp.ecuacion.lib.core.spi.Test92NoneAndLangCountryProvider
      with jp.ecuacion.lib.core.spi.impl.internal.Test92NoneAndLangCountryProviderImpl;
  uses jp.ecuacion.lib.core.spi.Test92NoneAndLangAndLangCountryProvider;
  provides jp.ecuacion.lib.core.spi.Test92NoneAndLangAndLangCountryProvider
      with jp.ecuacion.lib.core.spi.impl.internal.Test92NoneAndLangAndLangCountryProviderImpl;
  uses jp.ecuacion.lib.core.spi.Test92DuplicateInOneFileProvider;
  provides jp.ecuacion.lib.core.spi.Test92DuplicateInOneFileProvider
      with jp.ecuacion.lib.core.spi.impl.internal.Test92DuplicateInOneFileProviderImpl;
  uses jp.ecuacion.lib.core.spi.Test92DuplicateInMultipleFilesProvider;
  provides jp.ecuacion.lib.core.spi.Test92DuplicateInMultipleFilesProvider
      with jp.ecuacion.lib.core.spi.impl.internal.Test92DuplicateInMultipleFilesProviderImpl;
  uses jp.ecuacion.lib.core.spi.Test92DuplicateInMultipleFilesCoreProvider;
  provides jp.ecuacion.lib.core.spi.Test92DuplicateInMultipleFilesCoreProvider
      with jp.ecuacion.lib.core.spi.impl.internal.Test92DuplicateInMultipleFilesCoreProviderImpl;
}
