package pro.paulek.simplechat.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;
import pro.paulek.simplechat.domain.User;
import pro.paulek.simplechat.domain.websocket.WebsocketUser;
import pro.paulek.simplechat.domain.websocket.packet.AuthorizationPacket;
import pro.paulek.simplechat.domain.websocket.packet.TextPacket;
import pro.paulek.simplechat.repository.user.UserRepository;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class WebsocketHandler extends AbstractWebSocketHandler {
    private final Logger logger = Logger.getLogger(WebsocketHandler.class.getName());

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<WebSocketSession, WebsocketUser> websocketUsers = new HashMap<>();

    @Autowired
    private UserRepository userRepository;

    public WebsocketHandler(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        logger.info("Message received " + message.getPayload());

        JsonNode jsonNode = objectMapper.readTree(message.getPayload());

        String type = jsonNode.get("type").asText();

        if ("authorization".equals(type)) {
            AuthorizationPacket authorizationPacket = objectMapper.treeToValue(jsonNode, AuthorizationPacket.class);

            handleAuthorizationPacket(session, authorizationPacket);
        } else if ("text".equals(type)) {
            TextPacket textPacket = objectMapper.treeToValue(jsonNode, TextPacket.class);

            handleTextPacket(session, textPacket);
        } else {
            logger.warning("Unknown packet type: " + type);
        }
    }

    @Override
    public void afterConnectionEstablished(@NotNull WebSocketSession session) throws Exception {
        logger.info("Connection established");
    }

    @Override
    public void afterConnectionClosed(@NotNull WebSocketSession session, @NotNull CloseStatus status) throws Exception {
        logger.info("Connection closed");

        websocketUsers.remove(session);
    }

    private void handleAuthorizationPacket(WebSocketSession session, AuthorizationPacket packet) {

    }

    private void handleTextPacket(WebSocketSession session, TextPacket packet) {
        if (!websocketUsers.containsKey(session)) {
            logger.warning("User is not authorized");
            return;
        }

        String jsonMessage;
        try {
            jsonMessage = objectMapper.writeValueAsString(packet);
        } catch (JsonProcessingException e) {
            logger.log(Level.SEVERE, "Error while converting TextPacket to JSON", e);
            return;
        }

        websocketUsers.values().stream()
                .filter(websocketUser -> !websocketUser.getWebsocketSession().equals(session))
                .forEach(websocketUser -> {
                    try {
                        websocketUser.getWebsocketSession().sendMessage(new TextMessage(jsonMessage));
                    } catch (IOException e) {
                        logger.log(Level.SEVERE, "Error while sending TextPacket", e);
                    }
                });
    }

    //This method is used to get user from session, but will not work because the implementation of Websocket on browsers does not support this, huray!
    private Optional<User> getUserFromSession(WebSocketSession session) {
        return websocketUsers.values().stream()
                .filter(websocketUser -> websocketUser.getWebsocketSession().equals(session))
                .map(WebsocketUser::getUser)
                .findFirst();
    }
}
