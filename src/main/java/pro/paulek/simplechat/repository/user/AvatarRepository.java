package pro.paulek.simplechat.repository.user;

import org.springframework.data.jpa.repository.JpaRepository;
import pro.paulek.domain.user.Avatar;

public interface AvatarRepository  extends JpaRepository<Avatar, Long> {
}
