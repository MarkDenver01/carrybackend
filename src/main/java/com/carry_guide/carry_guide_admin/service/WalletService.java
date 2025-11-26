package com.carry_guide.carry_guide_admin.service;


import com.carry_guide.carry_guide_admin.dto.request.wallet.CashInRequest;
import com.carry_guide.carry_guide_admin.dto.request.wallet.UpdateWalletRequest;
import com.carry_guide.carry_guide_admin.dto.response.wallet.CashInInitResponse;
import com.carry_guide.carry_guide_admin.dto.response.wallet.WalletResponse;
import com.carry_guide.carry_guide_admin.dto.response.wallet.XenditInvoiceResponse;
import com.carry_guide.carry_guide_admin.model.entity.Customer;
import com.carry_guide.carry_guide_admin.model.entity.Wallet;
import com.carry_guide.carry_guide_admin.model.entity.WalletTransaction;
import com.carry_guide.carry_guide_admin.repository.JpaCustomerRepository;
import com.carry_guide.carry_guide_admin.repository.JpaWalletRepository;
import com.carry_guide.carry_guide_admin.repository.JpaWalletTransactionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class WalletService {
    private final JpaWalletRepository walletRepository;
    private final JpaWalletTransactionRepository walletTransactionRepository;
    private final XenditService xenditService;
    private final JpaCustomerRepository customerRepository;


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

    public WalletResponse getCustomerWalletBalance(String mobileNumber) {

        Customer customer = customerRepository.findByMobileNumber(mobileNumber)
                .orElseThrow(() -> new IllegalStateException("Customer not found"));

        return new WalletResponse(customer.getMobileNumber(), customer.getWalletBalance().longValueExact());
    }

    public WalletResponse updateWallet(UpdateWalletRequest request) {

        Customer customer = customerRepository.findByMobileNumber(request.getMobileNumber())
                .orElseThrow(() -> new IllegalStateException("Customer not found"));

        BigDecimal current = customer.getWalletBalance();
        BigDecimal amount = request.getAmount();

        if (request.isDeduct()) {
            // Check insufficient balance
            if (current.compareTo(amount) < 0) {
                throw new IllegalStateException("Insufficient wallet balance");
            }
            customer.setWalletBalance(current.subtract(amount));
        } else {
            // Add to wallet
            customer.setWalletBalance(current.add(amount));
        }

        customerRepository.save(customer);

        return new WalletResponse(customer.getMobileNumber(), customer.getWalletBalance().longValueExact());
    }
}
