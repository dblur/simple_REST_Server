package com.dblur.restaurantvoting.repository;

import com.dblur.restaurantvoting.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RestResource;

public interface UserRepository extends JpaRepository<User, Long> {

    @RestResource(rel = "by-lastname", path = "by-lastname")
    Page<User> findByLastnameIgnoreCase(String lastname, Pageable page);

    @RestResource(rel = "by-email", path = "by-email")
    User findByEmailIgnoreCase(String email);
}
