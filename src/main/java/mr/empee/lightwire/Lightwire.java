package mr.empee.lightwire;

import mr.empee.lightwire.model.BeanContext;
import mr.empee.lightwire.model.BeanLoader;
import mr.empee.lightwire.model.BeanProvider;

/**
 * Main class of the library
 */

public class Lightwire {

  private final BeanContext beanContext = new BeanContext();

  public static Lightwire create(Package scanPackage, Package... exclusions) {
    var instance = new Lightwire();
    instance.loadBeans(scanPackage, exclusions);
    return instance;
  }

  public Lightwire() {
    addBean(this);
  }

  public void loadBeans(Package scanPackage, Package... exclusions) {
    BeanLoader beanLoader = new BeanLoader(scanPackage, exclusions);
    beanLoader.load(beanContext);
  }

  public <T> T getBean(Class<T> clazz) {
    return beanContext.getBean(clazz);
  }

  public <T> void addBean(T bean) {
    beanContext.addProvider(new BeanProvider() {
      @Override
      public Object build() {
        return bean;
      }
    });
  }

}
