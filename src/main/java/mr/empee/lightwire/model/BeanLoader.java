package mr.empee.lightwire.model;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import mr.empee.lightwire.annotations.Factory;
import mr.empee.lightwire.annotations.Lazy;
import mr.empee.lightwire.annotations.Singleton;
import mr.empee.lightwire.exceptions.LightwireException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * BeanLoader is responsible for loading beans
 */

@RequiredArgsConstructor
public class BeanLoader {

  private final Map<Class<?>, BeanBuilder> beans = new HashMap<>();

  public BeanLoader(Package scanPackage) {
    for (var c : findAllClassesUsingClassLoader(scanPackage.getName())) {
      var isBean = c.isAnnotationPresent(Singleton.class) || c.isAnnotationPresent(Factory.class);
      if (c.isAnnotationPresent(Lazy.class) || !isBean) {
        continue;
      }

      beans.put(c, new BeanBuilder(c));
    }
  }

  public BeanLoader(Class<?>... classes) {
    for (Class<?> c : classes) {
      beans.put(c, new BeanBuilder(c));
    }
  }

  @SneakyThrows
  private Set<Class<?>> findAllClassesUsingClassLoader(String packageName) {
    var path = packageName.replaceAll("[.]", "/");

    try (
        var stream = ClassLoader.getSystemClassLoader().getResourceAsStream(path)
    ) {
      BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
      return reader.lines()
          .filter(line -> line.endsWith(".class"))
          .map(line -> getClass(line, packageName))
          .collect(Collectors.toSet());
    }
  }

  @SneakyThrows
  private Class<?> getClass(String className, String packageName) {
    return Class.forName(packageName + "." + className.substring(0, className.lastIndexOf('.')));
  }

  @SneakyThrows
  private Class<?> loadClass(String clazz) {
    return Class.forName(clazz);
  }

  private void checkForCircularDependency() {
    for (BeanBuilder builder : beans.values()) {
      checkForCircularDependency(null, builder, new HashSet<>());
    }
  }

  private void checkForCircularDependency(BeanBuilder parent, BeanBuilder bean, Set<Class<?>> visitedBeans) {
    boolean isNewDependency = visitedBeans.add(bean.getBeanClass());
    if (!isNewDependency) {
      throw new LightwireException("""
          Circular dependency detected!
            Bean already needed: %s
            Bean that needs it: %s
          """.formatted(bean.getBeanClass().getName(), parent.getBeanClass().getName())
      );
    }

    for (Class<?> dependency : bean.getDependencies()) {
      var builder = beans.get(dependency);
      if (builder == null) {
        //Null if it is a lazy load dep
        builder = new BeanBuilder(dependency);
      }

      checkForCircularDependency(bean, builder, visitedBeans);
    }
  }

  /**
   * Loads all targeted beans
   */
  public void load(BeanContext beanContext) {
    checkForCircularDependency();
    for (BeanBuilder builder : beans.values()) {
      try {
        beanContext.addProvider(builder.build(beanContext));
      } catch (InvocationTargetException e) {
        throw new LightwireException("Failed to build bean " + builder.getBeanClass().getName(), e.getCause());
      }
    }
  }
}
