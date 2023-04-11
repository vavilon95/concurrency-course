package course.concurrency.m3_shared.immutable;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;

public class MyExecutorService {

  private static final int THREAD_COUNT = 8;
  private final LinkedBlockingDeque<String> deque = new LinkedBlockingDeque<>(THREAD_COUNT);
  private final ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);

  public void addTask(String task) {
    try {
      deque.add(task);
    } catch (Exception e) {
      // so sad
    }
    executor.execute(this::process);
  }

  private void process() {
    var value = deque.peekLast();
    if (Objects.isNull(value)) {
      return;
    }
    // todo
    deque.removeLast();
  }
}
