package com.gochubat.domain.song.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gochubat.domain.song.entity.Song;
import com.gochubat.domain.song.repository.SongRepository;
import com.gochubat.domain.song.repository.SongVoteRepository;
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

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SongControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private JwtUtil jwtUtil;

	@Autowired
	private SongRepository songRepository;

	@Autowired
	private SongVoteRepository songVoteRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ObjectMapper objectMapper;

	private User user;
	private User other;
	private String userToken;
	private String otherToken;

	@BeforeEach
	void setUp() {
		user = userRepository.save(TestUserFactory.create("nv-song-1", "추천1", Tier.PEPPER));
		other = userRepository.save(TestUserFactory.create("nv-song-2", "추천2", Tier.PEPPER));
		userToken = jwtUtil.generateAccessToken(user.getId(), user.getTier().name());
		otherToken = jwtUtil.generateAccessToken(other.getId(), other.getTier().name());
	}

	@AfterEach
	void cleanup() {
		songVoteRepository.deleteAll();
		songRepository.deleteAll();
		userRepository.deleteAll();
	}

	@Test
	void list_is_public_with_zero_voted_for_anonymous() throws Exception {
		songRepository.save(Song.create("APT", "Rosé", "https://link", user));

		mockMvc.perform(get("/api/songs"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.total").value(1))
				.andExpect(jsonPath("$.data[0].title").value("APT"))
				.andExpect(jsonPath("$.data[0].votedByMe").value(false))
				.andExpect(jsonPath("$.data[0].voteCount").value(0));
	}

	@Test
	void create_requires_auth() throws Exception {
		mockMvc.perform(post("/api/songs")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(
								Map.of("title", "t", "artist", "a", "link", "https://l"))))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void create_persists_with_submitter() throws Exception {
		mockMvc.perform(post("/api/songs")
						.cookie(new Cookie("access_token", userToken))
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(
								Map.of("title", "신곡", "artist", "가수", "link", "https://music"))))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.title").value("신곡"))
				.andExpect(jsonPath("$.submittedBy").value("추천1"))
				.andExpect(jsonPath("$.voteCount").value(0));

		assertThat(songRepository.count()).isEqualTo(1L);
	}

	@Test
	void vote_toggle_round_trip() throws Exception {
		Song song = songRepository.save(Song.create("APT", "Rosé", "https://link", user));

		mockMvc.perform(post("/api/songs/" + song.getId() + "/vote")
						.cookie(new Cookie("access_token", userToken)))
				.andExpect(jsonPath("$.voted").value(true))
				.andExpect(jsonPath("$.voteCount").value(1));

		mockMvc.perform(post("/api/songs/" + song.getId() + "/vote")
						.cookie(new Cookie("access_token", userToken)))
				.andExpect(jsonPath("$.voted").value(false))
				.andExpect(jsonPath("$.voteCount").value(0));
	}

	@Test
	void list_aggregates_per_user_voted_flag() throws Exception {
		Song song = songRepository.save(Song.create("APT", "Rosé", "https://link", user));
		mockMvc.perform(post("/api/songs/" + song.getId() + "/vote")
						.cookie(new Cookie("access_token", userToken)));
		mockMvc.perform(post("/api/songs/" + song.getId() + "/vote")
						.cookie(new Cookie("access_token", otherToken)));

		mockMvc.perform(get("/api/songs").cookie(new Cookie("access_token", userToken)))
				.andExpect(jsonPath("$.data[0].voteCount").value(2))
				.andExpect(jsonPath("$.data[0].votedByMe").value(true));

		mockMvc.perform(get("/api/songs"))
				.andExpect(jsonPath("$.data[0].votedByMe").value(false));
	}

	@Test
	void vote_requires_auth() throws Exception {
		Song song = songRepository.save(Song.create("APT", "Rosé", "https://link", user));

		mockMvc.perform(post("/api/songs/" + song.getId() + "/vote"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void vote_returns_404_for_unknown_song() throws Exception {
		mockMvc.perform(post("/api/songs/99999/vote")
						.cookie(new Cookie("access_token", userToken)))
				.andExpect(status().isNotFound());
	}
}
