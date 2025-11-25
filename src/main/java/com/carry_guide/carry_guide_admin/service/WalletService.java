package com.carry_guide.carry_guide_admin.service;


import com.carry_guide.carry_guide_admin.dto.request.wallet.CashInRequest;
import com.carry_guide.carry_guide_admin.dto.response.wallet.CashInInitResponse;
import com.carry_guide.carry_guide_admin.dto.response.wallet.WalletResponse;
import com.carry_guide.carry_guide_admin.dto.response.wallet.XenditInvoiceResponse;
import com.carry_guide.carry_guide_admin.model.entity.Wallet;
import com.carry_guide.carry_guide_admin.model.entity.WalletTransaction;
import com.carry_guide.carry_guide_admin.repository.JpaWalletRepository;
import com.carry_guide.carry_guide_admin.repository.JpaWalletTransactionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WalletService {
    private final JpaWalletRepository walletRepository;
    private final JpaWalletTransactionRepository walletTransactionRepository;
    private final XenditService xenditService;

    @Transactional
    public CashInInitResponse initiateCashIn(CashInRequest request) {
        // external_id must be unique in Xendit
        String externalId = "cashin_" + request.getUserId() + "_" + System.currentTimeMillis();

        XenditInvoiceResponse invoice = xenditService.createGcashInvoice(
                externalId,
                request.getAmount(),
                request.getEmail()
        );

        // Save transaction as PENDING
        WalletTransaction tx = WalletTransaction.builder()
                .userId(request.getUserId())
                .externalId(externalId)
                .amount(request.getAmount())
                .status("PENDING")
                .build();

        walletTransactionRepository.save(tx);

        return new CashInInitResponse(
                invoice.getId(),
                invoice.getExternalId(),
                invoice.getInvoiceUrl(),
                invoice.getStatus()
        );
    }

    @Transactional
    public void handleInvoiceWebhook(String status, String externalId, Long paidAmount) {
        WalletTransaction tx = walletTransactionRepository.findByExternalId(externalId)
                .orElse(null);

        if (tx == null) {
            System.out.println("⚠ No matching wallet transaction for external_id: " + externalId);
            return;
        }

        // Avoid double processing
        if ("PAID".equals(tx.getStatus())) {
            System.out.println("⚠ Transaction already PAID, skipping: " + externalId);
            return;
        }

        tx.setStatus(status);
        walletTransactionRepository.save(tx);

        if ("PAID".equals(status)) {
            Wallet wallet = walletRepository.findByUserId(tx.getUserId())
                    .orElseGet(() -> {
                        Wallet w = new Wallet();
                        w.setUserId(tx.getUserId());
                        w.setBalance(0L);
                        return walletRepository.save(w);
                    });

            Long newBalance = wallet.getBalance() + paidAmount;
            wallet.setBalance(newBalance);
            walletRepository.save(wallet);

            System.out.println("💰 WALLET CREDITED userId=" + wallet.getUserId()
                    + " amount=" + paidAmount
                    + " newBalance=" + newBalance);
        } else if ("EXPIRED".equals(status)) {
            System.out.println("⏳ Cash-in expired for externalId=" + externalId);
        } else if ("PAID_AFTER_EXPIRY".equals(status)) {
            System.out.println("⚠ Paid after expiry externalId=" + externalId);
        }
    }

    public WalletResponse getWalletBalance(Long userId) {
        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseGet(() -> {
                    // create wallet automatically if not found
                    Wallet w = new Wallet();
                    w.setUserId(userId);
                    w.setBalance(0L);
                    return walletRepository.save(w);
                });

        return new WalletResponse(wallet.getUserId(), wallet.getBalance());
    }
}
