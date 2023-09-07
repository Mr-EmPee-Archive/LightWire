package mr.empee.lightwire.model;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import mr.empee.lightwire.annotations.Bean;
import mr.empee.lightwire.annotations.Lazy;
import mr.empee.lightwire.exceptions.LightwireException;
import mr.empee.lightwire.utils.ReflectionUtils;
import org.apache.bcel.classfile.AnnotationEntry;
import org.apache.bcel.classfile.ClassParser;
import org.apache.bcel.classfile.JavaClass;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * BeanLoader is responsible for loading beans
 */

@RequiredArgsConstructor
public class BeanLoader {

  private final Map<Class<?>, BeanBuilder> beans = new HashMap<>();

  public BeanLoader(Package scanPackage, Package... exclusions) {
    this(ReflectionUtils.getClasses(scanPackage), exclusions);
  }

  /**
   * Create a bean loader used to load beans from the given collection of classes
   */
  public BeanLoader(Collection<File> classes, Package... exclusions) {
    var target = classes.stream()
        .map(this::parseClass)
        .filter(JavaClass::isClass)
        .filter(c -> Arrays.stream(exclusions).noneMatch(p -> p.getName().equals(c.getPackageName())))
        .filter(c -> hasAnnotation(c, Bean.class))
        .filter(c -> !hasAnnotation(c, Lazy.class))
        .map(c -> loadClass(c.getClassName()))
        .toList();

    for (Class<?> c : target) {
      beans.put(c, new BeanBuilder(c));
    }
  }

  public BeanLoader(Class<?>... classes) {
    for (Class<?> c : classes) {
      beans.put(c, new BeanBuilder(c));
    }
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

  @SneakyThrows
  private Class<?> loadClass(String name) {
    return Class.forName(name);
  }

  private boolean hasAnnotation(JavaClass clazz, Class<? extends Annotation> annotation) {
    for (AnnotationEntry a : clazz.getAnnotationEntries()) {
      var type = a.getAnnotationType()
          .replace('/', '.')
          .replace('\\', '.');

      if (type.contains(annotation.getName())) {
        return true;
      }
    }

    return false;
  }

  @SneakyThrows
  private JavaClass parseClass(File file) {
    return new ClassParser(file.getAbsolutePath()).parse();
  }

}
