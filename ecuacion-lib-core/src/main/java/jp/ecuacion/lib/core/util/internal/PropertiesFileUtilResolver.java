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
package jp.ecuacion.lib.core.util.internal;

import jakarta.el.ELProcessor;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import jp.ecuacion.lib.core.exception.ViolationException;
import jp.ecuacion.lib.core.util.EmbeddedVariableUtil;
import jp.ecuacion.lib.core.util.EmbeddedVariableUtil.Options;
import jp.ecuacion.lib.core.util.PropertiesFileUtil.Arg;
import jp.ecuacion.lib.core.util.PropertiesFileUtil.Arg.ArgKind;
import jp.ecuacion.lib.core.util.enums.PropertiesFileUtilFileKindEnum;
import org.apache.commons.lang3.tuple.Pair;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Coordinates resolution of {@link Arg} values and {@code #{...}} property references
 * for {@link jp.ecuacion.lib.core.util.PropertiesFileUtil}.
 *
 * <p>Acts as the coordinator between
 * {@link PropertiesFileUtilBundleReader} (raw bundle reading) and
 * {@link PropertiesFileUtilFormatter} (message formatting).</p>
 */
public class PropertiesFileUtilResolver {

  private PropertiesFileUtilResolver() {}

  //@formatter:off
  private static final 
      Map<PropertiesFileUtilFileKindEnum, PropertiesFileUtilBundleReader> readerMap;
  //@formatter:on

  static {
    Map<PropertiesFileUtilFileKindEnum, PropertiesFileUtilBundleReader> tmp = new HashMap<>();
    for (PropertiesFileUtilFileKindEnum anEnum : PropertiesFileUtilFileKindEnum.values()) {
      tmp.put(anEnum, new PropertiesFileUtilBundleReader(anEnum));
    }
    readerMap = Collections.unmodifiableMap(tmp);
  }

  private static PropertiesFileUtilBundleReader obtainBundleReader(
      PropertiesFileUtilFileKindEnum fileKind) {
    return Objects.requireNonNull(readerMap.get(fileKind));
  }

  private static final List<PropertiesFileUtilFileKindEnum> FILE_KINDS_FOR_KEY_ONLY_SEARCH =
      List.of(PropertiesFileUtilFileKindEnum.MESSAGES, PropertiesFileUtilFileKindEnum.ITEM_NAMES,
          PropertiesFileUtilFileKindEnum.CONSTANTS, PropertiesFileUtilFileKindEnum.ENUM_NAMES);

  /**
   * Returns the processed property value for the given file kind and key.
   * Applies {@code #{...}} and {@code ${...}} resolution on top of the raw value.
   *
   * <p>Shorthand for
   * {@link #getProp(Locale, PropertiesFileUtilFileKindEnum, String, Map)}
   * with an empty EL parameter map. Use when no EL variable bindings are needed.</p>
   *
   * @param locale locale, may be {@code null} which means no {@code Locale} specified.
   * @param fileKind the file kind
   * @param key the key of the property
   * @return the processed value
   */
  public static String getProp(@Nullable Locale locale, PropertiesFileUtilFileKindEnum fileKind,
      String key) {
    return getProp(locale, fileKind, key, new HashMap<>());
  }

  /**
   * Returns the processed property value for the given file kind and key.
   * Applies {@code #{...}} and {@code ${...}} resolution on top of the raw value.
   *
   * @param locale locale, may be {@code null} which means no {@code Locale} specified.
   * @param fileKind the file kind
   * @param key the key of the property
   * @param elParameterMap parameters for EL expression evaluation
   * @return the processed value
   */
  public static String getProp(@Nullable Locale locale, PropertiesFileUtilFileKindEnum fileKind,
      String key, Map<@NonNull String, @Nullable Object> elParameterMap) {
    String raw = obtainBundleReader(fileKind).getProp(locale, key);
    return analyzedValueString(locale, raw, elParameterMap);
  }

  /**
   * Returns whether the given key exists in the specified file kind.
   *
   * @param fileKind the file kind
   * @param key the key of the property
   * @return {@code true} if the key exists
   */
  public static boolean hasProp(PropertiesFileUtilFileKindEnum fileKind, String key) {
    return obtainBundleReader(fileKind).hasProp(key);
  }

  /**
   * Resolves {@code Arg} to its value, preserving the original {@code Object} type
   * for {@link ArgKind#OBJECT} args.
   *
   * <p>{@link ArgKind#OBJECT} args return the raw value, enabling type-aware
   *     {@link java.text.MessageFormat} patterns (e.g., {@code {0,number,#,###}}).
   *     {@link ArgKind#MESSAGE_ID} and {@link ArgKind#FORMATTED_STRING} args resolve
   *     to {@code String}.</p>
   *
   * @param locale locale, may be {@code null} which means no {@code Locale} specified.
   * @param arg message argument
   * @return resolved value; {@code Object} for OBJECT kind, {@code String} for others;
   *     may be {@code null} if {@link Arg#object(Object)} was called with {@code null}
   */
  public static @Nullable Object resolveArgAsObject(@Nullable Locale locale, Arg arg) {

    if (arg.getArgKind() == ArgKind.MESSAGE_ID) {
      String msgIdStr = "";
      // argValue is always a non-null String for MESSAGE_ID (set via constructor)
      String argString = Objects.requireNonNull((String) arg.getArgValue());
      for (PropertiesFileUtilFileKindEnum fileKind : arg.getFileKinds()) {
        String template = getProp(locale, fileKind, argString);
        msgIdStr = PropertiesFileUtilFormatter.formatWithArgs(locale, template,
            resolveArgElements(locale, arg.getMessageArgs()));

        if (hasProp(fileKind, argString)) {
          break;
        }
      }

      return msgIdStr;

    } else if (arg.getArgKind() == ArgKind.FORMATTED_STRING) {
      List<@NonNull String> argStrList = new ArrayList<>();
      // argValue is always a non-null String for FORMATTED_STRING (set via constructor)
      String argString = analyzedValueString(locale,
          Objects.requireNonNull((String) arg.getArgValue()), new HashMap<>());

      for (Object tmpObj : arg.getMessageArgs()) {
        Arg tmpArg = tmpObj instanceof Arg a ? a : Arg.object(tmpObj);
        argStrList.add(resolveArgAsString(locale, tmpArg));
      }

      // replace ' to '' because MessageFormat removes single '.
      return MessageFormat.format(argString.replace("'", "''"),
          (Object[]) argStrList.toArray(String[]::new));

    } else if (arg.getArgKind() == ArgKind.OBJECT) {
      return arg.getArgValue();

    } else {
      throw new RuntimeException("Unexpected.");
    }
  }

  /**
   * Resolves {@code Arg} to a {@link String} as if the pattern were {@code "{0}"}.
   *
   * <p>Equivalent to {@code MessageFormat.format("{0}", resolveArgAsObject(locale, arg))}.
   *     Intended for use cases where an {@link Arg} is rendered as a standalone string
   *     (e.g., a message prefix) rather than as part of a
   *     {@link java.text.MessageFormat} pattern.</p>
   *
   * @param locale locale, may be {@code null} which means no {@code Locale} specified.
   * @param arg message argument
   * @return the string representation of the resolved {@code Arg}
   */
  public static String resolveArgAsString(@Nullable Locale locale, Arg arg) {
    return PropertiesFileUtilFormatter.formatWithArgs(locale, "{0}",
        new Object[] {resolveArgAsObject(locale, arg)});
  }

  /**
   * Resolves any {@link Arg} elements within a mixed {@code Object[]} into their plain
   * {@code Object} values, leaving non-{@link Arg} elements unchanged.
   *
   * @param locale locale, may be {@code null} which means no {@code Locale} specified.
   * @param args argument array possibly containing {@link Arg} instances
   * @return array with all {@link Arg} instances replaced by their resolved values
   */
  public static Object[] resolveArgElements(@Nullable Locale locale, Object[] args) {
    return Arrays.stream(args)
        .map(arg -> arg instanceof Arg a ? resolveArgAsObject(locale, a) : arg).toArray();
  }

  /**
   * Resolves any {@link Arg} values within a named-parameter map into their plain
   * {@code Object} values, leaving non-{@link Arg} values unchanged.
   *
   * <p>This allows callers to put {@link Arg} instances directly into the map that is passed to
   * {@link jp.ecuacion.lib.core.util.PropertiesFileUtil#getValidationMessage} etc., without
   * wrapping them in {@code ValidatorMessageParameterCreator.ItemNameParam}.</p>
   *
   * @param locale locale, may be {@code null} which means no {@code Locale} specified.
   * @param argMap named-parameter map possibly containing {@link Arg} values
   * @return new map with all {@link Arg} values replaced by their resolved values
   */
  public static Map<@NonNull String, @Nullable Object> resolveArgElementsInMap(
      @Nullable Locale locale, Map<@NonNull String, @Nullable Object> argMap) {
    Map<@NonNull String, @Nullable Object> result = new HashMap<>(argMap);
    result.replaceAll((ignored, v) -> v instanceof Arg a ? resolveArgAsObject(locale, a) : v);
    return result;
  }

  /**
   * Resolves {@code #{fileKind:key}} and {@code #{key}} references in {@code rawString},
   * and evaluates any {@code ${...}} EL expressions.
   *
   * @param locale locale, may be {@code null}
   * @param rawString raw string possibly containing {@code #{...}} or {@code ${...}} syntax
   * @param elParameterMap parameters for EL expression evaluation
   * @return fully resolved string
   */
  public static String analyzedValueString(@Nullable Locale locale, String rawString,
      Map<@NonNull String, @Nullable Object> elParameterMap) {
    StringBuilder sb = new StringBuilder();
    sb.append(rawString);
    List<Pair<@Nullable String, String>> list = null;
    locale = locale == null ? Locale.ENGLISH : locale;

    // conditional branch if el expression exists for processing speed.
    if (sb.toString().contains("#{")) {
      // Analyze messageString for #{fileKind:key} and #{key} format parameters.
      list = analyze(sb.toString());
      sb = new StringBuilder();

      for (Pair<@Nullable String, String> tuple : list) {
        String left = tuple.getLeft();
        if (left == null) {
          sb.append(tuple.getRight());

        } else if (left.isEmpty()) {
          sb.append(searchKeyAcrossFileKinds(locale, tuple.getRight()));

        } else {
          sb.append(
              getProp(locale, PropertiesFileUtilFileKindEnum.valueOf(left.toUpperCase(Locale.ROOT)),
                  tuple.getRight()));
        }
      }
    }

    // conditional branch if el expression exists for processing speed.
    if (sb.toString().contains("${")) {
      // Analyze messageString for ${xxx} (EL expression) format parameters.
      try {
        list = EmbeddedVariableUtil.getPartList(sb.toString(), new String[] {"${"}, "}",
            new Options().setIgnoresEmergenceOfEndSymbolOnly(true));

      } catch (ViolationException ex) {
        throw new RuntimeException(ex);
      }

      sb = new StringBuilder();
      ELProcessor elProcessor = new ELProcessor();
      elParameterMap.forEach(elProcessor::setValue);

      for (Pair<@Nullable String, String> tuple : list) {
        if (tuple.getLeft() == null) {
          sb.append(tuple.getRight());

        } else {
          sb.append(elProcessor.eval(tuple.getRight()).toString());
        }
      }
    }

    return sb.toString();
  }

  private static String searchKeyAcrossFileKinds(@Nullable Locale locale, String key) {
    for (PropertiesFileUtilFileKindEnum fileKind : FILE_KINDS_FOR_KEY_ONLY_SEARCH) {
      if (obtainBundleReader(fileKind).hasProp(key)) {
        return getProp(locale, fileKind, key);
      }
    }
    throw new RuntimeException(
        "Key '" + key + "' not found in any properties file for '#{key}' syntax.");
  }

  /**
   * Analyzes and obtain final message part.
   *
   * <p>Handles two syntax forms:</p>
   * <ul>
   * <li>{@code #{fileKind:key}} - resolves key from the specified file kind</li>
   * <li>{@code #{key}} - resolves key by searching across messages, item_names,
   *     constants, and enum_names</li>
   * </ul>
   *
   * <p>For example, when the message is {@code Hello, #{messages:human}!},
   *     the analyzed result is:<br>
   *     {@code (null, "Hello, "), ("messages", "human"), (null, "!")}.</p>
   *
   * <p>When the message is {@code Hello, #{human}!}, the left side of the pair is
   *     {@code ""} (empty string), indicating a key-only reference.</p>
   *
   * @param string string
   * @return list of pairs where left is fileKind (or null for literals, "" for key-only)
   */
  private static List<Pair<@Nullable String, String>> analyze(String string) {
    final String prefix = "#{";

    // Pass 1: #{fileKind:key} patterns (like #{messages:key}, #{item_names:key}).
    List<@NonNull String> fileKindStartSymbols =
        Arrays.stream(PropertiesFileUtilFileKindEnum.values())
            .map(en -> prefix + en.toString().toLowerCase(Locale.ROOT) + ":").toList();

    List<Pair<@Nullable String, String>> pass1Result =
        EmbeddedVariableUtil.getPartList(string, fileKindStartSymbols.toArray(String[]::new), "}",
            new Options().setIgnoresEmergenceOfEndSymbolOnly(true));

    // Pass 2: #{key} patterns (no fileKind) found in remaining literal parts.
    List<Pair<@Nullable String, String>> resultList = new ArrayList<>();
    for (Pair<@Nullable String, String> pair : pass1Result) {
      if (pair.getLeft() != null || !pair.getRight().contains(prefix)) {
        resultList.add(pair);
      } else {
        List<Pair<@Nullable String, String>> subList =
            EmbeddedVariableUtil.getPartList(pair.getRight(), new String[] {prefix}, "}",
                new Options().setIgnoresEmergenceOfEndSymbolOnly(true));
        for (Pair<@Nullable String, String> subPair : subList) {
          resultList.add(subPair.getLeft() != null ? Pair.of("", subPair.getRight()) : subPair);
        }
      }
    }

    // Error check: remaining "#{" in literal parts means incorrect syntax.
    if (resultList.stream().anyMatch(p -> p.getLeft() == null && p.getRight().contains(prefix))) {
      throw new RuntimeException("Improper '#{' symbols found in a message. message: " + string);
    }

    // Strip "#{" prefix and ":" suffix from fileKind start symbols; leave "" (key-only) as-is.
    return resultList.stream()
        .map(
            pair -> pair.getLeft() == null || Objects.requireNonNull(pair.getLeft()).isEmpty()
                ? pair
                : Pair.of(Objects.requireNonNull(pair.getLeft()).substring(prefix.length(),
                    Objects.requireNonNull(pair.getLeft()).length() - 1), pair.getRight()))
        .toList();
  }
}
