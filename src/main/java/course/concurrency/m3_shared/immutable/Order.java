package course.concurrency.m3_shared.immutable;

import java.util.ArrayList;
import java.util.List;

public class Order {

    private final Long id;
    private final List<Item> items;

    public Order(Long id, List<Item> items) {
        this.id = id;
        this.items = new ArrayList<>(items);
    }

    public Long getId() {
        return id;
    }

    public List<Item> getItems() {
        return new ArrayList<>(items);
    }

}
