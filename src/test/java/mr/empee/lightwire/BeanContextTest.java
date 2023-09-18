package mr.empee.lightwire;

import mr.empee.lightwire.exceptions.LightwireException;
import mr.empee.lightwire.model.BeanContext;
import mr.empee.lightwire.model.BeanProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Test the BeanContext that store and retrieve providers")
class BeanContextTest extends AbstractTest {

  @Test
  @DisplayName("Get a specific provider from the context")
  void getProvider() {
    BeanContext context = new BeanContext();
    context.addProvider(new BeanProvider(Integer.class) {
      @Override
      protected Integer build() {
        return 1000;
      }
    });

    assertNotNull(context.getProvider(Integer.class));
    ;
  }

  @Test
  @DisplayName("Get a provider using a parent class")
  void getProviderUsingParentClass() {
    BeanContext context = new BeanContext();
    context.addProvider(new BeanProvider(Integer.class) {
      @Override
      protected Integer build() {
        return 1000;
      }
    });

    assertNotNull(context.getProvider(Number.class));
    ;
  }

  @Test
  @DisplayName("Get multiple providers using the same class")
  void getMultipleProviders() {
    BeanContext context = new BeanContext();
    context.addProvider(new BeanProvider(Integer.class) {
      @Override
      protected Integer build() {
        return 1000;
      }
    });

    context.addProvider(new BeanProvider(Integer.class) {
      @Override
      protected Integer build() {
        return 1000;
      }
    });

    assertThrows(LightwireException.class, () -> context.getProvider(Number.class));
    assertEquals(2, context.getAllProviders(Number.class).size());
  }

}
