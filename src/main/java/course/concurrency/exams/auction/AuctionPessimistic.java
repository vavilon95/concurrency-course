package course.concurrency.exams.auction;

public class AuctionPessimistic implements Auction {

    private Notifier notifier;

    public AuctionPessimistic(Notifier notifier) {
        this.notifier = notifier;
    }

    private volatile Bid latestBid = new Bid(null, null, Long.MIN_VALUE);

    public boolean propose(Bid bid) {
        if (bid.getPrice() > latestBid.getPrice()) {
            synchronized(latestBid) {
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
}
