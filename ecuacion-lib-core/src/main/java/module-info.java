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
  exports jp.ecuacion.lib.core.exception;
  exports jp.ecuacion.lib.core.item;
  exports jp.ecuacion.lib.core.jakartavalidation.bean;
  exports jp.ecuacion.lib.core.jakartavalidation.constraints;
  exports jp.ecuacion.lib.core.jakartavalidation.constraintvalidator;
  exports jp.ecuacion.lib.core.logging;
  exports jp.ecuacion.lib.core.spi;
  exports jp.ecuacion.lib.core.spi.impl;
  exports jp.ecuacion.lib.core.test.spi;
  exports jp.ecuacion.lib.core.util;
  exports jp.ecuacion.lib.core.util.enums;
  exports jp.ecuacion.lib.core.violation;

  requires transitive jakarta.validation;
  requires jakarta.mail;
  requires transitive org.slf4j;
  requires org.apache.commons.lang3;
  requires org.hibernate.validator;
  requires jakarta.el;
  requires transitive org.jspecify;

  opens jp.ecuacion.lib.core.jakartavalidation.constraints to org.hibernate.validator;
  opens jp.ecuacion.lib.core.violation to org.hibernate.validator;

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

  // ecuacion lib / sutil / splib: messages
  uses jp.ecuacion.lib.core.spi.MessagesUtilExcelTableProvider;
  uses jp.ecuacion.lib.core.spi.MessagesUtilExcelReportToPdfProvider;

  // ecuacion-lib-core
  provides jp.ecuacion.lib.core.spi.MessagesLibCoreProvider
      with jp.ecuacion.lib.core.spi.impl.internal.MessagesLibCoreProviderImpl;
  provides jp.ecuacion.lib.core.spi.ValidationMessagesLibCoreProvider
      with jp.ecuacion.lib.core.spi.impl.internal.ValidationMessagesLibCoreProviderImpl;
  provides jp.ecuacion.lib.core.spi.ValidationMessagesWithItemNamesLibCoreProvider with
      jp.ecuacion.lib.core.spi.impl.internal.ValidationMessagesWithItemNamesLibCoreProviderImpl;

  uses jp.ecuacion.lib.core.spi.MessagesLibCoreProvider;
  uses jp.ecuacion.lib.core.spi.ValidationMessagesLibCoreProvider;
  uses jp.ecuacion.lib.core.spi.ValidationMessagesWithItemNamesLibCoreProvider;

  // ecuacion-lib-validation
  provides jp.ecuacion.lib.core.spi.MessagesLibValidationProvider
      with jp.ecuacion.lib.core.spi.impl.internal.MessagesLibValidationProviderImpl;
  provides jp.ecuacion.lib.core.spi.ValidationMessagesLibValidationProvider
      with jp.ecuacion.lib.core.spi.impl.internal.ValidationMessagesLibValidationProviderImpl;
  provides jp.ecuacion.lib.core.spi.ValidationMessagesWithItemNamesLibValidationProvider with
      jp.ecuacion.lib.core.spi.impl.internal.ValidationMessagesWithItemNamesLibValidationProviderImpl;

  uses jp.ecuacion.lib.core.spi.MessagesLibValidationProvider;
  uses jp.ecuacion.lib.core.spi.ValidationMessagesLibValidationProvider;
  uses jp.ecuacion.lib.core.spi.ValidationMessagesWithItemNamesLibValidationProvider;

  // ecuacion-lib-validation-business-messages
  provides jp.ecuacion.lib.core.spi.ValidationMessagesLibValidationBusinessMessagesProvider
      with jp.ecuacion.lib.core.spi.impl.internal
      .ValidationMessagesLibValidationBusinessMessagesProviderImpl;
  provides jp.ecuacion.lib.core.spi
      .ValidationMessagesWithItemNamesLibValidationBusinessMessagesProvider with
      jp.ecuacion.lib.core.spi.impl.internal
      .ValidationMessagesWithItemNamesLibValidationBusinessMessagesProviderImpl;

  uses jp.ecuacion.lib.core.spi.ValidationMessagesLibValidationBusinessMessagesProvider;
  uses jp.ecuacion.lib.core.spi
      .ValidationMessagesWithItemNamesLibValidationBusinessMessagesProvider;

  // for test below

  opens jp.ecuacion.lib.core.util to org.hibernate.validator;

  // lib-core
  provides jp.ecuacion.lib.core.test.spi.ApplicationLibCoreTestProvider
      with jp.ecuacion.lib.core.test.spi.internal.ApplicationLibCoreTestProviderImpl;
  provides jp.ecuacion.lib.core.test.spi.MessagesLibCoreTestProvider
      with jp.ecuacion.lib.core.test.spi.internal.MessagesLibCoreTestProviderImpl;
  provides jp.ecuacion.lib.core.test.spi.StringsLibCoreTestProvider
      with jp.ecuacion.lib.core.test.spi.internal.StringsLibCoreTestProviderImpl;
  provides jp.ecuacion.lib.core.test.spi.EnumNamesLibCoreTestProvider
      with jp.ecuacion.lib.core.test.spi.internal.EnumNamesLibCoreTestProviderImpl;
  provides jp.ecuacion.lib.core.test.spi.ValidationMessagesLibCoreTestProvider
      with jp.ecuacion.lib.core.test.spi.internal.ValidationMessagesLibCoreTestProviderImpl;
  provides jp.ecuacion.lib.core.test.spi.ValidationMessagesWithItemNamesLibCoreTestProvider with
      jp.ecuacion.lib.core.test.spi.internal.ValidationMessagesWithItemNamesLibCoreTestProviderImpl;

  uses jp.ecuacion.lib.core.test.spi.ApplicationLibCoreTestProvider;
  uses jp.ecuacion.lib.core.test.spi.MessagesLibCoreTestProvider;
  uses jp.ecuacion.lib.core.test.spi.StringsLibCoreTestProvider;
  uses jp.ecuacion.lib.core.test.spi.EnumNamesLibCoreTestProvider;
  uses jp.ecuacion.lib.core.test.spi.ValidationMessagesLibCoreTestProvider;
  uses jp.ecuacion.lib.core.test.spi.ValidationMessagesWithItemNamesLibCoreTestProvider;

  // lib-core : customized
  provides jp.ecuacion.lib.core.test.spi.PropsLocaleNoneLibCoreTestProvider
      with jp.ecuacion.lib.core.test.spi.internal.PropsLocaleNoneLibCoreTestProviderImpl;
  provides jp.ecuacion.lib.core.test.spi.PropsLocaleLangLibCoreTestProvider
      with jp.ecuacion.lib.core.test.spi.internal.PropsLocaleLangLibCoreTestProviderImpl;
  provides jp.ecuacion.lib.core.test.spi.PropsLocaleNoneAndLangLibCoreTestProvider with
      jp.ecuacion.lib.core.test.spi.internal.PropsLocaleNoneAndLangLibCoreTestProviderImpl;
  provides jp.ecuacion.lib.core.test.spi.PropsLocaleNoneAndLangCountryLibCoreTestProvider with
      jp.ecuacion.lib.core.test.spi.internal.PropsLocaleNoneAndLangCountryLibCoreTestProviderImpl;
  provides jp.ecuacion.lib.core.test.spi
      .PropsLocaleNoneAndLangAndLangCountryLibCoreTestProvider with
      jp.ecuacion.lib.core.test.spi.internal
      .PropsLocaleNoneAndLangAndLangCountryLibCoreTestProviderImpl;
  provides jp.ecuacion.lib.core.test.spi.PropsLocaleDuplicateInOneFileLibCoreTestProvider
      with jp.ecuacion.lib.core.test.spi.internal.PropsLocaleDuplicateInOneFileProviderImpl;
  provides jp.ecuacion.lib.core.test.spi
      .PropsLocaleDuplicateInMultipleFilesLibCoreTestProvider with
      jp.ecuacion.lib.core.test.spi.internal.PropsLocaleDuplicateInMultipleFilesProviderImpl;
  provides jp.ecuacion.lib.core.test.spi
      .PropsLocaleDuplicateInMultipleFilesLibCore2ndTestProvider with
      jp.ecuacion.lib.core.test.spi.internal
      .PropsLocaleDuplicateInMultipleFilesCoreProviderImpl;

  uses jp.ecuacion.lib.core.test.spi.PropsLocaleNoneLibCoreTestProvider;
  uses jp.ecuacion.lib.core.test.spi.PropsLocaleLangLibCoreTestProvider;
  uses jp.ecuacion.lib.core.test.spi.PropsLocaleNoneAndLangLibCoreTestProvider;
  uses jp.ecuacion.lib.core.test.spi.PropsLocaleNoneAndLangCountryLibCoreTestProvider;
  uses jp.ecuacion.lib.core.test.spi.PropsLocaleNoneAndLangAndLangCountryLibCoreTestProvider;
  uses jp.ecuacion.lib.core.test.spi.PropsLocaleDuplicateInOneFileLibCoreTestProvider;
  uses jp.ecuacion.lib.core.test.spi.PropsLocaleDuplicateInMultipleFilesLibCoreTestProvider;
  uses jp.ecuacion.lib.core.test.spi.PropsLocaleDuplicateInMultipleFilesLibCore2ndTestProvider;

  // lib-validation
  provides jp.ecuacion.lib.core.test.spi.MessagesLibValidationTestProvider
      with jp.ecuacion.lib.core.test.spi.internal.MessagesLibValidationTestProviderImpl;
  provides jp.ecuacion.lib.core.test.spi.ItemNamesLibValidationTestProvider
      with jp.ecuacion.lib.core.test.spi.internal.ItemNamesLibValidationTestProviderImpl;
  provides jp.ecuacion.lib.core.test.spi.EnumNamesLibValidationTestProvider
      with jp.ecuacion.lib.core.test.spi.internal.EnumNamesLibValidationTestProviderImpl;

  uses jp.ecuacion.lib.core.test.spi.MessagesLibValidationTestProvider;
  uses jp.ecuacion.lib.core.test.spi.ItemNamesLibValidationTestProvider;
  uses jp.ecuacion.lib.core.test.spi.EnumNamesLibValidationTestProvider;
}
