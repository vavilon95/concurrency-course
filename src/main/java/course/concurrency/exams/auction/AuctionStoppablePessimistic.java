package course.concurrency.exams.auction;

import java.util.Objects;

public class AuctionStoppablePessimistic implements AuctionStoppable {

    private Notifier notifier;

    public AuctionStoppablePessimistic(Notifier notifier) {
        this.notifier = notifier;
    }

    private volatile Bid latestBid;
    private volatile boolean stop = false;

    public synchronized boolean propose(Bid bid) {
        if (stop) {
            return false;
        }
        if (Objects.isNull(latestBid) || bid.getPrice() > latestBid.getPrice()) {
            latestBid = bid;
            notifier.sendOutdatedMessage(latestBid);
            return true;
        }
        return false;
    }

    public synchronized Bid getLatestBid() {
        return latestBid;
    }

    public synchronized Bid stopAuction() {
        stop = true;
        return latestBid;
    }
}
