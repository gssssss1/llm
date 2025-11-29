package io.cto.loomevent.core.util;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.UnaryOperator;

/** Utilities for lock-free programming operations. */
public final class LockFreeUtils {

  private LockFreeUtils() {
    // Prevent instantiation
  }

  /**
   * Atomically updates the current value of the AtomicReference using the given update function.
   *
   * @param <T> the type of the value
   * @param ref the AtomicReference to update
   * @param updateFunction the function to calculate the new value from the current value
   * @return the updated value
   */
  public static <T> T update(AtomicReference<T> ref, UnaryOperator<T> updateFunction) {
    T prev;
    T next;
    do {
      prev = ref.get();
      next = updateFunction.apply(prev);
    } while (!ref.compareAndSet(prev, next));
    return next;
  }
}
