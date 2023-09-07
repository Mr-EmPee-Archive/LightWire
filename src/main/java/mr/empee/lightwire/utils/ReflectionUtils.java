package mr.empee.lightwire.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;

import java.io.File;
import java.util.Collection;
import java.util.HashSet;

/**
 * Utility class for reflection related operations
 */

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ReflectionUtils {

  /**
   * Returns all the classes in the given package
   */
  @SneakyThrows
  public static Collection<File> getClasses(Package scanPackage) {
    var classLoader = Thread.currentThread().getContextClassLoader();
    var packagePath = scanPackage.getName().replace('.', '/');
    var file = new File(classLoader.getResource(packagePath).toURI());
    if (!file.exists() || !file.isDirectory()) {
      throw new IllegalArgumentException("The given package doesn't exist");
    }

    var classes = new HashSet<File>();
    FileUtils.walkFileTree(file, f -> {
      if (!f.getName().endsWith(".class")) {
        return;
      }

      classes.add(f);
    });

    return classes;
  }



}
