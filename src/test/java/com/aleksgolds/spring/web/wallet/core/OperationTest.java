package com.aleksgolds.spring.web.wallet.core;

import com.aleksgolds.spring.web.wallet.core.conventers.WalletConventer;
import com.aleksgolds.spring.web.wallet.core.dto.WalletDto;
import com.aleksgolds.spring.web.wallet.core.dto.WalletOperationDto;
import com.aleksgolds.spring.web.wallet.core.exception.*;
import com.aleksgolds.spring.web.wallet.core.model.Wallet;
import com.aleksgolds.spring.web.wallet.core.repository.WalletOperationRepository;
import com.aleksgolds.spring.web.wallet.core.repository.WalletRepository;
import com.aleksgolds.spring.web.wallet.core.service.WalletService;
import com.aleksgolds.spring.web.wallet.core.validator.WalletValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OperationTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private WalletOperationRepository walletOperationRepository;

    @Mock
    private WalletConventer walletConventer;

    @Mock
    private WalletValidator walletValidator;

    @InjectMocks
    private WalletService walletService;

    private UUID walletId;
    private Wallet wallet;
    private WalletDto walletDto;
    private WalletOperationDto depositOperation;
    private WalletOperationDto withdrawOperation;

    @BeforeEach
    void setUp() {
        walletId = UUID.randomUUID();
        wallet = new Wallet();
        wallet.setId(walletId);
        wallet.setBalance(1000L);
        wallet.setVersion(1L); // Важно для оптимистичной блокировки

        walletDto = new WalletDto();
        walletDto.setId(walletId);
        walletDto.setBalance(1000L);

        depositOperation = new WalletOperationDto();
        depositOperation.setWalletId(walletId);
        depositOperation.setAmount(500L);
        depositOperation.setOperationType("DEPOSIT");

        withdrawOperation = new WalletOperationDto();
        withdrawOperation.setWalletId(walletId);
        withdrawOperation.setAmount(300L);
        withdrawOperation.setOperationType("WITHDRAW");
    }

    @Test
    void processOperation_Deposit_ShouldIncreaseBalance() {
        Wallet savedWallet = new Wallet();
        savedWallet.setId(walletId);
        savedWallet.setBalance(1500L); // Ожидаемый баланс после депозита

        when(walletRepository.findWithVersionById(walletId)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any(Wallet.class))).thenReturn(savedWallet);

        when(walletConventer.walletModelToDto(any(Wallet.class)))
                .thenAnswer(inv -> {
                    Wallet w = inv.getArgument(0);
                    return new WalletDto(w.getId(), w.getBalance());
                });

        WalletDto result = walletService.processOperation(depositOperation);

        assertThat(result.getBalance()).isEqualTo(1500L);
        verify(walletRepository).save(argThat(w -> w.getBalance() == 1500L));
        verify(walletOperationRepository).save(argThat(op ->
                op.getAmount() == 500L &&
                        op.getOperationType().equals("DEPOSIT")
        ));
    }

    @Test
    void processOperation_Withdraw_ShouldDecreaseBalance() {
        Wallet savedWallet = new Wallet();
        savedWallet.setId(walletId);
        savedWallet.setBalance(700L); // Ожидаемый баланс после снятия

        when(walletRepository.findWithVersionById(walletId)).thenReturn(Optional.of(wallet));
        when(walletRepository.save(any(Wallet.class))).thenReturn(savedWallet);

        when(walletConventer.walletModelToDto(any(Wallet.class)))
                .thenAnswer(inv -> {
                    Wallet w = inv.getArgument(0);
                    return new WalletDto(w.getId(), w.getBalance());
                });

        WalletDto result = walletService.processOperation(withdrawOperation);

        assertThat(result.getBalance()).isEqualTo(700L);
        verify(walletRepository).save(argThat(w -> w.getBalance() == 700L));
        verify(walletOperationRepository).save(argThat(op ->
                op.getAmount() == 300L &&
                        op.getOperationType().equals("WITHDRAW")
        ));
    }

    @Test
    void processOperation_WalletNotFound_ShouldThrowException() {
        when(walletRepository.findWithVersionById(walletId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> walletService.processOperation(depositOperation))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Wallet with id " + walletId + " not found");
    }

    @Test
    void processOperation_InsufficientFunds_ShouldThrowException() {
        withdrawOperation.setAmount(1500L);
        when(walletRepository.findWithVersionById(walletId)).thenReturn(Optional.of(wallet));
        when(walletConventer.walletModelToDto(any(Wallet.class))).thenReturn(walletDto);

        assertThatThrownBy(() -> walletService.processOperation(withdrawOperation))
                .isInstanceOf(InsufficientFundsException.class)
                .hasMessageContaining("Not enough funds in the wallet with id: " + walletId);
    }

    @Test
    void createWallet_ShouldCreateNewWallet() {
        when(walletRepository.save(any(Wallet.class))).thenReturn(wallet);
        when(walletConventer.walletModelToDto(wallet)).thenReturn(walletDto);

        WalletDto result = walletService.createWallet(walletDto);

        assertThat(result.getId()).isEqualTo(walletId);
        assertThat(result.getBalance()).isEqualTo(1000L);
        verify(walletRepository).save(any(Wallet.class));
    }

    @Test
    void getWalletById_ShouldReturnWallet() {
        when(walletRepository.findWalletById(walletId)).thenReturn(Optional.of(wallet));
        when(walletConventer.walletModelToDto(wallet)).thenReturn(walletDto);

        WalletDto result = walletService.getWalletById(walletId);

        assertThat(result.getId()).isEqualTo(walletId);
    }

    @Test
    void getWalletById_NotFound_ShouldThrowException() {
        when(walletRepository.findWalletById(walletId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> walletService.getWalletById(walletId))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
