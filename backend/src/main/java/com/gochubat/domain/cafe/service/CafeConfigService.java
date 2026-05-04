package com.gochubat.domain.cafe.service;

import com.gochubat.domain.cafe.dto.CafeConfigResponse;
import com.gochubat.domain.cafe.dto.CafeConfigUpdateRequest;
import com.gochubat.domain.cafe.entity.CafeConfig;
import com.gochubat.domain.cafe.repository.CafeConfigRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CafeConfigService {

	private final CafeConfigRepository repository;

	public CafeConfigService(CafeConfigRepository repository) {
		this.repository = repository;
	}

	@Transactional
	public CafeConfigResponse get() {
		return CafeConfigResponse.from(loadOrSeed());
	}

	@Transactional
	public CafeConfigResponse update(CafeConfigUpdateRequest request) {
		CafeConfig config = loadOrSeed();
		config.update(request.heroBannerUrl(), request.heroHeadline(), request.heroSubtext(), request.footerText());
		return CafeConfigResponse.from(config);
	}

	private CafeConfig loadOrSeed() {
		return repository.findAll().stream().findFirst()
				.orElseGet(() -> repository.save(CafeConfig.defaults()));
	}
}
