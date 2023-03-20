package course.concurrency.exams.auction;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicMarkableReference;

public class AuctionStoppableOptimistic implements AuctionStoppable {

    private Notifier notifier;

    public AuctionStoppableOptimistic(Notifier notifier) {
        this.notifier = notifier;
    }

    private AtomicMarkableReference<Bid> latestBid = new AtomicMarkableReference(new Bid(null, null, Long.MIN_VALUE), true);

    public boolean propose(Bid bid) {
        Bid current;
        boolean currentMark;
        do {
            current = latestBid.getReference();
            currentMark = latestBid.isMarked();
            if (bid.getPrice() <= current.getPrice() || !currentMark) {
                return false;
            }
        } while (!latestBid.compareAndSet(current, bid, currentMark, true));
        notifier.sendOutdatedMessage(latestBid.getReference());
        return true;
    }

    public Bid getLatestBid() {
        return latestBid.getReference();
    }

    public Bid stopAuction() {
        latestBid.set(latestBid.getReference(), false);
        return latestBid.getReference();
    }
}
