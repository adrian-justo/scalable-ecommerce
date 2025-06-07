package com.apj.ecomm.account.domain;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.apj.ecomm.account.domain.model.CreateUserRequest;
import com.apj.ecomm.account.domain.model.LoginRequest;
import com.apj.ecomm.account.domain.model.UserResponse;
import com.apj.ecomm.account.web.exception.AlreadyRegisteredException;
import com.apj.ecomm.account.web.exception.IncorrectCredentialsException;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
class AuthService implements IAuthService {

	private final UserRepository repository;
	private final UserMapper mapper;
	private final PasswordEncoder encoder;
	private final AuthenticationManager manager;
	private final TokenService token;

	public Optional<UserResponse> register(CreateUserRequest request) {
		request.validate();
		return getUserByEither(request.username(), request.email(), request.mobileNo())
				.map(existing -> saveUser(mapper.updateEntity(request, existing), request))
				.orElse(saveUser(mapper.toEntity(request), request));
	}

	private Optional<User> getUserByEither(String username, String email, String mobileNo) {
		return repository.findByUsernameOrEmailOrMobileNo(username, email, mobileNo).map(user -> {
			if (user.isActive()) {
				throw new AlreadyRegisteredException();
			}
			return user;
		});
	}

	private Optional<UserResponse> saveUser(User user, CreateUserRequest request) {
		user.setNotificationTypes(getValidatedTypes(user));
		user.setPassword(encoder.encode(request.password()));
		user.setActive(true);
		return Optional.of(mapper.toResponse(repository.save(user)));
	}

	Set<NotificationType> getValidatedTypes(User user) {
		Set<NotificationType> types = user.getNotificationTypes() == null ? new HashSet<>()
				: user.getNotificationTypes();
		if (StringUtils.isBlank(user.getEmail()) && StringUtils.isNotBlank(user.getMobileNo())) {
			types.remove(NotificationType.EMAIL);
			types.add(NotificationType.SMS);
		} else if (StringUtils.isBlank(user.getMobileNo())) {
			types.remove(NotificationType.SMS);
			types.add(NotificationType.EMAIL);
		} else if (types.isEmpty()) {
			if (StringUtils.isNotBlank(user.getEmail())) {
				types.add(NotificationType.EMAIL);
			} else if (StringUtils.isNotBlank(user.getMobileNo())) {
				types.add(NotificationType.SMS);
			}
		}
		return types;
	}

	public String login(LoginRequest request) {
		Authentication auth = manager
				.authenticate(new UsernamePasswordAuthenticationToken(request.identifier(), request.password()));
		if (auth.isAuthenticated()) {
			return token.generate((User) auth.getPrincipal());
		} else {
			throw new IncorrectCredentialsException();
		}
	}

}
