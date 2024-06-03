package pro.paulek.simplechat.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.NO_CONTENT, reason = "No token has been given in the request")
public class NoTokenPresentException extends RuntimeException {

    public NoTokenPresentException() {
        super("No token has been given in the request");
    }
}
