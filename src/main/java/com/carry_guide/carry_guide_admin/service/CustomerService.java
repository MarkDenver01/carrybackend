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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CustomerService {
    @Value("${app.upload.folder.customer}")
    private String uploadFolderCustomer;

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

        // 1. Find user by email OR mobile (whichever is present)
        User user = userRepository.findByEmail(req.getEmail())
                .orElseGet(() ->
                        userRepository.findByMobileNumber(req.getMobileNumber())
                                .orElseThrow(() ->
                                        new EntityNotFoundException("Customer not found")
                                )
                );

        // 2. Find or create customer record
        Customer customer = customerRepository.findByUser(user)
                .orElseGet(() -> {
                    Customer newCustomer = new Customer();
                    newCustomer.setUser(user);
                    newCustomer.setCreatedDate(LocalDateTime.now());
                    newCustomer.setUserAccountStatus(AccountStatus.VERIFIED);
                    return newCustomer;
                });

        // 3. Update USER fields safely
        if (!Objects.equals(user.getEmail(), req.getEmail())) {
            user.setEmail(req.getEmail());
        }

        if (!Objects.equals(user.getMobileNumber(), req.getMobileNumber())) {
            user.setMobileNumber(req.getMobileNumber());
        }

        user.setUserName(req.getUserName());
        userRepository.save(user);

        // 4. Update CUSTOMER fields safely
        customer.setUserName(req.getUserName());
        customer.setEmail(req.getEmail());
        customer.setMobileNumber(req.getMobileNumber());
        customer.setAddress(req.getAddress());
        customer.setPhotoUrl(req.getPhotoUrl());

        customerRepository.save(customer);

        return customerMapper.toResponse(customer);
    }

    public String uploadCustomerPhoto(MultipartFile file) {

        try {
            // Ensure directory exists
            File dir = new File(uploadFolderCustomer);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            // Generate filename
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            File destination = new File(dir, fileName);

            // Store file
            file.transferTo(destination);

            // Return public URL for Render static serving
            return "https://carrybackend-dfyh.onrender.com/upload/customer/" + fileName;

        } catch (Exception e) {
            throw new RuntimeException("Failed to upload customer photo", e);
        }
    }

    public void deleteCustomer(Long id) {
        if (!customerRepository.existsById(id)) {
            throw new EntityNotFoundException("Customer not found");
        }
        customerRepository.deleteById(id);
    }

}
