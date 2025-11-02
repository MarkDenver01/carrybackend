package com.carry_guide.carry_guide_admin.presentation.controller;

import com.carry_guide.carry_guide_admin.model.entity.Tax;
import com.carry_guide.carry_guide_admin.service.TaxService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/tax")
@RequiredArgsConstructor
public class TaxController {

    @Autowired
    TaxService taxService;

    @GetMapping("/exemptions")
    public ResponseEntity<Optional<Tax>> getExemptions() {
        return ResponseEntity.ok(taxService.getCurrentTaxExemptions());
    }
}
