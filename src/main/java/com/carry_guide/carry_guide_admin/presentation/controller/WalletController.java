package com.carry_guide.carry_guide_admin.presentation.controller;

import com.carry_guide.carry_guide_admin.dto.request.wallet.CashInRequest;
import com.carry_guide.carry_guide_admin.dto.request.wallet.XenditEwalletWebhookPayload;
import com.carry_guide.carry_guide_admin.dto.response.wallet.CashInInitResponse;
import com.carry_guide.carry_guide_admin.dto.response.wallet.WalletResponse;
import com.carry_guide.carry_guide_admin.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class WalletController {
    private final WalletService walletService;

    @PostMapping("/public/wallet/cash-in")
    public ResponseEntity<CashInInitResponse> createCashIn(
            @RequestBody CashInRequest request
    ) {
        CashInInitResponse res = walletService.createCashIn(request);
        return ResponseEntity.ok(res);
    }

    @GetMapping("/wallet")
    public ResponseEntity<WalletResponse> getWallet(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String mobileNumber
    ) {
        WalletResponse res = walletService.getWallet(email, mobileNumber);
        return ResponseEntity.ok(res);
    }

    // Xendit webhook callback (configure this URL in Xendit dashboard)
    @PostMapping("/webhook/xendit/ewallet")
    public ResponseEntity<Void> handleXenditWebhook(
            @RequestBody XenditEwalletWebhookPayload payload
    ) {
        walletService.handleXenditWebhook(payload);
        return ResponseEntity.ok().build();
    }
}
