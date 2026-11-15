# FileUtil

`FileUtil` (`jp.ecuacion.lib.core.util.FileUtil`) is a utility class that provides
file path manipulation, file locking, and file size conversion.

---

## File Name and Path Operations

### Converting to a File-Safe Name

Replaces characters that cannot be used in file names (`\`, `/`, `:`, `*`, `?`, `"`, `<`, `>`, `|`)
with corresponding strings.

```java
FileUtil.getFileSavableName("report: 2024/01/15");
// → "report__colon__ 2024__slash__01__slash__15"
```

### Concatenating Paths

```java
FileUtil.concatFilePaths("/base/dir", "sub/dir", "file.txt");
// → "/base/dir/sub/dir/file.txt"

// Automatically absorbs duplicate separators
FileUtil.concatFilePaths("/base/dir/", "/sub/dir");
// → "/base/dir/sub/dir"
```

### Cleaning Paths

Normalizes separators to `/` and removes duplicate separators and trailing `/`.

```java
FileUtil.cleanPathStrWithSlash("/path//to\\file/");
// → "/path/to/file"
```

### Getting the Parent Directory Path

```java
FileUtil.getParentDirPath("/path/to/file.txt"); // "/path/to"
```

### Getting the File Name from a Path

```java
FileUtil.getFileNameFromFilePath("/path/to/file.txt"); // "file.txt"
```

---

## File Size

```java
FileUtil.getFileSizeInMb(10_485_760L);      // "10.0"
FileUtil.getFileSizeInMbWithUnit(10_485_760L); // "10.0 MB"
```

Returns rounded to 1 decimal place (0.1 MB precision).

---

## Relative Path Check and Wildcard Expansion

```java
FileUtil.isRelativePath("relative/path"); // true
FileUtil.isRelativePath("/absolute/path"); // false

FileUtil.containsWildCard("/path/to/*.txt"); // true

// Returns a list of paths matching the wildcard ("**" not supported)
List<String> paths = FileUtil.getPathListFromPathWithWildcard("/path/to/*.txt");
```

---

## File Locking

Provides exclusive control for simultaneous access from multiple processes.
Uses a dedicated lock file (separate from the business logic file).

### Acquiring, Checking, and Releasing a Lock

```java
File lockFile = new File("/tmp/myapp.lock");

// Acquire a lock (version can be null; only used for optimistic locking)
Pair<FileChannel, FileLock> channelAndLock = FileUtil.lock(lockFile, null);

try {
    // Exclusive processing

} finally {
    // Release the lock (writes a timestamp to the lock file before releasing)
    FileUtil.release(channelAndLock);
}

// Check if locked
boolean locked = FileUtil.isLocked("/tmp/myapp.lock");
```

If the lock is already acquired, `lock(...)` throws an `OverlappingFileLockException`.

### Optimistic Locking

You can implement optimistic locking by getting the lock file version (last update timestamp)
with `getLockFileVersion` and passing it to `lock(lockFile, version)`.

```java
// Get the version when displaying the screen
String version = FileUtil.getLockFileVersion(lockFile);

// Verify the version and lock during the update process
// If the version has changed, throws OverlappingFileLockException
Pair<FileChannel, FileLock> channelAndLock = FileUtil.lock(lockFile, version);
```
