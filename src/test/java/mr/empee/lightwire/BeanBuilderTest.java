package mr.empee.lightwire;

import lombok.RequiredArgsConstructor;
import mr.empee.lightwire.annotations.Factory;
import mr.empee.lightwire.annotations.Instance;
import mr.empee.lightwire.annotations.Provider;
import mr.empee.lightwire.annotations.Singleton;
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

  @Test
  @DisplayName("Inject singleton instance")
  void injectSingletonInstance() throws InvocationTargetException {
    var provider = new BeanBuilder(InjectSingletonBean.class).build(new BeanContext());
    provider.get();

    assertNotNull(InjectSingletonBean.instance);
  }

  @Singleton
  public static class NoDepsBean {
  }

  @Singleton
  @RequiredArgsConstructor
  public static class ConstructorDepsBean {
    private final NoDepsBean noDepsBean;
    private final NoDepsBean noDepsBean2;
  }

  @Factory
  public static class MethodDepsBean {
    @Provider
    public static BeanProvider<ConstructorDepsBean> buildInstance(NoDepsBean noDepsBean) {
      return new BeanProvider<>(ConstructorDepsBean.class) {
        @Override
        public ConstructorDepsBean build() {
          return new ConstructorDepsBean(noDepsBean, noDepsBean);
        }
      };
    }
  }

  @Singleton
  public static class InjectSingletonBean {
    @Instance
    private static InjectSingletonBean instance;
  }

}
