package course.concurrency.exams.auction;

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
            synchronized (this) {
                if (bid.getPrice() > latestBid.getPrice() && !stop) {
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
        synchronized(this) {
            stop = true;
            return latestBid;
        }
    }
}
