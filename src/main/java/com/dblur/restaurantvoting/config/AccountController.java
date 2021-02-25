package com.dblur.restaurantvoting.config;

import com.dblur.restaurantvoting.model.Role;
import com.dblur.restaurantvoting.model.User;
import com.dblur.restaurantvoting.repository.UserRepository;
import com.dblur.restaurantvoting.util.ValidationUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.rest.webmvc.RepositoryLinksResource;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.server.RepresentationModelProcessor;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;
import java.util.Set;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@RestController
@RequestMapping("/api/account")
@AllArgsConstructor
@Slf4j
public class AccountController implements RepresentationModelProcessor<RepositoryLinksResource> {

    private final UserRepository userRepository;

    @SuppressWarnings("uncheked")
    private static final RepresentationModelAssemblerSupport<User, EntityModel<User>> ASSEMBLER =
            new RepresentationModelAssemblerSupport<>(AccountController.class, (Class<EntityModel<User>>) (Class<?>) EntityModel.class) {
                @Override
                public EntityModel<User> toModel(User user) {
                    return EntityModel.of(user, linkTo(AccountController.class).withSelfRel());
                }
            };

    @GetMapping(produces = MediaTypes.HAL_JSON_VALUE)
    public EntityModel<User> getUser(@AuthenticationPrincipal AuthUser authUser) {
        log.info("get {}", authUser);
        return ASSEMBLER.toModel(authUser.getUser());
    }
    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete (@AuthenticationPrincipal AuthUser authUser) {
        log.info("delete {}", authUser);
        userRepository.delete(authUser.getUser());
    }

    @PostMapping(value = "/register", consumes = MediaTypes.HAL_JSON_VALUE)
    @ResponseStatus(value = HttpStatus.CREATED)
    public ResponseEntity<EntityModel<User>> registerUser(@Valid @RequestBody User user) {
        log.info("register {}", user);
        ValidationUtil.checkNew(user);
        user.setRoles(Set.of(Role.USER));
        user = userRepository.save(user);
        URI uriOfNewResource = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("api/account")
                .build().toUri();
        return ResponseEntity.created(uriOfNewResource).body(ASSEMBLER.toModel(user));
    }

    @PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateUser(@Valid @RequestBody User user, @AuthenticationPrincipal AuthUser authUser) {
        User oldUser = authUser.getUser();
        ValidationUtil.assureIdConsistent(user, oldUser.id());
        user.setRoles(oldUser.getRoles());
        if (user.getPassword() == null) {
            user.setPassword(oldUser.getPassword());
        }
        userRepository.save(user);
    }

    @GetMapping(value = "/pageDemo", produces = MediaTypes.HAL_JSON_VALUE)
    public PagedModel<EntityModel<User>> pageDemo(Pageable page, PagedResourcesAssembler<User> pagedAssembler) {
        Page<User> users = userRepository.findAll(page);
        return pagedAssembler.toModel(users, ASSEMBLER);
    }

    @Override
    public RepositoryLinksResource process(RepositoryLinksResource model) {
        model.add(linkTo(AccountController.class).withRel("account"));
        return model;
    }
}
