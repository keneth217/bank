package com.banking.app.repository;

import com.banking.app.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {
    Boolean existsByEmail(String email);
    Boolean existsByAccountNumber(String accountNumber);
    User findByAccountNumber(String accountNumber);


    Optional<User> findByEmail(String email);
    List<User> findUserByFirstNameContainingIgnoreCase(String firstname);
List<User> findUserByFirstNameContaining(String firstname);

    void deleteByAccountNumber(String accountNumber);
}
