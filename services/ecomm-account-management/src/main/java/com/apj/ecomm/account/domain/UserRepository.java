package com.apj.ecomm.account.domain;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

interface UserRepository extends JpaRepository<User, UUID> {

	Optional<User> findByUsername(String username);

	Optional<User> findByUsernameOrEmailOrMobileNo(String username, String email, String mobileNo);

	boolean existsByEmailOrMobileNo(String email, String mobileNo);

}
