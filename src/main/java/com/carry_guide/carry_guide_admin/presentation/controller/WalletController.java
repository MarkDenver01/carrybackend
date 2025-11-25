package com.carry_guide.carry_guide_admin.presentation.controller;

import com.carry_guide.carry_guide_admin.dto.request.wallet.CashInRequest;
import com.carry_guide.carry_guide_admin.dto.response.wallet.CashInInitResponse;
import com.carry_guide.carry_guide_admin.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/wallet")
@RequiredArgsConstructor
public class WalletController {
    private final WalletService walletService;

    @PostMapping("/cash-in")
    public ResponseEntity<CashInInitResponse> cashIn(@RequestBody CashInRequest request) {
        CashInInitResponse response = walletService.initiateCashIn(request);
        return ResponseEntity.ok(response);
    }
}
