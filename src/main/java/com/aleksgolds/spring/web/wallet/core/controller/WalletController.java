package com.aleksgolds.spring.web.wallet.core.controller;

import com.aleksgolds.spring.web.wallet.core.dto.WalletDto;
import com.aleksgolds.spring.web.wallet.core.dto.WalletOperationDto;
import com.aleksgolds.spring.web.wallet.core.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/wallets")
@RequiredArgsConstructor
public class WalletController {
    private final WalletService walletService;

    @GetMapping("/{id}")
    public WalletDto getWallet(@PathVariable UUID id) {
        return walletService.getWalletById(id);
    }

    @PostMapping("/create")
    public WalletDto createWallet(@RequestBody WalletDto dto) {
        return walletService.createWallet(dto);
    }

    @PostMapping("/operation")
    public WalletDto processWalletOperation(@RequestBody WalletOperationDto dto) {
        return walletService.processOperation(dto);
    }

}
