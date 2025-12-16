package com.apj.ecomm.account.domain;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.apj.ecomm.account.domain.model.CreateUserRequest;
import com.apj.ecomm.account.domain.model.LoginRequest;
import com.apj.ecomm.account.domain.model.UserResponse;
import com.apj.ecomm.account.web.exception.AlreadyRegisteredException;
import com.apj.ecomm.account.web.messaging.cart.CreateCartEvent;
import com.apj.ecomm.account.web.messaging.product.ShopNameUpdatedEvent;
import com.apj.ecomm.account.web.messaging.product.ShopStatusUpdatedEvent;

import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@Observed(name = "service.auth")
@RequiredArgsConstructor
class AuthService implements IAuthService {

	private final UserRepository repository;

	private final PasswordEncoder encoder;

	private final AuthenticationManager manager;

	private final TokenService token;

	private final ApplicationEventPublisher eventPublisher;

	private final PaymentProcessor processor;

	private final UserMapper mapper;

	public UserResponse register(final CreateUserRequest request) {
		final var existing = getUserByEither(request.username(), request.email(), request.mobileNo());
		if (existing.isEmpty())
			return mapper.toResponse(save(mapper.toEntity(request, encoder)));
		else
			return update(request, existing.get());
	}

	private Optional<User> getUserByEither(final String username, final String email, final String mobileNo) {
		return repository.findByUsernameOrEmailOrMobileNo(username, email, mobileNo).map(user -> {
			if (user.isActive()) {
				final var existing = new HashMap<String, List<String>>();
				if (username.equals(user.getUsername())) {
					existing.put("username", List.of(username));
				}
				if (email != null && email.equals(user.getEmail())) {
					existing.put("email", List.of(email));
				}
				if (mobileNo != null && mobileNo.equals(user.getMobileNo())) {
					existing.put("mobileNo", List.of(mobileNo));
				}
				throw new AlreadyRegisteredException(existing);
			}
			return user;
		});
	}

	private UserResponse update(final CreateUserRequest request, final User existing) {
		final var shopName = request.shopName();
		final var isShopNameUpdated = shopName != null && !shopName.equals(existing.getShopName());

		final var user = mapper.updateEntity(request, existing, encoder);
		user.setActive(true);
		final var updated = repository.save(user);

		if (updated.getRoles().contains(Role.SELLER)) {
			activateShop(updated.getAccountId(), updated.getId().toString());
			if (isShopNameUpdated) {
				final var shopId = mapper.toResponseNoIdentifier(updated).id();
				eventPublisher.publishEvent(new ShopNameUpdatedEvent(shopId, shopName));
			}
		}
		return mapper.toResponse(updated);
	}

	public String login(final LoginRequest request) {
		final var auth = manager
			.authenticate(new UsernamePasswordAuthenticationToken(request.identifier(), request.password()));
		if (auth.isAuthenticated()) {
			final var user = (User) auth.getPrincipal();

			if (user.getRoles().contains(Role.BUYER)) {
				eventPublisher.publishEvent(new CreateCartEvent(user.getId().toString()));
			}

			var accountId = user.getAccountId();
			if (user.getRoles().contains(Role.SELLER)) {
				if (StringUtils.isBlank(accountId)) {
					accountId = save(user).getAccountId();
				}
				activateShop(accountId, user.getId().toString());
			}

			return token.generate(user, processor.getTransferStatus(accountId));
		}
		else
			throw new BadCredentialsException("Credentials provided is incorrect");
	}

	private User save(final User user) {
		if (user.getRoles().contains(Role.SELLER)) {
			user.setAccountId(processor.create());
		}
		return repository.save(user);
	}

	private void activateShop(final String accountId, final String shopId) {
		if (processor.transferEnabledFor(accountId)) {
			eventPublisher.publishEvent(new ShopStatusUpdatedEvent(shopId, Boolean.TRUE));
		}
	}

}
