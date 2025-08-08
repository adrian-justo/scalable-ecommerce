package com.apj.ecomm.account.domain;

import org.springframework.data.domain.Pageable;

import com.apj.ecomm.account.domain.model.Paged;
import com.apj.ecomm.account.domain.model.UpdateUserRequest;
import com.apj.ecomm.account.domain.model.UserResponse;

public interface IUserService extends IAccountService {

	Paged<UserResponse> findAll(Pageable pageable);

	UserResponse findByUsername(String username);

	UserResponse update(String username, UpdateUserRequest request);

	void deleteByUsername(String username);

}
