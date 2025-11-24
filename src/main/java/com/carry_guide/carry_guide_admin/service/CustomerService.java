package com.carry_guide.carry_guide_admin.service;

import com.carry_guide.carry_guide_admin.domain.enums.AccountStatus;
import com.carry_guide.carry_guide_admin.dto.CustomerMapper;
import com.carry_guide.carry_guide_admin.dto.request.CustomerDetailRequest;
import com.carry_guide.carry_guide_admin.dto.response.CustomerDetailResponse;
import com.carry_guide.carry_guide_admin.dto.response.CustomerResponse;
import com.carry_guide.carry_guide_admin.model.entity.Customer;
import com.carry_guide.carry_guide_admin.model.entity.User;
import com.carry_guide.carry_guide_admin.repository.JpaCustomerRepository;
import com.carry_guide.carry_guide_admin.repository.JpaUserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerService {
    @Autowired
    JpaCustomerRepository customerRepository;

    @Autowired
    JpaUserRepository userRepository;

    @Autowired
    CustomerMapper customerMapper;

    public CustomerDetailResponse createCustomer(CustomerDetailRequest req) {
        Customer c = customerMapper.toEntity(req);

        c.setCreatedDate(LocalDateTime.now());
        c.setUserAccountStatus(AccountStatus.ACTIVATE);

        customerRepository.save(c);

        return customerMapper.toResponse(c);
    }

    public List<CustomerDetailResponse> getAllCustomers() {
        return customerRepository.findAll()
                .stream()
                .map(customerMapper::toResponse)
                .toList();
    }

//    public CustomerDetailResponse findCustomerById(Long customerId) {
//        return customerRepository.findByUser_UserId(customerId)
//                .orElseThrow(() -> new EntityNotFoundException("Customer not found"));
//    }

    public CustomerDetailResponse updateCustomer(Long id, CustomerDetailRequest req) {
        Customer c = customerRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Customer not found"));

        c.setUserName(req.getUserName());
        c.setMobileNumber(req.getMobileNumber());
        c.setEmail(req.getEmail());
        c.setPhotoUrl(req.getPhotoUrl());
        c.setAddress(req.getAddress());

        customerRepository.save(c);

        return customerMapper.toResponse(c);
    }

    public CustomerDetailResponse findCustomerByMobileOrEmail(String text) {
        return customerRepository.findByMobileNumberOrEmail(text, text)
                .map(customerMapper::toResponse)
                .orElse(null);
    }

    public CustomerDetailResponse updateCustomerDetails(CustomerDetailRequest req) {

        // 1️⃣ FIND USER (email or mobile)
        User user = userRepository.findByEmail(req.getEmail())
                .orElseGet(() ->
                        userRepository.findByMobileNumber(req.getMobileNumber())
                                .orElseThrow(() -> new EntityNotFoundException("Customer not found"))
                );

        // 2️⃣ DUPLICATION CHECKS (User-level)
        if (!user.getEmail().equals(req.getEmail()) &&
                userRepository.existsByEmail(req.getEmail())) {
            throw new IllegalArgumentException("Email already in use.");
        }

        if (!user.getMobileNumber().equals(req.getMobileNumber()) &&
                userRepository.existsByMobileNumber(req.getMobileNumber())) {
            throw new IllegalArgumentException("Mobile number already in use.");
        }

        // 3️⃣ FIND CUSTOMER RECORD
        Customer customer = customerRepository.findByUser(user).orElse(null);

        // 4️⃣ CREATE NEW CUSTOMER IF NOT EXISTING
        if (customer == null) {
            customer = new Customer();
            customer.setUser(user);
            customer.setCreatedDate(LocalDateTime.now());
            customer.setUserAccountStatus(AccountStatus.VERIFIED);
        }

        // 5️⃣ UPDATE USER DATA
        user.setEmail(req.getEmail());
        user.setMobileNumber(req.getMobileNumber());
        user.setUserName(req.getUserName());
        userRepository.save(user);

        // 6️⃣ UPDATE CUSTOMER PROFILE DATA
        customer.setUserName(user.getUserName());
        customer.setEmail(user.getEmail());
        customer.setMobileNumber(user.getMobileNumber());
        customer.setAddress(req.getAddress());
        customer.setPhotoUrl(req.getPhotoUrl());
        customerRepository.save(customer);

        // 7️⃣ RETURN RESPONSE
        return customerMapper.toResponse(customer);
    }

    public void deleteCustomer(Long id) {
        if (!customerRepository.existsById(id)) {
            throw new EntityNotFoundException("Customer not found");
        }
        customerRepository.deleteById(id);
    }
}
