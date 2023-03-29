package course.concurrency.m3_shared.collections;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class RestaurantService {

    private Map<String, Restaurant> restaurantMap = new ConcurrentHashMap<>() {{
        put("A", new Restaurant("A"));
        put("B", new Restaurant("B"));
        put("C", new Restaurant("C"));
    }};

    private ConcurrentHashMap<String, Integer> stat = new ConcurrentHashMap<>();

    public Restaurant getByName(String restaurantName) {
        addToStat(restaurantName);
        return restaurantMap.get(restaurantName);
    }

    public void addToStat(String restaurantName) {
        stat.compute(restaurantName, (key, value) -> Objects.isNull(value) ? 1 : value + 1);
    }

    public Set<String> printStat() {
        return stat.entrySet().stream().map(entry -> entry.getKey() + " - " + entry.getValue()).collect(Collectors.toSet());
    }
}
