package com.apj.ecomm.product.domain;

import java.util.List;

import org.springframework.data.domain.Pageable;

import com.apj.ecomm.product.domain.model.CreateProductRequest;
import com.apj.ecomm.product.domain.model.Paged;
import com.apj.ecomm.product.domain.model.ProductCatalog;
import com.apj.ecomm.product.domain.model.ProductResponse;
import com.apj.ecomm.product.domain.model.UpdateProductRequest;

public interface IProductService {

	Paged<ProductCatalog> findAll(String filter, Pageable pageable);

	ProductResponse findById(long id);

	ProductResponse list(String shopId, String shopName, CreateProductRequest request);

	ProductResponse update(long id, String shopId, UpdateProductRequest request);

	List<Long> getProductsBy(String shopId);

	ProductResponse update(String shopName, Long id);

}