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

import jakarta.el.ArrayELResolver;
import jakarta.el.CompositeELResolver;
import jakarta.el.ELContext;
import jakarta.el.ELException;
import jakarta.el.ELResolver;
import jakarta.el.ExpressionFactory;
import jakarta.el.ListELResolver;
import jakarta.el.MapELResolver;
import jakarta.el.StandardELContext;
import java.util.Iterator;
import org.jspecify.annotations.Nullable;

/**
 * An {@link ELContext} that resolves only explicitly bound variables — plus indexing into
 * arrays/lists/maps of already-bound values — and rejects property access or method invocation
 * on any object.
 *
 * <p>{@code ${...}} values only ever come from {@code *.properties} files the application itself
 *     controls, so the risk this closes is narrow. Still, {@link jakarta.el.ELProcessor} (used
 *     previously) grants full, unrestricted EL power — a value like
 *     {@code ${arg.getClass().getClassLoader()}} would run arbitrary reflection on whatever is
 *     bound as {@code arg}. This mirrors Hibernate Validator's own default (most restrictive) EL
 *     feature level for Bean Validation message interpolation ({@code VariablesELContext}), so
 *     {@code ValidationMessages*.properties} etc. get the same sandboxing Jakarta Bean Validation
 *     itself applies by default.</p>
 *
 * <p>Simple variable references (e.g. {@code ${inclusive}}) and operators/literals (e.g.
 *     {@code ${inclusive == true ? 'a' : 'b'}}, {@code ${1 + 1}}) are unaffected: those resolve
 *     via {@link ELContext#getVariableMapper()} or the EL engine's own operators, never through
 *     {@link #getELResolver()}.</p>
 */
class SafeElContext extends StandardELContext {

  private static final ELResolver RESOLVER;

  static {
    CompositeELResolver resolver = new CompositeELResolver();
    resolver.add(new ArrayELResolver(true));
    resolver.add(new ListELResolver(true));
    resolver.add(new MapELResolver(true));
    resolver.add(new DenyingElResolver());
    RESOLVER = resolver;
  }

  SafeElContext(ExpressionFactory expressionFactory) {
    super(expressionFactory);
  }

  @Override
  public void addELResolver(@Nullable ELResolver elResolver) {
    throw new UnsupportedOperationException(
        getClass().getSimpleName() + " does not support addELResolver.");
  }

  @Override
  public ELResolver getELResolver() {
    return RESOLVER;
  }

  /** Denies property access, method invocation, and property writes on any object. */
  private static final class DenyingElResolver extends ELResolver {

    @Override
    public @Nullable Object getValue(@Nullable ELContext context, @Nullable Object base,
        @Nullable Object property) {
      if (base == null) {
        // Unbound top-level identifier; let it resolve to null rather than erroring.
        return null;
      }

      if (context != null) {
        context.setPropertyResolved(true);
      }
      throw new ELException("Property access is not allowed in this expression: " + property);
    }

    @Override
    public @Nullable Object invoke(@Nullable ELContext context, @Nullable Object base,
        @Nullable Object method, Class<?> @Nullable [] paramTypes, Object @Nullable [] params) {
      if (context != null) {
        context.setPropertyResolved(true);
      }
      throw new ELException("Method invocation is not allowed in this expression: " + method);
    }

    @Override
    public void setValue(@Nullable ELContext context, @Nullable Object base,
        @Nullable Object property, @Nullable Object value) {
      if (context != null) {
        context.setPropertyResolved(true);
      }
      throw new ELException("Setting properties is not allowed in this expression: " + property);
    }

    @Override
    public boolean isReadOnly(@Nullable ELContext context, @Nullable Object base,
        @Nullable Object property) {
      return true;
    }

    @Override
    public @Nullable Class<?> getCommonPropertyType(@Nullable ELContext context,
        @Nullable Object base) {
      return null;
    }

    @Override
    public @Nullable Class<?> getType(@Nullable ELContext context, @Nullable Object base,
        @Nullable Object property) {
      return null;
    }

    // Raw Iterator (rather than Iterator<FeatureDescriptor>) avoids a compile-time reference to
    // java.beans.FeatureDescriptor, which lives in the java.desktop module that this project's
    // module-info does not (and should not, for one deprecated, unused legacy method) require.
    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public @Nullable Iterator getFeatureDescriptors(@Nullable ELContext context,
        @Nullable Object base) {
      return null;
    }
  }
}
