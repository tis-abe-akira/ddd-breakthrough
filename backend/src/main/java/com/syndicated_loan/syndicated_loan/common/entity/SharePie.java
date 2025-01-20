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
public class SharePie {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ElementCollection
    @CollectionTable(name = "share_pie_entries",
            joinColumns = @JoinColumn(name = "share_pie_id"))
    @MapKeyColumn(name = "investor_id")
    @Column(name = "share_percentage", nullable = false, precision = 19, scale = 4)
    private Map<Long, BigDecimal> shares = new HashMap<>();

    @Version
    private Long version;

    @OneToOne(mappedBy = "sharePie")
    private Position position;
}
