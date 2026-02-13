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

/** 
 * Holds kinds of property files.
 * 
 * <p>The first argument of the enum value (like "application", "messages", ...) 
 *     is called {@code filePrefix},
 *     which is the file literally the prefix of the property files.<br>
 *     But in some reasons some properties file kind need to have multiple file prefixes
 *     so the second argument {@code actualFilePrefixes} needed.</p>
 *     
 * <p>{@code actualFilePrefixes} is a data type of {@code String[][]}.
 *     If you want to simply use multiple prefixes, it's realized by 
 *     {@code new String[][] {new String[] {messages1, messages2}}}.
 *     With this prefix setting {@code PropertyFileUtilValueGetter} searches the key
 *     from files with these prefixes, and duplication of the key causes an error.
 *     <br><br>
 *     When you know {@code messages1} and {@code messages2} has duplication and 
 *     if {@code messages1} contains the key you don't want to search from {@code messages2},
 *     it can be realized by this setting.<br>
 *     {@code new String[][] {new String[] {messages1}, new String[] {messages2}}}.<br>
 *     Outside array manipulates the priority of the property files.</p>
 */
public enum PropertyFileUtilFileKindEnum {

  /** 
   * application.properties. 
   */
  APPLICATION(new String[][] {new String[] {"application"}}, true),

  /** 
   * messages.properties. 
   */
  MESSAGES(new String[][] {new String[] {"messages"}}, false),

  /** 
   * messagesWithItemNames.properties. 
   * 
   * <p>Generally, messages.properties is supposed to be used for messages, 
   *     and messagesWithItemNames is not very understandable for users.
   *     Besides that, since the case that both messages.properties and 
   *     messagesWithItemNames.properties 
   *     are used is very rare, it's better for messages.properties to be used even when
   *     you treat "messages with item names" without that rare case.
   *     That's why messages.properties is also contained with this value.
   */
  MESSAGES_WITH_ITEM_NAMES(
      new String[][] {new String[] {"messages_with_item_names"}, new String[] {"messages"}}, false),

  /**
   * strings.properties.
   */
  STRINGS(new String[][] {new String[] {"strings"}}, false),

  /** item_names. */
  ITEM_NAMES(new String[][] {new String[] {"item_names"}}, false),

  /** enum_names. */
  ENUM_NAMES(new String[][] {new String[] {"enum_names"}}, false),

  /** ValidationMessags. */
  VALIDATION_MESSAGES(new String[][] {new String[] {"ValidationMessages"}}, false),

  /** ValidationMessagsWithField. */
  VALIDATION_MESSAGES_WITH_ITEM_NAMES(
      new String[][] {new String[] {"ValidationMessagesWithItemNames"}}, false),

  VALIDATION_MESSAGES_PATTERN_DESCRIPTIONS(
      new String[][] {new String[] {"ValidationMessagesPatternDescriptions"}}, false);

  private String[][] actualFilePrefixes;
  private boolean throwsExceptionWhenKeyDoesNotExist;

  private PropertyFileUtilFileKindEnum(String[][] actualFilePrefixes,
      boolean throwsExceptionWhenKeyDoesNotExist) {
    this.actualFilePrefixes = actualFilePrefixes;
    this.throwsExceptionWhenKeyDoesNotExist = throwsExceptionWhenKeyDoesNotExist;
  }

  public String[][] getActualFilePrefixes() {
    return actualFilePrefixes;
  }

  /**
   * Returns throwsExceptionWhenKeyDoesNotExist.
   */
  public boolean throwsExceptionWhenKeyDoesNotExist() {
    return throwsExceptionWhenKeyDoesNotExist;
  }
}
