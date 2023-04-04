package course.concurrency.m3_shared.immutable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.springframework.lang.Nullable;
import org.springframework.util.CollectionUtils;

public class Order {

    public enum Status { NEW, IN_PROGRESS, DELIVERED }

    private final Long id;
    private final List<Item> items;
    @Nullable
    private final PaymentInfo paymentInfo;
    private final boolean isPacked;
    private final Status status;

    public Order(Long id, List<Item> items, PaymentInfo paymentInfo, boolean isPacked, Status status) {
        this.id = id;
        this.items = new ArrayList<>(items);
        this.paymentInfo = paymentInfo;
        this.isPacked = isPacked;
        this.status = status;
    }

    public boolean checkStatus() {
        return !CollectionUtils.isEmpty(items) && paymentInfo != null && isPacked;
    }

    public Long getId() {
        return id;
    }

    public List<Item> getItems() {
        return new ArrayList<>(items);
    }

    public boolean isPacked() {
        return isPacked;
    }

    public PaymentInfo getPaymentInfo() {
        return Objects.isNull(paymentInfo) ? null : paymentInfo.clone();
    }

    public Status getStatus() {
        return status;
    }
}
