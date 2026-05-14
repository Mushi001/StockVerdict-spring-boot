package org.henriette.stockverdict.controllers;

import org.henriette.stockverdict.models.Supplier;
import org.henriette.stockverdict.models.Users;
import org.henriette.stockverdict.services.SupplierService;
import org.henriette.stockverdict.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.henriette.stockverdict.dto.ApiResponse;
import org.henriette.stockverdict.dto.SupplierRequests.*;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * REST Controller for Supplier management.
 * Handles endpoints for creating, retrieving, and updating business suppliers.
 */
@RestController
@RequestMapping("/api/suppliers")
@Tag(name = "Suppliers", description = "Endpoints for managing product suppliers and their balances.")
public class SupplierController {

    private final SupplierService supplierService;
    private final UserService userService;

    @Autowired
    public SupplierController(SupplierService supplierService, UserService userService) {
        this.supplierService = supplierService;
        this.userService = userService;
    }

    /**
     * Retrieves all suppliers associated with a specific user.
     * 
     * @param userId The ID of the user.
     * @return ApiResponse containing a list of suppliers.
     */
    @GetMapping
    @Operation(summary = "Get all suppliers", description = "Retrieves all suppliers managed by a specific user.")
    public ResponseEntity<ApiResponse<List<Supplier>>> getSuppliers(@RequestParam Long userId) {
        List<Supplier> suppliers = supplierService.getSuppliersByUser(userId);
        return ResponseEntity.ok(new ApiResponse<>(true, suppliers));
    }

    /**
     * Retrieves a single supplier by their ID.
     * 
     * @param id The unique identifier of the supplier.
     * @return ApiResponse containing the supplier details.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get supplier by ID", description = "Retrieves the details of a single supplier.")
    public ResponseEntity<ApiResponse<Supplier>> getSupplierById(@PathVariable Long id) {
        Supplier supplier = supplierService.getSupplierById(id);
        if (supplier == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, "Supplier not found"));
        }
        return ResponseEntity.ok(new ApiResponse<>(true, supplier));
    }

    /**
     * Searches for suppliers by a keyword.
     * 
     * @param userId The ID of the user owning the suppliers.
     * @param keyword The search term (e.g., matching name or email).
     * @return ApiResponse containing the matching suppliers.
     */
    @GetMapping("/search")
    @Operation(summary = "Search suppliers", description = "Searches for suppliers by keyword across name, email, and contact info.")
    public ResponseEntity<ApiResponse<List<Supplier>>> searchSuppliers(@RequestParam Long userId,
                                                                       @RequestParam String keyword) {
        List<Supplier> results = supplierService.searchSuppliers(userId, keyword);
        return ResponseEntity.ok(new ApiResponse<>(true, results));
    }

    /**
     * Adds a new supplier to the system.
     * 
     * @param request The data transfer object containing the supplier details.
     * @return ApiResponse indicating success or failure.
     */
    @PostMapping
    @Operation(summary = "Add a new supplier", description = "Creates a new supplier record.")
    public ResponseEntity<ApiResponse<Void>> addSupplier(@RequestBody AddSupplierRequest request) {
        Long userId = request.userId();
        String name = request.name();
        String phone = request.phone();
        String email = request.email();
        String address = request.address();
        String contactPerson = request.contactPerson();
        String notes = request.notes();
        double balanceOwed = request.balanceOwed() != null ? request.balanceOwed() : 0.0;

        Users user = userService.findById(userId);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, "User not found"));
        }

        if (email != null && !email.isBlank() && supplierService.isEmailExists(email, userId, null)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiResponse<>(false, "A supplier with this email already exists"));
        }

        Supplier supplier = new Supplier(name, phone, email, address, contactPerson, balanceOwed, notes, user);

        if (supplierService.addSupplier(supplier)) {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>(true, "Supplier added successfully"));
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Failed to add supplier"));
    }

    /**
     * Updates an existing supplier's details.
     * 
     * @param id The unique identifier of the supplier.
     * @param request The data transfer object containing the updated supplier info.
     * @return ApiResponse indicating success or failure.
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update supplier", description = "Updates an existing supplier's information.")
    public ResponseEntity<ApiResponse<Void>> updateSupplier(@PathVariable Long id,
                                                            @RequestBody UpdateSupplierRequest request) {
        Long userId = request.userId();
        String email = request.email();

        if (email != null && !email.isBlank() && supplierService.isEmailExists(email, userId, id)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiResponse<>(false, "A supplier with this email already exists"));
        }

        Supplier updated = new Supplier();
        updated.setId(id);
        updated.setName(request.name());
        updated.setPhone(request.phone());
        updated.setEmail(email);
        updated.setAddress(request.address());
        updated.setContactPerson(request.contactPerson());
        updated.setNotes(request.notes());
        
        double balanceOwed = request.balanceOwed() != null ? request.balanceOwed() : 0.0;
        updated.setBalanceOwed(balanceOwed);

        if (supplierService.updateSupplier(updated)) {
            return ResponseEntity.ok(new ApiResponse<>(true, "Supplier updated successfully"));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiResponse<>(false, "Supplier not found or update failed"));
    }

    /**
     * Deletes a supplier from the database.
     * 
     * @param id The unique identifier of the supplier.
     * @return ApiResponse indicating success or failure.
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete supplier", description = "Removes a supplier record from the system.")
    public ResponseEntity<ApiResponse<Void>> deleteSupplier(@PathVariable Long id) {
        if (supplierService.deleteSupplier(id)) {
            return ResponseEntity.ok(new ApiResponse<>(true, "Supplier deleted successfully"));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiResponse<>(false, "Supplier not found"));
    }
}
