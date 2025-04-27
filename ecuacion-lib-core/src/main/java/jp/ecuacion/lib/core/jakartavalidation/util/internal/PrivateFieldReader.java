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
package jp.ecuacion.lib.core.jakartavalidation.util.internal;

import java.lang.reflect.Field;
import jp.ecuacion.lib.core.exception.unchecked.EclibRuntimeException;

/**
 * Offers reflection function to read values from private fields.
 * 
 * A class is created only for this reason because spotbug declares an error for it.
 * 
 * <code>
 * Public method 
 * jp.ecuacion.lib.core.util.internal.PrivateFieldReadUtil.getFieldValue(String, Object, String) 
 * uses reflection to modify a field it gets in its parameter 
 * which could increase the accessibility of any class. 
 * REFLF_REFLECTION_MAY_INCREASE_ACCESSIBILITY_OF_FIELD
 * </code>
 */
public class PrivateFieldReader {

  protected static Object getFieldValue(String fieldName, Object instance, String fieldKindName) {
    Field validationTargetField;
    
    // loop for finding fields in parent's class.
    Class<?> cls = instance.getClass();
    // store first exception
    Exception ex = null;
    
    while (true) {
      if (cls.equals(Object.class)) {
        break;
      }
      
      try {
        validationTargetField = cls.getDeclaredField(fieldName);
        validationTargetField.setAccessible(true);
        return validationTargetField.get(instance);

      } catch (Exception exception) {
        if (ex == null) {
          ex = exception;
        }
      }
      
      cls = cls.getSuperclass();
    }
    
    throw new EclibRuntimeException("'" + fieldKindName + "' field value cannot be obtained "
        + "from the field name '" + fieldName + "'", ex);
  }
}
