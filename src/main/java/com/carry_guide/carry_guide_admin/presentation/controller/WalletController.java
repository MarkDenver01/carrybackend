package com.carry_guide.carry_guide_admin.presentation.controller;

import com.carry_guide.carry_guide_admin.dto.request.wallet.CashInRequest;
import com.carry_guide.carry_guide_admin.dto.response.wallet.CashInInitResponse;
import com.carry_guide.carry_guide_admin.dto.response.wallet.WalletResponse;
import com.carry_guide.carry_guide_admin.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
public class WalletController {
    private final WalletService walletService;

    @PostMapping("/cash-in")
    public ResponseEntity<CashInInitResponse> cashIn(@RequestBody CashInRequest request) {
        return ResponseEntity.ok(walletService.initiateCashIn(request));
    }

    @GetMapping("/balance")
    public ResponseEntity<WalletResponse> getBalance(@RequestParam String mobileNumber) {
        return ResponseEntity.ok(walletService.getWalletBalance(mobileNumber));
    }
}
