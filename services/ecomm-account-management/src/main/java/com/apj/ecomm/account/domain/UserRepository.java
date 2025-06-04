package com.apj.ecomm.account.domain;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

interface UserRepository extends JpaRepository<User, UUID> {

	Optional<User> findByUsernameAndActiveTrue(String username);

	Optional<User> findByEmailAndActiveTrueOrMobileNoAndActiveTrue(String email, String mobileNo);

	Optional<User> findByUsernameOrEmailOrMobileNo(String username, String email, String mobileNo);

	boolean existsByUsernameAndActiveTrue(String username);

	boolean existsByEmailOrMobileNo(String email, String mobileNo);

	boolean existsByEmailAndActiveTrueOrMobileNoAndActiveTrue(String email, String mobileNo);

}
