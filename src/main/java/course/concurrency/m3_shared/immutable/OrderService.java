package course.concurrency.m3_shared.immutable;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;
import org.springframework.util.CollectionUtils;

public class OrderService {

  private final ConcurrentHashMap<Long, Order> currentOrders = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<Long, Boolean> packedOrders = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<Long, Boolean> deliveredOrders = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<Long, PaymentInfo> paymentOrders = new ConcurrentHashMap<>();
  private final LongAdder nextId = new LongAdder();

  private long nextId() {
    nextId.increment();
    return nextId.longValue();
  }

  public long createOrder(List<Item> items) {
    long id = nextId();
    Order order = new Order(id, items);
    currentOrders.putIfAbsent(id, order);
    return id;
  }

  public void updatePaymentInfo(long orderId, PaymentInfo paymentInfo) {
    paymentOrders.putIfAbsent(orderId, paymentInfo);
    checkPossibleDelivery(orderId);
  }

  public void setPacked(long orderId) {
    packedOrders.putIfAbsent(orderId, true);
    checkPossibleDelivery(orderId);
  }

  private void checkPossibleDelivery(long orderId) {
    var order = currentOrders.getOrDefault(orderId, null);
    if (Objects.nonNull(order)
        && !CollectionUtils.isEmpty(order.getItems())
        && packedOrders.containsKey(orderId)
        && paymentOrders.containsKey(orderId)) {
      deliver(order);
    }
  }

  private void deliver(Order order) {
    deliveredOrders.putIfAbsent(order.getId(), true);
  }

  public boolean isDelivered(long orderId) {
    return deliveredOrders.containsKey(orderId);
  }
}
