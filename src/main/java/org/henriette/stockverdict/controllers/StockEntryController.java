package org.henriette.stockverdict.controllers;

import org.henriette.stockverdict.models.Products;
import org.henriette.stockverdict.models.StockEntry;
import org.henriette.stockverdict.models.Supplier;
import org.henriette.stockverdict.models.Users;
import org.henriette.stockverdict.services.ProductService;
import org.henriette.stockverdict.services.StockEntryService;
import org.henriette.stockverdict.services.SupplierService;
import org.henriette.stockverdict.services.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.henriette.stockverdict.dto.StockEntryRequests.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for Stock Entry management.
 */
@RestController
@RequestMapping("/api/stock-entries")
public class StockEntryController {

    private final StockEntryService stockEntryService;
    private final ProductService productService;
    private final SupplierService supplierService;
    private final UserService userService;

    @Autowired
    public StockEntryController(StockEntryService stockEntryService, ProductService productService,
                                SupplierService supplierService, UserService userService) {
        this.stockEntryService = stockEntryService;
        this.productService = productService;
        this.supplierService = supplierService;
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getStockEntries(@RequestParam Long userId) {
        List<StockEntry> entries = stockEntryService.getStockEntriesByUser(userId);
        return ResponseEntity.ok(Map.of("success", true, "count", entries.size(), "stockEntries", entries));
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<Map<String, Object>> getStockEntriesByProduct(@PathVariable Long productId) {
        List<StockEntry> entries = stockEntryService.getStockEntriesByProduct(productId);
        return ResponseEntity.ok(Map.of("success", true, "count", entries.size(), "stockEntries", entries));
    }

    @GetMapping("/supplier/{supplierId}")
    public ResponseEntity<Map<String, Object>> getStockEntriesBySupplier(@PathVariable Long supplierId) {
        List<StockEntry> entries = stockEntryService.getStockEntriesBySupplier(supplierId);
        return ResponseEntity.ok(Map.of("success", true, "count", entries.size(), "stockEntries", entries));
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> addStockEntry(@RequestBody AddStockEntryRequest request) {
        Long userId = request.userId();
        Long productId = request.productId();
        Long supplierId = request.supplierId();
        Integer quantityAdded = request.quantityAdded();
        Double purchasePrice = request.purchasePrice();

        if (quantityAdded <= 0) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false, "message", "Invalid quantity"));
        }

        Users user = userService.findById(userId);
        Products product = productService.getProductById(productId);
        Supplier supplier = supplierService.getSupplierById(supplierId);

        if (user == null || product == null || supplier == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false, "message", "User, Product, or Supplier not found"));
        }

        StockEntry entry = new StockEntry(quantityAdded, purchasePrice, product, supplier, user);

        if (stockEntryService.addStockEntry(entry)) {
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "success", true, "message", "Stock entry added successfully"));
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false, "message", "Failed to add stock entry"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteStockEntry(@PathVariable Long id) {
        if (stockEntryService.deleteStockEntry(id)) {
            return ResponseEntity.ok(Map.of("success", true, "message", "Stock entry deleted successfully"));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "success", false, "message", "Stock entry not found"));
    }
}
