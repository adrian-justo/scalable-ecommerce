package com.apj.ecomm.account.domain;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import io.micrometer.observation.annotation.Observed;

@Observed(name = "repository.user")
interface UserRepository extends JpaRepository<User, UUID> {

	Optional<User> findByUsername(String username);

	Optional<User> findByEmailOrMobileNo(String email, String mobileNo);

	Optional<User> findByUsernameOrEmailOrMobileNo(String username, String email, String mobileNo);

}
