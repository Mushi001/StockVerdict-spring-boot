package org.henriette.stockverdict.controllers;

import org.henriette.stockverdict.models.Products;
import org.henriette.stockverdict.models.Supplier;
import org.henriette.stockverdict.models.Users;
import org.henriette.stockverdict.services.ProductService;
import org.henriette.stockverdict.services.SupplierService;
import org.henriette.stockverdict.services.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * REST Controller for Product management.
 */
@RestController
@RequestMapping("/api/products")
@Tag(name = "Products", description = "Endpoints for managing products, inventory, and low stock items")
public class ProductController {

    private final ProductService productService;
    private final SupplierService supplierService;
    private final UserService userService;

    @Autowired
    public ProductController(ProductService productService, SupplierService supplierService, UserService userService) {
        this.productService = productService;
        this.supplierService = supplierService;
        this.userService = userService;
    }

    @GetMapping
    @Operation(summary = "Get all products", description = "Retrieves all products managed by a specific user ID.")
    public ResponseEntity<Map<String, Object>> getProducts(@RequestParam Long userId) {
        List<Products> products = productService.getProductsByUser(userId);
        return ResponseEntity.ok(Map.of("success", true, "count", products.size(), "products", products));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID", description = "Retrieves details of a single product.")
    public ResponseEntity<Map<String, Object>> getProductById(@PathVariable Long id) {
        Products product = productService.getProductById(id);
        if (product == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false, "message", "Product not found"));
        }
        return ResponseEntity.ok(Map.of("success", true, "product", product));
    }

    @GetMapping("/low-stock")
    public ResponseEntity<Map<String, Object>> getLowStockProducts(@RequestParam Long userId) {
        List<Products> products = productService.getLowStockProducts(userId);
        return ResponseEntity.ok(Map.of("success", true, "count", products.size(), "products", products));
    }

    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchProducts(@RequestParam Long userId,
                                                              @RequestParam String keyword) {
        List<Products> results = productService.searchProducts(userId, keyword);
        return ResponseEntity.ok(Map.of("success", true, "count", results.size(), "products", results));
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> addProduct(@RequestBody Map<String, Object> request) {
        Long userId = Long.valueOf(request.get("userId").toString());
        String name = (String) request.get("name");
        String description = (String) request.get("description");
        String barcode = (String) request.get("barcode");
        Double purchasePrice = Double.valueOf(request.get("purchasePrice").toString());
        Double sellingPrice = Double.valueOf(request.get("sellingPrice").toString());
        Integer quantityInStock = Integer.valueOf(request.get("quantityInStock").toString());
        Integer reorderLevel = Integer.valueOf(request.get("reorderLevel").toString());
        
        Long supplierId = null;
        if (request.containsKey("supplierId") && request.get("supplierId") != null) {
            supplierId = Long.valueOf(request.get("supplierId").toString());
        }

        if (sellingPrice < purchasePrice) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false, "message", "Selling Price cannot be lower than Cost Price."));
        }

        Users user = userService.findById(userId);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false, "message", "User not found"));
        }

        Supplier supplier = null;
        if (supplierId != null) {
            supplier = supplierService.getSupplierById(supplierId);
        }

        if (barcode != null && !barcode.isBlank() && productService.isBarcodeExists(barcode, null)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "success", false, "message", "A product with this barcode already exists"));
        }

        Products product = new Products(name, description, barcode, purchasePrice, sellingPrice,
                quantityInStock, reorderLevel, user, supplier);

        if (productService.addProduct(product)) {
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "success", true, "message", "Product added successfully"));
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "success", false, "message", "Failed to add product"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateProduct(@PathVariable Long id,
                                                             @RequestBody Map<String, Object> request) {
        String barcode = (String) request.get("barcode");
        Double purchasePrice = Double.valueOf(request.get("purchasePrice").toString());
        Double sellingPrice = Double.valueOf(request.get("sellingPrice").toString());
        
        Long supplierId = null;
        if (request.containsKey("supplierId") && request.get("supplierId") != null) {
            supplierId = Long.valueOf(request.get("supplierId").toString());
        }

        if (sellingPrice < purchasePrice) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false, "message", "Selling Price cannot be lower than Cost Price."));
        }

        if (barcode != null && !barcode.isBlank() && productService.isBarcodeExists(barcode, id)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                    "success", false, "message", "A product with this barcode already exists"));
        }

        Supplier supplier = null;
        if (supplierId != null) {
            supplier = supplierService.getSupplierById(supplierId);
        }

        Products updated = new Products();
        updated.setId(id);
        updated.setName((String) request.get("name"));
        updated.setDescription((String) request.get("description"));
        updated.setBarcode(barcode);
        updated.setPurchasePrice(purchasePrice);
        updated.setSellingPrice(sellingPrice);
        updated.setQuantityInStock(Integer.valueOf(request.get("quantityInStock").toString()));
        updated.setReorderLevel(Integer.valueOf(request.get("reorderLevel").toString()));
        updated.setSupplier(supplier);

        if (productService.updateProduct(updated)) {
            return ResponseEntity.ok(Map.of("success", true, "message", "Product updated successfully"));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "success", false, "message", "Product not found or update failed"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteProduct(@PathVariable Long id) {
        if (productService.deleteProduct(id)) {
            return ResponseEntity.ok(Map.of("success", true, "message", "Product deleted successfully"));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "success", false, "message", "Product not found"));
    }
}
