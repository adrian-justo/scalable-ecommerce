package com.apj.ecomm.account.domain;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.apj.ecomm.account.domain.model.UpdateUserRequest;
import com.apj.ecomm.account.domain.model.UserResponse;
import com.apj.ecomm.account.web.exception.AlreadyRegisteredException;
import com.apj.ecomm.account.web.exception.ResourceNotFoundException;

import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@Observed(name = "service.user")
@RequiredArgsConstructor
class UserService implements IUserService {

	private final UserRepository repository;
	private final UserMapper mapper;
	private final PasswordEncoder encoder;

	@Transactional(readOnly = true)
	public List<UserResponse> findAll(Pageable pageable) {
		return repository.findAll(pageable).stream().map(mapper::toResponse).toList();
	}

	@Transactional(readOnly = true)
	public UserResponse findByUsername(String username) {
		return findByActive(username).map(mapper::toResponse).orElseThrow(ResourceNotFoundException::new);
	}

	public UserResponse update(String username, UpdateUserRequest request) {
		validateUpdate(request.email(), request.mobileNo());
		return findByActive(username).map(existing -> updateUser(mapper.updateEntity(request, existing), request))
				.orElseThrow(ResourceNotFoundException::new);
	}

	private void validateUpdate(String email, String mobileNo) {
		repository.findByEmailOrMobileNo(email, mobileNo).ifPresent(user -> {
			Map<String, List<String>> existing = new HashMap<>();
			if (email != null && email.equals(user.getEmail())) {
				existing.put("email", List.of(email));
			}
			if (mobileNo != null && mobileNo.equals(user.getMobileNo())) {
				existing.put("mobileNo", List.of(mobileNo));
			}
			throw new AlreadyRegisteredException(existing);
		});
	}

	private UserResponse updateUser(User updated, UpdateUserRequest request) {
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
		findByActive(username).ifPresentOrElse(user -> {
			user.setActive(false);
			repository.save(user);
		}, () -> {
			throw new ResourceNotFoundException();
		});
	}

	private Optional<User> findByActive(String username) {
		return repository.findByUsername(username).filter(User::isActive);
	}

}
