package com.apj.ecomm.account.domain;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.apj.ecomm.account.domain.model.Paged;
import com.apj.ecomm.account.domain.model.UpdateUserRequest;
import com.apj.ecomm.account.domain.model.UserResponse;
import com.apj.ecomm.account.web.exception.AlreadyRegisteredException;
import com.apj.ecomm.account.web.exception.ResourceNotFoundException;
import com.apj.ecomm.account.web.messaging.ShopNameUpdatedEvent;

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

	private final ApplicationEventPublisher eventPublisher;

	@Transactional(readOnly = true)
	public Paged<UserResponse> findAll(final Pageable pageable) {
		return new Paged<>(repository.findAll(pageable).map(mapper::toFullResponse));
	}

	@Transactional(readOnly = true)
	public UserResponse findByUsername(final String username) {
		return findByActive(username).map(mapper::toResponse).orElseThrow(ResourceNotFoundException::new);
	}

	public UserResponse update(final String username, final UpdateUserRequest request) {
		validateUpdate(request.email(), request.mobileNo());
		return findByActive(username).map(existing -> updateUser(existing, request))
			.orElseThrow(ResourceNotFoundException::new);
	}

	private void validateUpdate(final String email, final String mobileNo) {
		repository.findByEmailOrMobileNo(email, mobileNo).ifPresent(user -> {
			final var existing = new HashMap<String, List<String>>();
			if (email != null && email.equals(user.getEmail())) {
				existing.put("email", List.of(email));
			}
			if (mobileNo != null && mobileNo.equals(user.getMobileNo())) {
				existing.put("mobileNo", List.of(mobileNo));
			}
			throw new AlreadyRegisteredException(existing);
		});
	}

	private UserResponse updateUser(final User existing, final UpdateUserRequest request) {
		final var shopName = existing.getShopName();

		final var user = mapper.updateEntity(request, existing);
		user.setNotificationTypes(getValidatedTypes(user, user.getNotificationTypes()));
		if (request.password() != null) {
			user.setPassword(encoder.encode(request.password()));
		}

		final var updated = repository.save(user);
		publishEvent(shopName, updated, request.shopName());
		return mapper.toResponse(updated);
	}

	private void publishEvent(final String shopName, final User updated, final String updatedShopName) {
		final var response = mapper.toFullResponse(updated);
		if (updatedShopName != null && !updatedShopName.equals(shopName)) {
			eventPublisher.publishEvent(new ShopNameUpdatedEvent(response.id(), response.shopName()));
		}
	}

	public void deleteByUsername(final String username) {
		findByActive(username).ifPresentOrElse(user -> {
			user.setActive(false);
			repository.save(user);
		}, () -> {
			throw new ResourceNotFoundException();
		});
	}

	private Optional<User> findByActive(final String username) {
		return repository.findByUsername(username).filter(User::isActive);
	}

}
