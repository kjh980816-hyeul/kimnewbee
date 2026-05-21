package com.gochubat.domain.search.controller;

import com.gochubat.domain.search.dto.SearchResponse;
import com.gochubat.domain.search.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

	private final SearchService searchService;

	@GetMapping
	public SearchResponse search(@RequestParam("q") String q, @RequestParam(value = "type", required = false) String type) {
		return searchService.search(q, type);
	}
}
