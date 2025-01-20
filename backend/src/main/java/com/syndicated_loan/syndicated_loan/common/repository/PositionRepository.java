package com.syndicated_loan.syndicated_loan.common.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.syndicated_loan.syndicated_loan.common.entity.Position;

@Repository
public interface PositionRepository extends JpaRepository<Position, Long> {
}
