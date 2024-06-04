package pro.paulek.simplechat.repository.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pro.paulek.simplechat.domain.User;
import pro.paulek.simplechat.domain.security.Token;

import java.util.Collection;
import java.util.Optional;

@Repository
public interface TokenRepository extends JpaRepository<Token, Long> {

    Optional<Token> findByToken(String token);

    Optional<Token> findByUser(User user);

    Collection<Token> findAllByUser(User user);

    @Query(
            value = "SELECT * FROM token WHERE expired=0",
            nativeQuery = true
    )
    Collection<Token> findAllNotExpired();

    Boolean existsByToken(String token);

    Boolean existsByUser(User user);

}
