package com.apj.ecomm.account.domain;

import java.util.List;

import org.springframework.data.domain.Pageable;

import com.apj.ecomm.account.domain.model.UpdateUserRequest;
import com.apj.ecomm.account.domain.model.UserResponse;

public interface IUserService {

	List<UserResponse> findAll(Pageable pageable);

	UserResponse findByUsername(String username);

	UserResponse update(String username, UpdateUserRequest request);

	void deleteByUsername(String username);

}
