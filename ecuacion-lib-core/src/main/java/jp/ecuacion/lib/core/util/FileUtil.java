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

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jp.ecuacion.lib.core.annotation.RequireNonnull;
import jp.ecuacion.lib.core.exception.checked.BizLogicAppException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Provides File-related utility methods.
 */
public class FileUtil {

  /**
   * Prevents other classes from instantiating it.
   */
  private FileUtil() {}

  /**
   * Changes argument filename into file-savable name.
   */
  @Nonnull
  public static String getFileSavableName(@RequireNonnull String origName) {
    ObjectsUtil.requireNonNull(origName);

    String rtn = origName;
    if (origName.indexOf("\\") >= 0) {
      rtn = rtn.replaceAll("\\\\", "__yen__");
    }

    if (origName.indexOf("/") >= 0) {
      rtn = rtn.replaceAll("/", "__slash__");
    }

    if (origName.indexOf(":") >= 0) {
      rtn = rtn.replaceAll(":", "__colon__");
    }

    if (origName.indexOf("*") >= 0) {
      rtn = rtn.replaceAll("\\*", "__asterisk__");
    }

    if (origName.indexOf("?") >= 0) {
      rtn = rtn.replaceAll("\\?", "__question__");
    }

    if (origName.indexOf("\"") >= 0) {
      rtn = rtn.replaceAll("\"", "__dquotation__");
    }

    if (origName.indexOf("<") >= 0) {
      rtn = rtn.replaceAll("<", "__lessthan__");
    }

    if (origName.indexOf(">") >= 0) {
      rtn = rtn.replaceAll(">", "__morethan__");
    }

    if (origName.indexOf("|") >= 0) {
      rtn = rtn.replaceAll("\\|", "__pipe__");
    }

    return rtn;
  }

  /*
   * Concatenates two paths.
   * 
   * @param path1 path1
   * 
   * @param path2 path2
   * 
   * @return
   */
  @Nonnull
  private static String concatTwoFilePaths(@RequireNonnull String path1,
      @RequireNonnull String path2) {
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
  @Nonnull
  public static String concatFilePaths(@Nonnull String... paths) {
    String concatPath = "";
    for (String path : paths) {
      if (concatPath.equals("")) {
        concatPath = path;

      } else {
        concatPath = concatTwoFilePaths(concatPath, path);
      }
    }

    return concatPath;
  }

  /*
   * Returns the leftmost separator position of the path in the path string. 
   * Supports both slash (/) and backslash (\).<br>
   * Returns -1 if there is no separator position.
   */
  private static int getFirstPathSeparatorIndex(@RequireNonnull String path) {
    ObjectsUtil.requireNonNull(path);

    int firstSlashIndex = path.indexOf("/");
    int firstBackSlashIndex = path.indexOf("\\");

    if (firstSlashIndex == -1 && firstBackSlashIndex == -1) {
      // Return -1 when separator stirng not found.
      return -1;

    } else if (firstSlashIndex == -1) {
      // the case that only firstSlashIndex is -1
      return firstBackSlashIndex;

    } else if (firstBackSlashIndex == -1) {
      // the case that only firstBackSlashIndex is -1
      return firstSlashIndex;

    } else {
      // Reaching here means both "/" and "\" exists. Return smaller value in this case.
      return (firstSlashIndex < firstBackSlashIndex) ? firstSlashIndex : firstBackSlashIndex;
    }
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
  @Nonnull
  public static String cleanPathStrWithSlash(@RequireNonnull String path) {
    ObjectsUtil.requireNonNull(path);

    String rtnStr = null;
    // At the same time, unify the delimiter to "/".
    rtnStr = path.replaceAll("\\\\", "/");
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
   * Returns true if the argument path contains wildcard strings.
   */
  @Nonnull
  public static boolean containsWildCard(@RequireNonnull String path) {
    return (path.contains("?") || path.contains("*"));
  }

  /**
   * Returns a list of paths which match the path passed by the argument path with wildcards.
   * 
   * <p>"*", "?" are supported, but "**" not supported.<br>
   * The separator of returning Paths is "/"</p>
   */
  @Nonnull
  public static List<String> getPathListFromPathWithWildcard(@RequireNonnull String path)
      throws BizLogicAppException {

    final List<String> fullPathList = new ArrayList<>();
    // Clean the path string.
    path = cleanPathStrWithSlash(path);
    // If the path ends with a path separator, 
    // it will make the subsequent processing complicated, so remove it first.
    if (path.endsWith("/") || path.endsWith("\\")) {
      path = path.substring(0, path.length() - 1);
    }

    // Change relative paths to absolute paths
    if (isRelativePath(path)) {
      path = changeRelPathToFullPath(path);
    }

    // Clean the path string.
    path = cleanPathStrWithSlash(path);
    // Expand the wildcard.
    getPathListFromPathWithWildcardRecursively(path, "", fullPathList);

    return fullPathList;
  }

  /**
   * Returns true if the path is relative.
   * 
   * @param path path
   * @return true if the path is relative
   * @throws BizLogicAppException BizLogicAppException
   */
  @Nonnull
  public static boolean isRelativePath(@RequireNonnull String path) throws BizLogicAppException {
    // If no value is set for path, an error occurs.
    if (path == null || path.equals("")) {
      throw new BizLogicAppException("MSG_ERR_PATH_IS_NULL");
    }

    if (System.getProperty("os.name").toUpperCase().contains("WINDOWS")) {
      // In Windows, if the second character is ":", such as "c:\...", it is a full path.
      if (path.length() >= 2 && path.substring(1, 2).equals(":")) {
        return false;
      }

    } else {
      // For the time being, we have no choice but to assume that it is a Linux system, 
      // but if the first character is "/", then the full path
      if (path.length() >= 1 && path.substring(0, 1).equals("/")) {
        return false;
      }
    }

    return true;
  }

  @Nonnull
  private static String changeRelPathToFullPath(@RequireNonnull String path) {
    String curPath = new File(".").getAbsolutePath();
    String fullPath = concatFilePaths(curPath, path);
    // Replace them so that no strange "./" or ".\" remains.
    fullPath = fullPath.replaceAll("\\.\\\\", "").replaceAll("\\./", "");
    return fullPath;
  }

  private static void getPathListFromPathWithWildcardRecursively(@RequireNonnull String fullPath,
      @RequireNonnull String parentPath, @RequireNonnull List<String> rtnFullPathList)
      throws BizLogicAppException {
    ObjectsUtil.requireNonNull(fullPath, parentPath, rtnFullPathList);

    String myFileOrDirnameWithWildcard = null;
    boolean hasReachedFullPathDirDepth = false;

    if (parentPath.equals("")) {
      String myPathWithWildcard = fullPath.substring(0, getFirstPathSeparatorIndex(fullPath) + 1);
      // For the first path ("C:\" or "/") only, it is impossible for myPathWithWildcard 
      // to contain a wildcard, so if it does, an error will occur.
      if (myPathWithWildcard.contains("*") || myPathWithWildcard.contains("?")) {
        throw new BizLogicAppException("MSG_ERR_1ST_LEVEL_CANNOT_HAVE_WILDCARD", fullPath);
      }

      getPathListFromPathWithWildcardRecursively(fullPath, myPathWithWildcard, rtnFullPathList);

    } else {
      // ### Below is what I want as a whole.
      // ### Get the path one level below parentPath, match it with the list of files 
      // ### and folders under parentPath, and if the target list is found, 
      // ### recursively call this method with the directory one level lower.
      
      // First, get the fullPath (A) by removing the parentPath string.
      // Strictly speaking, because there is a wildcard in the middle, 
      // even if you remove the same number of characters, 
      // it will not match. You need to count the number of delimiters to match.
      int numOfSeparatorOfParentPath = StringUtils.countMatches(parentPath, "/");
      String fullPathMinusParentPath = fullPath
          .substring(StringUtils.ordinalIndexOf(fullPath, "/", numOfSeparatorOfParentPath) + 1);
      // Check if A contains a path separator
      int ind = getFirstPathSeparatorIndex(fullPathMinusParentPath);
      // Depending on whether there is a delimiter or not, 
      // the string to put in myFileOrDirnameWithWildcard is classified into cases.
      if (ind >= 0) {
        // If there is a delimiter, put the string up to the delimiter into myFileOrDirname
        myFileOrDirnameWithWildcard = fullPathMinusParentPath.substring(0, ind + 1);
      } else {
        // If there is no delimiter, it will be set to the last character.
        myFileOrDirnameWithWildcard = fullPathMinusParentPath;
        
        // In the case of directory specification, there may be a pattern 
        // where the delimiter character is the last character, 
        // but this is not possible because it is removed 
        // in the getPathListFromPathWithWildcard method.
        // Therefore, if we reach this else side, 
        // it means that we have descended to the fullPath directory depth, 
        // so we set hasReachedFullPathDirDepth to true.
        hasReachedFullPathDirDepth = true;
      }

      // If myFileOrDirname contains a wildcard, 
      // it must be compared with the list of files and directories under parentPath.
      if (myFileOrDirnameWithWildcard.contains("?") || myFileOrDirnameWithWildcard.contains("*")) {
        // The "." in the file name will be mistaken for a regular expression, 
        // so change it to "\\." first.
        String myFileOrDirnameWithRegEx = myFileOrDirnameWithWildcard.replaceAll("\\.", "\\\\.");
        // Replace wildcards with regular expressions
        myFileOrDirnameWithRegEx =
            myFileOrDirnameWithRegEx.replaceAll("\\?", ".").replaceAll("\\*", ".*");
        Pattern pattern1 = Pattern.compile(parentPath + myFileOrDirnameWithRegEx);

        // Loop through directories and files under parentPath
        String[] arr = new File(parentPath).list();
        if (arr == null) {
          throw new RuntimeException("arr cannot be null.");
        }

        for (String path : arr) {
          String myFullPath = parentPath + path;

          // Since the source of comparison is adopted cleanPathString, 
          // so we will also do this so that we can compare it.
          myFullPath = cleanPathStrWithSlash(myFullPath);

          // If it matches, recursively call
          Matcher matcher = pattern1.matcher(myFullPath);
          if (matcher.matches()) {
            if (hasReachedFullPathDirDepth) {
              rtnFullPathList.add(myFullPath);

            } else {
              getPathListFromPathWithWildcardRecursively(fullPath, myFullPath, rtnFullPathList);
            }
          }
        }
      } else {
        String myFullPath = parentPath + myFileOrDirnameWithWildcard;
        if (new File(myFullPath).exists()) {
          if (hasReachedFullPathDirDepth) {
            rtnFullPathList.add(myFullPath);
          } else {
            getPathListFromPathWithWildcardRecursively(fullPath, myFullPath, rtnFullPathList);
          }
        }
      }
    }
  }

  /**
   * Changes the path separator to "/".
   * 
   * @param origPath original path
   * @return the separator changed path
   */
  @Nonnull
  public static String getParentDirPath(@RequireNonnull String origPath) {
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
  @Nonnull
  public static String getFileNameFromFilePath(@RequireNonnull String path) {
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
  @Nonnull
  public static String getFileSizeInMb(@RequireNonnull Long fileSize) {
    ObjectsUtil.requireNonNull(fileSize);

    double d = Double.valueOf(fileSize);
    // Since we want to round to the second decimal place, 
    // we first divide by the digit that is one place less, then round it up and divide by 10.
    System.out.println(Math.round(d / 100000.0));
    return Double.valueOf(Math.round(d / 100000.0) / 10.0).toString();
  }

  /**
   * Returns file size in Megabyte.
   * 
   * @param fileSize fileSize
   * @return the file size in Megabyte
   */
  @Nonnull
  public static String getFileSizeInMbWithUnit(@RequireNonnull Long fileSize) {
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
  @Nonnull
  public static Pair<FileChannel, FileLock> lock(@RequireNonnull File lockFile,
      @Nullable String version) throws IOException {
    ObjectsUtil.requireNonNull(lockFile);
    FileLock lockedObject = null;

    FileChannel channel =
        FileChannel.open(lockFile.toPath(), StandardOpenOption.CREATE, StandardOpenOption.WRITE);

    // Attempts to lock a file. If lockedObject is null instead of throwing an exception, 
    // it may mean that the lock acquisition failed.
    lockedObject = channel.tryLock();
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
  @SuppressWarnings("resource")
  public static boolean isLocked(@RequireNonnull String path) throws IOException {
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
    } catch (Exception e) {
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
        e.printStackTrace();
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
  public static void release(@RequireNonnull Pair<FileChannel, FileLock> channelAndLock)
      throws IOException {
    FileChannel channel = channelAndLock.getLeft();
    FileLock lockedObject = channelAndLock.getRight();

    try {
      // Write timestamp string to update lockFile (which string is not used though).
      byte[] bytes = LocalDateTime.now().toString().getBytes();

      ByteBuffer src = ByteBuffer.allocate(bytes.length);
      src.put(bytes);
      src.position(0);

      channel.write(src);

      lockedObject.release();

    } catch (OverlappingFileLockException ex) {
      throw new OverlappingFileLockException();
    }
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
  @Nonnull
  public static String getLockFileVersion(@RequireNonnull File lockFile) throws IOException {
    // Create a directory if not exists.
    lockFile.getParentFile().mkdirs();

    // Create a file if not exists.
    if (!lockFile.exists()) {
      lockFile.createNewFile();
    }

    return Instant.ofEpochMilli(lockFile.lastModified()).atOffset(ZoneOffset.ofHours(0))
        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss.SSS"));
  }

}
