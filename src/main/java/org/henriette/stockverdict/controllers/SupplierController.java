package org.henriette.stockverdict.controllers;

import org.henriette.stockverdict.models.Supplier;
import org.henriette.stockverdict.models.Users;
import org.henriette.stockverdict.services.SupplierService;
import org.henriette.stockverdict.services.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<Map<String, Object>> addSupplier(@RequestBody Map<String, Object> request) {
        Long userId = Long.valueOf(request.get("userId").toString());
        String name = (String) request.get("name");
        String phone = (String) request.get("phone");
        String email = (String) request.get("email");
        String address = (String) request.get("address");
        String contactPerson = (String) request.get("contactPerson");
        String notes = (String) request.get("notes");
        
        double balanceOwed = 0.0;
        if (request.containsKey("balanceOwed") && request.get("balanceOwed") != null) {
            balanceOwed = Double.parseDouble(request.get("balanceOwed").toString());
        }

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
                                                              @RequestBody Map<String, Object> request) {
        Long userId = Long.valueOf(request.get("userId").toString());
        String email = (String) request.get("email");

        if (email != null && !email.isBlank() && supplierService.isEmailExists(email, userId, id)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "success", false, "message", "A supplier with this email already exists"));
        }

        Supplier updated = new Supplier();
        updated.setId(id);
        updated.setName((String) request.get("name"));
        updated.setPhone((String) request.get("phone"));
        updated.setEmail(email);
        updated.setAddress((String) request.get("address"));
        updated.setContactPerson((String) request.get("contactPerson"));
        updated.setNotes((String) request.get("notes"));
        
        double balanceOwed = 0.0;
        if (request.containsKey("balanceOwed") && request.get("balanceOwed") != null) {
            balanceOwed = Double.parseDouble(request.get("balanceOwed").toString());
        }
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
