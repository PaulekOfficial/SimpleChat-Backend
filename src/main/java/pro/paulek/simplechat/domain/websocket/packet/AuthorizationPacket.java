package pro.paulek.simplechat.domain.websocket.packet;

public class AuthorizationPacket {
    private Long userId;
    private String token;

    public AuthorizationPacket() {
    }

    public AuthorizationPacket(Long userId, String token) {
        this.userId = userId;
        this.token = token;
    }

    public Long getUserId() {
        return userId;
    }

    public String getToken() {
        return token;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
