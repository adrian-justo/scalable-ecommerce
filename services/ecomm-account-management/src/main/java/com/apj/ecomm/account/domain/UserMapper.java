package com.apj.ecomm.account.domain;

import java.util.HashSet;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.mapstruct.AfterMapping;
import org.mapstruct.BeanMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.apj.ecomm.account.domain.model.CreateUserRequest;
import com.apj.ecomm.account.domain.model.UpdateUserRequest;
import com.apj.ecomm.account.domain.model.UserResponse;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE,
		nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
interface UserMapper {

	@Mapping(target = "id", ignore = true)
	@Mapping(target = "createdAt", ignore = true)
	@Mapping(target = "updatedAt", ignore = true)
	@Mapping(target = "active", ignore = true)
	UserResponse toResponse(User user);

	@Mapping(target = "username", ignore = true)
	@Mapping(target = "email", ignore = true)
	@Mapping(target = "mobileNo", ignore = true)
	UserResponse toResponseNoIdentifier(User user);

	@Mapping(target = "password", qualifiedByName = "encode")
	@BeanMapping(qualifiedByName = "validate")
	User toEntity(CreateUserRequest request, @Context PasswordEncoder encoder);

	@Mapping(target = "password", qualifiedByName = "encode")
	@BeanMapping(qualifiedByName = "validate")
	User updateEntity(UpdateUserRequest updated, @MappingTarget User existing, @Context PasswordEncoder encoder);

	@Mapping(target = "password", qualifiedByName = "encode")
	@BeanMapping(qualifiedByName = "validate")
	User updateEntity(CreateUserRequest updated, @MappingTarget User existing, @Context PasswordEncoder encoder);

	@Named("encode")
	default String encode(final String text, @Context final PasswordEncoder encoder) {
		return encoder.encode(text);
	}

	@Named("validate")
	@AfterMapping
	default void validate(@MappingTarget final User user) {
		validateShopName(user);
		validatedNotificationTypes(user);
	}

	private void validateShopName(final User user) {
		if (user.getShopName() != null && (user.getRoles() == null || !user.getRoles().contains(Role.SELLER))) {
			user.setShopName(null);
		}
	}

	private void validatedNotificationTypes(final User user) {
		final var types = Optional.ofNullable(user.getNotificationTypes()).orElse(new HashSet<>());
		if (StringUtils.isBlank(user.getEmail()) && StringUtils.isNotBlank(user.getMobileNo())) {
			types.remove(NotificationType.EMAIL);
			types.add(NotificationType.SMS);
		}
		else if (StringUtils.isBlank(user.getMobileNo())) {
			types.remove(NotificationType.SMS);
			types.add(NotificationType.EMAIL);
		}
		else if (types.isEmpty()) {
			if (StringUtils.isNotBlank(user.getEmail())) {
				types.add(NotificationType.EMAIL);
			}
			else if (StringUtils.isNotBlank(user.getMobileNo())) {
				types.add(NotificationType.SMS);
			}
		}
		user.setNotificationTypes(types);
	}

}
