package course.concurrency.exams.auction;

public class Bid {
    private Long id; // ID Р·Р°СЏРІРєРё
    private Long participantId; // ID СѓС‡Р°СЃС‚РЅРёРєР°
    private Long price; // РїСЂРµРґР»РѕР¶РµРЅРЅР°СЏ С†РµРЅР°

    public Bid(Long id, Long participantId, Long price) {
        this.id = id;
        this.participantId = participantId;
        this.price = price;
    }

    public Long getId() {
        return id;
    }

    public Long getParticipantId() {
        return participantId;
    }

    public Long getPrice() {
        return price;
    }
}
