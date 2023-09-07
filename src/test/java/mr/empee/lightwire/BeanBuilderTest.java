package mr.empee.lightwire;

import lombok.RequiredArgsConstructor;
import mr.empee.lightwire.annotations.Bean;
import mr.empee.lightwire.annotations.Provider;
import mr.empee.lightwire.model.BeanBuilder;
import mr.empee.lightwire.model.BeanContext;
import mr.empee.lightwire.model.BeanProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;

@DisplayName("Injection of dependencies")
class BeanBuilderTest extends AbstractTest {

  @Test
  @DisplayName("Create a bean with no dependencies")
  void createBeanWithNoDependencies() throws InvocationTargetException {
    assertNotNull(new BeanBuilder(NoDepsBean.class).build(new BeanContext()));
  }

  @Test
  @DisplayName("Inject dependencies using constructor")
  void injectDepsUsingConstructor() throws InvocationTargetException {
    assertNotNull(new BeanBuilder(ConstructorDepsBean.class).build(new BeanContext()));
  }

  @Test
  @DisplayName("Inject dependencies using provider method")
  void injectDepsUsingMethod() throws InvocationTargetException {
    assertNotNull(new BeanBuilder(MethodDepsBean.class).build(new BeanContext()));
  }

  @Bean
  public static class NoDepsBean {
  }

  @Bean
  @RequiredArgsConstructor
  public static class ConstructorDepsBean {
    private final NoDepsBean noDepsBean;
    private final NoDepsBean noDepsBean2;
  }

  @Bean
  public static abstract class MethodDepsBean {
    @Provider
    public static BeanProvider buildInstance(NoDepsBean noDepsBean) {
      return new BeanProvider() {
        @Override
        public ConstructorDepsBean get() {
          return new ConstructorDepsBean(noDepsBean, noDepsBean);
        }
      };
    }
  }

}
