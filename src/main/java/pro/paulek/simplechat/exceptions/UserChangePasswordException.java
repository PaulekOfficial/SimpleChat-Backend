package pro.paulek.simplechat.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.NOT_ACCEPTABLE, reason = "User change password exception")
public class UserChangePasswordException extends RuntimeException {
        public UserChangePasswordException() {
            super("User change password exception");
        }
}
