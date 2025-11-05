package com.apj.ecomm.product.domain;

import java.util.List;

import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.apj.ecomm.product.domain.model.CreateProductRequest;
import com.apj.ecomm.product.domain.model.Paged;
import com.apj.ecomm.product.domain.model.ProductResponse;
import com.apj.ecomm.product.domain.model.UpdateProductFromMessageRequest;
import com.apj.ecomm.product.domain.model.UpdateProductRequest;
import com.apj.ecomm.product.web.exception.ResourceAccessDeniedException;
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
	@Cacheable(value = "catalog", sync = true)
	public List<Long> findProductIds(final String filter, final Pageable pageable) {
		return repository.findAll(specBuilder.build(onlyBuyable(filter)), pageable).map(Product::getId).toList();
	}

	private String onlyBuyable(String filter) {
		filter += filter.contains("stock") ? "" : ";stock>1";
		filter += filter.contains("active") ? "" : ";active:true";
		return filter;
	}

	public Paged<ProductResponse> getPaged(final List<ProductResponse> products, final Pageable pageable) {
		return new Paged<>(new PageImpl<>(products, pageable, products.size()));
	}

	@Transactional(readOnly = true)
	@Cacheable(value = "product", sync = true)
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
		final var product = repository.findById(id);
		if (product.isEmpty())
			throw new ResourceNotFoundException();
		return product.filter(p -> p.getShopId().equals(shopId))
			.map(existing -> mapper.toResponse(repository.save(mapper.updateEntity(request, existing))))
			.orElseThrow(ResourceAccessDeniedException::new);
	}

	@Transactional(readOnly = true)
	@Cacheable("catalog")
	public List<Long> getProductsBy(final String shopId) {
		return repository.findAllByShopId(shopId).stream().map(Product::getId).toList();
	}

	@CachePut(value = "product", key = "#result.id()")
	public ProductResponse update(final Long id, final UpdateProductFromMessageRequest request) {
		final var product = repository.findById(id);
		return product.map(existing -> mapper.toResponse(repository.save(mapper.updateEntity(request, existing))))
			.orElse(mapper.toResponse(product.orElseThrow()));
	}

}
