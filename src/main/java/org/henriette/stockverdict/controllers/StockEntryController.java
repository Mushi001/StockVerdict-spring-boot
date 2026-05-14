package org.henriette.stockverdict.controllers;

import org.henriette.stockverdict.dto.ApiResponse;
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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

/**
 * REST Controller for Stock Entry management.
 * Handles endpoints for logging new inventory arrivals and viewing stock history.
 */
@RestController
@RequestMapping("/api/stock-entries")
@Tag(name = "Stock Entries", description = "Endpoints for managing and logging stock additions and inventory purchases.")
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

    /**
     * Retrieves all stock entries logged by a specific user.
     * 
     * @param userId The ID of the user.
     * @return ApiResponse containing a list of stock entries.
     */
    @GetMapping
    @Operation(summary = "Get all stock entries", description = "Retrieves a history of all stock additions made by a user.")
    public ResponseEntity<ApiResponse<List<StockEntry>>> getStockEntries(@RequestParam Long userId) {
        List<StockEntry> entries = stockEntryService.getStockEntriesByUser(userId);
        return ResponseEntity.ok(new ApiResponse<>(true, entries));
    }

    /**
     * Retrieves all stock entries associated with a specific product.
     * 
     * @param productId The ID of the product.
     * @return ApiResponse containing a list of stock entries for that product.
     */
    @GetMapping("/product/{productId}")
    @Operation(summary = "Get stock entries by product", description = "Retrieves the stock addition history for a specific product.")
    public ResponseEntity<ApiResponse<List<StockEntry>>> getStockEntriesByProduct(@PathVariable Long productId) {
        List<StockEntry> entries = stockEntryService.getStockEntriesByProduct(productId);
        return ResponseEntity.ok(new ApiResponse<>(true, entries));
    }

    /**
     * Retrieves all stock entries associated with a specific supplier.
     * 
     * @param supplierId The ID of the supplier.
     * @return ApiResponse containing a list of stock entries provided by that supplier.
     */
    @GetMapping("/supplier/{supplierId}")
    @Operation(summary = "Get stock entries by supplier", description = "Retrieves the stock addition history provided by a specific supplier.")
    public ResponseEntity<ApiResponse<List<StockEntry>>> getStockEntriesBySupplier(@PathVariable Long supplierId) {
        List<StockEntry> entries = stockEntryService.getStockEntriesBySupplier(supplierId);
        return ResponseEntity.ok(new ApiResponse<>(true, entries));
    }

    /**
     * Logs a new stock entry and increments the product's available inventory.
     * 
     * @param request The data transfer object containing stock entry details.
     * @return ApiResponse indicating success or failure.
     */
    @PostMapping
    @Operation(summary = "Add a stock entry", description = "Logs a new stock arrival and automatically updates the product inventory levels.")
    public ResponseEntity<ApiResponse<Void>> addStockEntry(@RequestBody AddStockEntryRequest request) {
        Long userId = request.userId();
        Long productId = request.productId();
        Long supplierId = request.supplierId();
        Integer quantityAdded = request.quantityAdded();
        Double purchasePrice = request.purchasePrice();

        if (quantityAdded <= 0) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Invalid quantity"));
        }

        Users user = userService.findById(userId);
        Products product = productService.getProductById(productId);
        Supplier supplier = supplierService.getSupplierById(supplierId);

        if (user == null || product == null || supplier == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, "User, Product, or Supplier not found"));
        }

        StockEntry entry = new StockEntry(quantityAdded, purchasePrice, product, supplier, user);

        if (stockEntryService.addStockEntry(entry)) {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>(true, "Stock entry added successfully"));
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Failed to add stock entry"));
    }

    /**
     * Deletes a stock entry and optionally reverses its effect on inventory.
     * 
     * @param id The unique identifier of the stock entry.
     * @return ApiResponse indicating success or failure.
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete stock entry", description = "Removes a stock entry record from the system.")
    public ResponseEntity<ApiResponse<Void>> deleteStockEntry(@PathVariable Long id) {
        if (stockEntryService.deleteStockEntry(id)) {
            return ResponseEntity.ok(new ApiResponse<>(true, "Stock entry deleted successfully"));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiResponse<>(false, "Stock entry not found"));
    }
}
