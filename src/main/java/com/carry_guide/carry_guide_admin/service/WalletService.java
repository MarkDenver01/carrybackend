package com.carry_guide.carry_guide_admin.service;

import com.carry_guide.carry_guide_admin.domain.enums.AccountStatus;
import com.carry_guide.carry_guide_admin.dto.request.wallet.CashInRequest;
import com.carry_guide.carry_guide_admin.dto.request.wallet.XenditEwalletWebhookPayload;
import com.carry_guide.carry_guide_admin.dto.response.wallet.CashInInitResponse;
import com.carry_guide.carry_guide_admin.dto.response.wallet.WalletResponse;
import com.carry_guide.carry_guide_admin.model.entity.Customer;
import com.carry_guide.carry_guide_admin.model.entity.User;
import com.carry_guide.carry_guide_admin.model.entity.Wallet;
import com.carry_guide.carry_guide_admin.repository.JpaCustomerRepository;
import com.carry_guide.carry_guide_admin.repository.JpaUserRepository;
import com.carry_guide.carry_guide_admin.repository.JpaWalletRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class WalletService {

    private final JpaUserRepository userRepository;
    private final JpaCustomerRepository customerRepository;
    private final JpaWalletRepository walletRepository;
    private final XenditService xenditService;

    private Customer findCustomerByEmailOrMobile(String email, String mobile) {

        User user = null;

        if (email != null && !email.isBlank()) {
            user = userRepository.findByEmail(email).orElse(null);
        }

        if (user == null && mobile != null && !mobile.isBlank()) {
            user = userRepository.findByMobileNumber(mobile).orElse(null);
        }

        if (user == null) {
            throw new EntityNotFoundException("Customer not found");
        }

        return customerRepository.findByUser(user)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found"));
    }

    private Wallet getOrCreateWallet(Customer customer) {
        return walletRepository.findByCustomer(customer)
                .orElseGet(() -> {
                    Wallet w = new Wallet();
                    w.setCustomer(customer);
                    w.setBalance(BigDecimal.ZERO);
                    w.setUpdatedAt(LocalDateTime.now());
                    return walletRepository.save(w);
                });
    }

    public CashInInitResponse createCashIn(CashInRequest request) {

        if (request.getAmount() == null ||
                request.getAmount().compareTo(BigDecimal.ONE) < 0) {
            throw new IllegalArgumentException("Invalid amount");
        }

        Customer customer = findCustomerByEmailOrMobile(
                request.getEmail(),
                request.getMobileNumber()
        );

        // call xendit
        return xenditService.createGcashCharge(
                request.getAmount(),
                String.valueOf(customer.getCustomerId())
        );
    }

    public WalletResponse getWallet(String email, String mobile) {
        Customer customer = findCustomerByEmailOrMobile(email, mobile);
        Wallet wallet = getOrCreateWallet(customer);
        return new WalletResponse(wallet.getBalance());
    }

    public void handleXenditWebhook(XenditEwalletWebhookPayload payload) {

        if (!Objects.equals(payload.getStatus(), "SUCCEEDED") &&
                !Objects.equals(payload.getStatus(), "COMPLETED")) {
            return; // ignore not-success
        }

        // reference id = CASHIN-{customerId}-{uuid}
        String ref = payload.getReference_id();
        if (ref == null || !ref.startsWith("CASHIN-")) return;

        String[] parts = ref.split("-");
        if (parts.length < 3) return;

        Long customerId;
        try {
            customerId = Long.parseLong(parts[1]);
        } catch (NumberFormatException e) {
            return;
        }

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found"));

        Wallet wallet = getOrCreateWallet(customer);

        BigDecimal amount = payload.getCharge_amount();
        if (amount == null) return;

        wallet.setBalance(
                wallet.getBalance().add(amount)
        );
        wallet.setUpdatedAt(LocalDateTime.now());
        walletRepository.save(wallet);
    }
}
