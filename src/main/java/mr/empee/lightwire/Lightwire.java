package mr.empee.lightwire;

import mr.empee.lightwire.model.BeanContext;
import mr.empee.lightwire.model.BeanLoader;

/**
 * Main class of the library
 */

public class Lightwire {

  private final BeanContext beanContext = new BeanContext();

  public static Lightwire inject(Package scanPackage, Package... exclusions) {
    return new Lightwire(scanPackage, exclusions);
  }

  private Lightwire(Package scanPackage, Package... exclusions) {
    beanContext.addBean(this);

    BeanLoader beanLoader = new BeanLoader(scanPackage, exclusions);
    beanLoader.load(beanContext);
  }

  public <T> T getBean(Class<T> clazz) {
    return beanContext.getBean(clazz);
  }

}
