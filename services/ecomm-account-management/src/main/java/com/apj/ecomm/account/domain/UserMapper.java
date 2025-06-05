package com.apj.ecomm.account.domain;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

import com.apj.ecomm.account.domain.model.CreateUserRequest;
import com.apj.ecomm.account.domain.model.UpdateUserRequest;
import com.apj.ecomm.account.domain.model.UserResponse;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
interface UserMapper {

	UserResponse toResponse(User user);

	User toEntity(UserResponse response);

	User toEntity(CreateUserRequest request);

	User updateEntity(UpdateUserRequest updated, @MappingTarget User existing);

	User updateEntity(CreateUserRequest updated, @MappingTarget User existing);

}
