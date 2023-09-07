package mr.empee.lightwire.model;

import lombok.Getter;
import lombok.SneakyThrows;
import mr.empee.lightwire.annotations.Bean;
import mr.empee.lightwire.annotations.Provider;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;

/**
 * Builds a bean instance from a class
 */

public class BeanBuilder {

  @Getter
  private final Class<?> beanClass;
  private final BeanConstructor constructor;

  public BeanBuilder(Class<?> beanClass) {
    if (!isBean(beanClass)) {
      throw new IllegalArgumentException("The class " + beanClass.getName() + " isn't a buildable bean");
    }

    this.beanClass = beanClass;
    this.constructor = getBeanConstructor(beanClass);
  }

  private static Method findBeanConstructionMethod(Class<?> clazz) {
    var methods = Arrays.stream(clazz.getDeclaredMethods())
        .filter(m -> m.isAnnotationPresent(Provider.class))
        .toList();

    if (methods.isEmpty()) {
      return null;
    }

    if (methods.size() > 1) {
      throw new IllegalStateException("Multiple providers found for the bean " + clazz.getName());
    }

    var method = methods.get(0);
    if (!Modifier.isStatic(method.getModifiers())) {
      throw new IllegalStateException("The method for the bean " + clazz.getName() + " isn't static");
    }

    if (!method.getReturnType().equals(BeanProvider.class)) {
      throw new IllegalStateException("The provider for the bean " + clazz.getName() + " must return a bean provider");
    }

    return method;
  }

  private static Constructor<?> findBeanConstructor(Class<?> clazz) {
    var constructors = clazz.getDeclaredConstructors();
    if (constructors.length == 1) {
      return constructors[0];
    }

    var injectableConstructors = Arrays.stream(constructors)
        .filter(c -> c.isAnnotationPresent(Provider.class))
        .toList();

    if (injectableConstructors.size() > 1) {
      throw new IllegalStateException("Multiple method constructors found for the bean " + clazz.getName());
    }

    return injectableConstructors.get(0);
  }

  private static BeanConstructor getBeanConstructor(Class<?> clazz) {
    var method = findBeanConstructionMethod(clazz);
    if (method != null) {
      return new BeanConstructor(method, null);
    }

    return new BeanConstructor(null, findBeanConstructor(clazz));
  }

  private static boolean isBean(Class<?> clazz) {
    return clazz.isAnnotationPresent(Bean.class) && !clazz.isInterface() && !Modifier.isAbstract(clazz.getModifiers());
  }

  public List<? extends Class<?>> getDependencies() {
    return Arrays.stream(constructor.getDependencies())
        .map(Parameter::getType)
        .toList();
  }

  /**
   * Builds a bean instance from a class
   */
  public BeanProvider build(BeanContext context) throws InvocationTargetException {
    Parameter[] dependencies = constructor.getDependencies();
    Object[] args = new Object[dependencies.length];
    for (int i = 0; i < dependencies.length; i++) {
      args[i] = context.getBean(dependencies[i].getType());
    }

    return constructor.build(args);
  }

  private record BeanConstructor(Method method, Constructor<?> constructor) {
    public Parameter[] getDependencies() {
      if (method != null) {
        return method.getParameters();
      }

      return constructor.getParameters();
    }

    @SneakyThrows
    public BeanProvider build(Object... args) throws InvocationTargetException {
      if (method != null) {
        method.setAccessible(true);
        return (BeanProvider) method.invoke(null, args);
      }

      constructor.setAccessible(true);
      var instance = constructor.newInstance(args);
      return new BeanProvider() {
        @Override
        public Object get() {
          return instance;
        }
      };
    }
  }

}
