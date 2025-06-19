package com.aleksgolds.spring.web.wallet.core;
import com.aleksgolds.spring.web.wallet.core.conventers.WalletConventer;
import com.aleksgolds.spring.web.wallet.core.dto.WalletDto;
import com.aleksgolds.spring.web.wallet.core.dto.WalletOperationDto;
import com.aleksgolds.spring.web.wallet.core.exception.InsufficientFundsException;
import com.aleksgolds.spring.web.wallet.core.exception.ResourceNotFoundException;
import com.aleksgolds.spring.web.wallet.core.model.Wallet;
import com.aleksgolds.spring.web.wallet.core.model.WalletOperation;
import com.aleksgolds.spring.web.wallet.core.repository.WalletOperationRepository;
import com.aleksgolds.spring.web.wallet.core.repository.WalletRepository;
import com.aleksgolds.spring.web.wallet.core.service.WalletService;
import com.aleksgolds.spring.web.wallet.core.validator.WalletValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.retry.annotation.EnableRetry;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@EnableRetry
class WalletServiceTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private WalletOperationRepository walletOperationRepository;

    @Mock
    private WalletConventer walletConverter;

    @Mock
    private WalletValidator walletValidator;

    @InjectMocks
    private WalletService walletService;

    private final UUID testWalletId = UUID.randomUUID();
    private final Wallet testWallet = Wallet.builder()
            .id(testWalletId)
            .balance(1000L)
            .build();

    @Test
    void getWalletById_ShouldReturnWallet_WhenExists() {
        WalletDto expectedDto = new WalletDto(testWalletId, 1000L);
        when(walletRepository.findWalletById(testWalletId)).thenReturn(Optional.of(testWallet));
        when(walletConverter.walletModelToDto(testWallet)).thenReturn(expectedDto);

        WalletDto result = walletService.getWalletById(testWalletId);

        assertEquals(expectedDto, result);
        verify(walletRepository).findWalletById(testWalletId);
    }

    @Test
    void getWalletById_ShouldThrow_WhenNotFound() {
        when(walletRepository.findWalletById(testWalletId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                walletService.getWalletById(testWalletId));
    }

    @Test
    @DisplayName("Should throw InsufficientFundsException when balance is low")
    void processOperation_ShouldThrow_WhenInsufficientFunds() {

        WalletOperationDto withdrawDto = new WalletOperationDto(
                testWalletId, "WITHDRAW", 1500L);

        when(walletRepository.findWithVersionById(testWalletId))
                .thenReturn(Optional.of(testWallet));

        assertThrows(InsufficientFundsException.class, () ->
                walletService.processOperation(withdrawDto));
    }

    @Test
    void createWallet_ShouldReturnNewWallet() {
        WalletDto requestDto = new WalletDto(null, 1000L);
        WalletDto expectedDto = new WalletDto(testWalletId, 1000L);

        when(walletRepository.save(any())).thenAnswer(invocation -> {
            Wallet w = invocation.getArgument(0);
            w.setId(testWalletId);
            return w;
        });
        when(walletConverter.walletModelToDto(any())).thenReturn(expectedDto);

        WalletDto result = walletService.createWallet(requestDto);

        assertNotNull(result.getId());
        assertEquals(1000L, result.getBalance());
        verify(walletRepository).save(any(Wallet.class));
    }

    @Test
    void processOperation_ShouldValidateInput() {
        WalletOperationDto invalidDto = new WalletOperationDto(null, null, -100L);

        doThrow(new IllegalArgumentException("Invalid operation"))
                .when(walletValidator).validateWalletOperation(invalidDto);

        assertThrows(IllegalArgumentException.class, () ->
                walletService.processOperation(invalidDto));
    }
    @Test
    void getWalletOperationById_ShouldReturnOperations() {
        // Arrange
        WalletOperation operation1 = WalletOperation.builder()
                .id(UUID.randomUUID())
                .wallet(testWallet)
                .operationType("DEPOSIT")
                .amount(500L)
                .build();

        when(walletOperationRepository.findAllOperationByWalletId(testWalletId))
                .thenReturn(List.of(operation1));
        when(walletConverter.WalletOperationModelToDto(operation1))
                .thenReturn(new WalletOperationDto(
                        testWalletId,
                        "DEPOSIT",
                        500L));

        // Act
        List<WalletOperationDto> result = walletService.getWalletOperationById(testWalletId);

        // Assert
        assertEquals(1, result.size());
        assertEquals("DEPOSIT", result.get(0).getOperationType());
    }
}