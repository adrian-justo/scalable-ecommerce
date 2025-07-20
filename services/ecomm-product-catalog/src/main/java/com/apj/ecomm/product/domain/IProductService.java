package com.apj.ecomm.product.domain;

import java.util.List;

import org.springframework.data.domain.Pageable;

import com.apj.ecomm.product.domain.model.CreateProductRequest;
import com.apj.ecomm.product.domain.model.ProductCatalog;
import com.apj.ecomm.product.domain.model.ProductResponse;
import com.apj.ecomm.product.domain.model.UpdateProductRequest;

public interface IProductService {

	List<ProductCatalog> findAll(String filter, Pageable pageable);

	ProductResponse findById(long id);

	Integer getStock(long id);

	ProductResponse list(String shopName, CreateProductRequest request);

	ProductResponse update(long id, UpdateProductRequest request);

}