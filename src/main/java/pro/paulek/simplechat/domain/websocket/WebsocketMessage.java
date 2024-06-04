package pro.paulek.simplechat.domain.websocket;

public class WebsocketMessage {
    private Long userId;
    private String username;
    private String message;

    public WebsocketMessage(Long userId, String username, String message) {
        this.userId = userId;
        this.username = username;
        this.message = message;
    }

    public Long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getMessage() {
        return message;
    }
}
