package scannable;

import lombok.RequiredArgsConstructor;
import mr.empee.lightwire.annotations.Singleton;

@Singleton
@RequiredArgsConstructor
public class EagerBean1 {
  private final Integer dep;
  private final EagerBean2 dep2;
}
