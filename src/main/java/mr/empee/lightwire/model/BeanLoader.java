package mr.empee.lightwire.model;

import io.github.classgraph.AnnotationInfo;
import io.github.classgraph.AnnotationParameterValue;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import lombok.RequiredArgsConstructor;
import mr.empee.lightwire.annotations.Factory;
import mr.empee.lightwire.annotations.Singleton;
import mr.empee.lightwire.exceptions.LightwireException;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * BeanLoader is responsible for loading beans
 */

@RequiredArgsConstructor
public class BeanLoader {

  private final Map<Class<?>, BeanBuilder<?>> beans = new HashMap<>();

  public BeanLoader(Package scanPackage) {
    for (var c : findBeanClasses(scanPackage)) {
      beans.put(c, new BeanBuilder<>(c));
    }
  }

  private List<Class<?>> findBeanClasses(Package scanPackage) {
    var beanClasses = new ArrayList<Class<?>>();

    ClassGraph classGraph = new ClassGraph()
        .enableAnnotationInfo()
        .acceptPackages(scanPackage.getName());

    try (var scanResult = classGraph.scan()) {
      var foundClasses = new ArrayList<ClassInfo>();
      foundClasses.addAll(scanResult.getClassesWithAnnotation(Factory.class));
      foundClasses.addAll(scanResult.getClassesWithAnnotation(Singleton.class));

      for (ClassInfo routeClassInfo : foundClasses) {
        AnnotationInfo routeAnnotationInfo = routeClassInfo.getAnnotationInfo(Factory.class);
        if (routeAnnotationInfo == null) {
          routeAnnotationInfo = routeClassInfo.getAnnotationInfo(Singleton.class);
        }

        List<AnnotationParameterValue> routeParamVals = routeAnnotationInfo.getParameterValues();
        boolean isLazy = (boolean) routeParamVals.get(0).getValue();
        if (!isLazy) {
          beanClasses.add(routeClassInfo.loadClass());
        }
      }
    }

    return beanClasses;
  }

  public BeanLoader(Class<?>... classes) {
    for (Class<?> c : classes) {
      beans.put(c, new BeanBuilder<>(c));
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
        builder = new BeanBuilder<>(dependency);
      }

      checkForCircularDependency(bean, builder, visitedBeans);
    }
  }

  /**
   * Loads all targeted beans
   */
  public void load(BeanContext beanContext) {
    checkForCircularDependency();
    for (BeanBuilder<?> builder : beans.values()) {
      try {
        beanContext.addProvider(builder.build(beanContext));
      } catch (InvocationTargetException e) {
        throw new LightwireException("Failed to build bean " + builder.getBeanClass().getName(), e.getCause());
      }
    }
  }
}
