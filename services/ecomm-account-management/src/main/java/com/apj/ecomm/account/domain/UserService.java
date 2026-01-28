package com.apj.ecomm.account.domain;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.apj.ecomm.account.domain.model.Paged;
import com.apj.ecomm.account.domain.model.UpdateUserRequest;
import com.apj.ecomm.account.domain.model.UserResponse;
import com.apj.ecomm.account.web.client.AccountClient;
import com.apj.ecomm.account.web.exception.ActiveOrderExistsException;
import com.apj.ecomm.account.web.exception.AlreadyRegisteredException;
import com.apj.ecomm.account.web.exception.ResourceNotFoundException;
import com.apj.ecomm.account.web.messaging.product.ShopNameUpdatedEvent;
import com.apj.ecomm.account.web.messaging.product.ShopStatusUpdatedEvent;

import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@Observed(name = "service.user")
@RequiredArgsConstructor
class UserService implements IUserService {

	private final UserRepository repository;

	private final AccountClient client;

	private final ApplicationEventPublisher eventPublisher;

	private final PasswordEncoder encoder;

	private final PaymentProcessor processor;

	private final UserMapper mapper;

	@Transactional(readOnly = true)
	public Paged<UserResponse> findAll(final Pageable pageable) {
		return new Paged<>(repository.findAll(pageable).map(mapper::toResponseNoIdentifier));
	}

	@Transactional(readOnly = true)
	public UserResponse findByUsername(final String username, final String id) {
		final var user = findBy(username).orElseThrow(ResourceNotFoundException::new);
		if (!id.equals(user.getId().toString()))
			return mapper.toResponseNoIdentifier(user);
		if (user.isActive())
			return mapper.toResponse(user);
		throw new ResourceNotFoundException();
	}

	public UserResponse update(final String username, final UpdateUserRequest request) {
		validate(request.email(), request.mobileNo());
		return findByActive(username).map(existing -> update(existing, request))
			.orElseThrow(ResourceNotFoundException::new);
	}

	private void validate(final String email, final String mobileNo) {
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

	private UserResponse update(final User existing, final UpdateUserRequest request) {
		final var forDeactivation = forProductDeactivation(existing, request.roles());
		final var noExistingSellerRole = !existing.getRoles().contains(Role.SELLER);
		final var requestShopName = request.shopName();
		final var shopNameUpdated = requestShopName != null && !requestShopName.equals(existing.getShopName());

		final var updated = repository.save(mapper.updateEntity(request, existing, encoder));
		if (forDeactivation) {
			publishShopStatusUpdate(updated, Boolean.FALSE);
		}
		else {
			publishShopUpdates(noExistingSellerRole, updated, shopNameUpdated, requestShopName);
		}
		return mapper.toResponse(updated);
	}

	private void publishShopUpdates(final boolean noExistingSellerRole, final User updated,
			final boolean shopNameUpdated, final String requestShopName) {
		if (noExistingSellerRole && updated.getRoles().contains(Role.SELLER)
				&& processor.transferEnabledFor(updated.getAccountId())) {
			publishShopStatusUpdate(updated, Boolean.TRUE);
		}
		if (updated.getShopName() != null && shopNameUpdated) {
			final var shopId = mapper.toResponseNoIdentifier(updated).id();
			eventPublisher.publishEvent(new ShopNameUpdatedEvent(shopId, requestShopName));
		}
	}

	private boolean forProductDeactivation(final User existing, final Set<Role> roles) {
		return forProductDeactivation(existing, roles != null && !roles.contains(Role.SELLER));
	}

	public void deleteByUsername(final String username) {
		findByActive(username).ifPresentOrElse(this::deactivate, () -> {
			throw new ResourceNotFoundException();
		});
	}

	private void deactivate(final User user) {
		final var forPublish = forProductDeactivation(user);
		user.setActive(false);
		repository.save(user);
		if (forPublish) {
			publishShopStatusUpdate(user, Boolean.FALSE);
		}
	}

	private boolean forProductDeactivation(final User user) {
		return forProductDeactivation(user, true);
	}

	private boolean forProductDeactivation(final User user, final boolean otherCondition) {
		final var forDeactivation = user.getRoles().contains(Role.SELLER) && otherCondition;
		if (forDeactivation && client.activeOrderExists(user.getId().toString()))
			throw new ActiveOrderExistsException();
		return forDeactivation;
	}

	private void publishShopStatusUpdate(final User user, final Boolean active) {
		eventPublisher.publishEvent(new ShopStatusUpdatedEvent(user.getId().toString(), active));
	}

	private Optional<User> findByActive(final String username) {
		return findBy(username).filter(User::isActive);
	}

	private Optional<User> findBy(final String username) {
		return repository.findByUsername(username);
	}

	public Map<String, UserResponse> getDetails(final Set<String> ids) {
		return findAllBy(ids).stream()
			.filter(User::isActive)
			.collect(Collectors.toMap(user -> user.getId().toString(), mapper::toResponse));
	}

	public List<User> findAllBy(final Set<String> ids) {
		return repository.findAllById(ids.stream().map(UUID::fromString).toList());
	}

}
