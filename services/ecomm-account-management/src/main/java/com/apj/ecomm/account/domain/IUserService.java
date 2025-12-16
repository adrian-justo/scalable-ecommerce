package com.apj.ecomm.account.domain;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.data.domain.Pageable;

import com.apj.ecomm.account.domain.model.Paged;
import com.apj.ecomm.account.domain.model.UpdateUserRequest;
import com.apj.ecomm.account.domain.model.UserResponse;

public interface IUserService {

	Paged<UserResponse> findAll(Pageable pageable);

	UserResponse findByUsername(String username, String id);

	UserResponse update(String username, UpdateUserRequest request);

	void deleteByUsername(String username);

	Map<String, UserResponse> getDetails(Set<String> ids);

	List<User> findAllBy(Set<String> ids);

}
