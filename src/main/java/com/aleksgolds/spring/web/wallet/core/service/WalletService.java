package com.aleksgolds.spring.web.wallet.core.service;

import com.aleksgolds.spring.web.wallet.core.conventers.WalletConventer;
import com.aleksgolds.spring.web.wallet.core.dto.WalletDto;
import com.aleksgolds.spring.web.wallet.core.dto.WalletOperationDto;
import com.aleksgolds.spring.web.wallet.core.exception.InsufficientFundsException;
import com.aleksgolds.spring.web.wallet.core.exception.ResourceNotFoundException;
import com.aleksgolds.spring.web.wallet.core.exception.WalletServiceException;
import com.aleksgolds.spring.web.wallet.core.model.Wallet;
import com.aleksgolds.spring.web.wallet.core.model.WalletOperation;
import com.aleksgolds.spring.web.wallet.core.repository.WalletOperationRepository;
import com.aleksgolds.spring.web.wallet.core.repository.WalletRepository;
import com.aleksgolds.spring.web.wallet.core.validator.WalletValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WalletService {
    private final WalletRepository walletRepository;
    private final WalletOperationRepository walletOperationRepository;
    private final WalletConventer walletConventer;
    private final WalletValidator walletValidator;

    public UUID generateWalletUuid() {
        return UUID.randomUUID();
    }

    public WalletDto getWalletById(UUID id) {
        try{
            Wallet wallet = walletRepository.findWalletById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Wallet not found with id: " + id));
            return walletConventer.walletModelToDto(wallet);
        } catch (DataAccessException e) {
            throw new WalletServiceException("Failed to fetch wallet due to database error");
        }
    }

    public List<WalletOperationDto> getWalletOperationById(UUID id) {
        try{
            List<WalletOperation> wallets = walletOperationRepository.findAllOperationByWalletId(id);
            return wallets.stream()
                    .map(walletConventer::WalletOperationModelToDto)
                    .collect(Collectors.toList());

        } catch (DataAccessException e) {
            throw new WalletServiceException("Failed to fetch wallet due to database error");
        }
    }



    @Retryable(
            retryFor = org.springframework.dao.OptimisticLockingFailureException.class,
            maxAttempts = 5,
            backoff = @Backoff(delay = 200, multiplier = 2))
    @Transactional(isolation = Isolation.READ_COMMITTED, timeout = 5)
    public WalletDto processOperation(WalletOperationDto walletOperationDto) {
        walletValidator.validateWalletOperation(walletOperationDto);
        Wallet wallet = walletRepository.findWithVersionById(walletOperationDto.getWalletId())
                .orElseThrow(() -> new ResourceNotFoundException("Wallet with id " + walletOperationDto.getWalletId() + " not found"));
        walletValidator.validateWallet(walletConventer.walletModelToDto(wallet));
        long newBalance;
        if(walletOperationDto.getOperationType().equals(WalletOperationDto.OperationType.DEPOSIT.toString())){
            newBalance = wallet.getBalance() + walletOperationDto.getAmount();
        } else {
            if(wallet.getBalance() < walletOperationDto.getAmount()){
                throw new InsufficientFundsException("Not enough funds in the wallet with id: " + walletOperationDto.getWalletId());
            }
            newBalance = wallet.getBalance() - walletOperationDto.getAmount();
        }
        wallet.setBalance(newBalance);
        walletRepository.save(wallet);

        WalletOperation walletOperation = WalletOperation.builder()
                .id(generateWalletUuid())
                .wallet(wallet)
                .operationType(walletOperationDto.getOperationType())
                .amount(walletOperationDto.getAmount())
                .build();
        walletOperationRepository.save(walletOperation);
        return walletConventer.walletModelToDto(wallet);
    }

    @Retryable(retryFor = {org.springframework.dao.OptimisticLockingFailureException.class})
    @Transactional(isolation = Isolation.READ_COMMITTED, timeout = 5)
    public WalletDto createWallet(WalletDto dto) {
        UUID id = generateWalletUuid();
        Wallet wallet = new Wallet();
        wallet.setBalance(dto.getBalance());
        wallet.setId(id);

        Wallet savedWallet = walletRepository.save(wallet);
        return walletConventer.walletModelToDto(savedWallet);
    }
}
