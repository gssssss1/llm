package io.cto.loomevent.core.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class LockFreeUtilsTest {

  @Test
  void testUpdate() {
    AtomicReference<String> ref = new AtomicReference<>("start");
    String result = LockFreeUtils.update(ref, val -> val + "-updated");
    assertEquals("start-updated", result);
    assertEquals("start-updated", ref.get());
  }
}
