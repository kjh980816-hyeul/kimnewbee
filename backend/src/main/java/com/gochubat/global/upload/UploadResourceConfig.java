package com.gochubat.global.upload;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class UploadResourceConfig implements WebMvcConfigurer {

	private final UploadProperties properties;

	public UploadResourceConfig(UploadProperties properties) {
		this.properties = properties;
	}

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		Path absolute = Paths.get(properties.dir()).toAbsolutePath().normalize();
		String location = absolute.toUri().toString();
		String pattern = (properties.publicBaseUrl().endsWith("/")
				? properties.publicBaseUrl()
				: properties.publicBaseUrl() + "/") + "**";
		registry.addResourceHandler(pattern)
				.addResourceLocations(location);
	}
}
