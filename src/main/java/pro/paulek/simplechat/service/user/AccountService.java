package pro.paulek.simplechat.service.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Pageable;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import pro.paulek.simplechat.controller.AccountController;
import pro.paulek.simplechat.domain.Avatar;
import pro.paulek.simplechat.domain.User;
import pro.paulek.simplechat.domain.auth.password.ChangePasswordRequest;
import pro.paulek.simplechat.domain.auth.password.ChangePasswordResponse;
import pro.paulek.simplechat.repository.user.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

@Service("accountService")
public class AccountService {
    private final String PASSWORD_REGEX = "^(?=.*[A-Z])(?=.*[a-z])(?=.*[0-9])(?=.*[!@#$%^&*(),.?\":{}|<>]).*$";
    private final String BASE64_REGEX = "^(?:[A-Za-z0-9+/]{4})*(?:[A-Za-z0-9+/]{2}==|[A-Za-z0-9+/]{3}=)?$";
    private final Pattern BASE64_PATTERN = Pattern.compile(BASE64_REGEX);

    @Autowired
    private AvatarRepository avatarRepository;

    @Autowired
    private UserRepository repository;

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UserRepository userRepository;


    public AccountService(UserRepository repository, AvatarRepository avatarRepository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.avatarRepository = avatarRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public CollectionModel<EntityModel<User>> getUsers(Pageable pageable) {
        List<EntityModel<User>> users = repository.findAll(pageable).stream()
                .map(user -> EntityModel.of(user,
                        WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(AccountController.class).getUser(user.getId())).withSelfRel(),
                        WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(AccountController.class).getUsers(null)).withRel("/")
                ))
                .toList();

        return CollectionModel.of(users, WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(AccountController.class).getUsers(pageable)).withSelfRel());
    }

    public Optional<EntityModel<User>> getUser(Long id) {
        var userOptional = repository.findById(id);
        return userOptional.map(user -> EntityModel.of(user,
                WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(AccountController.class).getUser(user.getId())).withSelfRel(),
                WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(AccountController.class).getUsers(null)).withRel("/")
        ));
    }

    public CompletableFuture<ResponseEntity<EntityModel<ChangePasswordResponse>>> changePassword(Long userId, ChangePasswordRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            var optionalUser = repository.findById(userId);
            if (optionalUser.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            var credentials = optionalUser.get();
            if (!passwordEncoder.matches(request.getOldPassword(), credentials.getPassword())) {
               return ResponseEntity.ok().body(EntityModel.of(new ChangePasswordResponse(false, "Old password is incorrect"),
                       WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(AccountController.class).changeUserPassword(userId, request)).withSelfRel(),
                       WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(AccountController.class).getUser(userId)).withRel("/")
               ));
            }

            if (!request.getNewPassword().matches(PASSWORD_REGEX)) {
                return ResponseEntity.ok().body(EntityModel.of(new ChangePasswordResponse(false, "Password must contain at least one uppercase letter, one lowercase letter, one digit and one special character"),
                        WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(AccountController.class).changeUserPassword(userId, request)).withSelfRel(),
                        WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(AccountController.class).getUser(userId)).withRel("/")
                ));
            }

            credentials.setPassword(passwordEncoder.encode(request.getNewPassword()));
            userRepository.save(credentials);

            return ResponseEntity.ok(EntityModel.of(new ChangePasswordResponse(true, "Password changed successfully"),
                    WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(AccountController.class).changeUserPassword(userId, request)).withSelfRel(),
                    WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(AccountController.class).getUser(userId)).withRel("/")
            ));
        });
    }

    public CompletableFuture<Boolean> saveUserAvatar(User user, MultipartFile file) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                var base64Image = Base64.getEncoder().encodeToString(file.getBytes());
                var matcher = BASE64_PATTERN.matcher(base64Image);
                if (!matcher.matches()) {
                    return false;
                }

                var avatar = new Avatar(base64Image, file.getContentType(), 0, 0, user);
                avatarRepository.save(avatar);

                user.setAvatar(avatar);
                userRepository.save(user);

                return true;
            } catch (IOException e) {
                return false;
            }
        });
    }

    public CompletableFuture<Boolean> saveUserAvatar(Long userId, MultipartFile file) {
        return CompletableFuture.supplyAsync(() -> {
            var user = repository.findById(userId);
            if (user.isEmpty()) {
                return false;
            }

            return this.saveUserAvatar(user.get(), file).join();
        });
    }

    public CompletableFuture<Optional<Resource>> getUserAvatar(User user) {
        return CompletableFuture.supplyAsync(() -> {
            var avatar = user.getAvatar();
            if (avatar == null) {
                return Optional.empty();
            }

            var base64Image = avatar.getData();
            var bytes = Base64.getDecoder().decode(base64Image);
            Resource resource = new ByteArrayResource(bytes);
            return Optional.of(resource);
        });
    }

    public CompletableFuture<ResponseEntity<Resource>> getUserLatestAvatar(Long id) {
        return CompletableFuture.supplyAsync(() -> {
            var user = repository.findById(id);
            if (user.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            var resource = this.getUserAvatar(user.get()).join();
            return resource.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
        });
    }
}
