# FileUtil

`FileUtil` (`jp.ecuacion.lib.core.util.FileUtil`) is a utility class that provides
file path manipulation, file locking, and file size conversion.

---

## File Name and Path Operations

### Converting to a File-Safe Name

Replaces characters that cannot be used in file names (`\`, `/`, `:`, `*`, `?`, `"`, `<`, `>`, `|`)
with corresponding strings.
To prevent path traversal, 2 or more consecutive `.` are also replaced with `__dots__`.
Control characters such as newlines are removed.

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

Empty string elements are ignored.

### Cleaning Paths

Normalizes separators to `/` and removes duplicate separators and trailing `/`.
`/./` and a trailing `/.` are also removed.

```java
FileUtil.cleanPathStrWithSlash("/path//to\\file/");
// → "/path/to/file"
```

### Getting the Parent Directory Path

```java
FileUtil.getParentDirPath("/path/to/file.txt"); // "/path/to"
FileUtil.getParentDirPath("file.txt");          // ""
```

If the path contains no separator, an empty string is returned.

### Getting the File Name from a Path

```java
FileUtil.getFileNameFromFilePath("/path/to/file.txt"); // "file.txt"
FileUtil.getFileNameFromFilePath("file.txt");          // "file.txt"
```

---

## File Size

```java
FileUtil.getFileSizeInMb(10_000_000L);         // "10.0"
FileUtil.getFileSizeInMbWithUnit(10_000_000L); // "10.0 MB"
```

1 MB is calculated as 1,000,000 bytes, and the result is rounded to 1 decimal place (0.1 MB precision).

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
`release(...)` releases the lock and also closes the `FileChannel`.

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
