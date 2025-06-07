package com.apj.ecomm.account.domain;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
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
	private final PasswordEncoder encoder;

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
		request.validate();
		if (repository.existsByEmailOrMobileNo(request.email(), request.mobileNo())) {
			throw new AlreadyRegisteredException();
		}
		return findBy(username).map(existing -> update(mapper.updateEntity(request, existing), request));
	}

	private UserResponse update(User updated, UpdateUserRequest request) {
		updated.setNotificationTypes(getValidatedTypes(updated));
		if (request.password() != null) {
			updated.setPassword(encoder.encode(request.password()));
		}
		return mapper.toResponse(repository.save(updated));
	}

	Set<NotificationType> getValidatedTypes(User updated) {
		Set<NotificationType> types = new HashSet<>(updated.getNotificationTypes());
		if (StringUtils.isBlank(updated.getEmail()) && StringUtils.isNotBlank(updated.getMobileNo())) {
			types.remove(NotificationType.EMAIL);
			types.add(NotificationType.SMS);
		} else if (StringUtils.isBlank(updated.getMobileNo())) {
			types.remove(NotificationType.SMS);
			types.add(NotificationType.EMAIL);
		}
		return types;
	}

	public void deleteByUsername(String username) {
		findBy(username).ifPresent(user -> {
			user.setActive(false);
			repository.save(user);
		});
	}

	private Optional<User> findBy(String username) {
		return Optional
				.of(repository.findByUsername(username).filter(User::isActive).orElseThrow(UserNotFoundException::new));
	}

}
