package mr.empee.lightwire;

import lombok.RequiredArgsConstructor;
import mr.empee.lightwire.annotations.Singleton;
import mr.empee.lightwire.exceptions.LightwireException;
import mr.empee.lightwire.model.BeanContext;
import mr.empee.lightwire.model.BeanLoader;
import mr.empee.lightwire.model.BeanProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import scannablePackage.EagerBean1;
import scannablePackage.EagerBean2;

import java.util.List;

@DisplayName("Load default beans")
class BeanLoaderTest extends AbstractTest {

  @Test
  @DisplayName("Report circular dependencies")
  void throwsOnCircularDep() {
    var context = new BeanContext();
    var loader = new BeanLoader(context, List.of(CircularDepBean.class));

    assertThrows(LightwireException.class, loader::load);
  }

  @Test
  @DisplayName("Scan the package and loads eager beans")
  void loadBeansFromScan() {
    var targetPackage = EagerBean1.class.getPackage();

    BeanContext context = new BeanContext();
    context.addProvider(new BeanProvider<>(Integer.class) {
      @Override
      protected Integer build() {
        return 10;
      }
    });

    BeanLoader loader = new BeanLoader(targetPackage, context);
    loader.load();

    assertTrue(context.isLoaded(EagerBean1.class));
    assertTrue(context.isLoaded(EagerBean2.class));
  }

  @Singleton
  @RequiredArgsConstructor
  public static class CircularDepBean {
    private final CircularDepBean dep;
  }

}
