package com.apj.ecomm.account.web.client.cart;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.apj.ecomm.account.constants.AppConstants;
import com.apj.ecomm.account.web.client.AccountClient;
import com.apj.ecomm.account.web.util.PathValidator;

import io.micrometer.observation.annotation.Observed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "My Shopping Cart",
		description = "Endpoint for viewing your shopping cart. Only a buyer can access this endpoint")
@RestController
@RequestMapping("${api.version}${users.path}/{username}${carts.path}")
@SecurityScheme(name = "authToken", type = SecuritySchemeType.HTTP, bearerFormat = "JWT", scheme = "bearer")
@SecurityRequirement(name = "authToken")
@Observed(name = "controller.user.cart")
@CrossOrigin
@RequiredArgsConstructor
public class CartController {

	private final AccountClient client;

	@Operation(summary = "Shopping Cart Details", description = "View my shopping cart and its' details")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Cart" + AppConstants.MSG_OK),
			@ApiResponse(responseCode = "400", description = AppConstants.MSG_BAD_REQUEST, content = @Content),
			@ApiResponse(responseCode = "403", description = AppConstants.MSG_FORBIDDEN, content = @Content) })
	@GetMapping
	public BuyerCartResponse getCartOfBuyer(@PathVariable final String username,
			@Parameter(hidden = true) @RequestHeader(AppConstants.HEADER_USER_ID) final String buyerId) {
		PathValidator.username(username);
		return client.getCartOfBuyer(buyerId);
	}

}
