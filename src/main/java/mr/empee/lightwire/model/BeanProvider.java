package mr.empee.lightwire.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * A bean wrapper
 */

@RequiredArgsConstructor
public abstract class BeanProvider<T> {

  @Getter
  private final Class<T> type;

  protected abstract T build();

  public final T get() {
    return build();
  }
}
