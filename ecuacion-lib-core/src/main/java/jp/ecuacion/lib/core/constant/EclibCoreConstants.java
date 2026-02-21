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
package jp.ecuacion.lib.core.constant;


/**
 * Provides Constants.
 */
public final class EclibCoreConstants {

  /* Cannot be called from outside of the class. */
  private EclibCoreConstants() {

  }

  /** The common part of the package in ecuacion-lib. */
  public static final String PKG_PARENT = "jp.ecuacion.lib";
  public static final String PKG = "jp.ecuacion.lib.core";

  public static final String ECLIB_PREFIX = "[ecuacion-lib] ";

  /** The message contained in {@code RuntimeSystemException}. */
  public static final String MSG_RUNTIME_EXCEPTION_PREFIX = ECLIB_PREFIX + "[RuntimeException] ";
  
  /**
   * is a large partition or separator which separates lines for log, mail or others.
   */
  public static final String PARTITION_LARGE = "===============";

  /**
   * is a medium partition or separator which separates lines for log, mail or others.
   */
  public static final String PARTITION_MEDIUM = "----------";
}
