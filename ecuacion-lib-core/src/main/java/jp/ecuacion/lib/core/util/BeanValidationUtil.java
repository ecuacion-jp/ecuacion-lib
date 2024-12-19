/*
 * Copyright © 2012 ecuacion.jp (info@ecuacion.jp)
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package jp.ecuacion.lib.core.util;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.MessageInterpolator;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import jp.ecuacion.lib.core.annotation.RequireNonnull;
import jp.ecuacion.lib.core.exception.checked.BeanValidationAppException;
import jp.ecuacion.lib.core.exception.checked.MultipleAppException;
import jp.ecuacion.lib.core.exception.checked.SingleAppException;
import org.hibernate.validator.messageinterpolation.ResourceBundleMessageInterpolator;

/**
 * Provides validation-related utilities.
 */
public class BeanValidationUtil {
  private ConcurrentMap<Locale, Validator> validatorCache = new ConcurrentHashMap<>();

  /**
   * Validates and throws {@code MultipleAppException} if validation errors exist.
   * 
   * @param <T> any class
   * @param object object to validate
   * @throws MultipleAppException MultipleAppException
   */
  public <T> void validateThenThrow(@RequireNonnull T object) throws MultipleAppException {
    MultipleAppException exList = validateThenReturn(object);
    if (exList != null && exList.getList().size() > 0) {
      throw exList;
    }
  }

  /**
   * Validates and throws {@code MultipleAppException} if validation errors exist.
   * 
   * @param <T> any class
   * @param object object to validate
   * @param locale locale, may be {@code null} 
   *     which is treated as {@code Locale.getDefault()}.
   * @throws MultipleAppException MultipleAppException
   */
  public <T> void validateThenThrow(@RequireNonnull T object, @Nullable Locale locale)
      throws MultipleAppException {
    MultipleAppException exList = validateThenReturn(object, locale);
    if (exList != null && exList.getList().size() > 0) {
      throw exList;
    }
  }

  /**
   * Validates and returns {@code MultipleAppException} if validation errors exist.
   * 
   * @param <T> any class
   * @param object object to validate
   * @return MultipleAppException
   */
  @Nullable
  public <T> MultipleAppException validateThenReturn(@RequireNonnull T object) {
    return validateThenReturn(object, Locale.getDefault());
  }

  /**
   * Validates and returns {@code MultipleAppException} if validation errors exist.
   * 
   * @param <T> any class
   * @param object object
   * @param locale locale, may be {@code null} 
   *     which is treated as {@code Locale.getDefault()}.
   * @return MultipleAppException, may be null when no validation errors exist.
   */
  @Nullable
  public <T> MultipleAppException validateThenReturn(@RequireNonnull T object,
      @Nullable Locale locale) {
    Set<ConstraintViolation<T>> set = validate(object, locale);

    MultipleAppException exList = null;
    if (set != null && set.size() > 0) {
      List<SingleAppException> list = new ArrayList<>();
      for (ConstraintViolation<T> v : set) {
        list.add(new BeanValidationAppException(v));
      }

      exList = new MultipleAppException(list);

    } else {
      exList = null;
    }

    return exList;
  }

  /**
   * Validates and returns {@code ConstraintViolation} if validation errors exist.
   * 
   * @param <T> any class
   * @param object object to validate
   * @param locale locale, may be {@code null} 
   *     which is treated as {@code Locale.getDefault()}.
   * @return a Set of ConstraintViolation, may be null when no validation errors exist.
   * 
   * @see jakarta.validation.Validator
   */
  @Nonnull
  public <T> Set<ConstraintViolation<T>> validate(@RequireNonnull T object,
      @Nullable Locale locale) {
    ObjectsUtil.paramRequireNonNull(object);
    locale = (locale == null) ? Locale.getDefault() : locale;

    // Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
    Validator validator = validatorCache.computeIfAbsent(locale,
        (keyLocale) -> Validation.byDefaultProvider().configure()
            .messageInterpolator(new LocaleSpecifiedMessageInterpolator(keyLocale))
            .buildValidatorFactory().getValidator());

    // validator never returns null
    return validator.validate(object);
  }

  /**
   * Is used to set a locale to validator.
   */
  private static class LocaleSpecifiedMessageInterpolator implements MessageInterpolator {

    private Locale locale;
    private MessageInterpolator mi;

    public LocaleSpecifiedMessageInterpolator(Locale locale) {
      this.locale = locale;
      this.mi = new ResourceBundleMessageInterpolator();
    }

    @Override
    public String interpolate(String m, Context c) {
      return mi.interpolate(m, c, locale);
    }

    @Override
    public String interpolate(String m, Context c, Locale l) {
      return mi.interpolate(m, c, locale);
    }

  }
}
