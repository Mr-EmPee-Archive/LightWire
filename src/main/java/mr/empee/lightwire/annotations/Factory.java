package mr.empee.lightwire.annotations;

import mr.empee.lightwire.model.BeanProvider;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <pre>
 * This annotation is used to mark a class as a factory bean class.
 *
 * Use the {@link Provider} annotation to mark a static method as the factory method.
 * The factory method must return an instance of {@link BeanProvider}
 * </pre>
 */

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Factory {

  /**
   * If true the class will be loaded only when it is requested
   */
  boolean lazy() default false;

}
