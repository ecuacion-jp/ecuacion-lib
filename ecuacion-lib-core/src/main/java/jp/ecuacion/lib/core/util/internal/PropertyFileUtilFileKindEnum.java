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

  /** application.properties. */
  APP("application", new String[][] {new String[] {"application"}}),

  /** 
   * messages.properties. 
   */
  MSG("messages", new String[][] {new String[] {"messages"}}),

  /** itemの名称を記述. */
  ITEM_NAME("item_names", new String[][] {new String[] {"item_names"}}),

  /** enumの名称を記述. */
  ENUM_NAME("enum_names", new String[][] {new String[] {"enum_names"}}),

  /** ValidationMessags */
  VALIDATION_MESSAGES("ValidationMessages", new String[][] {new String[] {"ValidationMessages"}}),

  /** ValidationMessagsWithField */
  VALIDATION_MESSAGES_WITH_ITEM_NAMES("ValidationMessagesWithItemNames", new String[][] {
      new String[] {"ValidationMessagesWithItemNames"}, new String[] {"ValidationMessages"}}),

  VALIDATION_MESSAGES_PATTERN_DESCRIPTIONS("ValidationMessagesPatternDescriptions",
      new String[][] {new String[] {"ValidationMessagesPatternDescriptions"}});

  private String filePrefix;
  private String[][] actualFilePrefixes;

  private PropertyFileUtilFileKindEnum(String filePrefix, String[][] actualFilePrefixes) {
    this.filePrefix = filePrefix;
    this.actualFilePrefixes = actualFilePrefixes;
  }

  public String getFilePrefix() {
    return filePrefix;
  }

  public String[][] getActualFilePrefixes() {
    return actualFilePrefixes;
  }

  public static PropertyFileUtilFileKindEnum getEnumFromFilePrefix(String filePrefix) {
    for (PropertyFileUtilFileKindEnum anEnum : PropertyFileUtilFileKindEnum.values()) {
      if (anEnum.getFilePrefix().equals(filePrefix)) {
        return anEnum;
      }
    }

    return null;
  }
}
