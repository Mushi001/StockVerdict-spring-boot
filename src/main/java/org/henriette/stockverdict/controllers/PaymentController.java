package org.henriette.stockverdict.controllers;

import org.henriette.stockverdict.dto.ApiResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller for Payment processing integrations.
 * Provides endpoints for retrieving public payment links and processing status updates.
 */
@RestController
@RequestMapping("/api/payment")
@Tag(name = "Payments", description = "Endpoints for handling payments and integration with external payment gateways.")
public class PaymentController {

    @Value("${flutterwave.public.key:}")
    private String flutterwavePublicKey;

    @Value("${flutterwave.secret.key:}")
    private String flutterwaveSecretKey;

    /**
     * Retrieves the payment initialization link and keys for a specific trader.
     * 
     * @param traderId The ID of the trader receiving the payment.
     * @return ApiResponse containing the payment gateway keys and configuration.
     */
    @GetMapping("/trader/{traderId}")
    @Operation(summary = "Get payment info", description = "Retrieves the public keys required to initialize a payment for a specific trader.")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPaymentInfo(@PathVariable Long traderId) {
        Map<String, Object> paymentInfo = new HashMap<>();
        paymentInfo.put("publicKey", flutterwavePublicKey);
        
        return ResponseEntity.ok(new ApiResponse<>(true, paymentInfo));
    }

    /**
     * Webhook endpoint for receiving payment status updates from Flutterwave.
     * 
     * @param payload The webhook payload sent by the payment gateway.
     * @return ApiResponse acknowledging receipt of the webhook.
     */
    @PostMapping("/webhook")
    @Operation(summary = "Payment webhook", description = "Receives asynchronous payment status updates from the payment gateway.")
    public ResponseEntity<ApiResponse<Void>> handleWebhook(@RequestBody Map<String, Object> payload) {
        System.out.println("Webhook received: " + payload);
        
        return ResponseEntity.ok(new ApiResponse<>(true, "Webhook processed successfully"));
    }
}
