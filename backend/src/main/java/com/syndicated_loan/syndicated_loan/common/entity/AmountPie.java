package com.syndicated_loan.syndicated_loan.common.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Entity
@Getter
@Setter
public class AmountPie {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ElementCollection
    @CollectionTable(name = "amount_pie_entries",
            joinColumns = @JoinColumn(name = "amount_pie_id"))
    @MapKeyColumn(name = "investor_id")
    @Column(name = "amount", nullable = false, precision = 19, scale = 4)
    private Map<Long, BigDecimal> amounts = new HashMap<>();

    @Version
    private Long version;

    @OneToOne(mappedBy = "amountPie")
    private Transaction transaction;
}
