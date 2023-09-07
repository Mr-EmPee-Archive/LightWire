package mr.empee.lightwire.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.io.File;
import java.util.function.Consumer;

/**
 * Utility class for file operations
 */

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FileUtils {

  /**
   * Walks the file tree starting from the given
   * path and calls the consumer for each file
   */
  public static void walkFileTree(File start, Consumer<File> consumer) {
    if (start.isDirectory()) {
      var files = start.listFiles();
      if (files == null || files.length == 0) {
        return;
      }

      for (File file : files) {
        walkFileTree(file, consumer);
      }
    } else {
      consumer.accept(start);
    }
  }

}
