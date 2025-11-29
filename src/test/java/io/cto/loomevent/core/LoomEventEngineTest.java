package io.cto.loomevent.core;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

class LoomEventEngineTest {

  @Test
  void testStart() {
    LoomEventEngine engine = new LoomEventEngine();
    assertTrue(engine.start(), "Engine should start successfully");
  }

  @Test
  void testMocking() {
    // Just to verify Mockito is on classpath
    Logger logger = mock(Logger.class);
    logger.info("test");
    verify(logger).info("test");
  }
}
