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
package jp.ecuacion.lib.core.util;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Set;
import jp.ecuacion.lib.core.annotation.RequireNonnull;
import jp.ecuacion.lib.core.exception.checked.AppException;
import jp.ecuacion.lib.core.exception.checked.BizLogicAppException;
import jp.ecuacion.lib.core.exception.checked.ConstraintViolationBeanException;
import jp.ecuacion.lib.core.exception.checked.MultipleAppException;
import jp.ecuacion.lib.core.exception.checked.SingleAppException;
import jp.ecuacion.lib.core.exception.checked.ValidationAppException;
import jp.ecuacion.lib.core.exception.unchecked.EclibRuntimeException;
import jp.ecuacion.lib.core.exception.unchecked.UncheckedAppException;
import jp.ecuacion.lib.core.jakartavalidation.bean.ConstraintViolationBean;
import jp.ecuacion.lib.core.jakartavalidation.bean.ConstraintViolationBean.LocalizedMessageParameter;
import jp.ecuacion.lib.core.util.PropertyFileUtil.PropertyFileUtilFileKindEnum;

/**
 * Provides available utilities for Exceptions including AppExceptions.
 */
public class ExceptionUtil {

  public static final String SYSTEM_ERROR_OCCURED_SIGN =
      "=============== system error occurred ===============";
  private static final String RT = "\n";

  /**
   * Prevents other classes from instantiating it.
   */
  private ExceptionUtil() {}

  /**
   * Returns Exception message list.
   */
  @Nonnull
  public static <T> List<String> getMessageList(@RequireNonnull Set<T> constraintViolation) {
    return getMessageList(constraintViolation, null, null);
  }

  /**
   * Returns Exception message list.
   */
  @Nonnull
  public static <T> List<String> getMessageList(@RequireNonnull Set<T> constraintViolationSet,
      @Nullable Locale locale) {
    return getMessageList(constraintViolationSet, locale, null);
  }

  /**
   * Returns Exception message list.
   */
  @Nonnull
  public static <T> List<String> getMessageList(@RequireNonnull Set<T> constraintViolationSet,
      @Nullable Boolean isValidationMessagesWithItemNames) {
    return getMessageList(constraintViolationSet, null, isValidationMessagesWithItemNames);
  }

  /**
   * Returns Exception message list.
   */
  @Nonnull
  public static <T> List<String> getMessageList(@RequireNonnull Set<T> constraintViolationSet,
      Locale locale, Boolean isValidationMessagesWithItemNames) {

    return getMessageList(new ConstraintViolationBeanException(constraintViolationSet), locale,
        isValidationMessagesWithItemNames);
  }

  /**
   * Returns exception message for 1 exception.
   * 
   * <p>This method covers all the exceptions including Java standard exceptions, 
   * ConstraintViolationException used in Jakarta Validation
   * and AppExceptions defined in this library.</p>
   * 
   * <p>One exception normally has one message, 
   * but one ConstraintViolationException can have multiple messages 
   * so the return type is not a {@code String}, but a {@code List<String>}.
   *
   * @param throwable throwable
   * @return a list of messages
   */
  @Nonnull
  public static List<String> getMessageList(@RequireNonnull Throwable throwable) {
    return getMessageList(throwable, null);
  }

  /**
   * Returns exception message for 1 exception.
   * 
   * <p>This method covers all the exceptions including Java standard exceptions, 
   * ConstraintViolationException used in Jakarta Validation
   * and AppExceptions defined in this library.</p>
   * 
   * <p>One exception normally has one message, 
   * but one ConstraintViolationException can have multiple messages 
   * so the return type is not a {@code String}, but a {@code List<String>}.
   *
   * @param throwable throwable
   * @param locale locale, may be {@code null} 
   *     which is treated as {@code Locale.getDefault()}.
   * @return a list of messages
   */
  @Nonnull
  public static List<String> getMessageList(@RequireNonnull Throwable throwable,
      @Nullable Locale locale) {
    return getMessageList(throwable, locale, null);
  }

  /**
   * Returns exception message for 1 exception.
   * 
   * <p>This method covers all the exceptions including Java standard exceptions, 
   * ConstraintViolationException used in Jakarta Validation
   * and AppExceptions defined in this library.</p>
   * 
   * <p>One exception normally has one message, 
   * but one ConstraintViolationException can have multiple messages 
   * so the return type is not a {@code String}, but a {@code List<String>}.
   *
   * @param throwable throwable
   * @param locale locale, may be {@code null} 
   *     which is treated as {@code Locale.getDefault()}.
   * @param isValidationMessagesWithItemNames true 
   *     when itemName needed for ValidationAppException messages.
   *     
   * @return a list of messages
   */
  @Nonnull
  public static List<String> getMessageList(@RequireNonnull Throwable throwable,
      @Nullable Locale locale, @Nullable Boolean isValidationMessagesWithItemNames) {
    ObjectsUtil.requireNonNull(throwable);
    locale = locale == null ? Locale.getDefault() : locale;
    isValidationMessagesWithItemNames =
        isValidationMessagesWithItemNames == null ? false : isValidationMessagesWithItemNames;

    List<Throwable> exList = new ArrayList<>();
    List<String> rtnList = new ArrayList<>();

    // jakarta.validation.ConstraintViolationException can be thrown from unassumed locations.
    // In that case it's not transformed to AppBeanValidationException,
    // So the transformation procedure is added here.
    if (throwable instanceof ConstraintViolationException) {
      ConstraintViolationException cve = (ConstraintViolationException) throwable;
      for (ConstraintViolation<?> cv : cve.getConstraintViolations()) {
        exList.add(new ValidationAppException(cv));
      }

    } else if (throwable instanceof ConstraintViolationBeanException) {
      ConstraintViolationBeanException cve = (ConstraintViolationBeanException) throwable;
      for (ConstraintViolationBean<?> cv : cve.getConstraintViolationBeans()) {
        exList.add(new ValidationAppException(cv));
      }

    } else {
      exList.add(throwable);
    }

    for (Throwable th : exList) {
      if (th instanceof MultipleAppException) {
        // Continue because this exception doesn't have a message.
        continue;

      } else if (th instanceof BizLogicAppException) {
        BizLogicAppException ex = (BizLogicAppException) th;
        String message = isValidationMessagesWithItemNames
            ? PropertyFileUtil.getMessageWithItemName(locale, ex.getMessageId(),
                ex.getMessageArgs())
            : PropertyFileUtil.getMessage(locale, ex.getMessageId(), ex.getMessageArgs());
        rtnList.add(message);

      } else if (th instanceof ValidationAppException) {
        ValidationAppException ex = (ValidationAppException) th;

        String message = null;
        try {
          ConstraintViolationBean<?> bean = ex.getConstraintViolationBean();
          final Map<String, Object> map = new HashMap<>(bean.getParamMap());

          // Add parameters from messageParameterSet.
          for (LocalizedMessageParameter paramBean : bean.getMessageParameterSet()) {

            // Put propertyFileKey as value when paramBean.fileKinds().length == 0.
            if (paramBean.fileKinds() == null || paramBean.fileKinds().length == 0) {
              map.put(paramBean.parameterKey(), paramBean.propertyFileKey());
              continue;
            }

            String value = "";
            for (PropertyFileUtilFileKindEnum fileKind : paramBean.fileKinds()) {
              // Put return value of PropertyFileUtil.get() even when key does not exist.
              value = PropertyFileUtil.get(fileKind.toString(), locale, paramBean.propertyFileKey(),
                  paramBean.args());

              if (PropertyFileUtil.has(fileKind.toString(), paramBean.propertyFileKey())) {
                break;
              }
            }

            map.put(paramBean.parameterKey(), value);
          }

          // If bean.isMessageWithItemName() is not null (= explicitly specified), it's prioritized
          // because it is specified for each validation,
          // and isValidationMessagesWithItemNames is assumed to be used as system default value.
          Boolean bl = bean.isMessageWithItemName() != null ? bean.isMessageWithItemName()
              : isValidationMessagesWithItemNames;
          message = bl
              ? PropertyFileUtil.getValidationMessageWithItemName(locale, bean.getMessageId(), map)
              : PropertyFileUtil.getValidationMessage(locale, bean.getMessageId(), map);

          // Additional procedure which is like spring like itemName method to add itemName to
          // messages.
          // Replace {0} in messages to itemName.
          if (message.contains("{0}")) {
            List<String> ink =
                bean.getFieldInfoBeanList().stream().map(b -> b.itemNameKey).toList();
            message = MessageFormat.format(message,
                getItemNames(locale, ink.toArray(new String[ink.size()])));
          }

          // add prefix and postfix messages.
          if (bean.getMessagePrefix() != null) {
            message = PropertyFileUtil.getStringFromArg(locale, bean.getMessagePrefix()) + message;
          }

          if (bean.getMessagePostfix() != null) {
            message = message + PropertyFileUtil.getStringFromArg(locale, bean.getMessagePostfix());
          }

        } catch (MissingResourceException mre) {
          message = ex.getMessage();
        }

        rtnList.add(message);

      } else {
        rtnList.add(th.getMessage());
      }
    }

    return rtnList;

  }

  @Nonnull
  private static String getItemNames(Locale locale, @RequireNonnull String[] itemNameKeys) {
    final String prependParenthesis = PropertyFileUtil.getMessage(locale,
        "jp.ecuacion.lib.core.common.itemName.prependParenthesis");
    final String appendParenthesis = PropertyFileUtil.getMessage(locale,
        "jp.ecuacion.lib.core.common.itemName.appendParenthesis");
    final String separator =
        PropertyFileUtil.getMessage(locale, "jp.ecuacion.lib.core.common.itemName.separator");

    List<String> itemNameList = Arrays.asList(ObjectsUtil.requireNonNull(itemNameKeys)).stream()
        .map(key -> PropertyFileUtil.getItemName(locale, key))
        .map(name -> prependParenthesis + name + appendParenthesis).toList();

    return StringUtil.getSeparatedValuesString(itemNameList, separator);
  }

  /*
   * Returun a list of exceptions.
   * 
   * <p>This serializes exceptions obtained from exception's {@code getCause()} and {@code
   * MultipleAppException} and puts all into an {@code ArrayList}.</p>
   * 
   * <p>This is expected to use to log exceptions to file or others.</p>
   * 
   * @param throwable throwable
   * @return a list of throables
   */
  @Nonnull
  private static List<Throwable> serializeExceptions(@RequireNonnull Throwable throwable) {
    List<Throwable> list = new ArrayList<>();
    recursivelySerializeException(throwable, list);
    return list;
  }

  private static void recursivelySerializeException(@RequireNonnull Throwable throwable,
      @RequireNonnull List<Throwable> arr) {
    // Add itself to the list.
    ObjectsUtil.requireNonNull(arr.add(ObjectsUtil.requireNonNull(throwable)));

    // Add "cause" or related exceptions to the list.
    if (throwable instanceof MultipleAppException) {
      for (AppException childAe : ((MultipleAppException) throwable).getList()) {
        recursivelySerializeException(childAe, arr);
      }

    } else if (throwable.getCause() != null) {
      recursivelySerializeException((Throwable) throwable.getCause(), arr);
    }
  }

  /**
   * Returns listed exceptions with messages.
   * 
   * <p>This is used for log output to see error messages easily.</p>
   * 
   * <p>This is not used for online because in online apps messages 
   * shown to users is only SingleAppExceptions.</p>
   * 
   * @param throwable throwable
   * @return a list of Throwables
   */
  @Nonnull
  public static List<Throwable> getExceptionListWithMessages(@RequireNonnull Throwable throwable) {
    ObjectsUtil.requireNonNull(throwable);

    List<Throwable> rtnList = new ArrayList<>();
    // return serializeExceptions(throwable).stream()
    // .filter(t -> t.getMessage() != null && !t.getMessage().equals("")).toList();

    for (Throwable th : serializeExceptions(throwable)) {

      if (th instanceof MultipleAppException) {
        // MultipleAppException changed to have its message but it's ignored
        // because it's obtained from SingleAppExceptions which it carries.
        continue;

      } else if (th instanceof UncheckedAppException) {
        rtnList.add(((UncheckedAppException) th).getCause());

      } else if (th instanceof SingleAppException || th instanceof EclibRuntimeException
          || (th.getMessage() != null && !th.getMessage().equals(""))) {
        rtnList.add(th);
      }
    }

    return rtnList;
  }

  /**
   * Returns listed SingleAppExceptions (= AppExceptions with messages).
   * 
   * <p>This is not used for online</p>
   * 
   * @param appException AppException
   * @return list of SingleAppException
   */
  @Nonnull
  public static List<SingleAppException> getSingleAppExceptionList(
      @RequireNonnull AppException appException) {
    ObjectsUtil.requireNonNull(appException);

    List<SingleAppException> rtnList = new ArrayList<>();
    serializeExceptions(appException).forEach(ex -> {
      if (ex instanceof SingleAppException) {
        rtnList.add((SingleAppException) ex);
      }
    });

    // Throw RuntimeException when there's an exception but there's no messages.
    if (rtnList.isEmpty()) {
      throw new RuntimeException(appException);
    }

    return rtnList;
  }

  /**
   * Returns listed SingleAppExceptions (= AppExceptions with messages).
   * 
   * <p>This is used for online.</p>
   * 
   * <p>The overload method with RuntimeAppException won't be created
   * because getCause() of RuntimeAppException is AppException and 
   * it is not needed by then.</p>
   * 
   * @param appException AppException
   * @param locale locale, may be {@code null} 
   *     which is treated as {@code Locale.getDefault()}.
   * @return list of SingleAppException
   */
  @Nonnull
  public static List<String> getAppExceptionMessageList(@RequireNonnull AppException appException,
      @Nullable Locale locale) {
    List<String> rtnList = new ArrayList<>();
    getSingleAppExceptionList(ObjectsUtil.requireNonNull(appException)).stream()
        .map(ex -> getMessageList(ex, locale, false)).forEach(list -> rtnList.addAll(list));
    return rtnList;
  }

  /**
   * Returns listed SingleAppExceptions (= AppExceptions with messages).
   * 
   * <p>This is used for online.</p>
   * 
   * <p>The overload method with RuntimeAppException won't be created
   * because getCause() of RuntimeAppException is AppException and 
   * it is not needed by then.</p>
   * 
   * @param appException AppException
   * @param locale locale, may be {@code null} 
   *     which is treated as {@code Locale.getDefault()}.
   * @param isValidationMessagesWithItemNames true 
   *     when itemName needed for ValidationAppException messages.
   * @return list of SingleAppException
   */
  @Nonnull
  public static List<String> getAppExceptionMessageList(@RequireNonnull AppException appException,
      @Nullable Locale locale, boolean isValidationMessagesWithItemNames) {
    List<String> rtnList = new ArrayList<>();
    getSingleAppExceptionList(ObjectsUtil.requireNonNull(appException)).stream()
        .map(ex -> getMessageList(ex, locale, isValidationMessagesWithItemNames))
        .forEach(list -> rtnList.addAll(list));
    return rtnList;
  }

  /**
   * Returns strings or error log.
   * 
   * @param throwable throwable
   * @param additionalMessage additional message,
   *     may be {@code null} if no {@code additionalMessage} is needed.
   *     In the case of {@code null} no additional message is output.
   * @param locale locale, may be {@code null} 
   *     which is treated as {@code Locale.getDefault()}.
   * @return error log string
   */
  @Nonnull
  public static String getErrLogString(@RequireNonnull Throwable throwable,
      @Nullable String additionalMessage, @Nullable Locale locale) {
    return getErrLogString(throwable, additionalMessage, locale, null);
  }

  /**
   * Returns strings or error log.
   * 
   * @param throwable throwable
   * @param additionalMessage additional message,
   *     may be {@code null} if no {@code additionalMessage} is needed.
   *     In the case of {@code null} no additional message is output.
   * @param locale locale, may be {@code null} 
   *     which is treated as {@code Locale.getDefault()}.
   * @param packagesShown packages shown in the stack traces.
   *     This is used when the log displaying area is small.
   * @return error log string
   */
  @Nonnull
  public static String getErrLogString(@RequireNonnull Throwable throwable,
      @Nullable String additionalMessage, @Nullable Locale locale,
      @Nullable Integer packagesShown) {
    ObjectsUtil.requireNonNull(throwable);
    locale = (locale == null) ? Locale.ENGLISH : locale;

    StringBuilder sb = new StringBuilder();

    sb.append(SYSTEM_ERROR_OCCURED_SIGN + RT);
    if (additionalMessage != null) {
      sb.append(additionalMessage + RT);
      sb.append(RT);
    }

    sb.append(RT);

    getMessageAndStackTraceStringRecursively(sb, throwable, locale, packagesShown);

    return sb.toString();
  }

  /**
   * Adds Throwable message and stackTrace string to argument stringBuilder 
   *     for a throwable and its causes.
   * 
   * @param sb StringBuilder
   * @param th throwable
   * @param locale locale, may be null 
   * @param packagesShown null means all package of a class is shown 
   *     like "at jp.ecuacion.lib.core.util.ExceptionUtil.main(ExceptionUtil.java:468)".
   *     "0" shows no packages like "at ..main(ExceptionUtil.java:468)".
   *     "1" shows 1 package part like "at jp...main(ExceptionUtil.java:468)".
   */
  public static void getMessageAndStackTraceStringRecursively(StringBuilder sb, Throwable th,
      Locale locale, Integer packagesShown) {

    getMessageAndStackTraceString(sb, th, locale, packagesShown);

    // Also outputs for getCause().
    if (th.getCause() != null) {
      getMessageAndStackTraceStringRecursively(sb, th.getCause(), locale, packagesShown);
    }
  }

  /**
   * Adds Throwable message and stackTrace string to argument stringBuilder 
   *     for one throwable. (getCause() ignored)
   * 
   * @param sb StringBuilder
   * @param th throwable
   * @param locale locale, may be null 
   * @param packagesShown see getMessageAndStackTraceStringRecursively
   */
  private static void getMessageAndStackTraceString(StringBuilder sb, Throwable th, Locale locale,
      Integer packagesShown) {
    locale = (locale == null) ? Locale.ENGLISH : locale;

    // Call MultipleAppException#getMessage() explicitly
    // since getExceptionMessage skips the exception.
    String errMsg = (th instanceof MultipleAppException) ? ((MultipleAppException) th).getMessage()
        : getMessageList(th, locale, true).toString();
    sb.append(th.getClass().getCanonicalName() + " " + errMsg + RT);

    // Output stackTrace string
    getStackTraceString(sb, th, packagesShown);
  }

  /**
   * get stackTrace string for one throwable. (getCause() ignored)
   * 
   * @param sb StringBuilder
   * @param th throwable
   * @param packagesShown see getMessageAndStackTraceStringRecursively
   * @return stackTrace string
   */
  private static void getStackTraceString(StringBuilder sb, Throwable th, Integer packagesShown) {
    for (StackTraceElement ste : th.getStackTrace()) {
      String[] spl = ste.getClassName().split("\\.");
      String packageAndClass = ste.getClassName();
      if (packagesShown != null) {
        String packages = "";
        for (int i = 0; i < packagesShown; i++) {
          if (spl.length > i) {
            packages = packages + spl[i] + (spl.length - 1 == i ? "" : ".");
          }
        }

        packageAndClass = packages + (spl.length > packagesShown ? "." : "");
      }

      sb.append("\tat " + packageAndClass + "." + ste.getMethodName() + "(" + ste.getFileName()
          + ":" + ste.getLineNumber() + ")" + RT);
    }
  }
}
