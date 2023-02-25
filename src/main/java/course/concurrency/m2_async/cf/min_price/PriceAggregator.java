package course.concurrency.m2_async.cf.min_price;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class PriceAggregator {
  private int SLA = 2900;
  private PriceRetriever priceRetriever = new PriceRetriever();
  private ExecutorService executor = Executors.newCachedThreadPool();

  public void setPriceRetriever(PriceRetriever priceRetriever) {
    this.priceRetriever = priceRetriever;
  }

  private Collection<Long> shopIds = Set.of(10l, 45l, 66l, 345l, 234l, 333l, 67l, 123l, 768l);

  public void setShops(Collection<Long> shopIds) {
    this.shopIds = shopIds;
  }

  public double getMinPrice(long itemId) {
    var features =
        shopIds.stream()
            .map(
                shopId ->
                    CompletableFuture.supplyAsync(
                            () -> priceRetriever.getPrice(itemId, shopId), executor)
                        .orTimeout(SLA, TimeUnit.MILLISECONDS)
                        .exceptionally(throwable -> null))
            .toArray(CompletableFuture[]::new);

    return CompletableFuture.allOf(features)
        .thenApply(
            r ->
                Stream.of(features)
                    .map(feature -> (Double) feature.join())
                    .filter(Objects::nonNull)
                    .min(Double::compareTo)
                    .orElse(Double.NaN))
        .join();
  }
}
