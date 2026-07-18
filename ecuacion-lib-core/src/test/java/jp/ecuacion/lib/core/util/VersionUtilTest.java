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

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Tests for {@link VersionUtil}. */
@DisplayName("VersionUtil")
public class VersionUtilTest {

  @Test
  @DisplayName("getVersion(String): returns a non-blank version resolved by the Maven build "
      + "(not the unresolved @project.version@ placeholder)")
  void getVersionWithProductNameReturnsResolvedVersion() {
    String version = VersionUtil.getVersion("ecuacion-lib");

    assertThat(version).isNotBlank();
    assertThat(version).isNotEqualTo("@project.version@");
  }

  @Test
  @DisplayName("getVersion(String): caches the value so a second call for the same "
      + "productName returns the same instance")
  void getVersionCachesResult() {
    String first = VersionUtil.getVersion("ecuacion-lib");
    String second = VersionUtil.getVersion("ecuacion-lib");

    assertThat(second).isSameAs(first);
  }

  @Test
  @DisplayName("getVersion(String): returns null when no matching "
      + "version_<productName>.properties exists on the classpath")
  void getVersionWithProductNameReturnsNullWhenFileNotFound() {
    assertThat(VersionUtil.getVersion("nonexistent-product")).isNull();
  }

  @Test
  @DisplayName("getVersion(\"\"): returns null when version.properties does not exist "
      + "on the classpath (no such file is bundled with ecuacion-lib-core itself)")
  void getVersionWithEmptyProductNameReturnsNullWhenFileNotFound() {
    assertThat(VersionUtil.getVersion("")).isNull();
  }
}
