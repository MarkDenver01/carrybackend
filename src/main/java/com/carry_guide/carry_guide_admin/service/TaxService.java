package com.carry_guide.carry_guide_admin.service;


import com.carry_guide.carry_guide_admin.model.entity.Tax;
import com.carry_guide.carry_guide_admin.repository.JpaTaxRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class TaxService {
    @Autowired
    JpaTaxRepository taxRepository;

    public Optional<Tax> getCurrentTaxExemptions() {
        LocalDateTime now = LocalDateTime.now();
        return taxRepository.findByEffectiveFromBeforeAndEffectiveToAfter(now, now);
    }
}
