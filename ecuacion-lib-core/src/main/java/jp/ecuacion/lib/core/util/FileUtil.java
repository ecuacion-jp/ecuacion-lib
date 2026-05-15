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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.charset.StandardCharsets;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.lang3.tuple.Pair;
import org.jspecify.annotations.Nullable;

/**
 * Provides File-related utility methods.
 */
public class FileUtil {

  /**
   * Prevents other classes from instantiating it.
   */
  private FileUtil() {}

  private static final Map<String, String> SAVABLE_NAME_REPLACEMENTS = Map.of("\\\\", "__yen__",
      "/", "__slash__", ":", "__colon__", "\\*", "__asterisk__", "\\?", "__question__", "\"",
      "__dquotation__", "<", "__lessthan__", ">", "__morethan__", "\\|", "__pipe__");

  /**
   * Changes argument filename into file-savable name.
   */
  public static String getFileSavableName(String origName) {
    ObjectsUtil.requireNonNull(origName);

    String rtn = origName;
    for (Map.Entry<String, String> entry : SAVABLE_NAME_REPLACEMENTS.entrySet()) {
      rtn = rtn.replaceAll(entry.getKey(), entry.getValue());
    }

    return rtn;
  }

  /* Concatenates two paths. */
  private static String concatTwoFilePaths(String path1, String path2) {
    ObjectsUtil.requireNonNull(path1, path2);

    if (path1.endsWith("/")) {
      path1 = path1.substring(0, path1.length() - 1);
    }

    if (path2.startsWith("/")) {
      path2 = path2.substring(1);
    }

    return path1 + "/" + path2;
  }

  /**
   * Concatenates file paths.
   * 
   * @param paths paths
   * @return the concatenated path
   */
  public static String concatFilePaths(String... paths) {
    String concatPath = "";
    for (String path : paths) {
      if (concatPath.isEmpty()) {
        concatPath = path;

      } else {
        concatPath = concatTwoFilePaths(concatPath, path);
      }
    }

    return concatPath;
  }

  /**
   * Cleans a path string.
   * 
   * <p>Paths like {@code /path//to\file} change to the clean format like {@code /path/to/file}.<br>
   * Separator is always {@code /} even if this method is called in Windows OS.</p>
   * 
   * @param path path
   * @return the cleaned path
   */
  public static String cleanPathStrWithSlash(String path) {
    ObjectsUtil.requireNonNull(path);

    // At the same time, unify the delimiter to "/".
    String rtnStr = path.replaceAll("\\\\", "/");
    // When connecting paths into strings,
    // there may be consecutive path separators (/, \), so we need to clean them up.
    rtnStr = rtnStr.replaceAll("//", "/");

    // In ftp-related processing, paths are sometimes expressed with dots,
    // such as "/path/to/dir/.".
    // If "/./" is present or if the string ends with "/.",
    // a process has been added to remove the "/.".
    if (rtnStr.endsWith("/.")) {
      rtnStr = rtnStr.substring(0, rtnStr.length() - 2);
    }

    if (rtnStr.indexOf("/./") >= 0) {
      rtnStr =
          rtnStr.substring(0, rtnStr.indexOf("/./")) + rtnStr.substring(rtnStr.indexOf("/./") + 2);
    }

    // If there is a trailing "/", remove it
    if (rtnStr.endsWith("/")) {
      rtnStr = rtnStr.substring(0, rtnStr.length() - 1);
    }

    return rtnStr;
  }

  /**
   * Changes the path separator to "/".
   * 
   * @param origPath original path
   * @return the separator changed path
   */
  public static String getParentDirPath(String origPath) {
    ObjectsUtil.requireNonNull(origPath);

    // The separator used is now unified to "/".
    String path = cleanPathStrWithSlash(origPath);
    return path.substring(0, path.lastIndexOf("/"));
  }

  /**
   * Obtains filename from a path.
   * 
   * <p>Both "/" and "\" are treated as the separator.</p>
   * 
   * @param path path
   * @return filename
   */
  public static String getFileNameFromFilePath(String path) {
    ObjectsUtil.requireNonNull(path);

    // To avoid any impact whether or not there is a path separator ("/" or "\")
    // at the end of getParentDirPath, remove the leading path separator.
    return path.substring(getParentDirPath(path).length()).replace("\\", "").replace("/", "");
  }

  /**
   * Returns file size in Megabyte.
   * 
   * @param fileSize fileSize
   * @return the file size in Megabyte
   */
  public static String getFileSizeInMb(Long fileSize) {
    ObjectsUtil.requireNonNull(fileSize);

    double d = Double.valueOf(fileSize);

    // Round to 1 decimal place (0.1 MB precision): divide by 100,000, round, divide by 10.
    return Double.valueOf(Math.round(d / 100000.0) / 10.0).toString();
  }

  /**
   * Returns file size in Megabyte.
   * 
   * @param fileSize fileSize
   * @return the file size in Megabyte
   */
  public static String getFileSizeInMbWithUnit(Long fileSize) {
    return getFileSizeInMb(fileSize) + " MB";
  }

  /* ■□■□ File Lock ■□■□ */

  /**
   * Obtains the lock of the designated file.
   * 
   * <p>The argument {@code lockFile} is the file only for locks, not the business use files.</p>
   * 
   * <p>This method also supports the optimistic exclusive control.<br>
   *     The version is obtained from the timestamp string
   *     of the file by {@code getLockFileVersion} method.<br>
   *     When the lock is released, the lock file is updated by writing the timestamp to it.
   *     </p>
   * 
   * @param lockFile lockFile
   * @param version the version, may be {@code null} 
   *     when you don't have to validate the version from the optimistic exclusive control.
   * @return The {@code Pair} tuple which have {@code FileChannel} and {@code FileLock}.
   * @throws IOException IOException
   */
  public static Pair<FileChannel, FileLock> lock(File lockFile, @Nullable String version)
      throws IOException {
    ObjectsUtil.requireNonNull(lockFile);

    FileChannel channel =
        FileChannel.open(lockFile.toPath(), StandardOpenOption.CREATE, StandardOpenOption.WRITE);

    // Attempts to lock a file. If lockedObject is null instead of throwing an exception,
    // it may mean that the lock acquisition failed.
    FileLock lockedObject = channel.tryLock();
    if (lockedObject == null) {
      throw new OverlappingFileLockException();
    }

    // Comparison of timestamps displayed on the screen
    if (version != null) {
      String fileTimestamp = getLockFileVersion(lockFile);
      if (!version.equals(fileTimestamp)) {
        throw new OverlappingFileLockException();
      }
    }

    return Pair.of(channel, lockedObject);
  }

  /**
   * Checks if file is locked.
   * 
   * @param path path, Both absolute and relativve path is acceptable.
   * @return {@code true} when file is locked.
   * @throws IOException IOException
   */
  @SuppressWarnings({"resource", "Finally"})
  public static boolean isLocked(String path) throws IOException {
    ObjectsUtil.requireNonNull(path);

    File file = new File(path);
    // The implementation using FileLock doesn't work well.
    // If you try to get a FileOutputStream while it's locked, an IOException occurs.
    // (I wonder why...)
    // This has been taken into consideration when implementing the system.
    // If the file exists and is not read-only, but fos cannot be obtained,
    // it is considered a lock error.

    // Error if file does not exist
    if (!file.exists()) {
      throw new FileNotFoundException("File not found.");
    }

    // If the file is read-only, it cannot be locked and will always return false.
    if (!file.canWrite()) {
      return false;
    }

    // If it is a folder and not a file, it returns false (I'm not sure if that's okay...)
    if (file.isDirectory()) {
      return false;
    }

    FileOutputStream fos = null;
    try {
      fos = new FileOutputStream(file, true);
    } catch (Exception ignored) {
      // In this case, the lock is considered to be acquired.
      return true;
    }

    // Below is the textbook lock status check logic.
    FileChannel fc = null;
    FileLock lock = null;
    try {
      fc = fos.getChannel();
      lock = fc.tryLock();
      // If lock == null, the lock is already activated.
      if (lock == null) {
        return true;

      } else {
        return false;
      }

    } finally {
      try {
        if (lock != null) {
          lock.release();
        }

        if (fc != null) {
          fc.close();
        }

        if (fos != null) {
          fos.close();
        }

      } catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }

  /**
   * Releases the lock.
   * 
   * <p>The timestamp string is written to the lock file right before the lock is released.</p>
   * 
   * @param channelAndLock the return object of the method {@code lock}.
   * @throws IOException IOException
   */
  public static void release(Pair<FileChannel, FileLock> channelAndLock) throws IOException {
    FileChannel channel = channelAndLock.getLeft();
    final FileLock lockedObject = channelAndLock.getRight();

    // Write timestamp string to update lockFile (which string is not used though).
    @SuppressWarnings("JavaTimeDefaultTimeZone")
    byte[] bytes = LocalDateTime.now().toString().getBytes(StandardCharsets.UTF_8);

    ByteBuffer src = ByteBuffer.allocate(bytes.length);
    src.put(bytes);
    src.position(0);

    channel.write(src);

    lockedObject.release();
  }

  /**
   * Obtains the last update timestamp string from the lock file.
   * 
   * <p>This is used to get the version for optimistic exclusive control.</p>
   * 
   * <p>If the file does not exist, this method creates it.</p>
   *
   * @return The timestamp string in yyyy-mm-dd-hh-mi-ss.SSS format.
   *     To ignore the time offset, the time is always treated as UTC.
   */
  public static String getLockFileVersion(File lockFile) throws IOException {
    // Create a directory if not exists.
    Objects.requireNonNull(lockFile.getParentFile()).mkdirs();

    // Create a file if not exists.
    if (!lockFile.exists()) {
      lockFile.createNewFile();
    }

    return Instant.ofEpochMilli(lockFile.lastModified()).atOffset(ZoneOffset.ofHours(0))
        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss.SSS"));
  }

}
