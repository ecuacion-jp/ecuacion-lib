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

public enum PropertyFileUtilPropFileKindEnum {

  /** application.properties. */
  APP("application"),

  /** messages.properties. */
  MSG("messages"),

  /** enumの名称を記述. */
  FIELD_NAME("field_names"),

  /** fieldの各項目の名称. */
  ENUM_NAME("enum_names");

  private String filePrefix;

  private PropertyFileUtilPropFileKindEnum(String filePrefix) {
    this.filePrefix = filePrefix;
  }
  
  public String getFilePrefix() {
    return filePrefix;
  }
  
  public static PropertyFileUtilPropFileKindEnum getEnumFromFilePrefix(String filePrefix) {
    for (PropertyFileUtilPropFileKindEnum anEnum : PropertyFileUtilPropFileKindEnum.values()) {
      if (anEnum.getFilePrefix().equals(filePrefix)) {
        return anEnum;
      }
    }
    
    return null;
  }
}
