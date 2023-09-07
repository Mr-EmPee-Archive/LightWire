package mr.empee.lightwire.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <pre>
 * This annotation is used to mark a class as a singleton bean class.
 *
 * If you have multiple constructors use the {@link Provider} annotation
 * to mark which should be used for the dependency injection.
 *
 * You can use the {@link Instance} annotation to inject the instance
 * of the singleton on a static field.
 * </pre>
 */

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Singleton {
}
