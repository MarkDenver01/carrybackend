package com.carry_guide.carry_guide_admin.presentation.controller;

import com.carry_guide.carry_guide_admin.dto.request.CustomerDetailRequest;
import com.carry_guide.carry_guide_admin.dto.response.CustomerDetailResponse;
import com.carry_guide.carry_guide_admin.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class CustomerController {
    @Autowired
    CustomerService customerService;

    @PostMapping("/public/create/customer")
    public ResponseEntity<?> createCustomer(@RequestBody CustomerDetailRequest request) {
        CustomerDetailResponse response = customerService.createCustomer(request);
        return ResponseEntity.ok(response);
    }

//    @GetMapping("/{id}")
//    public CustomerDetailResponse getCustomer(@PathVariable Long id) {
//        return customerService.getAllCustomers(id);
//    }

    @GetMapping
    public ResponseEntity<?> getAllCustomers() {
        List<CustomerDetailResponse> list = customerService.getAllCustomers();
        return ResponseEntity.ok().body(list);
    }

    @PutMapping("/public/update/customer/{id}")
    public ResponseEntity<?> updateCustomer(
            @PathVariable Long id,
            @RequestBody CustomerDetailRequest request) {
        CustomerDetailResponse response = customerService.updateCustomer(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/delete/customer/{id}")
    public String delete(@PathVariable Long id) {
        customerService.deleteCustomer(id);
        return "Customer deleted successfully";
    }

    @PutMapping("/public/customer/update/{identifier}")
    public ResponseEntity<CustomerDetailResponse> updateCustomer(
            @PathVariable String identifier,
            @RequestBody CustomerDetailRequest req
    ) {
        return ResponseEntity.ok(customerService.updateCustomerDetails(identifier, req));
    }
}
