package mr.empee.lightwire.model;

/**
 * A bean wrapper
 */

public abstract class BeanProvider {
  public Class<?> getType() {
    return get().getClass();
  }

  public abstract Object get();
}
