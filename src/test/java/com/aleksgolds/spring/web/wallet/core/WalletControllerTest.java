package com.aleksgolds.spring.web.wallet.core;

import com.aleksgolds.spring.web.wallet.core.controller.WalletController;
import com.aleksgolds.spring.web.wallet.core.dto.WalletDto;
import com.aleksgolds.spring.web.wallet.core.dto.WalletOperationDto;
import com.aleksgolds.spring.web.wallet.core.exception.ResourceNotFoundException;
import com.aleksgolds.spring.web.wallet.core.exception.ValidationException;
import com.aleksgolds.spring.web.wallet.core.service.WalletService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WalletController.class)
@ExtendWith(MockitoExtension.class)
class WalletControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private WalletService walletService;

    private final UUID testWalletId = UUID.randomUUID();

    @Test
    void getWallet_ShouldReturnWallet() throws Exception {
        WalletDto walletDto = new WalletDto(testWalletId, 1000L);
        given(walletService.getWalletById(testWalletId)).willReturn(walletDto);

        mockMvc.perform(get("/api/v1/wallets/{id}", testWalletId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testWalletId.toString()))
                .andExpect(jsonPath("$.balance").value(1000L));
    }

    @Test
    void createWallet_ShouldCreateNewWallet() throws Exception {
        WalletDto requestDto = new WalletDto(null, 500L);
        WalletDto responseDto = new WalletDto(testWalletId, 500L);

        given(walletService.createWallet(any(WalletDto.class))).willReturn(responseDto);

        mockMvc.perform(post("/api/v1/wallets/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.balance").value(500));
    }

    @Test
    void processWalletOperation_Deposit_ShouldIncreaseBalance() throws Exception {
        WalletOperationDto operationDto = new WalletOperationDto(
                testWalletId,
                "DEPOSIT",
                200L);

        WalletDto walletDto = new WalletDto(testWalletId, 1200L);

        given(walletService.processOperation(any(WalletOperationDto.class))).willReturn(walletDto);

        mockMvc.perform(post("/api/v1/wallets/operation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(operationDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testWalletId.toString()))
                .andExpect(jsonPath("$.balance").value(1200));
    }

    @Test
    void processWalletOperation_WithInvalidInput_ShouldReturnBadRequest() throws Exception {
        WalletOperationDto invalidOperationDto = new WalletOperationDto(
                null,
                "",
                null
        );

        List<String> expectedErrors = List.of("walletOperationDto wallet id is required",
                "walletOperationDto amount is required",
                "walletOperationDto operationType is required"
        );

        when(walletService.processOperation(any()))
                .thenThrow(new ValidationException(expectedErrors));

        mockMvc.perform(post("/api/v1/wallets/operation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidOperationDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorFieldsMessages[0]")
                        .value("walletOperationDto wallet id is required"))
                .andExpect(jsonPath("$.errorFieldsMessages[1]")
                        .value("walletOperationDto amount is required"))
                .andExpect(jsonPath("$.errorFieldsMessages[2]")
                        .value("walletOperationDto operationType is required"));
    }

    @Test
    void getWallet_NotFound_ShouldReturn404() throws Exception {
        given(walletService.getWalletById(testWalletId))
                .willThrow(new ResourceNotFoundException("RESOURCE NOT FOUND "));

        mockMvc.perform(get("/api/v1/wallets/{id}", testWalletId))
                .andExpect(status().isNotFound());
    }

    @Test
    void processOperation_InvalidOperationType_ShouldReturnValidationErrors() throws Exception {
        WalletOperationDto operationDto = new WalletOperationDto(
                testWalletId,
                "INVALID_OPERATION",  // невалидный тип
                100L
        );

        List<String> expectedErrors = List.of(
                "walletOperationDto operationType must be DEPOSIT or WITHDRAW"
        );

        when(walletService.processOperation(any()))
                .thenThrow(new ValidationException(expectedErrors));

        mockMvc.perform(post("/api/v1/wallets/operation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(operationDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorFieldsMessages[0]")
                        .value("walletOperationDto operationType must be DEPOSIT or WITHDRAW"));
    }
}
