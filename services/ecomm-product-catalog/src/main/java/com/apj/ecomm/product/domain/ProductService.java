package com.apj.ecomm.product.domain;

import java.util.List;

import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.apj.ecomm.product.domain.model.CreateProductRequest;
import com.apj.ecomm.product.domain.model.ProductCatalog;
import com.apj.ecomm.product.domain.model.ProductResponse;
import com.apj.ecomm.product.domain.model.UpdateProductRequest;
import com.apj.ecomm.product.web.exception.ResourceNotFoundException;

import io.micrometer.observation.annotation.Observed;
import lombok.RequiredArgsConstructor;

@Service
@Transactional
@Observed(name = "service.product")
@RequiredArgsConstructor
class ProductService implements IProductService {

	private final ProductRepository repository;
	private final ProductSpecBuilder specBuilder;
	private final ProductMapper mapper;

	@Transactional(readOnly = true)
	@Cacheable("catalog")
	public List<ProductCatalog> findAll(String filter, Pageable pageable) {
		filter += filter.contains("stock") ? "" : ";stock>1";
		return repository.findAll(specBuilder.build(filter), pageable).stream().map(mapper::toCatalog).toList();
	}

	@Transactional(readOnly = true)
	@Cacheable("product")
	public ProductResponse findById(long id) {
		return repository.findById(id).map(mapper::toResponse).orElseThrow(ResourceNotFoundException::new);
	}

	@Transactional(readOnly = true)
	public Integer getStock(long id) {
		return repository.findById(id).map(Product::getStock).orElseThrow(ResourceNotFoundException::new);
	}

	@CachePut(value = "product", key = "#result.id()")
	public ProductResponse list(String shopName, CreateProductRequest request) {
		Product product = mapper.toEntity(request);
		product.setShopName(shopName);
		return mapper.toResponse(repository.save(product));
	}

	@CachePut(value = "product", key = "#result.id()")
	public ProductResponse update(long id, UpdateProductRequest request) {
		return repository.findById(id).map(existing -> {
			Product product = mapper.updateEntity(request, existing);
			return mapper.toResponse(repository.save(product));
		}).orElseThrow(ResourceNotFoundException::new);
	}

}
