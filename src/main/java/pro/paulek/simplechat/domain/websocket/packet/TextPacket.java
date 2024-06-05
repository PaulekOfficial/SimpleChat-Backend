package pro.paulek.simplechat.domain.websocket.packet;

import java.time.Instant;

public class TextPacket {
    private Long userId;
    private String message;
    private Instant timestamp;

    public TextPacket() {
    }

    public TextPacket(Long userId, String message, Instant timestamp) {
        this.userId = userId;
        this.message = message;
        this.timestamp = timestamp;
    }

    public Long getUserId() {
        return userId;
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

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
