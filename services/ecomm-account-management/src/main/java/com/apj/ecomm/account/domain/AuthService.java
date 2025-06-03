package com.apj.ecomm.account.domain;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.apj.ecomm.account.domain.model.CreateUserRequest;
import com.apj.ecomm.account.domain.model.LoginRequest;
import com.apj.ecomm.account.domain.model.UserResponse;
import com.apj.ecomm.account.web.exception.AlreadyRegisteredException;
import com.apj.ecomm.account.web.exception.UsernameTakenException;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
class AuthService implements IAuthService {

	private final UserRepository repository;
	private final UserMapper mapper;

	public Optional<UserResponse> register(CreateUserRequest request) {
		if (repository.existsByUsernameAndActiveTrue(request.username())) {
			throw new UsernameTakenException();
		}
		if (repository.existsByEmailAndActiveTrueOrMobileNoAndActiveTrue(request.email(), request.mobileNo())) {
			throw new AlreadyRegisteredException();
		}
		return Optional.of(repository
				.findByUsernameOrEmailOrMobileNo(request.username(), request.email(), request.mobileNo()).map(user -> {
					user.setActive(true);
					return mapper.toResponse(repository.save(user));
				}).orElseGet(() -> mapper.toResponse(repository.save(mapper.toEntity(request)))));
	}

	public Optional<String> login(LoginRequest request) {
		return repository.findByUsernameAndActiveTrue(request.identifier())
				.or(() -> repository.findByEmailAndActiveTrueOrMobileNoAndActiveTrue(request.identifier(),
						request.identifier()))
				.filter(user -> request.password().equals(user.getPassword())).map(user -> generateToken(user));
	}

	private String generateToken(User user) {
		// Token generation logic here (e.g., JWT)
		return "jwt.token.here"; // Placeholder implementation
	}

}
