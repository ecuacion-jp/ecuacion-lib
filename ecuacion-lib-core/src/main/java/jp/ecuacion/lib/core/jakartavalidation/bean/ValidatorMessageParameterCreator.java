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
package jp.ecuacion.lib.core.jakartavalidation.bean;

import jakarta.annotation.Nonnull;
import jakarta.validation.ConstraintViolation;
import java.util.Map;
import java.util.Set;
import jp.ecuacion.lib.core.jakartavalidation.bean.ConstraintViolationBean.LocalizedMessageParameter;

/**
 * Provides message parameter set used in ValidationMessages.properties as "{param}".
 * 
 * <p>Parameters defined in an annotation is automatically added to the parameter set 
 *     which is referred when the message is built, 
 *     but it's not enough when you want to create user-friendly messages.</p>
 * 
 * <p>Using this you can create flexible messages for each validator.</p>
 */
public interface ValidatorMessageParameterCreator {

  /**
   * Creates and returns message parameter set.
   */
  @Nonnull
  Set<LocalizedMessageParameter> create(ConstraintViolation<?> cv, Map<String, Object> paramMap,
      String rootRecordNameForForm);
}
