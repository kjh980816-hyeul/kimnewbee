package com.gochubat.global.config;

import com.gochubat.global.exception.ErrorCode;
import com.gochubat.global.exception.ErrorResponse;
import com.gochubat.global.jwt.JwtAuthenticationFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

	private final JwtAuthenticationFilter jwtAuthenticationFilter;
	private final ObjectMapper objectMapper;
	private final CorsProperties corsProperties;

	public SecurityConfig(
			JwtAuthenticationFilter jwtAuthenticationFilter,
			ObjectMapper objectMapper,
			CorsProperties corsProperties
	) {
		this.jwtAuthenticationFilter = jwtAuthenticationFilter;
		this.objectMapper = objectMapper;
		this.corsProperties = corsProperties;
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
				.cors(Customizer.withDefaults())
				.csrf(AbstractHttpConfigurer::disable)
				.formLogin(AbstractHttpConfigurer::disable)
				.httpBasic(AbstractHttpConfigurer::disable)
				.sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.authorizeHttpRequests(auth -> auth
						.requestMatchers("/actuator/health/**", "/h2-console/**").permitAll()
						.requestMatchers(HttpMethod.GET, "/uploads/**").permitAll()
						.requestMatchers("/api/files").authenticated()
						.requestMatchers("/api/auth/**").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/me/admin").permitAll()
						.requestMatchers("/api/me/**").authenticated()
						.requestMatchers(HttpMethod.GET, "/api/letters").permitAll()
						.requestMatchers("/api/letters/**").authenticated()
						.requestMatchers(HttpMethod.GET, "/api/notices/**").permitAll()
						.requestMatchers("/api/notices/**").authenticated()
						.requestMatchers(HttpMethod.GET, "/api/free/**", "/api/fanart/**", "/api/clips/**", "/api/pets/**").permitAll()
						.requestMatchers("/api/free/**", "/api/fanart/**", "/api/clips/**", "/api/pets/**").authenticated()
						.requestMatchers("/api/offline/**").authenticated()
						.requestMatchers(HttpMethod.GET, "/api/posts/*/comments").permitAll()
						.requestMatchers("/api/posts/*/comments", "/api/comments/**").authenticated()
						.requestMatchers(HttpMethod.POST, "/api/free/*/like", "/api/fanart/*/like", "/api/clips/*/like", "/api/pets/*/like", "/api/offline/*/like").authenticated()
						.requestMatchers(HttpMethod.GET, "/api/songs").permitAll()
						.requestMatchers("/api/songs/**").authenticated()
						.requestMatchers(HttpMethod.GET, "/api/boards").permitAll()
						.requestMatchers("/api/boards/**").authenticated()
						.requestMatchers(HttpMethod.GET, "/api/cafe/config").permitAll()
						.requestMatchers("/api/admin/**").authenticated()
						.anyRequest().permitAll())
				.exceptionHandling(eh -> eh
						.authenticationEntryPoint(authenticationEntryPoint())
						.accessDeniedHandler(accessDeniedHandler()))
				.headers(h -> h.frameOptions(frame -> frame.sameOrigin()))
				.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
		return http.build();
	}

	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration cfg = new CorsConfiguration();
		cfg.setAllowedOrigins(corsProperties.allowedOrigins());
		cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
		cfg.setAllowedHeaders(List.of("*"));
		cfg.setExposedHeaders(List.of("Set-Cookie"));
		cfg.setAllowCredentials(true);
		cfg.setMaxAge(3600L);
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/api/**", cfg);
		return source;
	}

	private AuthenticationEntryPoint authenticationEntryPoint() {
		return (request, response, authException) -> writeError(response, ErrorCode.UNAUTHORIZED);
	}

	private AccessDeniedHandler accessDeniedHandler() {
		return (request, response, accessDeniedException) -> writeError(response, ErrorCode.FORBIDDEN);
	}

	private void writeError(jakarta.servlet.http.HttpServletResponse response, ErrorCode errorCode) throws java.io.IOException {
		response.setStatus(errorCode.status().value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write(objectMapper.writeValueAsString(ErrorResponse.of(errorCode)));
	}
}
