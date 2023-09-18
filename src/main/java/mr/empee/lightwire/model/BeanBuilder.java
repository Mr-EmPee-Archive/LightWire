package mr.empee.lightwire.model;

import lombok.Getter;
import lombok.SneakyThrows;
import lombok.Value;
import mr.empee.lightwire.annotations.Factory;
import mr.empee.lightwire.annotations.Instance;
import mr.empee.lightwire.annotations.Provider;
import mr.empee.lightwire.annotations.Singleton;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * Builds a bean instance from a class
 */

public class BeanBuilder<T> {

  @Getter
  private final Class<T> beanClass;
  private final BeanConstructor constructor;

  public BeanBuilder(Class<T> beanClass) {
    if (!isBean(beanClass)) {
      throw new IllegalArgumentException("The class " + beanClass.getName() + " isn't a buildable bean");
    }

    this.beanClass = beanClass;
    if (beanClass.isAnnotationPresent(Factory.class)) {
      this.constructor = new BeanConstructor(findBeanConstructionMethod(beanClass), null);
    } else {
      this.constructor = new BeanConstructor(null, findBeanConstructor(beanClass));
    }
  }

  private static Method findBeanConstructionMethod(Class<?> clazz) {
    List<Method> methods = Arrays.stream(clazz.getDeclaredMethods())
        .filter(m -> m.isAnnotationPresent(Provider.class))
        .collect(Collectors.toList());

    if (methods.isEmpty()) {
      throw new IllegalStateException("Unable to find a provider for the bean " + clazz.getName());
    }

    if (methods.size() > 1) {
      throw new IllegalStateException("Multiple providers found for the bean " + clazz.getName());
    }

    Method method = methods.get(0);
    if (!Modifier.isStatic(method.getModifiers())) {
      throw new IllegalStateException("The provider of the bean " + clazz.getName() + " isn't static");
    }

    if (!method.getReturnType().equals(BeanProvider.class)) {
      throw new IllegalStateException("The provider for the bean " + clazz.getName() + " must return a BeanProvider");
    }

    return method;
  }

  private static Constructor<?> findBeanConstructor(Class<?> clazz) {
    Constructor<?>[] constructors = clazz.getDeclaredConstructors();
    if (constructors.length == 1) {
      return constructors[0];
    }

    List<Constructor<?>> injectableConstructors = Arrays.stream(constructors)
        .filter(c -> c.isAnnotationPresent(Provider.class))
        .collect(Collectors.toList());

    if (injectableConstructors.size() > 1) {
      throw new IllegalStateException("Multiple method constructors found for the bean " + clazz.getName());
    }

    return injectableConstructors.get(0);
  }

  @SneakyThrows
  private static void injectInstanceFields(Object target) {
    List<Field> fields = Arrays.stream(target.getClass().getDeclaredFields())
        .filter(f -> f.isAnnotationPresent(Instance.class))
        .filter(f -> Modifier.isStatic(f.getModifiers()))
        .collect(Collectors.toList());

    for (Field field : fields) {
      field.setAccessible(true);
      field.set(null, target);
    }
  }

  private static boolean isBean(Class<?> clazz) {
    return (clazz.isAnnotationPresent(Singleton.class) || clazz.isAnnotationPresent(Factory.class))
        && !clazz.isInterface() && !Modifier.isAbstract(clazz.getModifiers());
  }

  public Class<?>[] getDependencies() {
    return Arrays.stream(constructor.getDependencies())
        .map(Parameter::getType)
        .toArray(Class[]::new);
  }

  /**
   * Builds a bean instance from a class
   */
  public BeanProvider<T> build(BeanContext context) throws InvocationTargetException {
    Parameter[] dependencies = constructor.getDependencies();
    Object[] args = new Object[dependencies.length];
    for (int i = 0; i < dependencies.length; i++) {
      args[i] = context.getProvider(dependencies[i].getType()).get();
    }

    return constructor.build(args);
  }

  @Value
  private static class BeanConstructor {
    Method method;
    Constructor<?> constructor;

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
      Object instance = constructor.newInstance(args);
      injectInstanceFields(instance);
      return new BeanProvider(instance.getClass()) {
        @Override
        public Object build() {
          return instance;
        }
      };
    }
  }

}
