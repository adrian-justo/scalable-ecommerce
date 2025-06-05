package com.apj.ecomm.account.domain;

import java.util.Optional;

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
		user.setPassword(encoder.encode(request.password()));
		user.setActive(true);
		return Optional.of(mapper.toResponse(repository.save(user)));
	}

	public Optional<String> login(LoginRequest request) {
		return Optional.of(getUserByIdentifier(request.identifier())
				.map(user -> authenticateUser(user.getUsername(), request.password()))
				.orElseThrow(IncorrectCredentialsException::new));
	}

	private Optional<User> getUserByIdentifier(String identifier) {
		return repository.findByUsernameOrEmailOrMobileNo(identifier, identifier, identifier).filter(User::isActive);
	}

	private String authenticateUser(String username, String password) {
		Authentication auth = manager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
		return auth.isAuthenticated() ? token.generate((User) auth.getPrincipal()) : "";
	}

}
