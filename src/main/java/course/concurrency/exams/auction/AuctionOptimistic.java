package course.concurrency.exams.auction;

import java.util.concurrent.atomic.AtomicReference;

public class AuctionOptimistic implements Auction {

    private Notifier notifier;

    public AuctionOptimistic(Notifier notifier) {
        this.notifier = notifier;
    }

    private AtomicReference<Bid> latestBid = new AtomicReference<>(new Bid(null, null, Long.MIN_VALUE));

    public boolean propose(Bid bid) {
        Bid current;
        do {
            current = latestBid.get();
            if (bid.getPrice() <= latestBid.get().getPrice()) {
                return false;
            }
        } while (!latestBid.compareAndSet(current, bid));
        notifier.sendOutdatedMessage(latestBid.get());
        return true;
    }

    public Bid getLatestBid() {
        return latestBid.get();
    }
}
