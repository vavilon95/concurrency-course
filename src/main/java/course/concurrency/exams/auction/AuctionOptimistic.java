package course.concurrency.exams.auction;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public class AuctionOptimistic implements Auction {

    private Notifier notifier;

    public AuctionOptimistic(Notifier notifier) {
        this.notifier = notifier;
    }

    private AtomicReference<Bid> latestBid = new AtomicReference<>();

    public boolean propose(Bid bid) {
        if (Objects.isNull(latestBid.get()) || bid.getPrice() > latestBid.get().getPrice()) {
            notifier.sendOutdatedMessage(latestBid.compareAndExchange(latestBid.get(), bid));
            return true;
        }
        return false;
    }

    public Bid getLatestBid() {
        return latestBid.get();
    }
}
