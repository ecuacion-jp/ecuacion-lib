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
package jp.ecuacion.lib.core.jakartavalidation.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Provides the validation logic for {@code EnumElement}.
 */
public class EnumElementValidator implements ConstraintValidator<EnumElement, String> {

  private Class<?> enumClass;

  /**
   * Constructs a new instance.
   */
  public EnumElementValidator() {

  }

  /** Initializes an instance. */
  @Override
  public void initialize(EnumElement constraintAnnotation) {
    enumClass = constraintAnnotation.enumClass();
  }

  /**
   * Checks if an enum value exists.
   * 
   * <p>{@code Null} is valid following to the specification of Jakarta EE.<br>
   * {@code Empty ("")} is invalid.</p>
   */
  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    // true if value == null (which consists with the specification of jakarta validation)
    if (value == null) {
      return true;
    }

    try {
      // 取得したクラスがenumかを確認
      if (!enumClass.isEnum()) {
        throw new RuntimeException(
            "A class is found. An enum is supposed to be found: " + enumClass.getCanonicalName());
      }

      // 取得したenumがhasEnumFromNameメソッドを持つか確認
      Object[] objs = enumClass.getEnumConstants();
      for (Object obj : objs) {
        if (obj.toString().equals(value)) {
          return true;
        }
      }

      return false;

    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }
}
