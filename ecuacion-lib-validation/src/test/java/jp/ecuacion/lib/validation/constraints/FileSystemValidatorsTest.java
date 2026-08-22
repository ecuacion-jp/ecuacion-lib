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
package jp.ecuacion.lib.validation.constraints;

import static org.assertj.core.api.Assertions.assertThat;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Tests for path-existence validators. */
@DisplayName("Path-existence validators")
@SuppressWarnings("SameNameButDifferent")
public class FileSystemValidatorsTest {

  private static final Validator validator =
      Validation.buildDefaultValidatorFactory().getValidator();

  // -------------------------------------------------------------------------
  // FileExists
  // -------------------------------------------------------------------------

  @Nested
  @DisplayName("@FileExists")
  class FileExistsTests {

    @Test
    @DisplayName("null and empty string are valid")
    void nullAndEmpty() {
      assertThat(validator.validate(new StringBean(null))).isEmpty();
      assertThat(validator.validate(new StringBean(""))).isEmpty();
    }

    @Test
    @DisplayName("existing regular file is valid")
    void existingFile(@TempDir Path tempDir) throws IOException {
      Path file = Files.createFile(tempDir.resolve("existing.txt"));
      assertThat(validator.validate(new StringBean(file.toString()))).isEmpty();
      assertThat(validator.validate(new FileBean(file.toFile()))).isEmpty();
      assertThat(validator.validate(new PathBean(file))).isEmpty();
    }

    @Test
    @DisplayName("non-existing path is invalid")
    void nonExisting(@TempDir Path tempDir) {
      Path file = tempDir.resolve("nonExisting.txt");
      assertThat(validator.validate(new StringBean(file.toString()))).hasSize(1);
    }

    @Test
    @DisplayName("existing directory is invalid")
    void existingDirectory(@TempDir Path tempDir) {
      assertThat(validator.validate(new StringBean(tempDir.toString()))).hasSize(1);
    }

    private record StringBean(@FileExists @Nullable String value) {}

    private record FileBean(@FileExists @Nullable File value) {}

    private record PathBean(@FileExists @Nullable Path value) {}
  }

  // -------------------------------------------------------------------------
  // DirExists
  // -------------------------------------------------------------------------

  @Nested
  @DisplayName("@DirExists")
  class DirExistsTests {

    @Test
    @DisplayName("null and empty string are valid")
    void nullAndEmpty() {
      assertThat(validator.validate(new StringBean(null))).isEmpty();
      assertThat(validator.validate(new StringBean(""))).isEmpty();
    }

    @Test
    @DisplayName("existing directory is valid")
    void existingDirectory(@TempDir Path tempDir) {
      assertThat(validator.validate(new StringBean(tempDir.toString()))).isEmpty();
      assertThat(validator.validate(new FileBean(tempDir.toFile()))).isEmpty();
      assertThat(validator.validate(new PathBean(tempDir))).isEmpty();
    }

    @Test
    @DisplayName("non-existing path is invalid")
    void nonExisting(@TempDir Path tempDir) {
      Path dir = tempDir.resolve("nonExisting");
      assertThat(validator.validate(new StringBean(dir.toString()))).hasSize(1);
    }

    @Test
    @DisplayName("existing regular file is invalid")
    void existingFile(@TempDir Path tempDir) throws IOException {
      Path file = Files.createFile(tempDir.resolve("existing.txt"));
      assertThat(validator.validate(new StringBean(file.toString()))).hasSize(1);
    }

    private record StringBean(@DirExists @Nullable String value) {}

    private record FileBean(@DirExists @Nullable File value) {}

    private record PathBean(@DirExists @Nullable Path value) {}
  }

  // -------------------------------------------------------------------------
  // PathExists
  // -------------------------------------------------------------------------

  @Nested
  @DisplayName("@PathExists")
  class PathExistsTests {

    @Test
    @DisplayName("null and empty string are valid")
    void nullAndEmpty() {
      assertThat(validator.validate(new StringBean(null))).isEmpty();
      assertThat(validator.validate(new StringBean(""))).isEmpty();
    }

    @Test
    @DisplayName("existing regular file is valid")
    void existingFile(@TempDir Path tempDir) throws IOException {
      Path file = Files.createFile(tempDir.resolve("existing.txt"));
      assertThat(validator.validate(new StringBean(file.toString()))).isEmpty();
    }

    @Test
    @DisplayName("existing directory is valid")
    void existingDirectory(@TempDir Path tempDir) {
      assertThat(validator.validate(new FileBean(tempDir.toFile()))).isEmpty();
      assertThat(validator.validate(new PathBean(tempDir))).isEmpty();
    }

    @Test
    @DisplayName("non-existing path is invalid")
    void nonExisting(@TempDir Path tempDir) {
      Path path = tempDir.resolve("nonExisting");
      assertThat(validator.validate(new StringBean(path.toString()))).hasSize(1);
    }

    private record StringBean(@PathExists @Nullable String value) {}

    private record FileBean(@PathExists @Nullable File value) {}

    private record PathBean(@PathExists @Nullable Path value) {}
  }

  // -------------------------------------------------------------------------
  // FileExtension
  // -------------------------------------------------------------------------

  @Nested
  @DisplayName("@FileExtension")
  class FileExtensionTests {

    @Test
    @DisplayName("null and empty string are valid")
    void nullAndEmpty() {
      assertThat(validator.validate(new Bean(null))).isEmpty();
      assertThat(validator.validate(new Bean(""))).isEmpty();
    }

    @Test
    @DisplayName("matching extension is valid regardless of case")
    void matchingExtension() {
      assertThat(validator.validate(new Bean("book.xlsx"))).isEmpty();
      assertThat(validator.validate(new Bean("book.XLSX"))).isEmpty();
      assertThat(validator.validate(new Bean("dir/book.xlsx"))).isEmpty();
      assertThat(validator.validate(new FileBean(new File("book.xlsx")))).isEmpty();
      assertThat(validator.validate(new PathBean(Path.of("book.xlsx")))).isEmpty();
    }

    @Test
    @DisplayName("annotation value with or without leading dot behaves the same")
    void leadingDotIsIgnoredInAnnotationValue() {
      assertThat(validator.validate(new BeanWithDot("book.xlsx"))).isEmpty();
      assertThat(validator.validate(new BeanWithDot("book.csv"))).hasSize(1);
    }

    @Test
    @DisplayName("mismatching extension is invalid")
    void mismatchingExtension() {
      assertThat(validator.validate(new Bean("book.csv"))).hasSize(1);
    }

    @Test
    @DisplayName("no extension is invalid")
    void noExtension() {
      assertThat(validator.validate(new Bean("book"))).hasSize(1);
      assertThat(validator.validate(new Bean(".xlsx"))).hasSize(1);
    }

    private record Bean(@FileExtension("xlsx") @Nullable String value) {}

    private record FileBean(@FileExtension("xlsx") @Nullable File value) {}

    private record PathBean(@FileExtension("xlsx") @Nullable Path value) {}

    private record BeanWithDot(@FileExtension(".xlsx") @Nullable String value) {}
  }
}
