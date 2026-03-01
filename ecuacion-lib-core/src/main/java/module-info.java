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
 * <p>Classes and Utilities for {@code jakarta validation} are included 
 * sinse it is used in wide variation of projects.</p>
 */
module jp.ecuacion.lib.core {
  exports jp.ecuacion.lib.core.annotation;
  exports jp.ecuacion.lib.core.constant;
  exports jp.ecuacion.lib.core.exception.checked;
  exports jp.ecuacion.lib.core.exception.unchecked;
  exports jp.ecuacion.lib.core.item;
  exports jp.ecuacion.lib.core.jakartavalidation.bean;
  exports jp.ecuacion.lib.core.jakartavalidation.constraints;
  exports jp.ecuacion.lib.core.jakartavalidation.annotation;
  exports jp.ecuacion.lib.core.logging;
  exports jp.ecuacion.lib.core.spi;
  exports jp.ecuacion.lib.core.spi.impl;
  exports jp.ecuacion.lib.core.test.spi;
  exports jp.ecuacion.lib.core.util;

  requires transitive jakarta.validation;
  requires jakarta.mail;
  requires jakarta.annotation;
  requires transitive org.slf4j;
  requires org.apache.commons.lang3;
  requires org.hibernate.validator;
  requires jakarta.el;

  // for test
  opens jp.ecuacion.lib.core.jakartavalidation.bean to org.hibernate.validator;

  // apps: application
  uses jp.ecuacion.lib.core.spi.ApplicationProvider;
  uses jp.ecuacion.lib.core.spi.ApplicationProfileProvider;
  uses jp.ecuacion.lib.core.spi.ApplicationBaseProvider;
  uses jp.ecuacion.lib.core.spi.ApplicationCoreProvider;
  uses jp.ecuacion.lib.core.spi.ApplicationCoreProfileProvider;

  // apps: messages
  uses jp.ecuacion.lib.core.spi.MessagesProvider;
  uses jp.ecuacion.lib.core.spi.MessagesBaseProvider;
  uses jp.ecuacion.lib.core.spi.MessagesCoreProvider;

  // apps: item_names
  uses jp.ecuacion.lib.core.spi.ItemNamesProvider;
  uses jp.ecuacion.lib.core.spi.ItemNamesBaseProvider;
  uses jp.ecuacion.lib.core.spi.ItemNamesCoreProvider;

  // apps: enum_names
  uses jp.ecuacion.lib.core.spi.EnumNamesProvider;
  uses jp.ecuacion.lib.core.spi.EnumNamesBaseProvider;
  uses jp.ecuacion.lib.core.spi.EnumNamesCoreProvider;

  // apps: ValidationMessages
  uses jp.ecuacion.lib.core.spi.ValidationMessagesLibCoreProvider;

  // ecuacion lib / sutil / splib: messages
  uses jp.ecuacion.lib.core.spi.MessagesLibCoreProvider;
  uses jp.ecuacion.lib.core.spi.MessagesUtilPoiProvider;
  uses jp.ecuacion.lib.core.spi.MessagesUtilJpaProvider;
  uses jp.ecuacion.lib.core.spi.MessagesUtilPdfboxProvider;

  provides jp.ecuacion.lib.core.spi.MessagesLibCoreProvider
      with jp.ecuacion.lib.core.spi.impl.internal.MessagesLibCoreProviderImpl;

  provides jp.ecuacion.lib.core.spi.ValidationMessagesLibCoreProvider
      with jp.ecuacion.lib.core.spi.impl.internal.ValidationMessagesLibCoreProviderImpl;

  provides jp.ecuacion.lib.core.spi.MessagesLibValidationProvider
      with jp.ecuacion.lib.core.spi.impl.internal.MessagesLibValidationProviderImpl;
  provides jp.ecuacion.lib.core.spi.ValidationMessagesLibValidationProvider
      with jp.ecuacion.lib.core.spi.impl.internal.ValidationMessagesLibValidationProviderImpl;

  uses jp.ecuacion.lib.core.spi.MessagesLibValidationProvider;
  uses jp.ecuacion.lib.core.spi.ValidationMessagesLibValidationProvider;

  // for test below

  opens jp.ecuacion.lib.core.util to org.hibernate.validator;

  // lib-core
  provides jp.ecuacion.lib.core.test.spi.ApplicationLibCoreTestProvider
      with jp.ecuacion.lib.core.test.spi.internal.ApplicationLibCoreTestProviderImpl;
  provides jp.ecuacion.lib.core.test.spi.MessagesLibCoreTestProvider
      with jp.ecuacion.lib.core.test.spi.internal.MessagesLibCoreTestProviderImpl;
  provides jp.ecuacion.lib.core.test.spi.ValidationMessagesLibCoreTestProvider
      with jp.ecuacion.lib.core.test.spi.internal.ValidationMessagesLibCoreTestProviderImpl;

  uses jp.ecuacion.lib.core.test.spi.ApplicationLibCoreTestProvider;
  uses jp.ecuacion.lib.core.test.spi.MessagesLibCoreTestProvider;
  uses jp.ecuacion.lib.core.test.spi.ValidationMessagesLibCoreTestProvider;

  // lib-core : customized
  provides jp.ecuacion.lib.core.test.spi.Test92NoneLibCoreTestProvider
      with jp.ecuacion.lib.core.test.spi.internal.Test92NoneLibCoreTestProviderImpl;
  provides jp.ecuacion.lib.core.test.spi.Test92LangLibCoreTestProvider
      with jp.ecuacion.lib.core.test.spi.internal.Test92LangLibCoreTestProviderImpl;
  provides jp.ecuacion.lib.core.test.spi.Test92NoneAndLangLibCoreTestProvider
      with jp.ecuacion.lib.core.test.spi.internal.Test92NoneAndLangLibCoreTestProviderImpl;
  provides jp.ecuacion.lib.core.test.spi.Test92NoneAndLangCountryLibCoreTestProvider
      with jp.ecuacion.lib.core.test.spi.internal.Test92NoneAndLangCountryLibCoreTestProviderImpl;
  provides jp.ecuacion.lib.core.test.spi.Test92NoneAndLangAndLangCountryLibCoreTestProvider
      with jp.ecuacion.lib.core.test.spi.internal.Test92NoneAndLangAndLangCountryLibCoreTestProviderImpl;
  provides jp.ecuacion.lib.core.test.spi.Test92DuplicateInOneFileLibCoreTestProvider
      with jp.ecuacion.lib.core.test.spi.internal.Test92DuplicateInOneFileProviderImpl;
  provides jp.ecuacion.lib.core.test.spi.Test92DuplicateInMultipleFilesLibCoreTestProvider
      with jp.ecuacion.lib.core.test.spi.internal.Test92DuplicateInMultipleFilesProviderImpl;
  provides jp.ecuacion.lib.core.test.spi.Test92DuplicateInMultipleFilesLibCore2ndTestProvider
      with jp.ecuacion.lib.core.test.spi.internal.Test92DuplicateInMultipleFilesCoreProviderImpl;

  uses jp.ecuacion.lib.core.test.spi.Test92NoneLibCoreTestProvider;
  uses jp.ecuacion.lib.core.test.spi.Test92LangLibCoreTestProvider;
  uses jp.ecuacion.lib.core.test.spi.Test92NoneAndLangLibCoreTestProvider;
  uses jp.ecuacion.lib.core.test.spi.Test92NoneAndLangCountryLibCoreTestProvider;
  uses jp.ecuacion.lib.core.test.spi.Test92NoneAndLangAndLangCountryLibCoreTestProvider;
  uses jp.ecuacion.lib.core.test.spi.Test92DuplicateInOneFileLibCoreTestProvider;
  uses jp.ecuacion.lib.core.test.spi.Test92DuplicateInMultipleFilesLibCoreTestProvider;
  uses jp.ecuacion.lib.core.test.spi.Test92DuplicateInMultipleFilesLibCore2ndTestProvider;

  // lib-validation
  provides jp.ecuacion.lib.core.test.spi.ItemNamesLibValidationTestProvider
      with jp.ecuacion.lib.core.test.spi.internal.ItemNamesLibValidationTestProviderImpl;

  uses jp.ecuacion.lib.core.test.spi.ItemNamesLibValidationTestProvider;
}
