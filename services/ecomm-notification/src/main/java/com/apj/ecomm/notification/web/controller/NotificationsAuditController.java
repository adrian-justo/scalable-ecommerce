package com.apj.ecomm.notification.web.controller;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.apj.ecomm.notification.constants.AppConstants;
import com.apj.ecomm.notification.domain.INotificationsAuditService;
import com.apj.ecomm.notification.domain.model.NotificationResponse;
import com.apj.ecomm.notification.domain.model.Paged;
import com.apj.ecomm.notification.web.util.PathValidator;

import io.micrometer.observation.annotation.Observed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Notifications Audit API", description = "Endpoints for viewing notifications of all users")
@RestController
@RequestMapping("${api.version}${admin.path}${notifications.path}")
@SecurityScheme(name = "authToken", type = SecuritySchemeType.HTTP, bearerFormat = "JWT", scheme = "bearer")
@SecurityRequirement(name = "authToken")
@Observed(name = "controller.admin.notification")
@CrossOrigin
@RequiredArgsConstructor
public class NotificationsAuditController {

	private final INotificationsAuditService service;

	@Operation(summary = "Notifications Audit", description = "View all notifications")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Notifications" + AppConstants.MSG_OK),
			@ApiResponse(responseCode = "403", description = AppConstants.MSG_FORBIDDEN, content = @Content) })
	@GetMapping
	public Paged<NotificationResponse> getAllNotifications(
			@ParameterObject @PageableDefault(page = 0, size = 10) final Pageable pageable) {
		return service.findAll(pageable);
	}

	@Operation(summary = "Notification Audit Details", description = "View details of a specific notification")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Notification" + AppConstants.MSG_OK),
			@ApiResponse(responseCode = "403", description = AppConstants.MSG_FORBIDDEN, content = @Content),
			@ApiResponse(responseCode = "404", description = "Notification" + AppConstants.MSG_NOT_FOUND,
					content = @Content) })
	@GetMapping("/{notificationId}")
	public NotificationResponse getNotificationById(@PathVariable final String notificationId) {
		PathValidator.notificationId(notificationId);
		return service.findById(notificationId);
	}

}
