package com.gochubat.global.upload;

import com.gochubat.domain.user.entity.Tier;
import com.gochubat.domain.user.entity.User;
import com.gochubat.domain.user.repository.UserRepository;
import com.gochubat.global.jwt.JwtUtil;
import com.gochubat.support.TestUserFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.servlet.http.Cookie;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class FileUploadControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JwtUtil jwtUtil;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private UploadProperties uploadProperties;

	private User user;
	private String token;

	@BeforeEach
	void setUp() {
		user = userRepository.save(TestUserFactory.create("nv-up", "업로더", Tier.PEPPER));
		token = jwtUtil.generateAccessToken(user.getId(), user.getTier().name());
	}

	@AfterEach
	void cleanup() throws IOException {
		userRepository.deleteAll();
		Path dir = Paths.get(uploadProperties.dir()).toAbsolutePath();
		if (Files.exists(dir)) {
			try (Stream<Path> walk = Files.walk(dir)) {
				walk.sorted(Comparator.reverseOrder())
						.filter(p -> !p.equals(dir))
						.forEach(p -> {
							try { Files.deleteIfExists(p); } catch (IOException ignored) {}
						});
			}
		}
	}

	@Test
	void upload_returns_401_without_token() throws Exception {
		MockMultipartFile file = new MockMultipartFile("file", "x.png", "image/png", new byte[]{1, 2, 3});
		mockMvc.perform(multipart("/api/files").file(file))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void upload_succeeds_for_authenticated_png_returns_url() throws Exception {
		MockMultipartFile file = new MockMultipartFile("file", "cute.png", "image/png", new byte[]{1, 2, 3, 4});

		mockMvc.perform(multipart("/api/files").file(file).cookie(new Cookie("access_token", token)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.url").exists())
				.andExpect(jsonPath("$.url").value(org.hamcrest.Matchers.matchesPattern("/uploads/[0-9a-f-]+\\.png")));
	}

	@Test
	void upload_rejects_disallowed_mime_type() throws Exception {
		MockMultipartFile file = new MockMultipartFile("file", "evil.exe", "application/x-msdownload", new byte[]{0});

		mockMvc.perform(multipart("/api/files").file(file).cookie(new Cookie("access_token", token)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
	}

	@Test
	void upload_rejects_oversized_file() throws Exception {
		long over = uploadProperties.maxSizeBytes() + 1;
		MockMultipartFile file = new MockMultipartFile("file", "big.png", "image/png", new byte[(int) over]);

		mockMvc.perform(multipart("/api/files").file(file).cookie(new Cookie("access_token", token)))
				.andExpect(status().isBadRequest());
	}

	@Test
	void upload_rejects_empty_file() throws Exception {
		MockMultipartFile file = new MockMultipartFile("file", "empty.png", "image/png", new byte[0]);

		mockMvc.perform(multipart("/api/files").file(file).cookie(new Cookie("access_token", token)))
				.andExpect(status().isBadRequest());
	}

	@Test
	void upload_persists_file_in_configured_dir() throws Exception {
		MockMultipartFile file = new MockMultipartFile("file", "saved.jpg", "image/jpeg", new byte[]{1, 2, 3});

		mockMvc.perform(multipart("/api/files").file(file).cookie(new Cookie("access_token", token)))
				.andExpect(status().isCreated());

		Path dir = Paths.get(uploadProperties.dir()).toAbsolutePath();
		try (Stream<Path> walk = Files.walk(dir, 1)) {
			long jpgCount = walk.filter(p -> p.toString().endsWith(".jpg")).count();
			assertThat(jpgCount).isEqualTo(1L);
		}
	}
}
