package org.henriette.stockverdict.controllers;

import org.henriette.stockverdict.models.Users;
import org.henriette.stockverdict.services.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller for public payment links.
 */
@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    private final UserService userService;

    @Autowired
    public PaymentController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/trader/{id}")
    public ResponseEntity<Map<String, Object>> getTraderPaymentInfo(@PathVariable Long id) {
        Users trader = userService.getTraderPaymentInfo(id);

        if (trader != null) {
            Map<String, Object> paymentInfo = new HashMap<>();
            paymentInfo.put("name", trader.getName());
            paymentInfo.put("businessName", trader.getBusinessName());
            paymentInfo.put("momoCode", trader.getMomoCode());
            paymentInfo.put("bankAccountNumber", trader.getBankAccountNumber());
            paymentInfo.put("profileImageUrl", trader.getProfileImageUrl());

            return ResponseEntity.ok(Map.of("success", true, "paymentInfo", paymentInfo));
        }

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "success", false, "message", "Trader not found or not eligible for payments"));
    }
}
