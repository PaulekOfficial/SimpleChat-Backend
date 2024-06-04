package pro.paulek.simplechat.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import pro.paulek.simplechat.domain.User;
import pro.paulek.simplechat.domain.auth.password.ChangePasswordRequest;
import pro.paulek.simplechat.domain.auth.password.ChangePasswordResponse;
import pro.paulek.simplechat.exceptions.UserChangePasswordException;
import pro.paulek.simplechat.service.user.AccountService;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

@RestController()
@CrossOrigin()
@RequestMapping("/api/v1/accounts")
public class AccountController {
    @Autowired
    private AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = Objects.requireNonNull(accountService);
    }

    @GetMapping("/")
    @Transactional
    public CollectionModel<EntityModel<User>> getUsers(Pageable pageable) {
        return accountService.getUsers(pageable);
    }

    @GetMapping("/{id}")
    @Transactional
    public ResponseEntity<EntityModel<User>> getUser(@PathVariable Long id) {
        var user = accountService.getUser(id);
        return user.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping("/{id}/change-password")
    @Transactional
    public ResponseEntity<EntityModel<ChangePasswordResponse>> changeUserPassword(@PathVariable Long id, @RequestBody ChangePasswordRequest request) {
        return accountService.changePassword(id, request).whenComplete((response, throwable) -> {
            if (throwable != null) {
                throw new UserChangePasswordException();
            }
        }).join();
    }

    @GetMapping("/{id}/avatar")
    public ResponseEntity<Resource> getUserAvatar(@PathVariable Long id) {
        try {
            return accountService.getUserLatestAvatar(id).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/{id}/avatar")
    private ResponseEntity<?> saveUserAvatar(@RequestParam("files") MultipartFile multipartFile, Model model, @PathVariable Long id) throws IOException {
        var future = accountService.saveUserAvatar(id, multipartFile);

        if (future.join()) {
            return ResponseEntity.ok(null);
        }

        return ResponseEntity.badRequest().body("Invalid file");
    }
}
