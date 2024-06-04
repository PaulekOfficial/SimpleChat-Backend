package pro.paulek.simplechat.repository.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pro.paulek.simplechat.domain.User;
import pro.paulek.simplechat.domain.security.RefreshToken;

import java.util.Collection;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByRefreshToken(String token);

    Optional<RefreshToken> findByUser(User user);

    Collection<RefreshToken> findAllByUser(User user);

    @Query(
            value = "SELECT * FROM refresh_token WHERE expired=0",
            nativeQuery = true
    )
    Collection<RefreshToken> findAllNotExpired();

    Boolean existsByRefreshToken(String token);

    Boolean existsByUser(User user);

}
