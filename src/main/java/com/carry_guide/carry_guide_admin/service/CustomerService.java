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

        User user = userRepository.findByEmail(req.getEmail())
                .orElseGet(() ->
                        userRepository.findByMobileNumber(req.getMobileNumber())
                                .orElseThrow(() ->
                                        new EntityNotFoundException("Customer not found")
                                )
                );

        Customer customer = customerRepository.findByUser(user).orElse(null);

        if (customer == null) {
            customer = new Customer();
            customer.setUser(user);
            customer.setCreatedDate(LocalDateTime.now());
            customer.setUserAccountStatus(AccountStatus.VERIFIED);
        }

        user.setEmail(req.getEmail());
        user.setMobileNumber(req.getMobileNumber());
        user.setUserName(req.getUserName());
        userRepository.save(user);

        customer.setUserName(req.getUserName());
        customer.setMobileNumber(req.getMobileNumber());
        customer.setEmail(req.getEmail());
        customer.setAddress(req.getAddress());
        customer.setPhotoUrl(req.getPhotoUrl());
        customerRepository.save(customer);

        return customerMapper.toResponse(customer);
    }



    public void deleteCustomer(Long id) {
        if (!customerRepository.existsById(id)) {
            throw new EntityNotFoundException("Customer not found");
        }
        customerRepository.deleteById(id);
    }
}
