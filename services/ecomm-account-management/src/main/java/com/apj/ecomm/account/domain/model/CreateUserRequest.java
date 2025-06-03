package com.apj.ecomm.account.domain.model;

import java.util.List;

import com.apj.ecomm.account.domain.Role;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;

public record CreateUserRequest(@NotBlank(message = "Username is mandatory") String username,
		@NotBlank(message = "Email is mandatory") @Email(message = "Email is invalid") String email,
		@Pattern(regexp = "^$|(\\+\\d{1,3}[- ]?)?\\d{10}$", message = "Mobile no. is invalid") String mobileNo,
		@NotBlank(message = "Password is mandatory") @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[\\W])(?=\\S+$).{8,}$", message = "Password is not strong enough") String password,
		String name, @NotEmpty(message = "At least 1 Role must be set") List<Role> roles) {

}
