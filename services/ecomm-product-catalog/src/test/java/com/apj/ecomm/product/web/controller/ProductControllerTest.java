package com.apj.ecomm.product.web.controller;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.skyscreamer.jsonassert.JSONAssert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.apj.ecomm.product.constants.AppConstants;
import com.apj.ecomm.product.domain.IProductService;
import com.apj.ecomm.product.domain.model.CreateProductRequest;
import com.apj.ecomm.product.domain.model.ProductCatalog;
import com.apj.ecomm.product.domain.model.ProductResponse;
import com.apj.ecomm.product.domain.model.UpdateProductRequest;
import com.apj.ecomm.product.web.exception.ResourceNotFoundException;
import com.apj.ecomm.product.web.exception.RequestArgumentNotValidException;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(controllers = ProductController.class, properties = { "eureka.client.enabled=false",
		"spring.cloud.config.enabled=false" })
@AutoConfigureMockMvc(addFilters = false)
@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

	private final String uri = "/api/v1/products";

	private List<ProductResponse> response;

	@Autowired
	private MockMvc mvc;

	@Autowired
	private ObjectMapper mapper;

	@MockitoBean
	private IProductService service;

	@BeforeEach
	void setUp() throws Exception {
		mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
		try (InputStream inputStream = TypeReference.class.getResourceAsStream("/data/products.json")) {
			response = mapper.readValue(inputStream, new TypeReference<List<ProductResponse>>() {
			});
		}
	}

	@Test
	void productCatalog_getAll() throws Exception {
		List<ProductCatalog> catalog = response.stream().map(product -> new ProductCatalog(product.id(),
				product.images().stream().findFirst().orElse(null), product.name(), Objects.toString(product.price())))
				.toList();

		when(service.findAll(any(), any(PageRequest.class))).thenReturn(catalog);
		ResultActions action = mvc.perform(get(uri));

		String jsonResponse = mapper.writeValueAsString(catalog);
		action.andExpect(status().isOk()).andExpect(content().json(jsonResponse));
		assertEquals(jsonResponse, action.andReturn().getResponse().getContentAsString(), true);
	}

	@Test
	void productCatalog_getSpecific_found() throws Exception {
		ProductResponse productResponse = response.get(0);

		when(service.findById(anyLong())).thenReturn(productResponse);
		ResultActions action = mvc.perform(get(uri + "/1"));

		String jsonResponse = mapper.writeValueAsString(productResponse);
		action.andExpect(status().isOk()).andExpect(content().json(jsonResponse));
		assertEquals(jsonResponse, action.andReturn().getResponse().getContentAsString(), true);
	}

	@Test
	void productCatalog_getSpecific_nonNumeric() throws Exception {
		mvc.perform(get(uri + "/nonNumeric")).andExpect(
				result -> assertTrue(result.getResolvedException() instanceof MethodArgumentTypeMismatchException));
	}

	@Test
	void productCatalog_getSpecific_notValid() throws Exception {
		mvc.perform(get(uri + "/0"))
				.andExpect(result -> assertTrue(result.getResolvedException() instanceof ResourceNotFoundException));
	}

	@Test
	void getProductStock() throws Exception {
		when(service.getStock(anyLong())).thenReturn(1);
		mvc.perform(get(uri + "/1/stock")).andExpect(status().isOk()).andExpect(content().string("1"));
	}

	@Test
	void listProduct_success() throws Exception {
		String shopName = "Shop Name";
		CreateProductRequest request = new CreateProductRequest("name", "description", Set.of("image-uri"),
				Set.of("category"), 1, BigDecimal.ONE);
		ProductResponse productResponse = new ProductResponse(5L, request.name(), shopName, request.description(),
				request.images(), request.categories(), true, request.price());

		when(service.list(anyString(), any(CreateProductRequest.class))).thenReturn(productResponse);
		ResultActions action = mvc.perform(post(uri).header(AppConstants.HEADER_SHOP_NAME, shopName)
				.contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(request)));

		String jsonResponse = mapper.writeValueAsString(productResponse);
		action.andExpect(status().isCreated()).andExpect(content().json(jsonResponse));
		JSONAssert.assertEquals(jsonResponse, action.andReturn().getResponse().getContentAsString(), true);
	}

	@Test
	void listProduct_missingHeader() throws Exception {
		CreateProductRequest request = new CreateProductRequest("name", "description", Set.of("image-uri"),
				Set.of("category"), 1, new BigDecimal("0.001"));

		mvc.perform(post(uri).contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(request)))
				.andExpect(
						result -> assertTrue(result.getResolvedException() instanceof MissingRequestHeaderException));
	}

	@Test
	void listProduct_invalid() throws Exception {
		CreateProductRequest request = new CreateProductRequest("", "description", Set.of("image-uri"),
				Set.of("category"), 0, new BigDecimal("0.009"));

		mvc.perform(post(uri).header(AppConstants.HEADER_SHOP_NAME, "Shop Name").contentType(MediaType.APPLICATION_JSON)
				.content(mapper.writeValueAsString(request))).andExpect(
						result -> assertTrue(result.getResolvedException() instanceof MethodArgumentNotValidException));
	}

	@Test
	void updateProduct() throws Exception {
		UpdateProductRequest request = new UpdateProductRequest(null, "description", null, null, 1, BigDecimal.ONE);
		ProductResponse existing = response.get(0);
		ProductResponse updated = new ProductResponse(1L, existing.name(), existing.shopName(), request.description(),
				existing.images(), existing.categories(), true, request.price());

		when(service.update(anyLong(), any())).thenReturn(updated);
		ResultActions action = mvc.perform(
				put(uri + "/1").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(request)));

		String jsonResponse = mapper.writeValueAsString(updated);
		action.andExpect(status().isOk()).andExpect(content().json(jsonResponse));
		JSONAssert.assertEquals(jsonResponse, action.andReturn().getResponse().getContentAsString(), true);
	}

	@Test
	void updateProduct_invalid() throws Exception {
		UpdateProductRequest request = new UpdateProductRequest("", "description", null, null, 1, BigDecimal.ONE);

		mvc.perform(put(uri + "/1").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(request)))
				.andExpect(result -> assertTrue(
						result.getResolvedException() instanceof RequestArgumentNotValidException));
	}

}
