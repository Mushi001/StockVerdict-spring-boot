package org.henriette.stockverdict.controllers;

import org.henriette.stockverdict.dto.ApiResponse;
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

import org.henriette.stockverdict.dto.ProductRequests.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

/**
 * REST Controller for Product management.
 * Provides endpoints for creating, updating, retrieving, and managing product inventory.
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

    /**
     * Retrieves all products managed by a specific user.
     * 
     * @param userId The ID of the user requesting their products.
     * @return ApiResponse containing a list of Products.
     */
    @GetMapping
    @Operation(summary = "Get all products", description = "Retrieves all products managed by a specific user ID.")
    public ResponseEntity<ApiResponse<List<Products>>> getProducts(@RequestParam Long userId) {
        List<Products> products = productService.getProductsByUser(userId);
        return ResponseEntity.ok(new ApiResponse<>(true, products));
    }

    /**
     * Retrieves details of a single product by its ID.
     * 
     * @param id The unique identifier of the product.
     * @return ApiResponse containing the product details, or 404 if not found.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get product by ID", description = "Retrieves details of a single product.")
    public ResponseEntity<ApiResponse<Products>> getProductById(@PathVariable Long id) {
        Products product = productService.getProductById(id);
        if (product == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, "Product not found"));
        }
        return ResponseEntity.ok(new ApiResponse<>(true, product));
    }

    /**
     * Retrieves a list of products that have reached or fallen below their reorder level.
     * 
     * @param userId The ID of the user owning the products.
     * @return ApiResponse containing a list of low stock products.
     */
    @GetMapping("/low-stock")
    @Operation(summary = "Get low stock products", description = "Retrieves products that are at or below their reorder level.")
    public ResponseEntity<ApiResponse<List<Products>>> getLowStockProducts(@RequestParam Long userId) {
        List<Products> products = productService.getLowStockProducts(userId);
        return ResponseEntity.ok(new ApiResponse<>(true, products));
    }

    /**
     * Searches for products by keyword in their name or description.
     * 
     * @param userId The ID of the user owning the products.
     * @param keyword The search term.
     * @return ApiResponse containing a list of matching products.
     */
    @GetMapping("/search")
    @Operation(summary = "Search products", description = "Searches for products by keyword in the product name or description.")
    public ResponseEntity<ApiResponse<List<Products>>> searchProducts(@RequestParam Long userId,
                                                                      @RequestParam String keyword) {
        List<Products> results = productService.searchProducts(userId, keyword);
        return ResponseEntity.ok(new ApiResponse<>(true, results));
    }

    /**
     * Creates a new product in the inventory.
     * 
     * @param request The data transfer object containing product details.
     * @return ApiResponse indicating success or failure.
     */
    @PostMapping
    @Operation(summary = "Add a new product", description = "Creates a new product record in the system inventory.")
    public ResponseEntity<ApiResponse<Void>> addProduct(@RequestBody AddProductRequest request) {
        Long userId = request.userId();
        String name = request.name();
        String description = request.description();
        String barcode = request.barcode();
        Double purchasePrice = request.purchasePrice();
        Double sellingPrice = request.sellingPrice();
        Integer quantityInStock = request.quantityInStock();
        Integer reorderLevel = request.reorderLevel();
        Long supplierId = request.supplierId();

        if (sellingPrice < purchasePrice) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Selling Price cannot be lower than Cost Price."));
        }

        Users user = userService.findById(userId);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, "User not found"));
        }

        Supplier supplier = null;
        if (supplierId != null) {
            supplier = supplierService.getSupplierById(supplierId);
        }

        if (barcode != null && !barcode.isBlank() && productService.isBarcodeExists(barcode, null)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiResponse<>(false, "A product with this barcode already exists"));
        }

        Products product = new Products(name, description, barcode, purchasePrice, sellingPrice,
                quantityInStock, reorderLevel, user, supplier);

        if (productService.addProduct(product)) {
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new ApiResponse<>(true, "Product added successfully"));
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, "Failed to add product"));
    }

    /**
     * Updates an existing product's details.
     * 
     * @param id The unique identifier of the product to update.
     * @param request The data transfer object containing updated product details.
     * @return ApiResponse indicating success or failure.
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update product", description = "Updates an existing product's details.")
    public ResponseEntity<ApiResponse<Void>> updateProduct(@PathVariable Long id,
                                                           @RequestBody UpdateProductRequest request) {
        String barcode = request.barcode();
        Double purchasePrice = request.purchasePrice();
        Double sellingPrice = request.sellingPrice();
        Long supplierId = request.supplierId();

        if (sellingPrice < purchasePrice) {
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, "Selling Price cannot be lower than Cost Price."));
        }

        if (barcode != null && !barcode.isBlank() && productService.isBarcodeExists(barcode, id)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ApiResponse<>(false, "A product with this barcode already exists"));
        }

        Supplier supplier = null;
        if (supplierId != null) {
            supplier = supplierService.getSupplierById(supplierId);
        }

        Products updated = new Products();
        updated.setId(id);
        updated.setName(request.name());
        updated.setDescription(request.description());
        updated.setBarcode(barcode);
        updated.setPurchasePrice(purchasePrice);
        updated.setSellingPrice(sellingPrice);
        updated.setQuantityInStock(request.quantityInStock());
        updated.setReorderLevel(request.reorderLevel());
        updated.setSupplier(supplier);

        if (productService.updateProduct(updated)) {
            return ResponseEntity.ok(new ApiResponse<>(true, "Product updated successfully"));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiResponse<>(false, "Product not found or update failed"));
    }

    /**
     * Deletes a product from the system.
     * 
     * @param id The unique identifier of the product.
     * @return ApiResponse indicating success or failure.
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete product", description = "Removes a product from the inventory.")
    public ResponseEntity<ApiResponse<Void>> deleteProduct(@PathVariable Long id) {
        if (productService.deleteProduct(id)) {
            return ResponseEntity.ok(new ApiResponse<>(true, "Product deleted successfully"));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiResponse<>(false, "Product not found"));
    }
}
