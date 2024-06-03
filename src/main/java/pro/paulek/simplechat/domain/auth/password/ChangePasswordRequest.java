package pro.paulek.simplechat.domain.auth.password;

import jakarta.validation.constraints.NotBlank;

import java.util.Objects;

public class ChangePasswordRequest {
    @NotBlank
    private String oldPassword;

    @NotBlank
    private String newPassword;

    public ChangePasswordRequest() {
    }

    public ChangePasswordRequest(String oldPassword, String newPassword) {
        this.oldPassword = oldPassword;
        this.newPassword = newPassword;
    }

    public @NotBlank String getOldPassword() {
        return oldPassword;
    }

    public void setOldPassword(@NotBlank String oldPassword) {
        this.oldPassword = oldPassword;
    }

    public @NotBlank String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(@NotBlank String newPassword) {
        this.newPassword = newPassword;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChangePasswordRequest that)) return false;
        return Objects.equals(getOldPassword(), that.getOldPassword()) && Objects.equals(getNewPassword(), that.getNewPassword());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getOldPassword(), getNewPassword());
    }
}
