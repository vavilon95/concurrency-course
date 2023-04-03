package course.concurrency.m3_shared.immutable;

import course.concurrency.m3_shared.immutable.Order.Status;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;

public class OrderService {

  private final ConcurrentHashMap<Long, Order> currentOrders = new ConcurrentHashMap<>();
  private final LongAdder nextId = new LongAdder();

  private long nextId() {
    nextId.increment();
    return nextId.longValue();
  }

  public long createOrder(List<Item> items) {
    long id = nextId();
    currentOrders.putIfAbsent(id, new Order(id, items, null, false));
    return id;
  }

  public void updatePaymentInfo(long orderId, PaymentInfo paymentInfo) {
    currentOrders.computeIfPresent(
        orderId, (key, order) -> new Order(key, order.getItems(), paymentInfo, order.isPacked()));
    checkPossibleDelivery(orderId);
  }

  public void setPacked(long orderId) {
    currentOrders.computeIfPresent(
            orderId, (key, order) -> new Order(key, order.getItems(), order.getPaymentInfo(), true));
    checkPossibleDelivery(orderId);
  }

  private void checkPossibleDelivery(long orderId) {
    var order = currentOrders.getOrDefault(orderId, null);
    if (Objects.nonNull(order)
        && order.checkStatus()) {
      deliver(order);
    }
  }

  private void deliver(Order order) {

  }

  public boolean isDelivered(long orderId) {
    return currentOrders.get(orderId).getStatus().equals(Status.DELIVERED);
  }
}
