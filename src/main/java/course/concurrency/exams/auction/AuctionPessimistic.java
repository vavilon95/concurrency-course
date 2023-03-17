package course.concurrency.exams.auction;

import java.util.Objects;

public class AuctionPessimistic implements Auction {

    private Notifier notifier;

    public AuctionPessimistic(Notifier notifier) {
        this.notifier = notifier;
    }

    private volatile Bid latestBid;

    public synchronized boolean propose(Bid bid) {
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
}
