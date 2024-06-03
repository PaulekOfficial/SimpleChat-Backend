package pro.paulek.simplechat.service.security;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import pro.paulek.domain.user.User;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class UserDetailsImpl implements UserDetails {
    private static final long serialVersionUID = 1L;

    private final Long id;
    private final String nickname;
    private final String email;
    private final boolean active;
    private final boolean locked;
    private final ZonedDateTime accountExpireDate;
    private final ZonedDateTime credentialsExpireDate;
    private final Collection<? extends GrantedAuthority> authorities;
    @JsonIgnore
    private String password;

    public UserDetailsImpl(Long id, String nickname, String email, boolean active, boolean locked, ZonedDateTime accountExpireDate, ZonedDateTime credentialsExpireDate, String password, Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.nickname = nickname;
        this.email = email;
        this.active = active;
        this.locked = locked;
        this.accountExpireDate = accountExpireDate;
        this.credentialsExpireDate = credentialsExpireDate;
        this.password = password;
        this.authorities = authorities;
    }

    public static UserDetailsImpl build(User user) {
        List<GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName().name()))
                .collect(Collectors.toList());

        return new UserDetailsImpl(
                user.getId(),
                user.getNickname(),
                user.getEmail(),
                user.getCredentials().isActive(),
                user.getCredentials().isLocked(),
                user.getCredentials().getAccountExpireDate(),
                user.getCredentials().getCredentialsExpireDate(),
                user.getCredentials().getPassword(),
                authorities);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return nickname;
    }

    @Override
    public boolean isAccountNonExpired() {
        return accountExpireDate == null || ZonedDateTime.now().isAfter(accountExpireDate);
    }

    @Override
    public boolean isAccountNonLocked() {
        return !locked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return credentialsExpireDate == null || ZonedDateTime.now().isAfter(credentialsExpireDate);
    }

    @Override
    public boolean isEnabled() {
        return active;
    }

}
