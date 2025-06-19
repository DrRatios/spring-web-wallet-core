package com.aleksgolds.spring.web.wallet.core.dto;

import com.aleksgolds.spring.web.wallet.core.model.Wallet;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WalletOperationDto {
    private UUID walletId;
    private String operationType;
    private Long amount;

    public enum OperationType {
        DEPOSIT, WITHDRAW;
    }
}
