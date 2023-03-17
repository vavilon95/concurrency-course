package course.concurrency.exams.auction;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicMarkableReference;

public class AuctionStoppableOptimistic implements AuctionStoppable {

    private Notifier notifier;

    public AuctionStoppableOptimistic(Notifier notifier) {
        this.notifier = notifier;
    }

    private AtomicMarkableReference<Bid> latestBid = new AtomicMarkableReference(null, true);

    public boolean propose(Bid bid) {
        if ((Objects.isNull(latestBid.getReference())
                    || bid.getPrice() > latestBid.getReference().getPrice()
                ) && latestBid.isMarked()
        ) {
            latestBid.set(bid, latestBid.isMarked());
            if (!latestBid.isMarked()) {
                return false;
            }
            notifier.sendOutdatedMessage(latestBid.getReference());
            return true;
        }
        return false;
    }

    public Bid getLatestBid() {
        return latestBid.getReference();
    }

    public Bid stopAuction() {
        latestBid.set(latestBid.getReference(), false);
        return latestBid.getReference();
    }
}
