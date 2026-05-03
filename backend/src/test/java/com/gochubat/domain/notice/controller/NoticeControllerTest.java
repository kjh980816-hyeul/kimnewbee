package com.gochubat.domain.notice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gochubat.domain.notice.entity.Notice;
import com.gochubat.domain.notice.repository.NoticeRepository;
import com.gochubat.domain.user.entity.Tier;
import com.gochubat.domain.user.entity.User;
import com.gochubat.domain.user.repository.UserRepository;
import com.gochubat.global.jwt.JwtUtil;
import com.gochubat.support.TestUserFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.servlet.http.Cookie;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class NoticeControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JwtUtil jwtUtil;

	@Autowired
	private NoticeRepository noticeRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ObjectMapper objectMapper;

	@AfterEach
	void cleanup() {
		noticeRepository.deleteAll();
		userRepository.deleteAll();
	}

	@Test
	void list_is_public_and_returns_notices() throws Exception {
		User owner = userRepository.save(TestUserFactory.create("nv-list", "주인", Tier.OWNER));
		noticeRepository.save(Notice.create("리스트 공지", "내용", owner));

		mockMvc.perform(get("/api/notices"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.total").value(1))
				.andExpect(jsonPath("$.data[0].title").value("리스트 공지"))
				.andExpect(jsonPath("$.data[0].author").value("주인"));
	}

	@Test
	void detail_is_public_and_increments_view_count() throws Exception {
		User owner = userRepository.save(TestUserFactory.create("nv-vc", "주인", Tier.OWNER));
		Notice notice = noticeRepository.save(Notice.create("조회 공지", "본문", owner));

		mockMvc.perform(get("/api/notices/" + notice.getId()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.viewCount").value(1));

		mockMvc.perform(get("/api/notices/" + notice.getId()))
				.andExpect(jsonPath("$.viewCount").value(2));
	}

	@Test
	void detail_returns_not_found_for_missing_id() throws Exception {
		mockMvc.perform(get("/api/notices/99999"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.code").value("NOT_FOUND"));
	}

	@Test
	void create_returns_unauthorized_without_token() throws Exception {
		mockMvc.perform(post("/api/notices")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(new TestRequest("t", "c"))))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void create_returns_forbidden_for_non_owner() throws Exception {
		User pepper = userRepository.save(TestUserFactory.create("nv-pep", "고추", Tier.PEPPER));
		String token = jwtUtil.generateAccessToken(pepper.getId(), pepper.getTier().name());

		mockMvc.perform(post("/api/notices")
						.cookie(new Cookie("access_token", token))
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(new TestRequest("t", "c"))))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.code").value("FORBIDDEN"));
	}

	@Test
	void create_succeeds_for_owner_and_persists() throws Exception {
		User owner = userRepository.save(TestUserFactory.create("nv-create", "주인", Tier.OWNER));
		String token = jwtUtil.generateAccessToken(owner.getId(), owner.getTier().name());

		mockMvc.perform(post("/api/notices")
						.cookie(new Cookie("access_token", token))
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(new TestRequest("새 공지", "본문"))))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.title").value("새 공지"))
				.andExpect(jsonPath("$.author").value("주인"));

		assertThat(noticeRepository.count()).isEqualTo(1L);
	}

	@Test
	void create_returns_bad_request_when_title_blank() throws Exception {
		User owner = userRepository.save(TestUserFactory.create("nv-bad", "주인", Tier.OWNER));
		String token = jwtUtil.generateAccessToken(owner.getId(), owner.getTier().name());

		mockMvc.perform(post("/api/notices")
						.cookie(new Cookie("access_token", token))
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(new TestRequest("", "본문"))))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
	}

	@Test
	void update_modifies_notice_for_owner() throws Exception {
		User owner = userRepository.save(TestUserFactory.create("nv-up", "주인", Tier.OWNER));
		Notice notice = noticeRepository.save(Notice.create("이전", "이전 본문", owner));
		String token = jwtUtil.generateAccessToken(owner.getId(), owner.getTier().name());

		mockMvc.perform(patch("/api/notices/" + notice.getId())
						.cookie(new Cookie("access_token", token))
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(new TestRequest("수정", "수정 본문"))))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.title").value("수정"))
				.andExpect(jsonPath("$.content").value("수정 본문"));
	}

	@Test
	void delete_removes_for_owner_and_returns_404_when_missing() throws Exception {
		User owner = userRepository.save(TestUserFactory.create("nv-del", "주인", Tier.OWNER));
		Notice notice = noticeRepository.save(Notice.create("삭제 대상", "본문", owner));
		String token = jwtUtil.generateAccessToken(owner.getId(), owner.getTier().name());

		mockMvc.perform(delete("/api/notices/" + notice.getId())
						.cookie(new Cookie("access_token", token)))
				.andExpect(status().isNoContent());

		assertThat(noticeRepository.findById(notice.getId())).isEmpty();

		mockMvc.perform(delete("/api/notices/99999")
						.cookie(new Cookie("access_token", token)))
				.andExpect(status().isNotFound());
	}

	private record TestRequest(String title, String content) {
	}
}
