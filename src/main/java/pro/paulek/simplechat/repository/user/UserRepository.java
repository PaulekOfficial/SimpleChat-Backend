package pro.paulek.simplechat.repository.user;

import org.springframework.data.jpa.repository.JpaRepository;
import pro.paulek.simplechat.domain.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByNickname(String nickname);

    Optional<User> findByEmail(String email);

    Boolean existsByEmail(String email);

    Boolean existsByNickname(String nickname);
}
