package pro.paulek.simplechat.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;
import pro.paulek.simplechat.domain.User;
import pro.paulek.simplechat.domain.websocket.WebsocketMessage;
import pro.paulek.simplechat.domain.websocket.WebsocketUser;
import pro.paulek.simplechat.repository.user.UserRepository;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class WebsocketHandler extends AbstractWebSocketHandler {
    private final Logger logger = Logger.getLogger(WebsocketHandler.class.getName());

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<Long, WebsocketUser> websocketUsers = new HashMap<>();

    @Autowired
    private UserRepository userRepository;

    public WebsocketHandler(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Optional<User> optionalUser = this.getUserFromSession(session);

        if (optionalUser.isEmpty()) {
            return;
        }

        WebsocketMessage websocketMessage = new WebsocketMessage(optionalUser.get().getId(), optionalUser.get().getNickname(), message.getPayload());
        String jsonMessage = objectMapper.writeValueAsString(websocketMessage);

        websocketUsers.values().stream().filter(websocketUser -> !Objects.equals(websocketUser.getUser().getId(), optionalUser.get().getId())).forEach(websocketUser -> {
            try {
                websocketUser.getWebsocketSession().sendMessage(new TextMessage(jsonMessage));
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Error while sending message", e);
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public void afterConnectionEstablished(@NotNull WebSocketSession session) throws Exception {
        logger.info("Connection established");

        logger.log(Level.WARNING, session.getHandshakeHeaders().toString());

        var userId = session.getHandshakeHeaders().get("user-id");
        if (userId == null || userId.isEmpty()) {
            logger.warning("User id not found in headers");
            session.close(CloseStatus.BAD_DATA);
            return;
        }

        Optional<User> optionalUser = userRepository.findById(Long.valueOf(userId.get(0)));
        if (optionalUser.isEmpty()) {
            session.close(CloseStatus.BAD_DATA);
            session.close();
            return;
        }

        websocketUsers.put(optionalUser.get().getId(), new WebsocketUser(optionalUser.get(), session));
    }

    @Override
    public void afterConnectionClosed(@NotNull WebSocketSession session, @NotNull CloseStatus status) throws Exception {
        logger.info("Connection closed");

        Optional<User> optionalUser = this.getUserFromSession(session);

        if (optionalUser.isEmpty()) {
            return;
        }

        websocketUsers.remove(optionalUser.get().getId());
    }

    private Optional<User> getUserFromSession(WebSocketSession session) {
        return websocketUsers.values().stream()
                .filter(websocketUser -> websocketUser.getWebsocketSession().equals(session))
                .map(WebsocketUser::getUser)
                .findFirst();
    }
}
