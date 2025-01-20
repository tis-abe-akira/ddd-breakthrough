package com.syndicated_loan.syndicated_loan.common.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
public class Syndicate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "lead_bank_id", nullable = false)
    private Investor leadBank;

    @ManyToMany
    @JoinTable(
        name = "syndicate_members",
        joinColumns = @JoinColumn(name = "syndicate_id"),
        inverseJoinColumns = @JoinColumn(name = "investor_id")
    )
    private Set<Investor> members = new HashSet<>();

    @Column(name = "total_commitment", nullable = false, precision = 19, scale = 4)
    private BigDecimal totalCommitment;

    @OneToMany(mappedBy = "syndicate")
    private Set<Facility> facilities = new HashSet<>();

    @Version
    private Long version;
}
