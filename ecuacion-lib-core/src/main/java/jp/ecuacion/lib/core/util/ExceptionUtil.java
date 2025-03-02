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
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.stream.Collectors;
import jp.ecuacion.lib.core.annotation.RequireNonnull;
import jp.ecuacion.lib.core.beanvalidation.bean.BeanValidationErrorInfoBean;
import jp.ecuacion.lib.core.exception.checked.AppException;
import jp.ecuacion.lib.core.exception.checked.BeanValidationAppException;
import jp.ecuacion.lib.core.exception.checked.BizLogicAppException;
import jp.ecuacion.lib.core.exception.checked.MultipleAppException;
import jp.ecuacion.lib.core.exception.checked.SingleAppException;
import jp.ecuacion.lib.core.exception.unchecked.RuntimeAppException;
import jp.ecuacion.lib.core.exception.unchecked.RuntimeExceptionWithMessageId;
import jp.ecuacion.lib.core.exception.unchecked.RuntimeSystemException;
import org.apache.commons.lang3.StringUtils;

/**
 * Provides available utilities for Exceptions including AppExceptions.
 */
public class ExceptionUtil {

  private static final String RT = "\n";

  /**
   * Returns exception message for 1 exception.
   * 
   * <p>This method covers all the exceptions including Java standard exceptions, 
   * ConstraintViolationException used in Bean Validation
   * and AppExceptions defined in this library.</p>
   * 
   * <p>One exception normally has one message, 
   * but one ConstraintViolationException can have multiple messages 
   * so the return type is not a {@code String}, but a {@code List<String>}.
   *
   * @param throwable throwable
   * @param needsDetails Sets if detail message is needed. 
   *     This is true with log output or batch processing. 
   *     False when you show the message on screen.
   * @return a list of messages
   */
  @Nonnull
  public List<String> getExceptionMessage(@RequireNonnull Throwable throwable,
      boolean needsDetails) {
    return getExceptionMessage(throwable, Locale.getDefault(), needsDetails);
  }

  /**
   * Returns exception message for 1 exception.
   * 
   * <p>This method covers all the exceptions including Java standard exceptions, 
   * ConstraintViolationException used in Bean Validation
   * and AppExceptions defined in this library.</p>
   * 
   * <p>One exception normally has one message, 
   * but one ConstraintViolationException can have multiple messages 
   * so the return type is not a {@code String}, but a {@code List<String>}.
   *
   * @param throwable throwable
   * @param locale locale, may be {@code null} 
   *     which is treated as {@code Locale.getDefault()}.
   * @param needsDetails Sets if detail message is needed. 
   *     This is true with log output or batch processing. 
   *     False when you show the message on screen.
   * @return a list of messages
   */
  @Nonnull
  public List<String> getExceptionMessage(@RequireNonnull Throwable throwable,
      @Nullable Locale locale, boolean needsDetails) {
    ObjectsUtil.paramRequireNonNull(throwable);
    locale = locale == null ? Locale.getDefault() : locale;
    ObjectsUtil.paramRequireNonNull(needsDetails);

    List<Throwable> exList = new ArrayList<>();
    List<String> rtnList = new ArrayList<>();

    // jakarta.validation.ConstraintViolationExceptionは、想定外箇所で発生する場合
    // AppBeanValidationExceptionに変換できておらず直接投げられることも想定し、
    // ここでAppBeanValidationExceptionに変換しておく。複数エラーを格納しているのでlistに格納。
    if (throwable instanceof ConstraintViolationException) {
      ConstraintViolationException cve = (ConstraintViolationException) throwable;
      for (ConstraintViolation<?> cv : cve.getConstraintViolations()) {
        exList.add(new BeanValidationAppException(cv));
      }

    } else {
      exList.add(throwable);
    }

    for (Throwable th : exList) {
      // メッセージが存在しない場合はnullを返す
      if (th instanceof MultipleAppException) {
        // この例外は、メッセージは持っていないのでnull
        continue;

      } else if (th instanceof BizLogicAppException && th.getCause() == null) {
        BizLogicAppException ex = (BizLogicAppException) th;
        rtnList.add(PropertyFileUtil.getMsg(locale, ex.getMessageId(), ex.getMessageArgs()));

      } else if (th instanceof BeanValidationAppException) {
        BeanValidationAppException ex = (BeanValidationAppException) th;

        String message = null;
        try {
          BeanValidationErrorInfoBean bean = ex.getBeanValidationErrorInfoBean();
          Map<String, String> map = bean.getParamMap().entrySet().stream()
              .map(e -> new AbstractMap.SimpleEntry<>(e.getKey(), e.getValue().toString()))
              .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));

          message = ex.isMessageWithItemName()
              ? PropertyFileUtil.getValidationMessageWithItemName(locale, bean.getMessageTemplate(),
                  map)
              : PropertyFileUtil.getValidationMessage(locale, bean.getMessageTemplate(), map);

          // 標準validatorを使用するにあたってのspring likeな項目名追加処理。
          // messageに {0} があったら entity.field に置き換える
          if (message.contains("{0}")) {
            String className =
                bean.getRootClassName().substring(bean.getRootClassName().lastIndexOf(".") + 1);
            String itemName = StringUtils.uncapitalize(className) + "." + bean.getPropertyPath();
            // itemNameの中に"."が2つ以上ある場合は不要に長いので上位のパスを削除
            if (itemName.split("\\.").length > 2) {
              while (true) {
                itemName = itemName.substring(itemName.indexOf(".") + 1);
                if (itemName.split("\\.").length == 2) {
                  break;
                }
              }
            }

            // itemNameがmessages.propertiesにあったらそれに置き換える
            if (PropertyFileUtil.hasItemName(itemName)) {
              itemName = PropertyFileUtil.getItemName(locale, itemName);
            }

            message = MessageFormat.format(message, itemName);
          }

        } catch (MissingResourceException mre) {
          message = ex.getMessage();
        }
        rtnList.add((needsDetails) ? message + "\n" + ex.toString() : message);

      } else if (th instanceof RuntimeExceptionWithMessageId) {
        RuntimeExceptionWithMessageId ex = (RuntimeExceptionWithMessageId) th;
        rtnList.add(PropertyFileUtil.getMsg(locale, ex.getMessageId(), ex.getMessageArgs()));

      } else {
        rtnList.add(th.getMessage());
      }
    }

    return rtnList;
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
  private List<Throwable> serializeExceptions(@RequireNonnull Throwable throwable) {
    List<Throwable> list = new ArrayList<>();
    recursivelySerializeException(throwable, list);
    return ObjectsUtil.returnRequireNonNull(list);
  }

  /*
   * serializeException(Throwable th)から呼び出される内部メソッド。再帰呼び出しされるので、serializeExceptionとは別メソッドとしている。<br>
   * getCause()の中の例外と、MultipleAppException#exArrの中の例外を、全て階層なしのArrayListに詰める
   * 本メソッドは再帰的に呼び出されることから、引数と戻り値に両方リストがあると非常にわかりにくいので、引数に渡したlistに例外を追加していく形とする。
   *
   * @param th 例外
   * 
   * @param arr 最終的に「階層のない単純なList」がこれになる。
   */
  private void recursivelySerializeException(@RequireNonnull Throwable throwable,
      List<Throwable> arr) {
    // 自分をadd
    arr.add(throwable);

    // MultipleAppExceptionの場合は、その内部に保持している複数のApplicationExceptionを全てerrListに追加する
    // MultipleAppException.exArrに入るものはAppExceptionのみ
    if (throwable instanceof MultipleAppException) {
      for (AppException childAe : ((MultipleAppException) throwable).getList()) {
        recursivelySerializeException(childAe, arr);
      }

    } else if (throwable.getCause() != null) {
      // causeがあればそれも追加
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
  public List<Throwable> getExceptionListWithMessages(@RequireNonnull Throwable throwable) {
    ObjectsUtil.paramRequireNonNull(throwable);

    List<Throwable> rtnList = new ArrayList<>();
    // return serializeExceptions(throwable).stream()
    // .filter(t -> t.getMessage() != null && !t.getMessage().equals("")).toList();

    for (Throwable th : serializeExceptions(throwable)) {

      if (th instanceof MultipleAppException) {
        // MultipleAppException changed to have its message but it's ignored
        // because it's obtained from SingleAppExceptions which it carries.
        continue;

      } else if (th instanceof RuntimeAppException) {
        rtnList.add(((RuntimeAppException) th).getCause());

      } else if (th instanceof SingleAppException || th instanceof RuntimeExceptionWithMessageId
          || th instanceof RuntimeSystemException
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
  public List<SingleAppException> getSingleAppExceptionList(
      @RequireNonnull AppException appException) {
    ObjectsUtil.paramRequireNonNull(appException);

    List<SingleAppException> rtnList = new ArrayList<>();
    serializeExceptions(appException).forEach(ex -> {
      if (ex instanceof SingleAppException) {
        rtnList.add((SingleAppException) ex);
      }
    });

    // AppExceptionが上がってきたのにmessageがないのは想定外例外なので、改めて例外を投げてしまう。
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
  public List<String> getAppExceptionMessageList(@RequireNonnull AppException appException,
      @Nullable Locale locale) {
    // 呼び出し先でnullcheckをしているためここでは不要。

    List<String> rtnList = new ArrayList<>();
    getSingleAppExceptionList(appException).stream()
        .map(ex -> getExceptionMessage(ex, locale, false)).forEach(list -> rtnList.addAll(list));
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
   * @param packagesShown packages shown in the stack traces.
   *     This is used when the log displaying area is small.
   * @return error log string
   */
  @Nonnull
  public String getErrLogShortString(@RequireNonnull Throwable throwable,
      @Nullable String additionalMessage, @Nullable Locale locale, int packagesShown) {
    return getErrLogString(throwable, additionalMessage, locale, packagesShown);
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
  public String getErrLogString(@RequireNonnull Throwable throwable,
      @Nullable String additionalMessage, @Nullable Locale locale) {
    return getErrLogString(throwable, additionalMessage, locale, null);
  }

  @Nonnull
  private String getErrLogString(@RequireNonnull Throwable throwable,
      @Nullable String additionalMessage, @Nullable Locale locale, Integer packagesShown) {
    ObjectsUtil.paramRequireNonNull(throwable);
    locale = (locale == null) ? Locale.getDefault() : locale;

    StringBuilder sb = new StringBuilder();

    sb.append("=============== system error occured ===============" + RT);
    if (additionalMessage != null) {
      sb.append(additionalMessage + RT);
      sb.append(RT);
    }

    sb.append(RT);

    getErrInfoRecursively(sb, throwable, locale, packagesShown);

    return sb.toString();
  }

  /* 再起的に呼び出してThrowableの内容を出力する。 */
  private void getErrInfoRecursively(StringBuilder sb, Throwable th, Locale locale,
      Integer packagesShown) {

    locale = (locale == null) ? Locale.getDefault() : locale;

    // クラス名は共通で出力
    sb.append(th.getClass().getName() + RT);

    // メッセージは存在すれば出す
    String errMsg = getExceptionMessage(th, locale, true).toString();
    if (errMsg != null) {
      sb.append(errMsg + RT);
    }

    // stackTraceを出力
    sb.append(getStackTraceString(th, packagesShown));

    // causeがあればそれも出力
    if (th.getCause() != null) {
      getErrInfoRecursively(sb, th.getCause(), locale, packagesShown);
    }
  }

  private String getStackTraceString(Throwable th, Integer packagesShown) {
    StringBuilder sb = new StringBuilder();
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

    return sb.toString();
  }
}
