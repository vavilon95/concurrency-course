package course.concurrency;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MyBlockingQueueTest {

  @Test
  public void testHappyPath() throws InterruptedException {
    MyBlockingQueue<Integer> myBlockingQueue = new MyBlockingQueue<>(2);
    myBlockingQueue.enqueue(0);
    myBlockingQueue.enqueue(1);
    assertEquals(1, myBlockingQueue.dequeue());
    assertEquals(0, myBlockingQueue.dequeue());
  }
}
