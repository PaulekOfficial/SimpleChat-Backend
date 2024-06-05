package pro.paulek.simplechat.websocket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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
import pro.paulek.simplechat.repository.auth.TokenRepository;
import pro.paulek.simplechat.repository.user.UserRepository;

import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

@Component
public class WebsocketHandler extends AbstractWebSocketHandler {
    private final Logger logger = Logger.getLogger(WebsocketHandler.class.getName());

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private final Map<WebSocketSession, WebsocketUser> websocketUsers = new HashMap<>();

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TokenRepository tokenRepository;

    public WebsocketHandler(UserRepository userRepository, TokenRepository tokenRepository) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        logger.info("Message received " + message.getPayload());

        JsonNode jsonNode = objectMapper.readTree(message.getPayload());

        String type = jsonNode.get("type").asText();

        if (type.equalsIgnoreCase("authorization")) {
            AuthorizationPacket authorizationPacket = objectMapper.treeToValue(jsonNode, AuthorizationPacket.class);

            handleAuthorizationPacket(session, authorizationPacket);
        } else if (type.equalsIgnoreCase("text")) {
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
        Optional<User> userOptional = userRepository.findById(packet.getUserId());

        if (userOptional.isEmpty()) {
            logger.warning("User not found");
            return;
        }

        var tokenOptional = tokenRepository.findByToken(packet.getToken());
        if (tokenOptional.isEmpty()) {
            logger.warning("Token not found");
            return;
        }
        var token = tokenOptional.get();
        if (token.isExpired() || token.isRevoked()) {
            logger.warning("Token is expired or revoked");
            return;
        }

        websocketUsers.put(session, new WebsocketUser(userOptional.get(), session));
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

    public <T> String serializePacketWithAddedType(T packet, String type) throws JsonProcessingException {
        JsonNode jsonNode = objectMapper.valueToTree(packet);

        ObjectNode objectNode = (ObjectNode) jsonNode;

        objectNode.put("type", type);

        return objectMapper.writeValueAsString(objectNode);
    }

    //This method is used to get user from session, but will not work because the implementation of Websocket on browsers does not support this, huray!
    private Optional<User> getUserFromSession(WebSocketSession session) {
        return websocketUsers.values().stream()
                .filter(websocketUser -> websocketUser.getWebsocketSession().equals(session))
                .map(WebsocketUser::getUser)
                .findFirst();
    }
}
