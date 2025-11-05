package com.apj.ecomm.order.domain.model;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record InformationResponse(String name, String address, String email, String mobileNo) {
}