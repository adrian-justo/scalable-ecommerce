package com.apj.ecomm.account.domain;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

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
import com.apj.ecomm.account.web.messaging.CreateCartEvent;

import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@Observed(name = "service.auth")
@RequiredArgsConstructor
class AuthService implements IAuthService {

	private final UserRepository repository;

	private final UserMapper mapper;

	private final PasswordEncoder encoder;

	private final AuthenticationManager manager;

	private final TokenService token;

	private final ApplicationEventPublisher eventPublisher;

	public UserResponse register(final CreateUserRequest request) {
		return getUserByEither(request.username(), request.email(), request.mobileNo())
			.map(existing -> saveUser(mapper.updateEntity(request, existing), request))
			.orElseGet(() -> saveUser(mapper.toEntity(request), request));
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

	private UserResponse saveUser(final User user, final CreateUserRequest request) {
		user.setNotificationTypes(getValidatedTypes(user, user.getNotificationTypes()));
		user.setPassword(encoder.encode(request.password()));
		user.setActive(true);
		return mapper.toResponse(repository.save(user));
	}

	public String login(final LoginRequest request) {
		final var auth = manager
			.authenticate(new UsernamePasswordAuthenticationToken(request.identifier(), request.password()));
		if (auth.isAuthenticated()) {
			final var user = (User) auth.getPrincipal();
			if (user.getRoles().stream().anyMatch(role -> role.equals(Role.BUYER))) {
				eventPublisher.publishEvent(new CreateCartEvent(user.getId().toString()));
			}
			return token.generate(user);
		}
		else
			throw new BadCredentialsException("Credentials provided is incorrect");
	}

}
