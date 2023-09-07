package mr.empee.lightwire;

import lombok.RequiredArgsConstructor;
import mr.empee.lightwire.annotations.Bean;
import mr.empee.lightwire.exceptions.LightwireException;
import mr.empee.lightwire.model.BeanContext;
import mr.empee.lightwire.model.BeanLoader;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import scannablePackage.EagerBean1;
import scannablePackage.EagerBean2;
import scannablePackage.LazyBean1;
import scannablePackage.exclusion1.EagerBean3;
import scannablePackage.exclusion2.EagerBean4;

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
    var exclusions = new Package[] {
        EagerBean3.class.getPackage(),
        EagerBean4.class.getPackage()
    };

    BeanContext context = new BeanContext();
    BeanLoader loader = new BeanLoader(targetPackage, exclusions);
    loader.load(context);

    assertTrue(context.isLoaded(EagerBean1.class));
    assertTrue(context.isLoaded(EagerBean2.class));
    assertFalse(context.isLoaded(LazyBean1.class));
    assertFalse(context.isLoaded(EagerBean3.class));
    assertFalse(context.isLoaded(EagerBean4.class));
  }

  @Bean
  @RequiredArgsConstructor
  public static class CircularDepBean {
    private final CircularDepBean dep;
  }

}
