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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.channels.OverlappingFileLockException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import jp.ecuacion.lib.core.exception.ViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Tests for {@link FileUtil}. */
@DisplayName("FileUtil")
public class FileUtilTest {

  @Nested
  @DisplayName("getFileSavableName")
  class GetFileSavableName {

    @Test
    @DisplayName("normal characters are unchanged")
    void normalChars() {
      assertThat(FileUtil.getFileSavableName("normal-file.txt")).isEqualTo("normal-file.txt");
    }

    @Test
    @DisplayName("slash is replaced")
    void slash() {
      assertThat(FileUtil.getFileSavableName("a/b")).isEqualTo("a__slash__b");
    }

    @Test
    @DisplayName("colon is replaced")
    void colon() {
      assertThat(FileUtil.getFileSavableName("a:b")).isEqualTo("a__colon__b");
    }

    @Test
    @DisplayName("asterisk is replaced")
    void asterisk() {
      assertThat(FileUtil.getFileSavableName("a*b")).isEqualTo("a__asterisk__b");
    }

    @Test
    @DisplayName("question mark is replaced")
    void questionMark() {
      assertThat(FileUtil.getFileSavableName("a?b")).isEqualTo("a__question__b");
    }

    @Test
    @DisplayName("double-quote is replaced")
    void doubleQuote() {
      assertThat(FileUtil.getFileSavableName("a\"b")).isEqualTo("a__dquotation__b");
    }

    @Test
    @DisplayName("less-than is replaced")
    void lessThan() {
      assertThat(FileUtil.getFileSavableName("a<b")).isEqualTo("a__lessthan__b");
    }

    @Test
    @DisplayName("greater-than is replaced")
    void greaterThan() {
      assertThat(FileUtil.getFileSavableName("a>b")).isEqualTo("a__morethan__b");
    }

    @Test
    @DisplayName("pipe is replaced")
    void pipe() {
      assertThat(FileUtil.getFileSavableName("a|b")).isEqualTo("a__pipe__b");
    }
  }

  @Nested
  @DisplayName("concatFilePaths")
  class ConcatFilePaths {

    @Test
    @DisplayName("two simple paths joined with slash")
    void twoPaths() {
      assertThat(FileUtil.concatFilePaths("a", "b")).isEqualTo("a/b");
    }

    @Test
    @DisplayName("trailing slash on first path is normalized")
    void trailingSlashOnFirst() {
      assertThat(FileUtil.concatFilePaths("a/", "b")).isEqualTo("a/b");
    }

    @Test
    @DisplayName("leading slash on second path is normalized")
    void leadingSlashOnSecond() {
      assertThat(FileUtil.concatFilePaths("a", "/b")).isEqualTo("a/b");
    }

    @Test
    @DisplayName("three paths are joined sequentially")
    void threePaths() {
      assertThat(FileUtil.concatFilePaths("a", "b", "c")).isEqualTo("a/b/c");
    }
  }

  @Nested
  @DisplayName("cleanPathStrWithSlash")
  class CleanPathStrWithSlash {

    @Test
    @DisplayName("backslash is unified to slash")
    void backslashToSlash() {
      assertThat(FileUtil.cleanPathStrWithSlash("a\\b\\c")).isEqualTo("a/b/c");
    }

    @Test
    @DisplayName("double slash is removed")
    void doubleSlash() {
      assertThat(FileUtil.cleanPathStrWithSlash("a//b")).isEqualTo("a/b");
    }

    @Test
    @DisplayName("trailing slash is removed")
    void trailingSlash() {
      assertThat(FileUtil.cleanPathStrWithSlash("/a/b/")).isEqualTo("/a/b");
    }

    @Test
    @DisplayName("trailing /. is removed")
    void trailingDot() {
      assertThat(FileUtil.cleanPathStrWithSlash("/a/.")).isEqualTo("/a");
    }

    @Test
    @DisplayName("/./ in the middle is removed")
    void dotInMiddle() {
      assertThat(FileUtil.cleanPathStrWithSlash("/a/./b")).isEqualTo("/a/b");
    }
  }

  @Nested
  @DisplayName("containsWildCard")
  class ContainsWildCard {

    @Test
    @DisplayName("asterisk is detected as wildcard")
    void asterisk() {
      assertThat(FileUtil.containsWildCard("a/*.txt")).isTrue();
    }

    @Test
    @DisplayName("question mark is detected as wildcard")
    void questionMark() {
      assertThat(FileUtil.containsWildCard("a/?.txt")).isTrue();
    }

    @Test
    @DisplayName("no wildcard returns false")
    void noWildcard() {
      assertThat(FileUtil.containsWildCard("a/b.txt")).isFalse();
    }
  }

  @Nested
  @DisplayName("getParentDirPath")
  class GetParentDirPath {

    @Test
    @DisplayName("returns parent directory path")
    void basic() {
      assertThat(FileUtil.getParentDirPath("/a/b/c.txt")).isEqualTo("/a/b");
    }

    @Test
    @DisplayName("backslash path is normalized before extracting parent")
    void backslash() {
      assertThat(FileUtil.getParentDirPath("a\\b\\c.txt")).isEqualTo("a/b");
    }
  }

  @Nested
  @DisplayName("getFileNameFromFilePath")
  class GetFileNameFromFilePath {

    @Test
    @DisplayName("filename extracted from slash-separated path")
    void slashPath() {
      assertThat(FileUtil.getFileNameFromFilePath("/a/b/c.txt")).isEqualTo("c.txt");
    }

    @Test
    @DisplayName("filename extracted from backslash-separated path")
    void backslashPath() {
      assertThat(FileUtil.getFileNameFromFilePath("a\\b\\c.txt")).isEqualTo("c.txt");
    }
  }

  @Nested
  @DisplayName("getFileSizeInMb")
  class GetFileSizeInMb {

    @Test
    @DisplayName("1,000,000 bytes returns 1.0")
    void oneMb() {
      assertThat(FileUtil.getFileSizeInMb(1_000_000L)).isEqualTo("1.0");
    }

    @Test
    @DisplayName("1,500,000 bytes returns 1.5")
    void oneAndHalfMb() {
      assertThat(FileUtil.getFileSizeInMb(1_500_000L)).isEqualTo("1.5");
    }

    @Test
    @DisplayName("getFileSizeInMbWithUnit appends MB unit")
    void withUnit() {
      assertThat(FileUtil.getFileSizeInMbWithUnit(2_000_000L)).isEqualTo("2.0 MB");
    }
  }

  @Nested
  @DisplayName("isRelativePath")
  class IsRelativePath {

    @Test
    @DisplayName("path starting with / is absolute on Unix")
    void absolute() {
      assertThat(FileUtil.isRelativePath("/foo/bar")).isFalse();
    }

    @Test
    @DisplayName("path not starting with / is relative on Unix")
    void relative() {
      assertThat(FileUtil.isRelativePath("foo/bar")).isTrue();
    }

    @Test
    @DisplayName("empty path throws ViolationException")
    void emptyPathThrows() {
      assertThatThrownBy(() -> FileUtil.isRelativePath(""))
          .isInstanceOf(ViolationException.class);
    }
  }

  // -------------------------------------------------------------------------
  // getLockFileVersion
  // -------------------------------------------------------------------------

  @Nested
  @DisplayName("getLockFileVersion")
  class GetLockFileVersion {

    @Test
    @DisplayName("non-existing file is created and timestamp returned")
    void createsFileIfNotExists(@TempDir Path tempDir) throws IOException {
      File lockFile = tempDir.resolve("new.lock").toFile();
      assertThat(lockFile.exists()).isFalse();
      String version = FileUtil.getLockFileVersion(lockFile);
      assertThat(lockFile.exists()).isTrue();
      assertThat(version).matches("\\d{4}-\\d{2}-\\d{2}-\\d{2}-\\d{2}-\\d{2}\\.\\d{3}");
    }

    @Test
    @DisplayName("existing file returns its modification timestamp")
    void existingFileReturnsTimestamp(@TempDir Path tempDir) throws IOException {
      File lockFile = tempDir.resolve("existing.lock").toFile();
      lockFile.createNewFile();
      String version = FileUtil.getLockFileVersion(lockFile);
      assertThat(version).matches("\\d{4}-\\d{2}-\\d{2}-\\d{2}-\\d{2}-\\d{2}\\.\\d{3}");
    }
  }

  // -------------------------------------------------------------------------
  // lock and release
  // -------------------------------------------------------------------------

  @Nested
  @DisplayName("lock and release")
  class LockAndRelease {

    @Test
    @DisplayName("lock without version: returns non-null channel and lock")
    void lockWithoutVersion(@TempDir Path tempDir) throws IOException {
      File lockFile = tempDir.resolve("test.lock").toFile();
      lockFile.createNewFile();
      var result = FileUtil.lock(lockFile, null);
      assertThat(result).isNotNull();
      assertThat(result.getLeft()).isNotNull();
      assertThat(result.getRight()).isNotNull();
      FileUtil.release(result);
    }

    @Test
    @DisplayName("lock with matching version: succeeds")
    void lockWithMatchingVersion(@TempDir Path tempDir) throws IOException {
      File lockFile = tempDir.resolve("test.lock").toFile();
      String version = FileUtil.getLockFileVersion(lockFile);
      var result = FileUtil.lock(lockFile, version);
      assertThat(result).isNotNull();
      FileUtil.release(result);
    }

    @Test
    @DisplayName("lock with wrong version: throws OverlappingFileLockException")
    void lockWithWrongVersionThrows(@TempDir Path tempDir) throws IOException {
      File lockFile = tempDir.resolve("test.lock").toFile();
      lockFile.createNewFile();
      assertThatThrownBy(() -> FileUtil.lock(lockFile, "wrong-version"))
          .isInstanceOf(OverlappingFileLockException.class);
    }

    @Test
    @DisplayName("release: updates lock file timestamp")
    void releaseUpdatesTimestamp(@TempDir Path tempDir) throws IOException {
      File lockFile = tempDir.resolve("test.lock").toFile();
      var result = FileUtil.lock(lockFile, null);
      FileUtil.release(result);
      String version = FileUtil.getLockFileVersion(lockFile);
      assertThat(version).matches("\\d{4}-\\d{2}-\\d{2}-\\d{2}-\\d{2}-\\d{2}\\.\\d{3}");
    }
  }

  // -------------------------------------------------------------------------
  // isLocked
  // -------------------------------------------------------------------------

  @Nested
  @DisplayName("isLocked")
  class IsLocked {

    @Test
    @DisplayName("non-existing file throws FileNotFoundException")
    void nonExistingFileThrows(@TempDir Path tempDir) {
      assertThatThrownBy(
          () -> FileUtil.isLocked(tempDir.resolve("nonexistent.txt").toString()))
          .isInstanceOf(FileNotFoundException.class);
    }

    @Test
    @DisplayName("regular unlocked file returns false")
    void unlockedFileReturnsFalse(@TempDir Path tempDir) throws IOException {
      File file = tempDir.resolve("test.txt").toFile();
      file.createNewFile();
      assertThat(FileUtil.isLocked(file.getAbsolutePath())).isFalse();
    }

    @Test
    @DisplayName("directory returns false")
    void directoryReturnsFalse(@TempDir Path tempDir) throws IOException {
      assertThat(FileUtil.isLocked(tempDir.toFile().getAbsolutePath())).isFalse();
    }

    @Test
    @DisplayName("read-only file returns false")
    void readOnlyFileReturnsFalse(@TempDir Path tempDir) throws IOException {
      File file = tempDir.resolve("readonly.txt").toFile();
      file.createNewFile();
      file.setReadOnly();
      try {
        assertThat(FileUtil.isLocked(file.getAbsolutePath())).isFalse();
      } finally {
        file.setWritable(true);
      }
    }
  }

  // -------------------------------------------------------------------------
  // getPathListFromPathWithWildcard
  // -------------------------------------------------------------------------

  @Nested
  @DisplayName("getPathListFromPathWithWildcard")
  class GetPathListFromPathWithWildcard {

    @Test
    @DisplayName("asterisk wildcard: matches files by extension")
    void asteriskWildcard(@TempDir Path tempDir) throws IOException {
      Path sub = Files.createDirectory(tempDir.resolve("sub"));
      Files.createFile(sub.resolve("file1.txt"));
      Files.createFile(sub.resolve("file2.txt"));
      Files.createFile(sub.resolve("other.log"));

      List<String> result =
          FileUtil.getPathListFromPathWithWildcard(tempDir + "/sub/*.txt");
      assertThat(result).hasSize(2);
      assertThat(result).allMatch(p -> p.endsWith(".txt"));
    }

    @Test
    @DisplayName("question mark wildcard: matches single character")
    void questionMarkWildcard(@TempDir Path tempDir) throws IOException {
      Path sub = Files.createDirectory(tempDir.resolve("sub"));
      Files.createFile(sub.resolve("file1.txt"));
      Files.createFile(sub.resolve("file2.txt"));

      List<String> result =
          FileUtil.getPathListFromPathWithWildcard(tempDir + "/sub/file?.txt");
      assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("wildcard with no match returns empty list")
    void noMatch(@TempDir Path tempDir) throws IOException {
      Files.createDirectory(tempDir.resolve("sub"));
      List<String> result =
          FileUtil.getPathListFromPathWithWildcard(tempDir + "/sub/*.xyz");
      assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("no wildcard: finds existing file directly")
    void noWildcard(@TempDir Path tempDir) throws IOException {
      Path sub = Files.createDirectory(tempDir.resolve("sub"));
      Path file = Files.createFile(sub.resolve("data.txt"));

      List<String> result = FileUtil.getPathListFromPathWithWildcard(file.toString());
      assertThat(result).hasSize(1);
      assertThat(result.get(0)).endsWith("data.txt");
    }

    @Test
    @DisplayName("relative path: converts to absolute before matching")
    void relativePath() {
      List<String> result =
          FileUtil.getPathListFromPathWithWildcard("nonexistent_junit_dir_xyz/*.txt");
      assertThat(result).isEmpty();
    }

  }
}
