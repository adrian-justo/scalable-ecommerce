package com.apj.ecomm.account.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
import com.apj.ecomm.account.web.exception.EmailSmsMissingException;
import com.apj.ecomm.account.web.exception.RoleMissingException;
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
		sanitize(request);
		return findBy(username).map(existing -> mapper.toResponse(update(request, existing)));
	}

	private void sanitize(UpdateUserRequest request) {
		if (request.email() != null && request.email().isBlank() && request.mobileNo() != null
				&& request.mobileNo().isBlank()) {
			throw new EmailSmsMissingException();
		}
		if (request.roles() != null && request.roles().isEmpty()) {
			throw new RoleMissingException();
		}
		if (repository.existsByEmailOrMobileNo(request.email(), request.mobileNo())) {
			throw new AlreadyRegisteredException();
		}
	}

	private User update(UpdateUserRequest updated, User existing) {
		if (updated.password() != null) {
			existing.setPassword(encoder.encode(updated.password()));
		}
		existing.setNotificationTypes(getUpdatedNotificationTypes(updated));
		return repository.save(mapper.updateEntity(updated, existing));
	}

	private List<NotificationType> getUpdatedNotificationTypes(UpdateUserRequest updated) {
		List<NotificationType> types = new ArrayList<>();
		if (StringUtils.isNotBlank(updated.email())) {
			types.add(NotificationType.EMAIL);
		}
		if (StringUtils.isNotBlank(updated.mobileNo())) {
			types.add(NotificationType.SMS);
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
