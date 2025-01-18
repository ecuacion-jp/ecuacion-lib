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
import org.apache.commons.lang3.tuple.Pair;

/**
 * Provides file lock utility methods.
 * 
 * <p>The method {@code lock} obtains the lock of the lock file, 
 *     and {@code release} releases the lock.</p>
 */
public class FileLockUtil {

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
  public static Pair<FileChannel, FileLock> lock(File lockFile, String version) throws IOException {

    FileLock lockedObject = null;

    FileChannel channel =
        FileChannel.open(lockFile.toPath(), StandardOpenOption.CREATE, StandardOpenOption.WRITE);

    // ファイルロックを試行。例外発生ではなくlockedObjectがnullの場合は、ロック取得に失敗したことになる場合もあるらしい
    lockedObject = channel.tryLock();
    if (lockedObject == null) {
      throw new OverlappingFileLockException();
    }

    // 画面表示時のtimestampとの差異比較
    if (version != null) {
      String fileTimestamp = getLockFileVersion(lockFile);
      if (!version.equals(fileTimestamp)) {
        throw new OverlappingFileLockException();
      }
    }

    return Pair.of(channel, lockedObject);
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
    FileLock lockedObject = channelAndLock.getRight();
    
    try {
      // 特に使用はしないのだが、lockFileを更新する目的でtimestampの文字列を書き込んでおく。
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
  public static String getLockFileVersion(File lockFile) throws IOException {
    // ディレクトリが存在しなければ作成
    lockFile.getParentFile().mkdirs();

    // ファイルが存在しなければ作成
    if (!lockFile.exists()) {
      lockFile.createNewFile();
    }

    return Instant.ofEpochMilli(lockFile.lastModified()).atOffset(ZoneOffset.ofHours(0))
        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss.SSS"));
  }

}
