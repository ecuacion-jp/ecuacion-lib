package jp.ecuacion.lib.validation.constraints.internal;

import java.util.ArrayList;
import java.util.List;
import jp.ecuacion.lib.core.jakartavalidation.constraints.ClassValidator;
import org.apache.commons.lang3.StringUtils;

public abstract class AllAnyValidator extends ClassValidator {

  protected int numberOfNonEmptyValues(Object object, String[] propertyPaths) {
    List<Object> list = new ArrayList<>();
    
    for (String propertyPath : propertyPaths) {
      Object fieldValue = getValue(object, propertyPath);

      if (fieldValue instanceof String) {
        if (StringUtils.isNotEmpty((String) fieldValue)) {
          list.add(fieldValue);
        }

      } else {
        if (fieldValue != null) {
          list.add(fieldValue);
        }
      }
    }
    
    return list.size();
  }
}
