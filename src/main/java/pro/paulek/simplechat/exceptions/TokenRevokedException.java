package pro.paulek.simplechat.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.NOT_ACCEPTABLE, reason = "Given token is revoked")
public class TokenRevokedException extends RuntimeException {

    public TokenRevokedException() {
        super("Given token is revoked");
    }
}
