package com.gochubat.domain.post.repository;

import com.gochubat.domain.post.entity.BoardType;
import com.gochubat.domain.post.entity.Post;
import com.gochubat.domain.user.entity.Tier;
import com.gochubat.domain.user.entity.User;
import com.gochubat.domain.user.repository.UserRepository;
import com.gochubat.support.TestUserFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class PostRepositoryTest {

	@Autowired
	private PostRepository postRepository;

	@Autowired
	private UserRepository userRepository;

	private User user;

	@BeforeEach
	void setUp() {
		user = userRepository.save(TestUserFactory.create("nv-post", "작성자", Tier.PEPPER));
	}

	@Test
	void find_by_type_returns_only_matching_type_with_author() {
		postRepository.save(Post.createFree("자유 글", "본문", user));
		postRepository.save(Post.createFanart("팬아트 글", "설명", user, "https://img/1.png"));
		postRepository.save(Post.createClip("클립", "설명", user, "https://youtu.be/x"));

		List<Post> freePosts = postRepository.findByTypeWithAuthor(BoardType.FREE);

		assertThat(freePosts).hasSize(1);
		assertThat(freePosts.get(0).getTitle()).isEqualTo("자유 글");
		assertThat(freePosts.get(0).getAuthor().getNickname()).isEqualTo("작성자");
	}

	@Test
	void find_by_id_and_type_returns_post_when_match() {
		Post saved = postRepository.save(Post.createFree("타이틀", "본문", user));

		Optional<Post> found = postRepository.findByIdAndTypeWithAuthor(saved.getId(), BoardType.FREE);

		assertThat(found).isPresent();
		assertThat(found.get().getTitle()).isEqualTo("타이틀");
	}

	@Test
	void find_by_id_and_type_returns_empty_when_type_mismatch() {
		Post saved = postRepository.save(Post.createFree("자유 글", "본문", user));

		assertThat(postRepository.findByIdAndTypeWithAuthor(saved.getId(), BoardType.FANART)).isEmpty();
	}

	@Test
	void find_by_type_orders_by_created_desc_with_id_tiebreak() {
		Post first = postRepository.save(Post.createFree("첫 글", "1", user));
		Post second = postRepository.save(Post.createFree("두번째", "2", user));

		List<Post> posts = postRepository.findByTypeWithAuthor(BoardType.FREE);

		assertThat(posts).extracting(Post::getId).containsExactly(second.getId(), first.getId());
	}
}
