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
package jp.ecuacion.lib.jpa.entity;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Provides the parent of jpa entities.
 */
public abstract class AbstractEntity {

  /**
   * Returns the value of the designated fieldName.
   * 
   * @param fieldName fieldName. Cannot be {@code null}.
   * 
   * @return value. May be null when the value is null.
   */
  @Nullable
  public Object getValue(@Nonnull String fieldName) {
    try {
      Field field = this.getClass().getDeclaredField(fieldName);
      Object value = field.get(this);
      return value;

    } catch (NoSuchFieldException | SecurityException | IllegalArgumentException
        | IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }

  /** 
   * Returns the surrogate-key field name, to which '@Id' is added.
   * 
   * @return surrogate key field name.
   */
  @Nonnull
  public String getPkFieldName() {
    Field[] fields = this.getClass().getDeclaredFields();

    for (Field field : fields) {
      Annotation annotation = field.getAnnotation(Id.class);
      if (annotation != null) {
        return Objects.requireNonNull(field.getName());
      }
    }

    // surrogate keyをつけることはecuacion-lib-jpaでは必須要件なのでRuntimeExceptionとする
    throw new RuntimeException("Field with '@Id' not found.");
  }

  /** 
   * Returns the auto-increment field name, to which '@GeneratedValue' is added.
   * 
   * @return auto-increment field name.
   */
  @Nonnull
  public Set<String> getAutoIncrementFieldNameSet() {
    Field[] fields = this.getClass().getDeclaredFields();
    Set<String> rtnSet = new HashSet<>();

    for (Field field : fields) {
      Annotation annotation = field.getAnnotation(GeneratedValue.class);
      if (annotation != null) {
        rtnSet.add(field.getName());
      }
    }

    return rtnSet;
  }

  /**
   * Returns if the designated field is auto-increment.
   * 
   * @param fieldName fieldName.
   * @return field is auto-increment
   */
  public boolean isAutoIncrement(@Nonnull String fieldName) {
    return getAutoIncrementFieldNameSet().contains(fieldName);
  }

  /**
   * Returns an array of fields which are the Unique Constraint.
   * 
   * @return set of unique key column list.
   */
  @Nonnull
  public Set<List<String>> getSetOfUniqueConstraintFieldList() {
    Set<List<String>> rtnSet = new HashSet<>();

    Table table = this.getClass().getAnnotation(Table.class);
    UniqueConstraint[] ucs = table.uniqueConstraints();

    if (ucs == null) {
      return rtnSet;
    }

    for (UniqueConstraint uc : ucs) {
      rtnSet.add(Arrays.asList(uc.columnNames()));
    }

    return rtnSet;
  }

  /**
   * Returns if the entity has natural keys.
   * 
   * @return has natural keys.
   */
  public boolean hasNaturalKey() {
    return getSetOfUniqueConstraintFieldList().size() != 0;
  }

  /**
   * Provides preInsert procedure.
   * 
   * <p>When you use spring framework, this won't be used.</p>
   */
  public abstract void preInsert();

  /**
   * Provides preUpdate procedure.
   * 
   * <p>When you use spring framework, this won't be used.</p>
   */
  public abstract void preUpdate();

  /**
   * Returns if the entity has soft-delete field.
   * 
   * @return has soft-delete field.
   */
  public abstract boolean hasSoftDeleteField();

  /**
   * Returns field name array.
   * 
   * @return field name array.
   */
  public abstract String[] getFieldNameArr();
}
