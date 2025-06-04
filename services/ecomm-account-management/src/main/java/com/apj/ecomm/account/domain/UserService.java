package com.apj.ecomm.account.domain;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.apj.ecomm.account.domain.model.UpdateUserRequest;
import com.apj.ecomm.account.domain.model.UserResponse;
import com.apj.ecomm.account.web.exception.AlreadyRegisteredException;
import com.apj.ecomm.account.web.exception.UserNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
class UserService implements IUserService {

	private final UserRepository repository;
	private final UserMapper mapper;

	@Transactional(readOnly = true)
	public List<UserResponse> findAll(int pageNo, int size) {
		Pageable pageable = PageRequest.of(pageNo - 1, size, Sort.by("id").ascending());
		return repository.findAll(pageable).stream().map(mapper::toResponse).toList();
	}

	@Transactional(readOnly = true)
	public Optional<UserResponse> findByUsername(String username) {
		return findBy(username).map(mapper::toResponse);
	}

	public Optional<UserResponse> update(String username, UpdateUserRequest request) {
		if (repository.existsByEmailOrMobileNo(request.email(), request.mobileNo())) {
			throw new AlreadyRegisteredException();
		}
		return findBy(username).map(user -> mapper.toResponse(repository.save(mapper.updateEntity(request, user))));
	}

	public void deleteByUsername(String username) {
		findBy(username).ifPresent(user -> {
			user.setActive(false);
			repository.save(user);
		});
	}

	private Optional<User> findBy(String username) {
		return Optional.of(repository.findByUsernameAndActiveTrue(username).orElseThrow(UserNotFoundException::new));
	}

}
