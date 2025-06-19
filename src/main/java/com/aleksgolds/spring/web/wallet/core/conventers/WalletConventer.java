package com.aleksgolds.spring.web.wallet.core.conventers;

import com.aleksgolds.spring.web.wallet.core.dto.WalletDto;
import com.aleksgolds.spring.web.wallet.core.dto.WalletOperationDto;
import com.aleksgolds.spring.web.wallet.core.model.Wallet;
import com.aleksgolds.spring.web.wallet.core.model.WalletOperation;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class WalletConventer {

    public WalletDto walletModelToDto(Wallet wallet) {
        return WalletDto.builder()
                .id(wallet.getId())
                .balance(wallet.getBalance())
                .build();
    }
    public Wallet WalletDtoToModel(WalletDto walletDto) {
        return Wallet.builder()
                .id(walletDto.getId())
                .balance(walletDto.getBalance())
                .build();
    }
    public WalletOperationDto WalletOperationModelToDto(WalletOperation walletOperation) {
        return WalletOperationDto.builder()
                .walletId(walletOperation.getWallet().getId())
                .amount(walletOperation.getAmount())
                .operationType(walletOperation.getOperationType())
                .build();
    }

    public WalletOperation WalletOperationDtoToModel(WalletOperationDto walletOperationDto) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
