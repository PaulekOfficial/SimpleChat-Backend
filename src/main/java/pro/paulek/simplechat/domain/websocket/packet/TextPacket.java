package pro.paulek.simplechat.domain.websocket.packet;

import java.time.Instant;

public class TextPacket {
    private String message;
    private Instant timestamp;

    public TextPacket() {
    }

    public TextPacket(String message, Instant timestamp) {
        this.message = message;
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
