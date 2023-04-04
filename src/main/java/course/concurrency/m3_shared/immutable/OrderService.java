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
    currentOrders.computeIfPresent(
        orderId, (key, order) -> new Order(key, order.getItems(), paymentInfo, order.isPacked(), Status.IN_PROGRESS));
    checkPossibleDelivery(orderId);
  }

  public void setPacked(long orderId) {
    currentOrders.computeIfPresent(
            orderId, (key, order) -> new Order(key, order.getItems(), order.getPaymentInfo(), true, Status.IN_PROGRESS));
    checkPossibleDelivery(orderId);
  }

  private void checkPossibleDelivery(long orderId) {
    var order = currentOrders.getOrDefault(orderId, null);
    if (Objects.nonNull(order) && order.checkStatus()) {
      if (!order.getStatus().equals(Status.START_DELIVERY)
          && currentOrders.replace(
              orderId,
              order,
              new Order(
                  orderId,
                  order.getItems(),
                  order.getPaymentInfo(),
                  order.isPacked(),
                  Status.START_DELIVERY))) {
        deliver(orderId);
      }
    }
  }

  private void deliver(long orderId) {
    //delivery
    currentOrders.computeIfPresent(
            orderId, (key, value) -> new Order(key, value.getItems(), value.getPaymentInfo(), value.isPacked(), Status.DELIVERED));
  }

  public boolean isDelivered(long orderId) {
    return currentOrders.get(orderId).getStatus().equals(Status.DELIVERED);
  }
}
