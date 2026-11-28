# FileUtil

`FileUtil`（`jp.ecuacion.lib.core.util.FileUtil`）はファイルパス操作・ファイルロック・ファイルサイズ変換などを提供するユーティリティクラスです。

---

## ファイル名・パス操作

### ファイル保存可能な名前への変換

ファイル名に使用できない文字（`\`, `/`, `:`, `*`, `?`, `"`, `<`, `>`, `|`）を対応する文字列に置換します。
パストラバーサルを防ぐため、2つ以上連続した `.` も `__dots__` に置換します。
また、改行などの制御文字は除去します。

```java
FileUtil.getFileSavableName("report: 2024/01/15");
// → "report__colon__ 2024__slash__01__slash__15"
```

### パスの連結

```java
FileUtil.concatFilePaths("/base/dir", "sub/dir", "file.txt");
// → "/base/dir/sub/dir/file.txt"

// 区切り文字の重複を自動的に吸収
FileUtil.concatFilePaths("/base/dir/", "/sub/dir");
// → "/base/dir/sub/dir"
```

空文字の要素は無視します。

### パスのクリーニング

区切り文字を `/` に統一し、重複区切りや末尾の `/` を除去します。
`/./` や末尾の `/.` も除去します。

```java
FileUtil.cleanPathStrWithSlash("/path//to\\file/");
// → "/path/to/file"
```

### 親ディレクトリパスの取得

```java
FileUtil.getParentDirPath("/path/to/file.txt"); // "/path/to"
FileUtil.getParentDirPath("file.txt");          // ""
```

区切り文字を含まないパスの場合は空文字を返します。

### パスからファイル名を取得

```java
FileUtil.getFileNameFromFilePath("/path/to/file.txt"); // "file.txt"
FileUtil.getFileNameFromFilePath("file.txt");          // "file.txt"
```

---

## ファイルサイズ

```java
FileUtil.getFileSizeInMb(10_000_000L);         // "10.0"
FileUtil.getFileSizeInMbWithUnit(10_000_000L); // "10.0 MB"
```

1 MB を 1,000,000 バイトとして計算し、小数点以下1桁（0.1 MB 精度）に丸めて返します。

---

## ファイルロック

複数プロセスからの同時アクセスを排他制御します。
ロック専用ファイル（ビジネスロジック用ファイルとは別）を使用します。

### ロックの取得・確認・解放

```java
File lockFile = new File("/tmp/myapp.lock");

// ロックを取得（versionはnull可。楽観的排他制御を行う場合のみ使用）
Pair<FileChannel, FileLock> channelAndLock = FileUtil.lock(lockFile, null);

try {
    // 排他処理

} finally {
    // ロックを解放（解放前にロックファイルにタイムスタンプを書き込む）
    FileUtil.release(channelAndLock);
}

// ロック中かどうかの確認
boolean locked = FileUtil.isLocked("/tmp/myapp.lock");
```

ロックが取得済みの場合、`lock(...)` は `OverlappingFileLockException` をスローします。
`release(...)` はロックを解放し、`FileChannel` もクローズします。

### 楽観的排他制御

`getLockFileVersion` でロックファイルのバージョン（最終更新タイムスタンプ）を取得し、
`lock(lockFile, version)` に渡すことで楽観的排他制御ができます。

```java
// 画面表示時にバージョンを取得
String version = FileUtil.getLockFileVersion(lockFile);

// 更新処理時にバージョンを検証してからロック
// バージョンが変わっていれば OverlappingFileLockException をスロー
Pair<FileChannel, FileLock> channelAndLock = FileUtil.lock(lockFile, version);
```
