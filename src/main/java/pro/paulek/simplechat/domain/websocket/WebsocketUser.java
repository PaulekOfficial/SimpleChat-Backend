package pro.paulek.simplechat.domain.websocket;

import org.springframework.web.socket.WebSocketSession;
import pro.paulek.simplechat.domain.User;

public class WebsocketUser {
    private User user;
    private WebSocketSession websocketSession;

    public WebsocketUser(User user, WebSocketSession websocketSession) {
        this.user = user;
        this.websocketSession = websocketSession;
    }

    public User getUser() {
        return user;
    }

    public WebSocketSession getWebsocketSession() {
        return websocketSession;
    }
}
