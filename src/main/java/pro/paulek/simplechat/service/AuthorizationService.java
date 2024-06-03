package pro.paulek.simplechat.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Service;
import pro.paulek.simplechat.domain.auth.*;
import pro.paulek.simplechat.domain.security.RefreshToken;
import pro.paulek.simplechat.domain.security.Token;
import pro.paulek.simplechat.domain.user.User;
import pro.paulek.simplechat.domain.user.UserCredentials;
import pro.paulek.simplechat.domain.user.UserInformation;
import pro.paulek.simplechat.exceptions.NoTokenPresentException;
import pro.paulek.simplechat.exceptions.RefreshTokenNotFoundException;
import pro.paulek.simplechat.exceptions.TokenInvalidException;
import pro.paulek.simplechat.exceptions.TokenRevokedException;
import pro.paulek.simplechat.repository.auth.RefreshTokenRepository;
import pro.paulek.simplechat.repository.auth.TokenRepository;
import pro.paulek.simplechat.repository.user.RoleRepository;
import pro.paulek.simplechat.repository.user.UserCredentialsRepository;
import pro.paulek.simplechat.repository.user.UserInformationRepository;
import pro.paulek.simplechat.repository.user.UserRepository;
import pro.paulek.simplechat.service.security.JwtService;
import pro.paulek.simplechat.service.security.UserDetailsImpl;
import pro.paulek.simplechat.service.security.UserDetailsServiceImpl;

import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AuthorizationService {
    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    UserCredentialsRepository userCredentialsRepository;

    @Autowired
    UserInformationRepository userInformationRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder encoder;

    @Autowired
    JwtService jwtService;

    @Autowired
    TokenRepository tokenRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response, LogoutRequest logoutRequest) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            new SecurityContextLogoutHandler().logout(request, response, authentication);
        }

        String jwt = jwtService.parseJwt(request);
        UserDetails userDetails = userDetailsService.loadUserByUsername(jwtService.getUserNameFromJwtToken(jwt));
        if (!jwtService.isTokenValid(jwt, userDetails, request)) {
            throw new TokenInvalidException();
        }

        Optional<Token> preToken = tokenRepository.findByToken(jwt);
        if (preToken.isEmpty()) {
            throw new NoTokenPresentException();
        }

        Token token = preToken.get();

        if (!token.isRevoked()) {
            token.setRevoked(true);
            token.setRevokedTime(ZonedDateTime.now());
        }

        if (!token.isExpired()) {
            token.setExpired(true);
            token.setExpiredTime(ZonedDateTime.now());
        }

        tokenRepository.save(token);

        Optional<RefreshToken> refreshTokenOptional = refreshTokenRepository.findByRefreshToken(logoutRequest.getRefreshToken());
        if (refreshTokenOptional.isEmpty()) {
            throw new RefreshTokenNotFoundException(logoutRequest.getRefreshToken());
        }

        RefreshToken oldRefreshToken = refreshTokenOptional.get();

        if (!oldRefreshToken.isRevoked()) {
            oldRefreshToken.setRevoked(true);
            oldRefreshToken.setRevokedTime(ZonedDateTime.now());
        }

        if (!oldRefreshToken.isExpired()) {
            oldRefreshToken.setExpired(true);
            oldRefreshToken.setExpiredTime(ZonedDateTime.now());
        }
        refreshTokenRepository.save(oldRefreshToken);

        return ResponseEntity.ok(new MessageResponse("User logged out successfully!"));
    }

    public ResponseEntity<?> login(HttpServletRequest request, LoginRequest loginRequest) {
        String login = loginRequest.getLogin().toLowerCase();

        if (loginRequest.getLogin().contains("@")) {
            Optional<User> optionalUser = userRepository.findByEmail(loginRequest.getLogin());
            if (optionalUser.isPresent()) {
                login = optionalUser.get().getNickname().toLowerCase();
            }
        }
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(login, loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtService.generateToken(authentication, request);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String refresh = jwtService.generateToken(userDetails, request, 10);
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        Optional<User> user = userRepository.findByNickname(login);

        if (user.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        tokenRepository.save(new Token(user.get().getCredentials(), jwt, ZonedDateTime.now()));
        refreshTokenRepository.save(new RefreshToken(user.get().getCredentials(), refresh, ZonedDateTime.now()));

        return ResponseEntity.ok(new JwtResponse(
                jwt,
                refresh,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                roles));
    }

    public ResponseEntity<?> refreshToken(HttpServletRequest request, RefreshRequest refreshRequest) {
        String token = jwtService.parseJwt(request);

        String username0 = jwtService.getUserNameFromJwtToken(token);
        String username = jwtService.getUserNameFromJwtToken(refreshRequest.getRefreshToken());

        if (username.isEmpty() || username0.isEmpty() || !username0.equals(username)) {
            return ResponseEntity.badRequest().build();
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        if (!jwtService.isTokenValid(refreshRequest.getRefreshToken(), userDetails, request)) {
            return ResponseEntity.badRequest().build();
        }

        Optional<User> user = userRepository.findByNickname(username);
        if (user.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Optional<Token> realToken = tokenRepository.findByToken(token);
        if (realToken.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        if (realToken.get().isRevoked()) {
            return ResponseEntity.badRequest().build();
        }

        UserDetailsImpl userDetailsImpl = UserDetailsImpl.build(user.get());
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        String jwt = jwtService.generateToken(userDetailsImpl, request);
        String newRefresh = jwtService.generateToken(userDetailsImpl, request);

        Token oldToken = realToken.get();
        if (oldToken.isRevoked()) {
            throw new TokenRevokedException();
        }
        oldToken.setRevoked(true);
        oldToken.setRevokedTime(ZonedDateTime.now());

        if (!oldToken.isExpired()) {
            oldToken.setExpired(true);
            oldToken.setExpiredTime(ZonedDateTime.now());
        }
        tokenRepository.save(oldToken);
        tokenRepository.save(new Token(user.get().getCredentials(), jwt, ZonedDateTime.now()));

        Optional<RefreshToken> refreshTokenOptional = refreshTokenRepository.findByRefreshToken(refreshRequest.getRefreshToken());
        if (refreshTokenOptional.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        RefreshToken oldRefreshToken = refreshTokenOptional.get();
        if (!oldRefreshToken.isRevoked()) {
            oldRefreshToken.setRevoked(true);
            oldRefreshToken.setRevokedTime(ZonedDateTime.now());
        }

        if (!oldRefreshToken.isExpired()) {
            oldRefreshToken.setExpired(true);
            oldRefreshToken.setExpiredTime(ZonedDateTime.now());
        }
        refreshTokenRepository.save(oldRefreshToken);
        refreshTokenRepository.save(new RefreshToken(user.get().getCredentials(), jwt, ZonedDateTime.now()));

        return ResponseEntity.ok(new JwtResponse(
                jwt,
                newRefresh,
                userDetailsImpl.getId(),
                userDetailsImpl.getUsername(),
                userDetailsImpl.getEmail(),
                roles));
    }

    public ResponseEntity<?> register(SignupRequest signUpRequest) {
        if (userRepository.existsByNickname(signUpRequest.getUsername().toLowerCase())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Username is already taken!"));
        }

        if (userRepository.existsByEmail(signUpRequest.getEmail())) {
            return ResponseEntity
                    .badRequest()
                    .body(new MessageResponse("Error: Email is already in use!"));
        }

        UserInformation userInformation = new UserInformation(
                signUpRequest.getFirstName(),
                signUpRequest.getLastName(),
                "",
                ZonedDateTime.now()
        );

        UserCredentials userCredentials = new UserCredentials(
                encoder.encode(signUpRequest.getPassword()),
                ZonedDateTime.now()
        );

        User user = new User(
                signUpRequest.getUsername().toLowerCase(),
                signUpRequest.getEmail(),
                userCredentials,
                userInformation
        );
        userRepository.save(user);
        userCredentialsRepository.save(userCredentials);
        userInformationRepository.save(userInformation);

        return ResponseEntity.ok(new MessageResponse("User " + signUpRequest.getUsername() + " has been registered!"));
    }

    public ResponseEntity<?> verify() {
        return ResponseEntity.ok(new MessageResponse("Token ok!"));
    }

    public void revokeAllAccess(UserCredentials userCredentials) {
        this.revokeAllTokens(userCredentials);
        this.revokeAllRefreshTokens(userCredentials);
    }

    public void revokeAllTokens(UserCredentials userCredentials) {
        Collection<Token> tokens = this.tokenRepository.findAllByUserCredentials(userCredentials);
        tokens.forEach(token -> {
            if (!token.isRevoked()) {
                token.setRevoked(true);
                token.setRevokedTime(ZonedDateTime.now());
            }

            if (!token.isExpired()) {
                token.setExpired(true);
                token.setExpiredTime(ZonedDateTime.now());
            }

            tokenRepository.save(token);
        });
    }

    public void revokeAllRefreshTokens(UserCredentials userCredentials) {
        Collection<RefreshToken> tokens = this.refreshTokenRepository.findAllByUserCredentials(userCredentials);
        tokens.forEach(token -> {
            if (!token.isRevoked()) {
                token.setRevoked(true);
                token.setRevokedTime(ZonedDateTime.now());
            }

            if (!token.isExpired()) {
                token.setExpired(true);
                token.setExpiredTime(ZonedDateTime.now());
            }

            refreshTokenRepository.save(token);
        });
    }
}
