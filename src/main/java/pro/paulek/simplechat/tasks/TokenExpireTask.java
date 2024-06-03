package pro.paulek.simplechat.tasks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pro.paulek.simplechat.repository.auth.RefreshTokenRepository;
import pro.paulek.simplechat.repository.auth.TokenRepository;
import pro.paulek.simplechat.service.security.JwtService;

import java.time.ZonedDateTime;
import java.util.concurrent.TimeUnit;

@EnableAsync
@Component
public class TokenExpireTask {

    private static final Logger log = LoggerFactory.getLogger(TokenExpireTask.class);

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private JwtService jwtService;

    @Async
    @Scheduled(fixedRate = 5, timeUnit = TimeUnit.MINUTES)
    public void checkForExpiredTokens() {
        log.info("Checking for expired authorisation tokens");
        this.tokenRepository.findAllNotExpired().forEach(token -> {
            if (!token.isExpired() && jwtService.isTokenExpired(token.getToken())) {
                token.setExpired(true);
                token.setExpiredTime(ZonedDateTime.now());
                this.tokenRepository.save(token);

                log.info("Token id: {}, has been expired", token.getId());
            }
        });
    }

    @Async
    @Scheduled(fixedRate = 5, timeUnit = TimeUnit.MINUTES)
    public void checkForExpiredRefreshTokens() {
        log.info("Checking for expired refresh authorisation tokens");
        this.refreshTokenRepository.findAllNotExpired().forEach(refreshToken -> {
            if (!refreshToken.isExpired() && jwtService.isTokenExpired(refreshToken.getRefreshToken())) {
                refreshToken.setExpired(true);
                refreshToken.setExpiredTime(ZonedDateTime.now());

                this.refreshTokenRepository.save(refreshToken);

                log.info("Refresh id: {} , has been expired", refreshToken.getId());
            }
        });
    }
}
