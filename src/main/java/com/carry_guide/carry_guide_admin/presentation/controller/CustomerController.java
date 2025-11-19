package com.carry_guide.carry_guide_admin.presentation.controller;

import com.carry_guide.carry_guide_admin.dto.request.CustomerDetailRequest;
import com.carry_guide.carry_guide_admin.dto.response.CustomerDetailResponse;
import com.carry_guide.carry_guide_admin.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user/public/api/customers")
@RequiredArgsConstructor
public class CustomerController {
    @Autowired
    CustomerService customerService;

    @PostMapping
    public CustomerDetailResponse createCustomer(@RequestBody CustomerDetailRequest request) {
        return customerService.createCustomer(request);
    }

//    @GetMapping("/{id}")
//    public CustomerDetailResponse getCustomer(@PathVariable Long id) {
//        return customerService.getAllCustomers(id);
//    }

    @GetMapping
    public List<CustomerDetailResponse> getAllCustomers() {
        return customerService.getAllCustomers();
    }

    @PutMapping("/{id}")
    public CustomerDetailResponse updateCustomer(
            @PathVariable Long id,
            @RequestBody CustomerDetailRequest request) {
        return customerService.updateCustomer(id, request);
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {
        customerService.deleteCustomer(id);
        return "Customer deleted successfully";
    }
}
