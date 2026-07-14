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
package jp.ecuacion.lib.core.util.enums;

/**
 * Holds groups of {@link PropertiesFileUtilFileKindEnum}, each carrying the common
 * behavior shared by its member file kinds.
 *
 * <p>{@code evaluatesElExpression} is {@code true} only for {@code VALIDATION_MESSAGE},
 * since {@code ${...}} EL evaluation is meaningful only for Jakarta Bean Validation
 * message interpolation, where annotation attributes are bound as EL variables.
 * {@code application.properties} / {@code messages.properties} etc. never bind such
 * variables, so evaluating {@code ${...}} there would either be a no-op at best or
 * throw at worst.</p>
 *
 * <p>{@code resolvesExternalPlaceholders} is {@code true} only for {@code CONFIG}.
 * ecuacion-lib has no built-in notion of environment variables or framework-specific
 * property sources; it only exposes an extension point
 * ({@link jp.ecuacion.lib.core.util.PropertiesFileUtil#setExternalPlaceholderResolver}) that
 * a framework-specific module (e.g., a Spring-based one) can use to plug in its own
 * resolution of {@code ${...}} in {@code application.properties} values, without
 * ecuacion-lib depending on that framework.</p>
 */
public enum PropertiesFileUtilFileKindGroupEnum {

  /** {@code application.properties}. */
  CONFIG(true, false, true),

  /** {@code messages}, {@code item_names}, {@code enum_names}, {@code constants}, etc. */
  MESSAGE(false, false, false),

  /** {@code ValidationMessages}, {@code ValidationMessagesWithItemNames}, etc. */
  VALIDATION_MESSAGE(false, true, false);

  private final boolean throwsExceptionWhenKeyDoesNotExist;
  private final boolean evaluatesElExpression;
  private final boolean resolvesExternalPlaceholders;

  private PropertiesFileUtilFileKindGroupEnum(boolean throwsExceptionWhenKeyDoesNotExist,
      boolean evaluatesElExpression, boolean resolvesExternalPlaceholders) {
    this.throwsExceptionWhenKeyDoesNotExist = throwsExceptionWhenKeyDoesNotExist;
    this.evaluatesElExpression = evaluatesElExpression;
    this.resolvesExternalPlaceholders = resolvesExternalPlaceholders;
  }

  /**
   * Returns whether a missing key throws an exception for file kinds in this group.
   */
  public boolean throwsExceptionWhenKeyDoesNotExist() {
    return throwsExceptionWhenKeyDoesNotExist;
  }

  /**
   * Returns whether {@code ${...}} EL expressions are evaluated for file kinds in this group.
   */
  public boolean evaluatesElExpression() {
    return evaluatesElExpression;
  }

  /**
   * Returns whether a registered external placeholder resolver is applied
   * for file kinds in this group.
   */
  public boolean resolvesExternalPlaceholders() {
    return resolvesExternalPlaceholders;
  }
}
