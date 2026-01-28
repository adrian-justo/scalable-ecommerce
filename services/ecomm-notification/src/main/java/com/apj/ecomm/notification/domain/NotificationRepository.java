package com.apj.ecomm.notification.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import io.micrometer.observation.annotation.Observed;

@Observed(name = "repository.notification")
interface NotificationRepository extends MongoRepository<Notification, String> {

	Page<Notification> findAllByUserId(String userId, Pageable pageable);

}