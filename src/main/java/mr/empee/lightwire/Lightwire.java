package mr.empee.lightwire;

import mr.empee.lightwire.model.BeanContext;
import mr.empee.lightwire.model.BeanLoader;
import mr.empee.lightwire.model.BeanProvider;

/**
 * Main class of the library
 */

public class Lightwire {

  private final BeanContext beanContext = new BeanContext();

  public static Lightwire inject(Package scanPackage, Package... exclusions) {
    return new Lightwire(scanPackage, exclusions);
  }

  private Lightwire(Package scanPackage, Package... exclusions) {
    addBean(this);

    BeanLoader beanLoader = new BeanLoader(scanPackage, exclusions);
    beanLoader.load(beanContext);
  }

  public <T> T getBean(Class<T> clazz) {
    return beanContext.getBean(clazz);
  }

  public <T> void addBean(T bean) {
    beanContext.addProvider(new BeanProvider() {
      @Override
      public Object get() {
        return bean;
      }
    });
  }

}
