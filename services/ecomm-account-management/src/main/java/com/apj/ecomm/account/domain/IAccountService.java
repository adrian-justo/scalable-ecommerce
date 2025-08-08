package com.apj.ecomm.account.domain;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

interface IAccountService {

	default Set<NotificationType> getValidatedTypes(final User user, Set<NotificationType> types) {
		if (types == null) {
			types = new HashSet<>();
		}
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
		return types;
	}

}
