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
 * <p>{@code resolvesCrossReferences} is {@code false} only for {@code CONFIG}. Unlike
 * {@code ${...}}, {@code #{fileKind:key}} / {@code #{key}} is not merely unused for
 * {@code application.properties} — it actively collides with Spring's own {@code #{...}}
 * SpEL delimiter (used by {@code @Value}, always registered by default via {@code
 * StandardBeanExpressionResolver}). A raw {@code application.properties} value containing
 * {@code #{fileKind:key}} would parse as (invalid) SpEL and throw at bean-creation time if
 * that same key were ever also injected via {@code @Value}. Disabling cross-reference
 * resolution for {@code CONFIG} entirely — even when read via {@link
 * jp.ecuacion.lib.core.util.PropertiesFileUtil#getApplication}, not just via Spring — makes
 * {@code #{...}} a plain literal in {@code application.properties} values regardless of
 * retrieval path, so this footgun cannot be triggered.</p>
 */
public enum PropertiesFileUtilFileKindGroupEnum {

  /** {@code application.properties}. */
  CONFIG(true, false, false),

  /** {@code messages}, {@code item_names}, {@code enum_names}, {@code constants}, etc. */
  MESSAGE(false, false, true),

  /** {@code ValidationMessages}, {@code ValidationMessagesWithItemNames}, etc. */
  VALIDATION_MESSAGE(false, true, true);

  private final boolean throwsExceptionWhenKeyDoesNotExist;
  private final boolean evaluatesElExpression;
  private final boolean resolvesCrossReferences;

  private PropertiesFileUtilFileKindGroupEnum(boolean throwsExceptionWhenKeyDoesNotExist,
      boolean evaluatesElExpression, boolean resolvesCrossReferences) {
    this.throwsExceptionWhenKeyDoesNotExist = throwsExceptionWhenKeyDoesNotExist;
    this.evaluatesElExpression = evaluatesElExpression;
    this.resolvesCrossReferences = resolvesCrossReferences;
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
   * Returns whether {@code #{fileKind:key}} / {@code #{key}} cross-references are resolved
   * for file kinds in this group.
   */
  public boolean resolvesCrossReferences() {
    return resolvesCrossReferences;
  }
}
