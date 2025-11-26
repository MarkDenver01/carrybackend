package com.carry_guide.carry_guide_admin.presentation.controller;

import com.carry_guide.carry_guide_admin.dto.request.wallet.CashInRequest;
import com.carry_guide.carry_guide_admin.dto.request.wallet.UpdateWalletRequest;
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

    // ✅ GET WALLET BALANCE
    @GetMapping("/balance/{mobile}")
    public ResponseEntity<WalletResponse> getCustomerWalletBalance(@PathVariable String mobile) {
        WalletResponse response = walletService.getWalletBalance(mobile);
        return ResponseEntity.ok(response);
    }

    // ✅ UPDATE WALLET BALANCE
    @PostMapping("/update")
    public ResponseEntity<?> updateWallet(@RequestBody UpdateWalletRequest request) {
        try {
            WalletResponse response = walletService.updateWallet(request);
            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
