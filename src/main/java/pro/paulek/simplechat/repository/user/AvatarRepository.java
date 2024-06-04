package pro.paulek.simplechat.repository.user;

import org.springframework.data.jpa.repository.JpaRepository;
import pro.paulek.simplechat.domain.Avatar;

public interface AvatarRepository  extends JpaRepository<Avatar, Long> {
}
