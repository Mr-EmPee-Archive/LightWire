package mr.empee.lightwire.model;

/**
 * A bean wrapper
 */

public abstract class BeanProvider {
  public Class<?> getType() {
    return get().getClass();
  }

  protected abstract Object build();

  public final Object get() {
    return build();
  }
}
