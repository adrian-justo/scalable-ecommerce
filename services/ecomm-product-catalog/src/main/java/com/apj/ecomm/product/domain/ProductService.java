package com.apj.ecomm.product.domain;

import java.util.ArrayList;
import java.util.List;

import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.apj.ecomm.product.constants.AppConstants;
import com.apj.ecomm.product.domain.model.CreateProductRequest;
import com.apj.ecomm.product.domain.model.Paged;
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
	@Cacheable(value = "catalog", unless = "#result.totalElements() < " + AppConstants.DEFAULT_PAGE_SIZE)
	public Paged<ProductCatalog> findAll(String filter, final Pageable pageable) {
		filter += filter.contains("stock") ? "" : ";stock>1";
		final Page<ProductCatalog> result = repository.findAll(specBuilder.build(filter), pageable)
			.map(mapper::toCatalog);

		final var response = new Paged<>(result.getContent(), result.getNumber(), result.getSize(),
				result.getTotalPages(), new ArrayList<>(), result.getTotalElements());
		response.setSort(result.getSort());
		return response;
	}

	@Transactional(readOnly = true)
	@Cacheable("product")
	public ProductResponse findById(final long id) {
		return repository.findById(id).map(mapper::toResponse).orElseThrow(ResourceNotFoundException::new);
	}

	@CachePut(value = "product", key = "#result.id()")
	public ProductResponse list(final String shopId, final String shopName, final CreateProductRequest request) {
		final var product = mapper.toEntity(request);
		product.setShopId(shopId);
		product.setShopName(shopName);
		return mapper.toResponse(repository.save(product));
	}

	@CachePut(value = "product", key = "#result.id()")
	public ProductResponse update(final long id, final String shopId, final UpdateProductRequest request) {
		return repository.findById(id)
			.filter(p -> p.getShopId().equals(shopId))
			.map(existing -> mapper.toResponse(repository.save(mapper.updateEntity(request, existing))))
			.orElseThrow(ResourceNotFoundException::new);
	}

	public List<Long> getProductsBy(final String shopId) {
		return repository.findAllByShopId(shopId).stream().map(Product::getId).toList();
	}

	@CachePut(value = "product", key = "#result.id()")
	public ProductResponse update(final String shopName, final Long id) {
		return repository.findById(id).map(product -> {
			product.setShopName(shopName);
			return mapper.toResponse(repository.save(product));
		}).orElseThrow(ResourceNotFoundException::new);
	}

}
