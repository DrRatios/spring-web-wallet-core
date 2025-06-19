package com.aleksgolds.spring.web.wallet.core.validator;

import com.aleksgolds.spring.web.wallet.core.dto.WalletDto;
import com.aleksgolds.spring.web.wallet.core.dto.WalletOperationDto;
import com.aleksgolds.spring.web.wallet.core.exception.ValidationException;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;
@Component
public class WalletValidator {

    public void validateWallet(WalletDto walletDto) {
        List<String> errors = new ArrayList<>();
        if(walletDto.getId() == null) {
            errors.add("wallet id is required");
        }
        if(walletDto.getBalance() == null || walletDto.getBalance() < 0) {
            errors.add("wallet balance is required and must be positive");
        }
        if(!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
    }

    public void validateWalletOperation(WalletOperationDto walletOperationDto) {
        List<String> errors = new ArrayList<>();
        if(walletOperationDto.getWalletId() == null) {
            errors.add("walletOperationDto wallet id is required");
        }
        if(walletOperationDto.getAmount() == null) {
            errors.add("walletOperationDto amount is required");
        }
        if(walletOperationDto.getOperationType().isBlank()) {
            errors.add("walletOperationDto operationType is required");
        }
        if(!walletOperationDto.getOperationType().matches("DEPOSIT|WITHDRAW")) {
            errors.add("walletOperationDto operationType must be DEPOSIT or WITHDRAW");
        }
        if(!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
    }
}
