package com.gochubat.domain.board.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gochubat.domain.board.entity.Board;
import com.gochubat.domain.board.entity.BoardLayout;
import com.gochubat.domain.board.repository.BoardRepository;
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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.servlet.http.Cookie;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class BoardControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JwtUtil jwtUtil;

	@Autowired
	private BoardRepository boardRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ObjectMapper objectMapper;

	private User pepper;
	private User owner;
	private String pepperToken;
	private String ownerToken;

	@BeforeEach
	void setUp() {
		pepper = userRepository.save(TestUserFactory.create("nv-bd-p", "고추", Tier.PEPPER));
		owner = userRepository.save(TestUserFactory.create("nv-bd-o", "주인", Tier.OWNER));
		pepperToken = jwtUtil.generateAccessToken(pepper.getId(), pepper.getTier().name());
		ownerToken = jwtUtil.generateAccessToken(owner.getId(), owner.getTier().name());
	}

	@AfterEach
	void cleanup() {
		userRepository.deleteAll();
	}

	@Test
	void public_list_returns_active_boards_only() throws Exception {
		mockMvc.perform(get("/api/boards"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.total").value(org.hamcrest.Matchers.greaterThan(0)));
	}

	@Test
	void admin_list_requires_owner_403_for_pepper() throws Exception {
		mockMvc.perform(get("/api/admin/boards").cookie(new Cookie("access_token", pepperToken)))
				.andExpect(status().isForbidden());
	}

	@Test
	void admin_list_returns_all_boards_for_owner() throws Exception {
		mockMvc.perform(get("/api/admin/boards").cookie(new Cookie("access_token", ownerToken)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.total").value(org.hamcrest.Matchers.greaterThan(0)))
				.andExpect(jsonPath("$.data[0].slug").exists());
	}

	@Test
	void create_succeeds_for_owner_and_persists() throws Exception {
		Map<String, Object> body = Map.of(
				"slug", "test-board",
				"name", "테스트 게시판",
				"layout", "LIST",
				"readTier", "SEED",
				"writeTier", "PEPPER"
		);

		mockMvc.perform(post("/api/admin/boards")
						.cookie(new Cookie("access_token", ownerToken))
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(body)))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.slug").value("test-board"))
				.andExpect(jsonPath("$.layout").value("list"))
				.andExpect(jsonPath("$.active").value(true));

		assertThat(boardRepository.existsBySlug("test-board")).isTrue();
	}

	@Test
	void create_rejects_invalid_slug_pattern() throws Exception {
		Map<String, Object> body = Map.of(
				"slug", "INVALID Slug!",
				"name", "x",
				"layout", "LIST",
				"readTier", "SEED",
				"writeTier", "PEPPER"
		);

		mockMvc.perform(post("/api/admin/boards")
						.cookie(new Cookie("access_token", ownerToken))
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(body)))
				.andExpect(status().isBadRequest());
	}

	@Test
	void create_rejects_duplicate_slug() throws Exception {
		Board existing = boardRepository.save(Board.create(
				"dup-test", "기존", BoardLayout.LIST, Tier.SEED, Tier.PEPPER, 100));

		Map<String, Object> body = Map.of(
				"slug", existing.getSlug(),
				"name", "중복",
				"layout", "LIST",
				"readTier", "SEED",
				"writeTier", "PEPPER"
		);

		mockMvc.perform(post("/api/admin/boards")
						.cookie(new Cookie("access_token", ownerToken))
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(body)))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.code").value("INVALID_REQUEST"));

		boardRepository.delete(existing);
	}

	@Test
	void update_changes_name_layout_tiers_active() throws Exception {
		Board board = boardRepository.save(Board.create(
				"upd-test", "이전", BoardLayout.LIST, Tier.SEED, Tier.PEPPER, 200));

		Map<String, Object> body = Map.of(
				"name", "수정된",
				"layout", "GALLERY",
				"readTier", "PEPPER",
				"writeTier", "CORN",
				"active", false
		);

		mockMvc.perform(patch("/api/admin/boards/" + board.getId())
						.cookie(new Cookie("access_token", ownerToken))
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(body)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value("수정된"))
				.andExpect(jsonPath("$.layout").value("gallery"))
				.andExpect(jsonPath("$.active").value(false));

		boardRepository.delete(board);
	}

	@Test
	void delete_succeeds_for_owner() throws Exception {
		Board board = boardRepository.save(Board.create(
				"del-test", "삭제", BoardLayout.LIST, Tier.SEED, Tier.PEPPER, 300));
		Long id = board.getId();

		mockMvc.perform(delete("/api/admin/boards/" + id)
						.cookie(new Cookie("access_token", ownerToken)))
				.andExpect(status().isNoContent());

		assertThat(boardRepository.findById(id)).isEmpty();
	}

	@Test
	void reorder_updates_orderIndex_in_request_order() throws Exception {
		Board a = boardRepository.save(Board.create("ord-a", "A", BoardLayout.LIST, Tier.SEED, Tier.PEPPER, 401));
		Board b = boardRepository.save(Board.create("ord-b", "B", BoardLayout.LIST, Tier.SEED, Tier.PEPPER, 402));

		List<Board> all = boardRepository.findAllByOrderByOrderIndexAscIdAsc();
		List<Long> reversed = all.stream().map(Board::getId).toList().reversed();

		Map<String, Object> body = Map.of("orderedIds", reversed);

		mockMvc.perform(put("/api/admin/boards/order")
						.cookie(new Cookie("access_token", ownerToken))
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(body)))
				.andExpect(status().isOk());

		boardRepository.delete(a);
		boardRepository.delete(b);
	}

	@Test
	void create_403_for_pepper() throws Exception {
		Map<String, Object> body = Map.of(
				"slug", "x", "name", "x", "layout", "LIST", "readTier", "SEED", "writeTier", "PEPPER"
		);
		mockMvc.perform(post("/api/admin/boards")
						.cookie(new Cookie("access_token", pepperToken))
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(body)))
				.andExpect(status().isForbidden());
	}
}
