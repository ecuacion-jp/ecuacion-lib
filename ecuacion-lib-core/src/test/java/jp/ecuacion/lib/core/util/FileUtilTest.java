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
import java.nio.file.Path;
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

    @Test
    @DisplayName("consecutive dots are replaced")
    void dots() {
      assertThat(FileUtil.getFileSavableName("a..b")).isEqualTo("a__dots__b");
    }

    @Test
    @DisplayName("control characters are removed")
    void controlCharacters() {
      assertThat(FileUtil.getFileSavableName("a\u0000b\nc")).isEqualTo("abc");
    }

    @Test
    @DisplayName("dots joined by removing control characters are also replaced")
    void dotsJoinedByRemovingControlCharacters() {
      assertThat(FileUtil.getFileSavableName(".\u0000.")).isEqualTo("__dots__");
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

    @Test
    @DisplayName("empty strings are ignored")
    void emptyStringsIgnored() {
      assertThat(FileUtil.concatFilePaths("", "a", "", "b", "")).isEqualTo("a/b");
      assertThat(FileUtil.concatFilePaths("", "")).isEqualTo("");
    }

    @Test
    @DisplayName("null element throws NullPointerException")
    @SuppressWarnings({"null", "NullAway"})
    void nullElement() {
      assertThatThrownBy(() -> FileUtil.concatFilePaths((String) null))
          .isInstanceOf(NullPointerException.class);
      assertThatThrownBy(() -> FileUtil.concatFilePaths("a", null))
          .isInstanceOf(NullPointerException.class);
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

    @Test
    @DisplayName("all /./ are removed")
    void multipleDots() {
      assertThat(FileUtil.cleanPathStrWithSlash("/a/./b/./c")).isEqualTo("/a/b/c");
      assertThat(FileUtil.cleanPathStrWithSlash("/a/././b")).isEqualTo("/a/b");
      assertThat(FileUtil.cleanPathStrWithSlash("a/./.")).isEqualTo("a");
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

    @Test
    @DisplayName("path without separator returns empty string")
    void noSeparator() {
      assertThat(FileUtil.getParentDirPath("c.txt")).isEqualTo("");
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

    @Test
    @DisplayName("path without separator returns the path itself")
    void noSeparator() {
      assertThat(FileUtil.getFileNameFromFilePath("c.txt")).isEqualTo("c.txt");
    }

    @Test
    @DisplayName("path to be cleaned returns the correct filename")
    void pathToBeCleaned() {
      assertThat(FileUtil.getFileNameFromFilePath("/a//b/c.txt")).isEqualTo("c.txt");
      assertThat(FileUtil.getFileNameFromFilePath("/a/./c.txt")).isEqualTo("c.txt");
      assertThat(FileUtil.getFileNameFromFilePath("/a/b/")).isEqualTo("b");
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

    @Test
    @DisplayName("relative path without a directory is accepted")
    void relativePathWithoutDirectory() throws IOException {
      File lockFile = new File("FileUtilTest-" + System.nanoTime() + ".lock");
      try {
        assertThat(lockFile.getParentFile()).isNull();
        String version = FileUtil.getLockFileVersion(lockFile);
        assertThat(lockFile.exists()).isTrue();
        assertThat(version).matches("\\d{4}-\\d{2}-\\d{2}-\\d{2}-\\d{2}-\\d{2}\\.\\d{3}");
      } finally {
        lockFile.delete();
      }
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

    @Test
    @DisplayName("lock with wrong version: the lock is not left, so it can be locked again")
    void lockAgainAfterWrongVersion(@TempDir Path tempDir) throws IOException {
      File lockFile = tempDir.resolve("test.lock").toFile();
      lockFile.createNewFile();
      assertThatThrownBy(() -> FileUtil.lock(lockFile, "wrong-version"))
          .isInstanceOf(OverlappingFileLockException.class);
      var result = FileUtil.lock(lockFile, null);
      FileUtil.release(result);
    }

    @Test
    @DisplayName("lock twice in the same JVM: throws OverlappingFileLockException")
    void lockTwice(@TempDir Path tempDir) throws IOException {
      File lockFile = tempDir.resolve("test.lock").toFile();
      var result = FileUtil.lock(lockFile, null);
      try {
        assertThatThrownBy(() -> FileUtil.lock(lockFile, null))
            .isInstanceOf(OverlappingFileLockException.class);
      } finally {
        FileUtil.release(result);
      }
    }

    @Test
    @DisplayName("release: closes the channel, so it can be locked again")
    void releaseClosesChannel(@TempDir Path tempDir) throws IOException {
      File lockFile = tempDir.resolve("test.lock").toFile();
      var result = FileUtil.lock(lockFile, null);
      FileUtil.release(result);
      assertThat(result.getLeft().isOpen()).isFalse();
      FileUtil.release(FileUtil.lock(lockFile, null));
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
    @DisplayName("file locked by lock() in the same JVM returns true")
    void lockedInSameJvmReturnsTrue(@TempDir Path tempDir) throws IOException {
      File lockFile = tempDir.resolve("test.lock").toFile();
      var result = FileUtil.lock(lockFile, null);
      try {
        assertThat(FileUtil.isLocked(lockFile.getAbsolutePath())).isTrue();
      } finally {
        FileUtil.release(result);
      }
      assertThat(FileUtil.isLocked(lockFile.getAbsolutePath())).isFalse();
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

}
