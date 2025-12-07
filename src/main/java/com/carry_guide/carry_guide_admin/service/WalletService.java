package com.carry_guide.carry_guide_admin.service;

import com.carry_guide.carry_guide_admin.dto.request.wallet.CashInRequest;
import com.carry_guide.carry_guide_admin.dto.request.wallet.UpdateWalletRequest;
import com.carry_guide.carry_guide_admin.dto.response.wallet.CashInInitResponse;
import com.carry_guide.carry_guide_admin.dto.response.wallet.WalletResponse;
import com.carry_guide.carry_guide_admin.dto.response.wallet.XenditInvoiceResponse;
import com.carry_guide.carry_guide_admin.model.entity.Customer;
import com.carry_guide.carry_guide_admin.model.entity.WalletTransaction;
import com.carry_guide.carry_guide_admin.repository.JpaCustomerRepository;
import com.carry_guide.carry_guide_admin.repository.JpaWalletTransactionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final JpaWalletTransactionRepository walletTransactionRepository;
    private final JpaCustomerRepository customerRepository;
    private final XenditService xenditService;

    /* ============================================================
       ✅ CASH-IN (FRONTEND → XENDIT → SAVE TRANSACTION AS PENDING)
    ============================================================ */
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

    /* ============================================================
       ✅ XENDIT WEBHOOK → CREDIT TO CUSTOMER WALLET (SINGLE SOURCE)
    ============================================================ */
    @Transactional
    public void handleInvoiceWebhook(String status, String externalId, Long paidAmount) {

        WalletTransaction tx = walletTransactionRepository.findByExternalId(externalId)
                .orElse(null);

        if (tx == null) {
            System.out.println("⚠ No matching wallet transaction: " + externalId);
            return;
        }

        // ✅ Prevent double credit
        if ("PAID".equals(tx.getStatus())) {
            return;
        }

        tx.setStatus(status);
        walletTransactionRepository.save(tx);

        if ("PAID".equals(status)) {

            Customer customer = customerRepository.findByMobileNumber(tx.getMobileNumber())
                    .orElseThrow(() -> new IllegalStateException("Customer not found for wallet credit"));

            BigDecimal current = customer.getWalletBalance();
            BigDecimal paid = BigDecimal.valueOf(paidAmount);

            customer.setWalletBalance(current.add(paid));
            customerRepository.save(customer);

            System.out.println("✅ WALLET CREDITED: " + tx.getMobileNumber()
                    + " +₱" + paid + " NEW BALANCE: " + customer.getWalletBalance());
        }
    }

    /* ============================================================
       ✅ GET WALLET BALANCE (CUSTOMER TABLE SOURCE)
    ============================================================ */
    public WalletResponse getWalletBalance(String mobileNumber) {

        Customer customer = customerRepository.findByMobileNumber(mobileNumber)
                .orElseThrow(() -> new IllegalStateException("Customer not found"));

        return new WalletResponse(
                customer.getMobileNumber(),
                customer.getWalletBalance().longValueExact()
        );
    }

    /* ============================================================
       ✅ MANUAL UPDATE / DEDUCT WALLET (ADMIN / CHECKOUT USE)
    ============================================================ */
    @Transactional
    public WalletResponse updateWalletBalance(UpdateWalletRequest request) {

        Customer customer = customerRepository.findByMobileNumber(request.getMobileNumber())
                .orElseThrow(() -> new IllegalStateException("Customer not found"));

        BigDecimal current = customer.getWalletBalance();
        BigDecimal amount = request.getAmount();

        if (request.isDeduct()) {
            if (current.compareTo(amount) < 0) {
                throw new IllegalStateException("Insufficient wallet balance");
            }
            customer.setWalletBalance(current.subtract(amount));
        } else {
            customer.setWalletBalance(current.add(amount));
        }

        customerRepository.save(customer);

        return new WalletResponse(
                customer.getMobileNumber(),
                customer.getWalletBalance().longValueExact()
        );
    }
}
