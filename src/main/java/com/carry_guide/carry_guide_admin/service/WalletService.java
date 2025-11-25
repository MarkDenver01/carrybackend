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
        String externalId = "cashin_" + request.getMobileNumber() + "_" + System.currentTimeMillis();

        XenditInvoiceResponse invoice = xenditService.createGcashInvoice(
                externalId,
                request.getAmount(),
                request.getEmail()
        );

        WalletTransaction tx = WalletTransaction.builder()
                .mobileNumber(request.getMobileNumber())
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
            System.out.println("⚠ No matching wallet transaction: " + externalId);
            return;
        }

        if ("PAID".equals(tx.getStatus())) {
            return;
        }

        tx.setStatus(status);
        walletTransactionRepository.save(tx);

        if ("PAID".equals(status)) {

            Wallet wallet = walletRepository.findByMobileNumber(tx.getMobileNumber())
                    .orElseGet(() ->
                            walletRepository.save(
                                    Wallet.builder()
                                            .mobileNumber(tx.getMobileNumber())
                                            .balance(0L)
                                            .build()
                            ));

            Long newBalance = wallet.getBalance() + paidAmount;
            wallet.setBalance(newBalance);

            walletRepository.save(wallet);

            System.out.println("💰 Wallet credited: " + tx.getMobileNumber() + " new balance: " + newBalance);
        }
    }

    public WalletResponse getWalletBalance(String mobileNumber) {
        Wallet wallet = walletRepository.findByMobileNumber(mobileNumber)
                .orElseGet(() ->
                        walletRepository.save(
                                Wallet.builder()
                                        .mobileNumber(mobileNumber)
                                        .balance(0L)
                                        .build()
                        ));

        return new WalletResponse(wallet.getMobileNumber(), wallet.getBalance());
    }
}
