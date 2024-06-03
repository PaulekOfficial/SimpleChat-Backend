package pro.paulek.simplechat.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.NOT_FOUND, reason = "Refresh token has not been found in database")
public class RefreshTokenNotFoundException extends RuntimeException {

    private String token;

    public RefreshTokenNotFoundException(String token) {
        super(String.format("Refresh token %s has not been found in database", token));
    }
}
