package org.henriette.stockverdict.controllers;

import org.henriette.stockverdict.dto.ApiResponse;
import org.henriette.stockverdict.models.Customer;
import org.henriette.stockverdict.models.Users;
import org.henriette.stockverdict.services.CustomerService;
import org.henriette.stockverdict.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.henriette.stockverdict.dto.CustomerRequests.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

/**
 * REST Controller for Customer management.
 * Provides endpoints for creating, updating, retrieving, and searching customer records.
 */
@RestController
@RequestMapping("/api/customers")
@Tag(name = "Customers", description = "Endpoints for managing customer relationships and details.")
public class CustomerController {

    private final CustomerService customerService;
    private final UserService userService;

    @Autowired
    public CustomerController(CustomerService customerService, UserService userService) {
        this.customerService = customerService;
        this.userService = userService;
    }

    /**
     * Retrieves all customers associated with a specific user.
     * 
     * @param userId The ID of the user requesting their customers.
     * @return ApiResponse containing a list of Customers.
     */
    @GetMapping
    @Operation(summary = "Get all customers", description = "Retrieves all customers associated with the provided user ID.")
    public ResponseEntity<ApiResponse<List<Customer>>> getCustomers(@RequestParam Long userId) {
        List<Customer> customers = customerService.getCustomersByUser(userId);
        return ResponseEntity.ok(new ApiResponse<>(true, customers));
    }

    /**
     * Retrieves a specific customer by their ID.
     * 
     * @param id The unique identifier of the customer.
     * @return ApiResponse containing the customer details, or 404 if not found.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get customer by ID", description = "Retrieves the details of a single customer.")
    public ResponseEntity<ApiResponse<Customer>> getCustomerById(@PathVariable Long id) {
        Customer customer = customerService.getCustomerById(id);
        if (customer == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, "Customer not found"));
        }
        return ResponseEntity.ok(new ApiResponse<>(true, customer));
    }

    /**
     * Searches for customers by keyword.
     * 
     * @param userId The ID of the user owning the customers.
     * @param keyword The search term.
     * @return ApiResponse containing a list of matching customers.
     */
    @GetMapping("/search")
    @Operation(summary = "Search customers", description = "Searches for customers by keyword across their name, email, or phone number.")
    public ResponseEntity<ApiResponse<List<Customer>>> searchCustomers(@RequestParam Long userId,
                                                                       @RequestParam String keyword) {
        List<Customer> results = customerService.searchCustomers(userId, keyword);
        return ResponseEntity.ok(new ApiResponse<>(true, results));
    }

    /**
     * Adds a new customer to the database.
     * 
     * @param request The data transfer object containing customer details.
     * @return ApiResponse indicating success or failure.
     */
    @PostMapping
    @Operation(summary = "Add a new customer", description = "Creates a new customer record.")
    public ResponseEntity<ApiResponse<Void>> addCustomer(@RequestBody AddCustomerRequest request) {
        Long userId = request.userId();
        String name = request.name();
        String phone = request.phone();
        String email = request.email();
        String address = request.address();

        if (name == null || name.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Name is required"));
        }

        Users user = userService.findById(userId);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, "User not found"));
        }

        if (email != null && !email.isBlank() && customerService.isEmailExists(email, null)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiResponse<>(false, "A customer with this email already exists"));
        }

        Customer customer = new Customer(name, phone, email, address, user);
        if (customerService.addCustomer(customer)) {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>(true, "Customer added successfully"));
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Failed to add customer"));
    }

    /**
     * Updates an existing customer's information.
     * 
     * @param id The unique identifier of the customer to update.
     * @param request The data transfer object containing updated details.
     * @return ApiResponse indicating success or failure.
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update customer", description = "Updates an existing customer's contact and personal details.")
    public ResponseEntity<ApiResponse<Void>> updateCustomer(@PathVariable Long id,
                                                            @RequestBody UpdateCustomerRequest request) {
        String email = request.email();
        if (email != null && !email.isBlank() && customerService.isEmailExists(email, id)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiResponse<>(false, "A customer with this email already exists"));
        }

        Customer updated = new Customer();
        updated.setId(id);
        updated.setName(request.name());
        updated.setPhone(request.phone());
        updated.setEmail(email);
        updated.setAddress(request.address());

        if (customerService.updateCustomer(updated)) {
            return ResponseEntity.ok(new ApiResponse<>(true, "Customer updated successfully"));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiResponse<>(false, "Customer not found or update failed"));
    }

    /**
     * Deletes a customer record from the system.
     * 
     * @param id The unique identifier of the customer.
     * @return ApiResponse indicating success or failure.
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete customer", description = "Removes a customer record from the database.")
    public ResponseEntity<ApiResponse<Void>> deleteCustomer(@PathVariable Long id) {
        if (customerService.deleteCustomer(id)) {
            return ResponseEntity.ok(new ApiResponse<>(true, "Customer deleted successfully"));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiResponse<>(false, "Customer not found"));
    }

    /**
     * Returns the total count of customers for a specific user.
     * 
     * @param userId The ID of the user.
     * @return ApiResponse containing the total count.
     */
    @GetMapping("/count")
    @Operation(summary = "Count total customers", description = "Retrieves the total count of customers managed by a user.")
    public ResponseEntity<ApiResponse<Long>> countCustomers(@RequestParam Long userId) {
        Long count = customerService.countCustomersByUser(userId);
        return ResponseEntity.ok(new ApiResponse<>(true, count));
    }
}
