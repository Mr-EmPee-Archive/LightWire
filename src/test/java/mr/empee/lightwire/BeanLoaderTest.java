package mr.empee.lightwire;

import lombok.RequiredArgsConstructor;
import mr.empee.lightwire.annotations.Singleton;
import mr.empee.lightwire.exceptions.LightwireException;
import mr.empee.lightwire.model.BeanContext;
import mr.empee.lightwire.model.BeanLoader;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import scannablePackage.EagerBean1;
import scannablePackage.EagerBean2;

@DisplayName("Load default beans")
class BeanLoaderTest extends AbstractTest {

  @Test
  @DisplayName("Report circular dependencies")
  void throwsOnCircularDep() {
    var loader = new BeanLoader(CircularDepBean.class);
    var context = new BeanContext();

    assertThrows(LightwireException.class, () -> {
      loader.load(context);
    });
  }

  @Test
  @DisplayName("Scan the package and loads eager beans")
  void loadBeansFromScan() {
    var targetPackage = EagerBean1.class.getPackage();

    BeanContext context = new BeanContext();
    BeanLoader loader = new BeanLoader(targetPackage, getClass().getClassLoader());
    loader.load(context);

    assertTrue(context.isLoaded(EagerBean1.class));
    assertTrue(context.isLoaded(EagerBean2.class));
  }

  @Singleton
  @RequiredArgsConstructor
  public static class CircularDepBean {
    private final CircularDepBean dep;
  }

}
