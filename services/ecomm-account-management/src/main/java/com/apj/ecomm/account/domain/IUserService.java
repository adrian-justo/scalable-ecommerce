package com.apj.ecomm.account.domain;

import java.util.List;
import java.util.Optional;

import com.apj.ecomm.account.domain.model.UpdateUserRequest;
import com.apj.ecomm.account.domain.model.UserResponse;

public interface IUserService {

	List<UserResponse> findAll(int pageNo, int size);

	Optional<UserResponse> findByUsername(String username);

	Optional<UserResponse> update(String username, UpdateUserRequest request);

	void deleteByUsername(String username);

}
