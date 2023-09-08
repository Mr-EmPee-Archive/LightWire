package mr.empee.lightwire.model;

import mr.empee.lightwire.exceptions.LightwireException;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * BeanContext is a container for all beans in the application.
 */

public class BeanContext {

  private final List<BeanProvider<?>> providers = new ArrayList<>();

  private  <T> List<BeanProvider<T>> findAllProviders(Class<T> clazz) {
    return providers.stream()
        .filter(provider -> clazz.isAssignableFrom(provider.getType()))
        .map(provider -> (BeanProvider<T>) provider)
        .toList();
  }

  public <T> BeanProvider<T> getProvider(Class<T> clazz) {
    var providers = findAllProviders(clazz);
    if (providers.size() == 1) {
      return providers.get(0);
    } else if (providers.size() > 1) {
      throw new LightwireException("Multiple beans found for type " + clazz.getName());
    }

    var provider = buildProvider(clazz);
    addProvider(provider);
    return provider;
  }

  public <T> List<BeanProvider<T>> getAllProviders(Class<T> clazz) {
    var providers = findAllProviders(clazz);
    if (providers.isEmpty()) {
      providers = List.of(buildProvider(clazz));
      addProvider(providers.get(0));
    }

    return providers;
  }

  public void addProvider(BeanProvider<?> bean) {
    providers.add(bean);
  }

  public boolean isLoaded(Class<?> clazz) {
    return !findAllProviders(clazz).isEmpty();
  }

  private <T> BeanProvider<T> buildProvider(Class<T> clazz) {
    try {
      return new BeanBuilder<>(clazz).build(this);
    } catch (InvocationTargetException e) {
      throw new LightwireException("Failed to build bean " + clazz.getName(), e.getCause());
    }
  }

}
