package mr.empee.lightwire.model;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import lombok.RequiredArgsConstructor;
import mr.empee.lightwire.annotations.Singleton;
import mr.empee.lightwire.exceptions.LightwireException;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * BeanLoader is responsible for loading beans
 */

@RequiredArgsConstructor
public class BeanLoader {

  private final BeanContext context;
  private final Map<Class<?>, BeanBuilder<?>> beans = new HashMap<>();

  public BeanLoader(Package scanPackage, BeanContext context) {
    this(context, findBeanClasses(scanPackage));
  }

  private static List<Class<?>> findBeanClasses(Package scanPackage) {
    var beanClasses = new ArrayList<Class<?>>();
    ClassGraph classGraph = new ClassGraph()
        .enableAnnotationInfo()
        .acceptPackages(scanPackage.getName());

    try (var scanResult = classGraph.scan()) {
      for (ClassInfo routeClassInfo : scanResult.getClassesWithAnnotation(Singleton.class)) {
        var values = routeClassInfo.getAnnotationInfo(Singleton.class).getParameterValues();
        boolean isLazy = (boolean) values.get(0).getValue();
        if (!isLazy) {
          beanClasses.add(routeClassInfo.loadClass());
        }
      }
    }

    return beanClasses;
  }

  public BeanLoader(BeanContext context, Collection<Class<?>> classes) {
    this.context = context;

    for (Class<?> c : classes) {
      beans.put(c, new BeanBuilder<>(c));
    }
  }

  private void checkForCircularDependency() {
    for (BeanBuilder<?> builder : beans.values()) {
      checkForCircularDependency(null, builder);
    }
  }

  private void checkForCircularDependency(BeanBuilder<?> start, BeanBuilder<?> bean) {
    if (start == null) {
      start = bean;
    } else if (start.getBeanClass() == bean.getBeanClass()) {
      throw new LightwireException("Circular dependency detected for bean " + start.getBeanClass());
    }

    for (Class<?> dependency : bean.getDependencies()) {
      var builder = beans.get(dependency);
      if (builder == null) {
        if (context.isLoaded(dependency)) {
          continue;
        }

        //Null if it is a lazy load dep
        builder = new BeanBuilder<>(dependency);
      }

      checkForCircularDependency(start, builder);
    }
  }

  /**
   * Loads all targeted beans
   */
  public void load() {
    checkForCircularDependency();

    var sortedBeans = beans.values().stream()
        .sorted((b1, b2) -> getBeanPriority(b1).compareTo(getBeanPriority(b2)))
        .collect(Collectors.toList());

    for (BeanBuilder<?> builder : sortedBeans) {
      try {
        if (context.isLoaded(builder.getBeanClass())) {
          continue;
        }

        context.addProvider(builder.build(context));
      } catch (InvocationTargetException e) {
        throw new LightwireException("Failed to build bean " + builder.getBeanClass().getName(), e.getCause());
      }
    }
  }

  private Integer getBeanPriority(BeanBuilder<?> bean) {
    var annotation = bean.getBeanClass().getAnnotation(Singleton.class);
    if (annotation == null) {
      return 0;
    }

    return annotation.priority().ordinal();
  }
}
