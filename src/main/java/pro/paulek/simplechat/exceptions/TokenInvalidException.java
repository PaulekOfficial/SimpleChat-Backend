package pro.paulek.simplechat.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.NOT_ACCEPTABLE, reason = "The given token is invalid, try with a valid token")
public class TokenInvalidException extends RuntimeException {

    public TokenInvalidException() {
        super("The given token is invalid, try with a valid token");
    }
}
