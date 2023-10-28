package mr.empee.lightwire;

import lombok.RequiredArgsConstructor;
import mr.empee.lightwire.annotations.Singleton;
import mr.empee.lightwire.exceptions.LightwireException;
import mr.empee.lightwire.model.BeanContext;
import mr.empee.lightwire.model.BeanLoader;
import mr.empee.lightwire.model.BeanProvider;
import scannable.EagerBean1;
import scannable.EagerBean2;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
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

  @Test
  @DisplayName("Load beans with priority")
  void loadWithPriority() {
    var context = new BeanContext();
    var loader = new BeanLoader(context, List.of(BeanWithLowPriority.class, BeanWithHighPriority.class));

    loader.load();

    var providers = context.getAllProviders(Object.class);
    assertEquals(2, providers.size());

    assertEquals(BeanWithHighPriority.class, BeanWithLowPriority.loadOrder.get(0));
    assertEquals(BeanWithLowPriority.class, BeanWithLowPriority.loadOrder.get(1));
  }

  @Singleton
  @RequiredArgsConstructor
  public static class CircularDepBean {
    private final CircularDepBean dep;
  }

  @Singleton(priority = Singleton.Priority.LOW)
  public static class BeanWithLowPriority {
    private static List<Class<?>> loadOrder = new ArrayList<>();

    public BeanWithLowPriority() {
      loadOrder.add(this.getClass());
    }
  }

  @Singleton(priority = Singleton.Priority.HIGH)
  public static class BeanWithHighPriority {

    public BeanWithHighPriority() {
      BeanWithLowPriority.loadOrder.add(this.getClass());
    }
  }

}
