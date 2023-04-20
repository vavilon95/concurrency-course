package course.concurrency;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class MyBlockingQueue<T> {

  private final Object[] array;
  private final ReentrantLock reentrantLock = new ReentrantLock();
  private final Condition notFull = reentrantLock.newCondition();
  private final Condition notEmpty = reentrantLock.newCondition();
  private int index = 0;

  public MyBlockingQueue(int size) {
    array = new Object[size];
  }

  public void enqueue(T value) throws InterruptedException {
    reentrantLock.lockInterruptibly();
    try {
      while (index == array.length) {
        notFull.await();
      }
      put(value);
    } finally {
      reentrantLock.unlock();
    }
  }

  public T dequeue() throws InterruptedException {
    reentrantLock.lockInterruptibly();
    try {
      while (index == 0) {
        notEmpty.await();
      }
      return get();
    } finally {
      reentrantLock.unlock();
    }
  }

  private T get() {
    Object value = array[index - 1];
    index--;
    notFull.signal();
    return (T) value;
  }

  private void put(T value) {
    array[index] = value;
    index++;
    notEmpty.signal();
  }
}
