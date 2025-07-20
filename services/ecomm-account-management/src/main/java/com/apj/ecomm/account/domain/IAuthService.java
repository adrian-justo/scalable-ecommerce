package com.apj.ecomm.account.domain;

import com.apj.ecomm.account.domain.model.CreateUserRequest;
import com.apj.ecomm.account.domain.model.LoginRequest;
import com.apj.ecomm.account.domain.model.UserResponse;

public interface IAuthService {

	UserResponse register(CreateUserRequest request);

	String login(LoginRequest request);

}
