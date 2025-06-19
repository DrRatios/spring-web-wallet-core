package com.aleksgolds.spring.web.wallet.core.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "wallet_operation")
public class WalletOperation {
    @Id
    @Column(name = "id")
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id")
    private Wallet wallet;

    @Column(name = "operation_type")
    private String operationType;

    @Column(name = "amount")
    private Long amount;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

}
