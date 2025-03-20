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

  private String enumPackage;
  private String enumClass;

  /**
   * Constructs a new instance.
   */
  public EnumElementValidator() {

  }

  /** Initializes an instance. */
  @Override
  public void initialize(EnumElement constraintAnnotation) {
    enumPackage = constraintAnnotation.enumPackage();
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
    Class<?> cls;

    // nullの場合はtrueとする。（bean validationの仕様に合わせ空文字はtrueにはしない）
    if (value == null) {
      return true;

    } else {
      String classFullName = enumPackage + "." + enumClass;
      try {
        // enumクラスが存在するかを確認
        cls = Class.forName(classFullName);
        // 取得したクラスがenumかを確認
        if (!cls.isEnum()) {
          throw new RuntimeException(
              "A class is found. An enum is supposed to be found: " + classFullName);
        }

        // 取得したenumがhasEnumFromNameメソッドを持つか確認
        Object[] objs = cls.getEnumConstants();
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
}
