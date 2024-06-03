package pro.paulek.simplechat.domain.auth.password;

import java.util.Objects;

public class ChangePasswordResponse {
    private boolean success;
    private String message;

    public ChangePasswordResponse() {
    }

    public ChangePasswordResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChangePasswordResponse that)) return false;
        return isSuccess() == that.isSuccess() && Objects.equals(getMessage(), that.getMessage());
    }

    @Override
    public int hashCode() {
        return Objects.hash(isSuccess(), getMessage());
    }
}
