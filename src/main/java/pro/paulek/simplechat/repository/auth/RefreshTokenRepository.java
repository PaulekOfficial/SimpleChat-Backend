package pro.paulek.simplechat.repository.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pro.paulek.simplechat.domain.security.RefreshToken;
import pro.paulek.simplechat.domain.user.UserCredentials;

import java.util.Collection;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByRefreshToken(String token);

    Optional<RefreshToken> findByUserCredentials(UserCredentials userCredentials);

    Collection<RefreshToken> findAllByUserCredentials(UserCredentials userCredentials);

    @Query(
            value = "SELECT * FROM refresh_token WHERE expired=0",
            nativeQuery = true
    )
    Collection<RefreshToken> findAllNotExpired();

    Boolean existsByRefreshToken(String token);

    Boolean existsByUserCredentials(UserCredentials userCredentials);

}
