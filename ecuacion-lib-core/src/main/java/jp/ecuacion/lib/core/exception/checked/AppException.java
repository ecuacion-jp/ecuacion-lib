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
package jp.ecuacion.lib.core.exception.checked;

/** 
 * Is an abstact exception class describing an occurence of a validation error in ecuacion apps.
 * 
 * <p>Let us call a validation of data executed by an app an "app-validation". 
 *     {@code AppException} is created and thrown 
 *     when the result of an app-validation is not okay.</p>
 * 
 * <p>App-validations are classified into two kinds as follows.</p>
 * 
 * <ol>
 * <li>input validation: the validation for each single input data, 
 *     like "not empty", "minimum value", ... </li>
 * <li>business logic validation: the validation from business requirements, 
 *     like "selective not empty", "the input value must be one of values in db", ...</li>
 * </ol>
 * 
 * <p>{@code BeanValidationAppException} covers the former, 
 *     and {@code BizLogicAppException} the latter. <br>
 *     Both of them are the child of {@code SingleAppException}.</p>
 */
public abstract class AppException extends Exception {

  private static final long serialVersionUID = 1L;

  /** Constructs a new instance. */
  public AppException() {

  }
}
