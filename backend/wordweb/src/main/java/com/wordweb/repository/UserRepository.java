package com.wordweb.repository;

import com.wordweb.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByUserNameAndUserBirth(String userName, String userBirth);

    Optional<User> findByUserNameAndEmail(String userName, String email);
}
