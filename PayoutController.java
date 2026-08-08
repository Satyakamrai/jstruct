package com.company.payouts.controller;

import com.company.payouts.service.PayoutService;
import com.company.payouts.model.PayoutRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/payouts")
public class PayoutController {

    @Autowired
    private PayoutService payoutService;

    @PostMapping("/processBatch")
    public ResponseEntity<String> processBatchPayouts(@RequestBody List<PayoutRequest> requests) {
        try {
            payoutService.process(requests);
            return ResponseEntity.ok("Batch processed successfully");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Internal Server Error");
        }
    }
}
