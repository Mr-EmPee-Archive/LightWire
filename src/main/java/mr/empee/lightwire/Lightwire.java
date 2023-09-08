package mr.empee.lightwire;

import mr.empee.lightwire.model.BeanContext;
import mr.empee.lightwire.model.BeanLoader;
import mr.empee.lightwire.model.BeanProvider;

import java.util.List;

/**
 * Main class of the library
 */

public class Lightwire {

  private final BeanContext beanContext = new BeanContext();

  public static Lightwire create(Package scanPackage) {
    var instance = new Lightwire();
    instance.loadBeans(scanPackage);
    return instance;
  }

  public Lightwire() {
    addBean(this);
  }

  public void loadBeans(Package scanPackage) {
    BeanLoader beanLoader = new BeanLoader(scanPackage);
    beanLoader.load(beanContext);
  }

  public <T> T getBean(Class<T> clazz) {
    return beanContext.getProvider(clazz).get();
  }

  public <T> List<T> getAllBeans(Class<T> clazz) {
    return beanContext.getAllProviders(clazz).stream()
        .map(BeanProvider::get)
        .toList();
  }

  public <T> void addBean(T bean) {
    beanContext.addProvider(new BeanProvider(bean.getClass()) {
      @Override
      public Object build() {
        return bean;
      }
    });
  }

}
