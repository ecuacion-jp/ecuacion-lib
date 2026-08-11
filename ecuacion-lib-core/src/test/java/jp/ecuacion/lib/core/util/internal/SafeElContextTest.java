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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import jakarta.el.ELException;
import jakarta.el.ELResolver;
import jakarta.el.ExpressionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** Tests for {@link SafeElContext}. */
@DisplayName("SafeElContext")
public class SafeElContextTest {

  @SuppressWarnings("null")
  private SafeElContext elContext;
  @SuppressWarnings("null")
  private ELResolver resolver;

  @BeforeEach
  void setUp() {
    elContext = new SafeElContext(ExpressionFactory.newInstance());
    resolver = elContext.getELResolver();
  }

  @Test
  @DisplayName("addELResolver is not supported, so the resolver chain cannot be widened")
  void addElResolverIsUnsupported() {
    assertThatThrownBy(() -> elContext.addELResolver(resolver))
        .isInstanceOf(UnsupportedOperationException.class);
  }

  @Nested
  @DisplayName("the resolver returned by getELResolver")
  class Resolver {

    @Test
    @DisplayName("getValue: an unbound top-level identifier (base == null) resolves to null")
    void getValueWithNullBaseReturnsNull() {
      assertThat(resolver.getValue(elContext, null, "unbound")).isNull();
    }

    @Test
    @DisplayName("getValue: property access on a bound value is rejected")
    void getValueWithNonNullBaseIsRejected() {
      assertThatThrownBy(() -> resolver.getValue(elContext, new Object(), "someProperty"))
          .isInstanceOf(ELException.class);
      assertThat(elContext.isPropertyResolved()).isTrue();
    }

    @Test
    @DisplayName("invoke: method invocation on a bound value is rejected")
    void invokeIsRejected() {
      assertThatThrownBy(
          () -> resolver.invoke(elContext, new Object(), "toString", null, null))
          .isInstanceOf(ELException.class);
      assertThat(elContext.isPropertyResolved()).isTrue();
    }

    @Test
    @DisplayName("setValue: writing a property on a bound value is rejected")
    void setValueIsRejected() {
      assertThatThrownBy(
          () -> resolver.setValue(elContext, new Object(), "someProperty", "someValue"))
          .isInstanceOf(ELException.class);
      assertThat(elContext.isPropertyResolved()).isTrue();
    }
  }
}
