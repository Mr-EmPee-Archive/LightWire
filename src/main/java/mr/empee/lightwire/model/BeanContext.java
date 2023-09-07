package mr.empee.lightwire.model;

import mr.empee.lightwire.exceptions.LightwireException;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 * BeanContext is a container for all beans in the application.
 */

public class BeanContext {

  private final Map<Class<?>, BeanProvider> beans = new HashMap<>();

  public <T> T getBean(Class<T> clazz) {
    var provider = beans.get(clazz);
    if (provider == null) {
      provider = buildBean(clazz);
      addProvider(provider);
    }

    return (T) provider.get();
  }

  public void addProvider(BeanProvider bean) {
    beans.put(bean.getType(), bean);
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
