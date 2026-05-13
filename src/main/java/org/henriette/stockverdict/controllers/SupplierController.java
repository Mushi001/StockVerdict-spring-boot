package org.henriette.stockverdict.controllers;

import org.henriette.stockverdict.models.Supplier;
import org.henriette.stockverdict.models.Users;
import org.henriette.stockverdict.services.SupplierService;
import org.henriette.stockverdict.services.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.henriette.stockverdict.dto.SupplierRequests.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for Supplier management.
 */
@RestController
@RequestMapping("/api/suppliers")
public class SupplierController {

    private final SupplierService supplierService;
    private final UserService userService;

    @Autowired
    public SupplierController(SupplierService supplierService, UserService userService) {
        this.supplierService = supplierService;
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getSuppliers(@RequestParam Long userId) {
        List<Supplier> suppliers = supplierService.getSuppliersByUser(userId);
        return ResponseEntity.ok(Map.of("success", true, "count", suppliers.size(), "suppliers", suppliers));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getSupplierById(@PathVariable Long id) {
        Supplier supplier = supplierService.getSupplierById(id);
        if (supplier == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false, "message", "Supplier not found"));
        }
        return ResponseEntity.ok(Map.of("success", true, "supplier", supplier));
    }

    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchSuppliers(@RequestParam Long userId,
                                                               @RequestParam String keyword) {
        List<Supplier> results = supplierService.searchSuppliers(userId, keyword);
        return ResponseEntity.ok(Map.of("success", true, "count", results.size(), "suppliers", results));
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> addSupplier(@RequestBody AddSupplierRequest request) {
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
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false, "message", "User not found"));
        }

        if (email != null && !email.isBlank() && supplierService.isEmailExists(email, userId, null)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "success", false, "message", "A supplier with this email already exists"));
        }

        Supplier supplier = new Supplier(name, phone, email, address, contactPerson, balanceOwed, notes, user);

        if (supplierService.addSupplier(supplier)) {
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "success", true, "message", "Supplier added successfully"));
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false, "message", "Failed to add supplier"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateSupplier(@PathVariable Long id,
                                                              @RequestBody UpdateSupplierRequest request) {
        Long userId = request.userId();
        String email = request.email();

        if (email != null && !email.isBlank() && supplierService.isEmailExists(email, userId, id)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "success", false, "message", "A supplier with this email already exists"));
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
            return ResponseEntity.ok(Map.of("success", true, "message", "Supplier updated successfully"));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "success", false, "message", "Supplier not found or update failed"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteSupplier(@PathVariable Long id) {
        if (supplierService.deleteSupplier(id)) {
            return ResponseEntity.ok(Map.of("success", true, "message", "Supplier deleted successfully"));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "success", false, "message", "Supplier not found"));
    }
}
