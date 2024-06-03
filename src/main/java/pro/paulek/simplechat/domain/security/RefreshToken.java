package pro.paulek.simplechat.domain.security;

import jakarta.annotation.Nullable;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import pro.paulek.domain.user.UserCredentials;

import java.time.ZonedDateTime;
import java.util.Objects;

@Entity
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserCredentials userCredentials;

    @NotNull
    @Column(length = 500, unique = true)
    private String refreshToken;

    private boolean expired;

    @Nullable
    private ZonedDateTime expiredTime;

    private boolean revoked;

    @Nullable
    private ZonedDateTime revokedTime;

    private ZonedDateTime timestamp;

    public RefreshToken() {
    }

    public RefreshToken(UserCredentials userCredentials, @NotNull String refreshToken, ZonedDateTime timestamp) {
        this.userCredentials = userCredentials;
        this.refreshToken = refreshToken;
        this.timestamp = timestamp;
    }

    public RefreshToken(Long id, UserCredentials userCredentials, @NotNull String refreshToken, boolean expired, @Nullable ZonedDateTime expiredTime, boolean revoked, @Nullable ZonedDateTime revokedTime, ZonedDateTime timestamp) {
        this.id = id;
        this.userCredentials = userCredentials;
        this.refreshToken = refreshToken;
        this.expired = expired;
        this.expiredTime = expiredTime;
        this.revoked = revoked;
        this.revokedTime = revokedTime;
        this.timestamp = timestamp;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UserCredentials getUser() {
        return userCredentials;
    }

    public void setUser(UserCredentials userCredentials) {
        this.userCredentials = userCredentials;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String token) {
        this.refreshToken = token;
    }

    public boolean isExpired() {
        return expired;
    }

    public void setExpired(boolean expired) {
        this.expired = expired;
    }

    @Nullable
    public ZonedDateTime getExpiredTime() {
        return expiredTime;
    }

    public void setExpiredTime(@Nullable ZonedDateTime expiredTime) {
        this.expiredTime = expiredTime;
    }

    public boolean isRevoked() {
        return revoked;
    }

    public void setRevoked(boolean revoked) {
        this.revoked = revoked;
    }

    @Nullable
    public ZonedDateTime getRevokedTime() {
        return revokedTime;
    }

    public void setRevokedTime(@Nullable ZonedDateTime revokedTime) {
        this.revokedTime = revokedTime;
    }

    public ZonedDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(ZonedDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RefreshToken token1)) return false;
        return expired == token1.expired && revoked == token1.revoked && Objects.equals(id, token1.id) && Objects.equals(userCredentials, token1.userCredentials) && Objects.equals(refreshToken, token1.refreshToken) && Objects.equals(expiredTime, token1.expiredTime) && Objects.equals(revokedTime, token1.revokedTime) && Objects.equals(timestamp, token1.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userCredentials, refreshToken, expired, expiredTime, revoked, revokedTime, timestamp);
    }

    @Override
    public String toString() {
        return "Token{" +
                "id=" + id +
                ", user=" + userCredentials +
                ", token='" + refreshToken + '\'' +
                ", expired=" + expired +
                ", expiredTime=" + expiredTime +
                ", revoked=" + revoked +
                ", revokedTime=" + revokedTime +
                ", timestamp=" + timestamp +
                '}';
    }
}
