package com.carry_guide.carry_guide_admin.presentation.controller;

import com.carry_guide.carry_guide_admin.dto.request.CustomerDetailRequest;
import com.carry_guide.carry_guide_admin.dto.response.CustomerDetailResponse;
import com.carry_guide.carry_guide_admin.dto.response.UploadPhotoResponse;
import com.carry_guide.carry_guide_admin.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

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

    @PutMapping("/public/customer/update")
    public ResponseEntity<CustomerDetailResponse> updateCustomer(
            @RequestBody CustomerDetailRequest req
    ) {
        return ResponseEntity.ok(customerService.updateCustomerDetails(req));
    }

    @PostMapping(value = "/public/customer/upload-photo", consumes = "multipart/form-data")
    public ResponseEntity<UploadPhotoResponse> uploadCustomerPhoto(
            @RequestParam("file") MultipartFile file
    ) {
        String url = customerService.uploadCustomerPhoto(file);
        return ResponseEntity.ok(new UploadPhotoResponse(url));
    }


    // 👉 TOTAL CUSTOMERS (lahat ng nasa customer_info)
    @GetMapping("/public/customers/total")
    public ResponseEntity<Long> getTotalCustomers() {
        long total = customerService.getTotalCustomers();
        return ResponseEntity.ok(total);
    }

    // 👉 TOTAL ACTIVE CUSTOMERS (optional, kung need mo sa dashboard)
    @GetMapping("/public/customers/total-active")
    public ResponseEntity<Long> getTotalActiveCustomers() {
        long totalActive = customerService.getTotalActiveCustomers();
        return ResponseEntity.ok(totalActive);
    }

}
