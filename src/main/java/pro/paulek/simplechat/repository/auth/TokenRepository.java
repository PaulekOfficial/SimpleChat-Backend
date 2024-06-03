package pro.paulek.simplechat.repository.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pro.paulek.simplechat.domain.security.Token;
import pro.paulek.simplechat.domain.user.UserCredentials;

import java.util.Collection;
import java.util.Optional;

@Repository
public interface TokenRepository extends JpaRepository<Token, Long> {

    Optional<Token> findByToken(String token);

    Optional<Token> findByUserCredentials(UserCredentials userCredentials);

    Collection<Token> findAllByUserCredentials(UserCredentials userCredentials);

    @Query(
            value = "SELECT * FROM token WHERE expired=0",
            nativeQuery = true
    )
    Collection<Token> findAllNotExpired();

    Boolean existsByToken(String token);

    Boolean existsByUserCredentials(UserCredentials userCredentials);

}
