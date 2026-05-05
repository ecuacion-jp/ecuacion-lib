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
package jp.ecuacion.lib.core.violation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import jp.ecuacion.lib.core.exception.ViolationException;
import jp.ecuacion.lib.core.exception.ViolationWarningException;
import jp.ecuacion.lib.core.util.PropertiesFileUtil.Arg;
import jp.ecuacion.lib.core.violation.Violations.MessageParameters;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** Tests for {@link Violations}. */
@DisplayName("Violations")
public class ViolationsTest {

  @Test
  @DisplayName("empty violations does not throw")
  public void emptyViolationsDoesNotThrow() {
    new Violations().throwIfAny();
  }

  @Test
  @DisplayName("throwIfAny throws ViolationException when a violation is present")
  public void throwIfAny() {
    Violations violations = new Violations().add(new BusinessViolation("TEST_KEY"));
    Assertions.assertThrows(ViolationException.class, violations::throwIfAny);
  }

  @Test
  @DisplayName("added violations are stored in order")
  public void violationsStoredCorrectly() {
    Violations violations = new Violations()
        .add(new BusinessViolation("KEY_1"))
        .add(new BusinessViolation("KEY_2"))
        .add(new BusinessViolation("KEY_3"));

    assertThat(violations.getBusinessViolations()).hasSize(3);
    assertThat(violations.getBusinessViolations().get(0).getMessageId()).isEqualTo("KEY_1");
    assertThat(violations.getBusinessViolations().get(1).getMessageId()).isEqualTo("KEY_2");
    assertThat(violations.getBusinessViolations().get(2).getMessageId()).isEqualTo("KEY_3");
  }

  @Test
  @DisplayName("thrown ViolationException holds the violations")
  public void thrownExceptionHoldsViolations() {
    Violations violations = new Violations().add(new BusinessViolation("TEST_KEY"));

    @SuppressWarnings("null")
    ViolationException ex =
        Assertions.assertThrows(ViolationException.class, violations::throwIfAny);
    assertThat(ex.getViolations()).isNotNull();
    assertThat(ex.getViolations().getBusinessViolations()).hasSize(1);
  }

  @Test
  @DisplayName("throwIfAny(Class) does not throw when empty")
  public void throwIfAnyWithClassEmpty() {
    new Violations().throwIfAny(CustomViolationException.class);
  }

  @Test
  @DisplayName("throwIfAny(Class) throws the specified exception type when violations present")
  public void throwIfAnyWithClassThrows() {
    Violations violations = new Violations().add(new BusinessViolation("KEY"));
    Assertions.assertThrows(CustomViolationException.class,
        () -> violations.throwIfAny(CustomViolationException.class));
  }

  @Test
  @DisplayName("throwIfAny(Class) throws RuntimeException when class has no Violations constructor")
  public void throwIfAnyWithNoViolationsConstructor() {
    Violations violations = new Violations().add(new BusinessViolation("KEY"));
    assertThatThrownBy(() -> violations.throwIfAny(NoViolationsConstructorException.class))
        .isInstanceOf(RuntimeException.class);
  }

  @Test
  @DisplayName("throwWarningIfAny does not throw when empty")
  public void throwWarningIfAnyEmpty() {
    new Violations().throwWarningIfAny();
  }

  @Test
  @DisplayName("throwWarningIfAny throws ViolationWarningException when violations present")
  public void throwWarningIfAnyThrows() {
    Violations violations = new Violations().add(new BusinessViolation("KEY"));
    assertThatThrownBy(violations::throwWarningIfAny)
        .isInstanceOf(ViolationWarningException.class);
  }

  @Test
  @DisplayName("addAll(List) adds all business violations")
  public void addAllList() {
    List<BusinessViolation> list =
        List.of(new BusinessViolation("K1"), new BusinessViolation("K2"));
    Violations violations = new Violations().addAll(list);
    assertThat(violations.getBusinessViolations()).hasSize(2);
    assertThat(violations.getBusinessViolations().get(0).getMessageId()).isEqualTo("K1");
    assertThat(violations.getBusinessViolations().get(1).getMessageId()).isEqualTo("K2");
  }

  static class CustomViolationException extends ViolationException {
    private static final long serialVersionUID = 1L;

    public CustomViolationException(Violations violations) {
      super(violations);
    }
  }

  static class NoViolationsConstructorException extends ViolationException {
    private static final long serialVersionUID = 1L;

    private NoViolationsConstructorException() {
      super(new Violations());
    }
  }

  // -------------------------------------------------------------------------
  // add(...) overloads
  // -------------------------------------------------------------------------

  @Nested
  @DisplayName("add overloads")
  class AddOverloads {

    @SuppressWarnings("MultipleNullnessAnnotations")
    private static record ValidatedBean(@NotNull @Nullable String value) {}

    private static final Validator validator =
        Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    @DisplayName("add(ConstraintViolation): stores constraint violation")
    void addConstraintViolation() {
      ConstraintViolation<?> cv = validator.validate(new ValidatedBean(null)).iterator().next();
      Violations v = new Violations().add(cv);
      assertThat(v.getConstraintViolations()).hasSize(1);
    }

    @Test
    @DisplayName("add(String, String[]): stores business violation with string args")
    void addStringWithStringArgs() {
      Violations v = new Violations().add("MSG_ID", "arg1");
      assertThat(v.getBusinessViolations()).hasSize(1);
      assertThat(v.getBusinessViolations().get(0).getMessageId()).isEqualTo("MSG_ID");
    }

    @Test
    @DisplayName("add(String[], String, String[]): stores with property paths and string args")
    void addWithPathsAndStringArgs() {
      Violations v = new Violations().add(new String[]{"field"}, "MSG_ID", "arg1");
      assertThat(v.getBusinessViolations()).hasSize(1);
    }

    @Test
    @DisplayName("add(String, Arg[]): stores business violation with Arg args")
    void addStringWithArgArray() {
      Violations v = new Violations().add("MSG_ID", new Arg[]{Arg.string("arg1")});
      assertThat(v.getBusinessViolations()).hasSize(1);
    }

    @Test
    @DisplayName("add(String[], String, Arg[]): stores with property paths and Arg args")
    void addWithPathsAndArgArray() {
      Violations v =
          new Violations().add(new String[]{"field"}, "MSG_ID", new Arg[]{Arg.string("x")});
      assertThat(v.getBusinessViolations()).hasSize(1);
    }

    @Test
    @DisplayName("add(Object, String[], String, String[]): stores with rootBean and string args")
    void addWithRootBeanAndStringArgs() {
      Object rootBean = new Object();
      Violations v = new Violations().add(rootBean, new String[]{"field"}, "MSG_ID", "arg1");
      assertThat(v.getBusinessViolations()).hasSize(1);
      assertThat(v.getBusinessViolations().get(0).getRootBean()).isSameAs(rootBean);
    }

    @Test
    @DisplayName("add(Object, String[], String, Arg[]): stores with rootBean and Arg args")
    void addWithRootBeanAndArgArray() {
      Object rootBean = new Object();
      Violations v = new Violations().add(rootBean, new String[]{"field"}, "MSG_ID",
          new Arg[]{Arg.string("x")});
      assertThat(v.getBusinessViolations()).hasSize(1);
      assertThat(v.getBusinessViolations().get(0).getRootBean()).isSameAs(rootBean);
    }

    @Test
    @DisplayName("messageParameters(MP): setter stores the given MessageParameters")
    void messageParametersSetter() {
      MessageParameters mp = Violations.newMessageParameters();
      Violations v = new Violations().messageParameters(mp);
      assertThat(v.messageParameters()).isSameAs(mp);
    }
  }

  // -------------------------------------------------------------------------
  // MessageParameters constructors and setters
  // -------------------------------------------------------------------------

  @Nested
  @DisplayName("Violations.MessageParameters")
  class MessageParametersTests {

    @Test
    @DisplayName("4-arg constructor (String prefix/postfix): sets all fields")
    void constructorWithStrings() {
      MessageParameters mp = new MessageParameters(true, "prefix-", "-postfix", false);
      assertThat(mp.isMessageWithItemName()).isTrue();
      assertThat(mp.showsItemNamePath()).isFalse();
      assertThat(mp.getMessagePrefix()).isNotNull();
      assertThat(mp.getMessagePostfix()).isNotNull();
    }

    @Test
    @DisplayName("4-arg constructor (Arg prefix/postfix): sets all fields including showsItemNamePath")
    void constructorWithArgs() {
      MessageParameters mp = new MessageParameters(false, true, Arg.string("["), Arg.string("]"));
      assertThat(mp.isMessageWithItemName()).isFalse();
      assertThat(mp.showsItemNamePath()).isTrue();
      assertThat(mp.getMessagePrefix()).isNotNull();
      assertThat(mp.getMessagePostfix()).isNotNull();
    }

    @Test
    @DisplayName("messagePrefix(String): sets prefix from string")
    void messagePrefixString() {
      MessageParameters mp = Violations.newMessageParameters().messagePrefix("[ERROR] ");
      assertThat(mp.getMessagePrefix()).isNotNull();
    }

    @Test
    @DisplayName("messagePostfix(String): sets postfix from string")
    void messagePostfixString() {
      MessageParameters mp = Violations.newMessageParameters().messagePostfix(" [end]");
      assertThat(mp.getMessagePostfix()).isNotNull();
    }
  }
}
