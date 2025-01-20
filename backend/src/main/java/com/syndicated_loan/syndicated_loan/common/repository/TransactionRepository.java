package com.syndicated_loan.syndicated_loan.common.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

import com.syndicated_loan.syndicated_loan.common.entity.Transaction;
import com.syndicated_loan.syndicated_loan.common.entity.Position;

import java.time.LocalDateTime;
import java.util.List;

@NoRepositoryBean
public interface TransactionRepository<T extends Transaction> extends JpaRepository<T, Long> {
    List<T> findByRelatedPosition(Position position);
    List<T> findByRelatedPositionAndDateBetween(Position position, LocalDateTime startDate, LocalDateTime endDate);
    List<T> findByType(String type);
    List<T> findByRelatedPositionAndType(Position position, String type);
}
