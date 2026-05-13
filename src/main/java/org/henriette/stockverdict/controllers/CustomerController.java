package org.henriette.stockverdict.controllers;

import org.henriette.stockverdict.models.Customer;
import org.henriette.stockverdict.models.Users;
import org.henriette.stockverdict.services.CustomerService;
import org.henriette.stockverdict.services.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.henriette.stockverdict.dto.CustomerRequests.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for Customer management.
 * Provides CRUD endpoints and search functionality.
 */
@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService customerService;
    private final UserService userService;

    @Autowired
    public CustomerController(CustomerService customerService, UserService userService) {
        this.customerService = customerService;
        this.userService = userService;
    }

    /**
     * GET /api/customers?userId={userId}
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getCustomers(@RequestParam Long userId) {
        List<Customer> customers = customerService.getCustomersByUser(userId);
        return ResponseEntity.ok(Map.of("success", true, "count", customers.size(), "customers", customers));
    }

    /**
     * GET /api/customers/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getCustomerById(@PathVariable Long id) {
        Customer customer = customerService.getCustomerById(id);
        if (customer == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false, "message", "Customer not found"));
        }
        return ResponseEntity.ok(Map.of("success", true, "customer", customer));
    }

    /**
     * GET /api/customers/search?userId={userId}&keyword={keyword}
     */
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchCustomers(@RequestParam Long userId,
                                                               @RequestParam String keyword) {
        List<Customer> results = customerService.searchCustomers(userId, keyword);
        return ResponseEntity.ok(Map.of("success", true, "count", results.size(), "customers", results));
    }

    /**
     * POST /api/customers
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> addCustomer(@RequestBody AddCustomerRequest request) {
        Long userId = request.userId();
        String name = request.name();
        String phone = request.phone();
        String email = request.email();
        String address = request.address();

        if (name == null || name.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Name is required"));
        }

        Users user = userService.findById(userId);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false, "message", "User not found"));
        }

        if (email != null && !email.isBlank() && customerService.isEmailExists(email, null)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "success", false, "message", "A customer with this email already exists"));
        }

        Customer customer = new Customer(name, phone, email, address, user);
        if (customerService.addCustomer(customer)) {
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "success", true, "message", "Customer added successfully"));
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false, "message", "Failed to add customer"));
    }

    /**
     * PUT /api/customers/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateCustomer(@PathVariable Long id,
                                                              @RequestBody UpdateCustomerRequest request) {
        String email = request.email();
        if (email != null && !email.isBlank() && customerService.isEmailExists(email, id)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "success", false, "message", "A customer with this email already exists"));
        }

        Customer updated = new Customer();
        updated.setId(id);
        updated.setName(request.name());
        updated.setPhone(request.phone());
        updated.setEmail(email);
        updated.setAddress(request.address());

        if (customerService.updateCustomer(updated)) {
            return ResponseEntity.ok(Map.of("success", true, "message", "Customer updated successfully"));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "success", false, "message", "Customer not found or update failed"));
    }

    /**
     * DELETE /api/customers/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteCustomer(@PathVariable Long id) {
        if (customerService.deleteCustomer(id)) {
            return ResponseEntity.ok(Map.of("success", true, "message", "Customer deleted successfully"));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "success", false, "message", "Customer not found"));
    }

    /**
     * GET /api/customers/count?userId={userId}
     */
    @GetMapping("/count")
    public ResponseEntity<Map<String, Object>> countCustomers(@RequestParam Long userId) {
        Long count = customerService.countCustomersByUser(userId);
        return ResponseEntity.ok(Map.of("success", true, "count", count));
    }
}
