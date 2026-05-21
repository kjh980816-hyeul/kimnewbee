package com.gochubat.domain.attendance.repository;

import com.gochubat.domain.attendance.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

	Optional<Attendance> findByUserIdAndDate(Long userId, LocalDate date);

	@Query("SELECT a.date FROM Attendance a WHERE a.userId = :userId AND a.date BETWEEN :from AND :to ORDER BY a.date ASC")
	List<LocalDate> findDatesInRange(@Param("userId") Long userId, @Param("from") LocalDate from, @Param("to") LocalDate to);

	@Query("SELECT a FROM Attendance a WHERE a.userId = :userId ORDER BY a.date DESC")
	List<Attendance> findRecentByUserId(@Param("userId") Long userId, org.springframework.data.domain.Pageable pageable);
}
