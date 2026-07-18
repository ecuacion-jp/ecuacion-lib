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

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import org.jspecify.annotations.Nullable;

/**
 * Provides the version of ecuacion products (e.g. {@code ecuacion-lib}, {@code ecuacion-splib},
 * {@code ecuacion-utils}), or of an individual app.
 *
 * <p>Each ecuacion product bundles its own {@code version_<productName>.properties} file
 * (e.g. {@code version_ecuacion-lib.properties}), and an individual app may likewise bundle a
 * {@code version.properties} file (pass {@code ""} as {@code productName} to read it). In both
 * cases {@code @project.version@} is replaced with the build version via Maven resource
 * filtering. This class is implemented in {@code ecuacion-lib-core} because it is always
 * present as a dependency of every other ecuacion product, letting a single shared
 * implementation read every version file off the classpath.</p>
 */
public class VersionUtil {

  private static final Map<String, String> versionCache = new ConcurrentHashMap<>();

  private VersionUtil() {}

  /**
   * Returns the version of the specified ecuacion product, read from
   * {@code version_<productName>.properties} on the classpath. For an individual app (not an
   * ecuacion product), pass {@code ""} to read {@code version.properties} instead.
   *
   * @param productName the product name, e.g. {@code "ecuacion-lib"}, {@code "ecuacion-splib"},
   *     {@code "ecuacion-utils"}; or {@code ""} for an individual app's own version.properties
   * @return the version string (e.g. {@code "16.0.0"}), or {@code null} if the corresponding
   *     version properties file does not exist on the classpath
   */
  public static @Nullable String getVersion(String productName) {
    String cached = versionCache.get(productName);
    if (cached != null) {
      return cached;
    }

    String version = readVersion(productName);
    if (version != null) {
      versionCache.put(productName, version);
    }

    return version;
  }

  private static @Nullable String readVersion(String productName) {
    String resourceName =
        "/version" + (productName.isEmpty() ? "" : "_" + productName) + ".properties";

    Properties props = new Properties();
    try (InputStream is = VersionUtil.class.getResourceAsStream(resourceName)) {
      if (is == null) {
        return null;
      }

      props.load(is);

    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }

    return props.getProperty("version");
  }
}
