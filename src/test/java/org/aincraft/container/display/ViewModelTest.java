package org.aincraft.container.display;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Binding-store lifecycle: create intentionally, updateIfBound without allocating, onRemove cleanup.
 */
class ViewModelTest {

  private static final class RecordingViewModel extends ViewModel<String, StringBuilder> {

    final AtomicInteger createCount = new AtomicInteger();
    final AtomicInteger updateCount = new AtomicInteger();
    final List<StringBuilder> removed = new ArrayList<>();

    @Override
    protected @NotNull Object keyOf(@NotNull String model) {
      return model;
    }

    @Override
    protected @NotNull StringBuilder createBinding(@NotNull String model) {
      createCount.incrementAndGet();
      return new StringBuilder("bound:").append(model);
    }

    @Override
    public void update(@NotNull String model) {
      updateCount.incrementAndGet();
      StringBuilder binding = findBinding(model);
      if (binding != null) {
        binding.append("|u");
      }
    }

    @Override
    protected void onRemove(@NotNull StringBuilder binding) {
      removed.add(binding);
    }
  }

  private RecordingViewModel vm;

  @BeforeEach
  void setUp() {
    vm = new RecordingViewModel();
  }

  @Test
  void updateIfBound_doesNotCreateBinding() {
    assertFalse(vm.updateIfBound("anvil-1"));
    assertEquals(0, vm.createCount.get());
    assertEquals(0, vm.updateCount.get());
    assertNull(vm.findBinding("anvil-1"));
  }

  @Test
  void getBinding_createsOnce_andUpdateIfBoundRuns() {
    StringBuilder first = vm.getBinding("s1");
    StringBuilder second = vm.getBinding("s1");

    assertSame(first, second);
    assertEquals(1, vm.createCount.get());
    assertTrue(vm.isBound("s1"));

    assertTrue(vm.updateIfBound("s1"));
    assertEquals(1, vm.updateCount.get());
    assertEquals("bound:s1|u", first.toString());
  }

  @Test
  void findBinding_nullWhenMissing_sameWhenPresent() {
    assertNull(vm.findBinding("x"));
    StringBuilder b = vm.getBinding("x");
    assertSame(b, vm.findBinding("x"));
  }

  @Test
  void remove_invokesOnRemoveAndUnbinds() {
    StringBuilder b = vm.getBinding("gone");
    vm.remove("gone");

    assertFalse(vm.isBound("gone"));
    assertNull(vm.findBinding("gone"));
    assertEquals(1, vm.removed.size());
    assertSame(b, vm.removed.getFirst());
  }

  @Test
  void removeAll_cleansEveryBinding() {
    vm.getBinding("a");
    vm.getBinding("b");
    vm.removeAll();

    assertFalse(vm.isBound("a"));
    assertFalse(vm.isBound("b"));
    assertEquals(2, vm.removed.size());
  }
}
