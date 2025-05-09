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
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jp.ecuacion.lib.core.annotation.RequireNonnull;
import jp.ecuacion.lib.core.exception.checked.BizLogicAppException;
import org.apache.commons.lang3.StringUtils;

/**
 * Provides File-related utility methods.
 */
public class FileUtil {

  /**
  * Prevents to create an instance.
  */
  private FileUtil() {}

  /**
   * Changes argument filename into file-savable name.
   */
  @Nonnull
  public static String getFileSavableName(@RequireNonnull String origName) {
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
   * パス文字列のうち、パスの一番左側の区切り位置を返す。スラッシュ(/)にもバックスラッシュ(\)にも対応.<br> 区切り位置が存在しない場合は-1を返す
   */
  private static int getFirstPathSeparatorIndex(@RequireNonnull String path) {
    int firstSlashIndex = path.indexOf("/");
    int firstBackSlashIndex = path.indexOf("\\");

    if (firstSlashIndex == -1 && firstBackSlashIndex == -1) {
      // 区切り位置が存在しない場合は-1を返す
      return -1;

    } else if (firstSlashIndex == -1) {
      // firstSlashIndexのみが-1の場合
      return firstBackSlashIndex;

    } else if (firstBackSlashIndex == -1) {
      // firstBackSlashIndexのみが-1の場合
      return firstSlashIndex;

    } else {
      // 上記以外のパターンは、「/」も「\」も存在する状態。この場合は、値が小さい方を返す
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
    String rtnStr = null;
    // 併せて、区切り文字を「/」に統一する
    rtnStr = path.replaceAll("\\\\", "/");
    // パスを文字列接続していく中で、パスの区切り文字（/、\）が連続してしまうことがあるのでそれをきれいにする。
    rtnStr = rtnStr.replaceAll("//", "/");

    // ftp系の処理で、パスが「/path/to/dir/.」のように、ドットで表現されることがある。「/./」がある場合か、文字列の最後が「/.」の場合は「/.」を取り除く処理を追加
    if (rtnStr.endsWith("/.")) {
      rtnStr = rtnStr.substring(0, rtnStr.length() - 2);
    }

    if (rtnStr.indexOf("/./") >= 0) {
      rtnStr =
          rtnStr.substring(0, rtnStr.indexOf("/./")) + rtnStr.substring(rtnStr.indexOf("/./") + 2);
    }

    // 最後に"/"がつく場合は取り除く
    if (rtnStr.endsWith("/")) {
      rtnStr = rtnStr.substring(0, rtnStr.length() - 1);
    }

    return rtnStr;
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
    File file = new File(path);
    // FileLockによる実装がうまくいかない。ロックがかかっている状態でFileOutputStreamを取得しようとすると、
    // その時点でIOExceptionが発生してしまう。（なんでだろう・・）
    // それも考慮して実装。ファイルが存在する・かつ読み取り専用ファイルでないのにfosを取得できない場合はロックエラーとみなす

    // ファイルが存在しない場合はエラー
    if (!file.exists()) {
      throw new FileNotFoundException("ファイルが存在しません。");
    }

    // ファイルが読み取り専用の場合はロックがかけられないので常にfalseを返す
    if (!file.canWrite()) {
      return false;
    }

    // ファイルでなくフォルダの場合はfalseを返す（それでいいのかどうかよくわからないが・・・）
    if (file.isDirectory()) {
      return false;
    }

    FileOutputStream fos = null;
    try {
      fos = new FileOutputStream(file, true);
    } catch (Exception e) {
      // この場合はロックが取得されているとみなす
      return true;
    }

    // 以下、教科書的なロック状態チェックロジック
    FileChannel fc = null;
    FileLock lock = null;
    try {
      fc = fos.getChannel();
      lock = fc.tryLock();
      // lock == nullの場合は、既にロックがかかっている状態
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
   * Returns true if the argument path contains wildcard strings.
   */
  public static boolean containsWildCard(String path) {
    return (path.contains("?") || path.contains("*"));
  }

  /**
   * Returns a list of paths which match the path passed by the argument path with wildcards.
   * 
   * <p>"*", "?" are supported, but "**" not supported.<br>
   * The separator of returning Paths is "/"</p>
   */
  public static List<String> getPathListFromPathWithWildcard(String path)
      throws BizLogicAppException {

    final List<String> fullPathList = new ArrayList<>();
    // パス文字列をきれいにしておく
    path = cleanPathStrWithSlash(path);
    // pathの最後がパス区切り文字だとこの後の処理で煩雑になるので、先に取り除いておく
    if (path.endsWith("/") || path.endsWith("\\")) {
      path = path.substring(0, path.length() - 1);
    }

    // 相対パスで渡された場合に絶対パスに変更
    if (isRelativePath(path)) {
      path = changeRelPathToFullPath(path);
    }

    // パスをきれいにしておく
    path = cleanPathStrWithSlash(path);
    // ワイルドカードを展開
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
  public static boolean isRelativePath(String path) throws BizLogicAppException {
    // pathに値が設定されていない場合はエラー
    if (path == null || path.equals("")) {
      throw new BizLogicAppException("MSG_ERR_PATH_IS_NULL");
    }

    if (System.getProperty("os.name").toUpperCase().contains("WINDOWS")) {
      // windowsの場合は、「c:\...」のように、2文字目に「:」が来る場合はフルパス
      if (path.length() >= 2 && path.substring(1, 2).equals(":")) {
        return false;
      }

    } else {
      // とりあえずlinux系を想定するしかないのだが、一文字目が「/」であればフルパス
      if (path.length() >= 1 && path.substring(0, 1).equals("/")) {
        return false;
      }
    }

    return true;
  }

  private static String changeRelPathToFullPath(String path) {
    String curPath = new File(".").getAbsolutePath();
    String fullPath = concatFilePaths(curPath, path);
    // 変に「./」、「.\」が残らないように置き換えしておく
    fullPath = fullPath.replaceAll("\\.\\\\", "").replaceAll("\\./", "");
    return fullPath;
  }

  private static void getPathListFromPathWithWildcardRecursively(String fullPath, String parentPath,
      List<String> rtnFullPathList) throws BizLogicAppException {

    String myFileOrDirnameWithWildcard = null;
    boolean hasReachedFullPathDirDepth = false;

    if (parentPath.equals("")) {
      String myPathWithWildcard = fullPath.substring(0, getFirstPathSeparatorIndex(fullPath) + 1);
      // 最初のパス（「C:\」ないし「/」）に限っては、myPathWithWildcardの中にワイルドカードが含まれていることはありえないのでもしあればエラーとする
      if (myPathWithWildcard.contains("*") || myPathWithWildcard.contains("?")) {
        throw new BizLogicAppException("MSG_ERR_1ST_LEVEL_CANNOT_HAVE_WILDCARD", fullPath);
      }

      getPathListFromPathWithWildcardRecursively(fullPath, myPathWithWildcard, rtnFullPathList);

    } else {
      // ### parentPathの一つ下のパスを取得し、それとparentPath配下のファイル・フォルダ一覧を取得したものとmatchをかけて、
      // ### 対象のリストがあれば、一段ディレクトリが下がった状態で本メソッドを再帰呼び出し、というのがここでやりたい全体感。

      // まず、fullPathからparentPathの文字列を取り除いたもの（A)を取得
      // 厳密には、途中にワイルドカードが含まれるので、同じ文字数分を除去しても合わなくなってしまう。区切り文字の数を数えて合わせる必要あり
      int numOfSeparatorOfParentPath = StringUtils.countMatches(parentPath, "/");
      String fullPathMinusParentPath = fullPath
          .substring(StringUtils.ordinalIndexOf(fullPath, "/", numOfSeparatorOfParentPath) + 1);
      // Aにパス区切り文字が入っているかをチェック
      int ind = getFirstPathSeparatorIndex(fullPathMinusParentPath);
      // 区切り文字があるかないかで、myFileOrDirnameWithWildcardに入れる文字列を場合分け。
      if (ind >= 0) {
        // 区切り文字があれば、myFileOrDirnameに区切り文字までの文字列を入れる
        myFileOrDirnameWithWildcard = fullPathMinusParentPath.substring(0, ind + 1);
      } else {
        // 区切り文字がない場合は最後の文字までを設定
        myFileOrDirnameWithWildcard = fullPathMinusParentPath;
        // ディレクトリ指定の場合で、区切り文字が最後の文字に来るパターンもありうるが、それはgetPathListFromPathWithWildcardメソッドの中で
        // 取り除いているのでありえない。
        // よって、このelse側に来たということは、fullPath分のディレクトリ階層の深さまで下がってきたことになるため、
        // hasReachedFullPathDirDepthをtrueにする
        hasReachedFullPathDirDepth = true;
      }

      // myFileOrDirnameにワイルドカードを含んでいる場合は、parentPath配下のファイル・ディレクトリの一覧と比較をする必要あり
      if (myFileOrDirnameWithWildcard.contains("?") || myFileOrDirnameWithWildcard.contains("*")) {
        // ファイル名に含まれる「.」が正規表現のものと勘違いされてしまうので、先に「\\.」に変更しておく
        String myFileOrDirnameWithRegEx = myFileOrDirnameWithWildcard.replaceAll("\\.", "\\\\.");
        // ワイルドカードから正規表現に置き換え
        myFileOrDirnameWithRegEx =
            myFileOrDirnameWithRegEx.replaceAll("\\?", ".").replaceAll("\\*", ".*");
        Pattern pattern1 = Pattern.compile(parentPath + myFileOrDirnameWithRegEx);

        // parentPath配下のディレクトリ、ファイルでループ
        String[] arr = new File(parentPath).list();
        if (arr == null) {
          throw new RuntimeException("arr cannot be null.");
        }

        for (String path : arr) {
          String myFullPath = parentPath + path;

          // 比較元がcleanPathStringをしてあるので、比較できるようにこっちもしておく
          myFullPath = cleanPathStrWithSlash(myFullPath);

          // 一致する場合は再帰呼び出し
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
  public static String getParentDirPath(String origPath) {
    // 使用されている区切りを"/"に統一
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
    // getParentDirPathの最後にパス区切り文字（"/"または"\"）があってもなくても影響が出ないように、頭のパス区切り文字を消しておく
    return path.substring(getParentDirPath(path).length()).replace("\\", "").replace("/", "");
  }

  /**
   * Returns file size in Megabyte.
   * 
   * @param fileSize fileSize
   * @return the file size in Megabyte
   */
  public static String getFileSizeInMb(Long fileSize) {
    double d = Double.valueOf(fileSize);
    // 小数第二位で四捨五入したいので、一旦一桁少ない桁で割り算し、四捨五入後10で割る
    System.out.println(Math.round(d / 100000.0));
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
}
