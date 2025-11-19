package com.carry_guide.carry_guide_admin.service;

import com.carry_guide.carry_guide_admin.domain.enums.AccountStatus;
import com.carry_guide.carry_guide_admin.dto.CustomerMapper;
import com.carry_guide.carry_guide_admin.dto.request.CustomerDetailRequest;
import com.carry_guide.carry_guide_admin.dto.response.CustomerDetailResponse;
import com.carry_guide.carry_guide_admin.dto.response.CustomerResponse;
import com.carry_guide.carry_guide_admin.model.entity.Customer;
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

    public CustomerDetailResponse updateCustomerDetails(String identifier, CustomerDetailRequest req) {

        // 1. Try identifier as ID
        Customer customer = null;

        // 1. Try search by mobile OR email FIRST
        customer = customerRepository
                .findByMobileNumberOrEmail(identifier, identifier)
                .orElse(null);

        // 2. If not found → try ID (only if short length)
        if (customer == null && identifier.matches("\\d+") && identifier.length() <= 5) {
            Long id = Long.parseLong(identifier);
            customer = customerRepository.findById(id).orElse(null);
        }

        // 3. Still null?
        if (customer == null) {
            throw new EntityNotFoundException("Customer not found");
        }


        // 3. Duplication checks before updating
//        if (!customer.getMobileNumber().equals(req.getMobileNumber()) &&
//                customerRepository.existsByMobileNumber(req.getMobileNumber())) {
//            throw new IllegalArgumentException("Mobile number already in use.");
//        }

//        if (!customer.getEmail().equals(req.getEmail()) &&
//                customerRepository.existsByEmail(req.getEmail())) {
//            throw new IllegalArgumentException("Email already in use.");
//        }

        // 4. Update fields
        customer.setUserName(req.getUserName());
        customer.setMobileNumber(req.getMobileNumber());
        customer.setEmail(req.getEmail());
        customer.setPhotoUrl(req.getPhotoUrl());
        customer.setAddress(req.getAddress());

        // 5. Save changes
        customerRepository.save(customer);

        // 6. Convert to response
        return customerMapper.toResponse(customer);
    }


    public void deleteCustomer(Long id) {
        if (!customerRepository.existsById(id)) {
            throw new EntityNotFoundException("Customer not found");
        }
        customerRepository.deleteById(id);
    }
}
