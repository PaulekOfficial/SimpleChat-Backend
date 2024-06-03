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
import pro.paulek.simplechat.domain.auth.password.ChangePasswordRequest;
import pro.paulek.simplechat.domain.auth.password.ChangePasswordResponse;
import pro.paulek.simplechat.domain.user.Avatar;
import pro.paulek.simplechat.domain.user.User;
import pro.paulek.simplechat.domain.user.response.SimpleUser;
import pro.paulek.simplechat.domain.user.response.UserInformationResponse;
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
    private UserInformationRepository userInformationRepository;

    @Autowired
    private UserCredentialsRepository userCredentialsRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository repository;

    @Autowired
    private PasswordEncoder passwordEncoder;


    public AccountService(UserInformationRepository userInformationRepository, UserCredentialsRepository userCredentialsRepository, RoleRepository roleRepository, UserRepository repository, AvatarRepository avatarRepository, PasswordEncoder passwordEncoder) {
        this.userInformationRepository = userInformationRepository;
        this.userCredentialsRepository = userCredentialsRepository;
        this.roleRepository = roleRepository;
        this.repository = repository;
        this.avatarRepository = avatarRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public CollectionModel<EntityModel<SimpleUser>> getUsers(Pageable pageable) {
        List<EntityModel<SimpleUser>> users = repository.findAll(pageable).stream()
                .map(user -> EntityModel.of(new SimpleUser(user),
                        WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(AccountController.class).getUser(user.getId())).withSelfRel(),
                        WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(AccountController.class).getUsers(null)).withRel("/")
                ))
                .toList();

        return CollectionModel.of(users, WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(AccountController.class).getUsers(pageable)).withSelfRel());
    }

    public Optional<EntityModel<SimpleUser>> getUser(Long id) {
        var userOptional = repository.findById(id);
        return userOptional.map(user -> EntityModel.of(new SimpleUser(user),
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

            var credentials = optionalUser.get().getCredentials();
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
            userCredentialsRepository.save(credentials);

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

                var information = user.getInformation();
                var avatar = new Avatar(base64Image, file.getContentType(), 0, 0, user);
                avatarRepository.save(avatar);
                information.addAvatar(avatar);
                userInformationRepository.save(information);

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
            var avatar = user.getInformation().getLastAvatar();
            if (avatar == null) {
                return Optional.empty();
            }

            var base64Image = avatar.getData();
            var bytes = Base64.getDecoder().decode(base64Image);
            Resource resource = new ByteArrayResource(bytes);
            return Optional.of(resource);
        });
    }

    public CompletableFuture<EntityModel<UserInformationResponse>> getUserInformation(Long id) {
        return CompletableFuture.supplyAsync(() -> {
            var user = repository.findById(id);
            if (user.isEmpty()) {
                return null;
            }

            var information = user.get().getInformation();
            var response = new UserInformationResponse(
                    user.get().getId(),
                    user.get().getNickname(),
                    user.get().getEmail(),
                    information.getFirstName(),
                    information.getLastName(),
                    information.getDescription(),
                    information.getCountry(),
                    information.getVoivodeship(),
                    information.getTelephonePrefix(),
                    information.getTelephoneNumber(),
                    information.getBirthDate(),
                    information.getAddresses(),
                    information.getCreateTimestamp()
            );

            return EntityModel.of(
                    response,
                    WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(AccountController.class).getUserInformation(response.getId())).withSelfRel(),
                    WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(AccountController.class).getUsersInformation()).withRel("/details")
            );
        });
    }

    public CompletableFuture<ResponseEntity<?>> updateUserInformation(Long id, UserInformationResponse userInformationResponse) {
        return CompletableFuture.supplyAsync(() -> {
            var user = repository.findById(id);
            if (user.isEmpty()) {
                return null;
            }

            var userData = user.get();
            if (userInformationResponse.getUsername() != null) {
                //Validate if other user has this nickname
                var userWithNickname = repository.findByNickname(userInformationResponse.getUsername());
                if (userWithNickname.isPresent() && !Objects.equals(userWithNickname.get().getId(), id)) {
                    return ResponseEntity.badRequest().body("Nickname is already taken");
                }

                userData.setNickname(userInformationResponse.getUsername());
            }
            if (userInformationResponse.getEmail() != null) {
                //Validate if other user has this email
                var userWithEmail = repository.findByEmail(userInformationResponse.getEmail());
                if (userWithEmail.isPresent() && !Objects.equals(userWithEmail.get().getId(), id)) {
                    return ResponseEntity.badRequest().body("Email is already taken");
                }

                userData.setEmail(userInformationResponse.getEmail());
            }

            var information = user.get().getInformation();
            if (userInformationResponse.getFirstName() != null) {
                information.setFirstName(userInformationResponse.getFirstName());
            }

            if (userInformationResponse.getLastName() != null) {
                information.setLastName(userInformationResponse.getLastName());
            }

            if (userInformationResponse.getDescription() != null) {
                information.setDescription(userInformationResponse.getDescription());
            }
            userInformationRepository.save(information);

            return ResponseEntity.ok("User information updated successfully");
        });
    }

    public CompletableFuture<CollectionModel<EntityModel<UserInformationResponse>>> getUsersInformation() {
        return CompletableFuture.supplyAsync(() -> {
            List<EntityModel<UserInformationResponse>> usersInformation = new ArrayList<>();

            for (User user : repository.findAll()) {
                var information = user.getInformation();
                var response = new UserInformationResponse(
                        user.getId(),
                        user.getNickname(),
                        user.getEmail(),
                        information.getFirstName(),
                        information.getLastName(),
                        information.getDescription(),
                        information.getCountry(),
                        information.getVoivodeship(),
                        information.getTelephonePrefix(),
                        information.getTelephoneNumber(),
                        information.getBirthDate(),
                        information.getAddresses(),
                        information.getCreateTimestamp()
                );

                usersInformation.add(EntityModel.of(
                        response,
                        WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(AccountController.class).getUserInformation(response.getId())).withSelfRel(),
                        WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(AccountController.class).getUsersInformation()).withRel("/details")
                ));
            }

            return CollectionModel.of(
                    usersInformation,
                    WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(AccountController.class).getUsersInformation()).withSelfRel());
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
