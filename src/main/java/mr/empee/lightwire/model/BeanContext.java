package mr.empee.lightwire.model;

import mr.empee.lightwire.exceptions.LightwireException;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 * BeanContext is a container for all beans in the application.
 */

public class BeanContext {

  private final Map<Class<?>, Object> beans = new HashMap<>();

  public <T> T getBean(Class<T> clazz) {
    var bean = beans.get(clazz);
    if (bean == null) {
      var provider = buildBean(clazz);
      bean = provider.get();
      addBean(bean);
    }

    return (T) bean;
  }

  public void addBean(Object bean) {
    beans.put(bean.getClass(), bean);
  }

  public boolean isLoaded(Class<?> clazz) {
    return beans.containsKey(clazz);
  }

  private <T> BeanProvider buildBean(Class<T> clazz) {
    try {
      return new BeanBuilder(clazz).build(this);
    } catch (InvocationTargetException e) {
      throw new LightwireException("Failed to build bean " + clazz.getName(), e.getCause());
    }
  }

}
