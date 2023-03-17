package course.concurrency.exams.auction;

import java.util.Objects;

public class AuctionStoppablePessimistic implements AuctionStoppable {

    private Notifier notifier;

    public AuctionStoppablePessimistic(Notifier notifier) {
        this.notifier = notifier;
    }

    private volatile Bid latestBid = new Bid(null, null, Long.MIN_VALUE);
    private volatile boolean stop = false;

    public boolean propose(Bid bid) {
        if (stop) {
            return false;
        }
        if (bid.getPrice() > latestBid.getPrice()) {
            synchronized (latestBid) {
                if (bid.getPrice() > latestBid.getPrice()) {
                    latestBid = bid;
                    notifier.sendOutdatedMessage(latestBid);
                    return true;
                }
            }
            return false;
        }
        return false;
    }

    public Bid getLatestBid() {
        return latestBid;
    }

    public Bid stopAuction() {
        stop = true;
        return latestBid;
    }
}
