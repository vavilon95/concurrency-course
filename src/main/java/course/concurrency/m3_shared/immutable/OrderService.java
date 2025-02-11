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
    currentOrders.putIfAbsent(id, new Order(id, items, null, false, Status.NEW));
    return id;
  }

  public void updatePaymentInfo(long orderId, PaymentInfo paymentInfo) {
    Order orderWithPayment = currentOrders.computeIfPresent(
            orderId, (key, order) -> new Order(key, order.getItems(), paymentInfo, order.isPacked(), Status.IN_PROGRESS));
    if (Objects.nonNull(orderWithPayment) && orderWithPayment.checkStatus()) {
      deliver(orderWithPayment);
    }
  }

  public void setPacked(long orderId) {
    Order orderPacked = currentOrders.computeIfPresent(
            orderId, (key, order) -> new Order(key, order.getItems(), order.getPaymentInfo(), true, Status.IN_PROGRESS));
    if (Objects.nonNull(orderPacked) && orderPacked.checkStatus()) {
      deliver(orderPacked);
    }
  }

  private void deliver(Order order) {
    //delivery
    currentOrders.computeIfPresent(
            order.getId(), (key, value) -> new Order(key, value.getItems(), value.getPaymentInfo(), value.isPacked(), Status.DELIVERED));
  }

  public boolean isDelivered(long orderId) {
    return currentOrders.get(orderId).getStatus().equals(Status.DELIVERED);
  }
}
