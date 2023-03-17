package course.concurrency.exams.auction;

import java.util.concurrent.atomic.AtomicReference;

public class AuctionOptimistic implements Auction {

    private Notifier notifier;

    public AuctionOptimistic(Notifier notifier) {
        this.notifier = notifier;
    }

    private AtomicReference<Bid> latestBid = new AtomicReference<>(new Bid(null, null, Long.MIN_VALUE));

    public boolean propose(Bid bid) {
        if (bid.getPrice() > latestBid.get().getPrice()) {
            notifier.sendOutdatedMessage(latestBid.updateAndGet((bidSave -> bid.getPrice() > bidSave.getPrice() ? bid : bidSave)));
            return true;
        }
        return false;
    }

    public Bid getLatestBid() {
        return latestBid.get();
    }
}
