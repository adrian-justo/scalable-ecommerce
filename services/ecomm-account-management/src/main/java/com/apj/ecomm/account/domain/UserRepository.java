package com.apj.ecomm.account.domain;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

interface UserRepository extends JpaRepository<User, UUID> {

	Optional<User> findByUsernameAndActiveTrue(String username);

	Optional<User> findByEmailAndActiveTrueOrMobileNoAndActiveTrue(String email, String mobileNo);

	Optional<User> findByUsernameOrEmailOrMobileNo(String username, String email, String mobileNo);

	boolean existsByUsernameAndActiveTrue(String username);

	boolean existsByEmailOrMobileNo(String email, String mobileNo);

	boolean existsByEmailAndActiveTrueOrMobileNoAndActiveTrue(String email, String mobileNo);

	default Page<User> findAll(int pageNo, int size) {
		Pageable pageable = PageRequest.of(pageNo - 1, size, Sort.by("id").ascending());
		return findAll(pageable);
	}

}
