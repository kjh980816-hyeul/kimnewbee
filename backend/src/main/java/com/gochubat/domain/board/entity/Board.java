package com.gochubat.domain.board.entity;

import com.gochubat.domain.user.entity.Tier;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "boards")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Board {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true, length = 60)
	private String slug;

	@Column(nullable = false, length = 60)
	private String name;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 16)
	private BoardLayout layout;

	@Enumerated(EnumType.STRING)
	@Column(name = "read_tier", nullable = false, length = 16)
	private Tier readTier;

	@Enumerated(EnumType.STRING)
	@Column(name = "write_tier", nullable = false, length = 16)
	private Tier writeTier;

	@Column(name = "order_index", nullable = false)
	private int orderIndex;

	@Column(nullable = false)
	private boolean active;

	@Column(name = "created_at", nullable = false, updatable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	private Board(String slug, String name, BoardLayout layout, Tier readTier, Tier writeTier, int orderIndex) {
		this.slug = slug;
		this.name = name;
		this.layout = layout;
		this.readTier = readTier;
		this.writeTier = writeTier;
		this.orderIndex = orderIndex;
		this.active = true;
	}

	public static Board create(String slug, String name, BoardLayout layout, Tier readTier, Tier writeTier, int orderIndex) {
		return new Board(slug, name, layout, readTier, writeTier, orderIndex);
	}

	public void rename(String name) {
		this.name = name;
	}

	public void changeLayout(BoardLayout layout) {
		this.layout = layout;
	}

	public void changeTiers(Tier readTier, Tier writeTier) {
		this.readTier = readTier;
		this.writeTier = writeTier;
	}

	public void changeOrder(int orderIndex) {
		this.orderIndex = orderIndex;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	@PrePersist
	void prePersist() {
		LocalDateTime now = LocalDateTime.now();
		this.createdAt = now;
		this.updatedAt = now;
	}

	@PreUpdate
	void preUpdate() {
		this.updatedAt = LocalDateTime.now();
	}
}
