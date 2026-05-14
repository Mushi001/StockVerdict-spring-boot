package org.henriette.stockverdict.controllers;

import org.henriette.stockverdict.models.Customer;
import org.henriette.stockverdict.models.Products;
import org.henriette.stockverdict.models.SaleItem;
import org.henriette.stockverdict.models.Sales;
import org.henriette.stockverdict.models.Users;
import org.henriette.stockverdict.services.CustomerService;
import org.henriette.stockverdict.services.ProductService;
import org.henriette.stockverdict.services.SaleService;
import org.henriette.stockverdict.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.henriette.stockverdict.dto.ApiResponse;
import org.henriette.stockverdict.dto.SaleRequests.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * REST Controller for Sales management.
 * Handles endpoints for processing point-of-sale transactions, deducting inventory, and viewing sale history.
 */
@RestController
@RequestMapping("/api/sales")
@Tag(name = "Sales & Transactions", description = "Endpoints for managing point-of-sale transactions and retrieving sale history.")
public class SaleController {

    private final SaleService saleService;
    private final ProductService productService;
    private final UserService userService;
    private final CustomerService customerService;

    @Autowired
    public SaleController(SaleService saleService, ProductService productService,
                          UserService userService, CustomerService customerService) {
        this.saleService = saleService;
        this.productService = productService;
        this.userService = userService;
        this.customerService = customerService;
    }

    /**
     * Retrieves the entire sales history for a specific user.
     * 
     * @param userId The ID of the user.
     * @return ApiResponse containing a list of sales records.
     */
    @GetMapping
    @Operation(summary = "Get sales history", description = "Retrieves all sales history for a specific user.")
    public ResponseEntity<ApiResponse<List<Sales>>> getSales(@RequestParam Long userId) {
        List<Sales> sales = saleService.getSalesByUser(userId);
        return ResponseEntity.ok(new ApiResponse<>(true, sales));
    }

    /**
     * Retrieves the details of a specific sale including all items purchased.
     * 
     * @param id The unique identifier of the sale.
     * @return ApiResponse containing the sale record and a list of sale items.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get sale details", description = "Retrieves a specific sale and its associated line items.")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSaleById(@PathVariable Long id) {
        Sales sale = saleService.getSaleById(id);
        if (sale == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, "Sale not found"));
        }
        List<SaleItem> items = saleService.getSaleItemsBySale(id);
        return ResponseEntity.ok(new ApiResponse<>(true, Map.of("sale", sale, "items", items)));
    }

    /**
     * Retrieves the sales history associated with a specific customer.
     * 
     * @param customerId The unique identifier of the customer.
     * @return ApiResponse containing a list of sales made by the customer.
     */
    @GetMapping("/customer/{customerId}")
    @Operation(summary = "Get sales by customer", description = "Retrieves all sales records associated with a specific customer.")
    public ResponseEntity<ApiResponse<List<Sales>>> getSalesByCustomer(@PathVariable Long customerId) {
        List<Sales> sales = saleService.getSalesByCustomer(customerId);
        return ResponseEntity.ok(new ApiResponse<>(true, sales));
    }

    /**
     * Creates a new sale transaction. 
     * Processes the shopping cart, checks inventory levels, deducts stock, and saves the transaction.
     * 
     * @param request The data transfer object containing the user, customer, payment method, and cart items.
     * @return ApiResponse indicating whether the sale was successful.
     */
    @PostMapping
    @Operation(summary = "Create a new sale", description = "Processes a shopping cart of items, deduces stock, and creates a transaction record.")
    public ResponseEntity<ApiResponse<Void>> createSale(@RequestBody CreateSaleRequest request) {
        try {
            Long userId = request.userId();
            Users user = userService.findById(userId);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(new ApiResponse<>(false, "User not found"));
            }

            String paymentMethod = request.paymentMethod();
            
            Customer customer = null;
            if (request.customerId() != null) {
                String customerIdStr = request.customerId();
                if ("NEW".equals(customerIdStr)) {
                    String newName = request.newCustomerName();
                    String newPhone = request.newCustomerPhone();
                    if (newName != null && !newName.isBlank()) {
                        customer = new Customer(newName, newPhone, "", "", user);
                        customerService.addCustomer(customer);
                    }
                } else {
                    customer = customerService.getCustomerById(Long.valueOf(customerIdStr));
                }
            }

            List<SaleItemRequest> itemsData = request.items();
            if (itemsData == null || itemsData.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>(false, "No items selected for sale"));
            }

            List<SaleItem> items = new ArrayList<>();
            for (SaleItemRequest itemData : itemsData) {
                Long productId = itemData.productId();
                Integer qty = itemData.quantity();

                if (qty == null || qty <= 0) continue;

                Products product = productService.getProductById(productId);
                if (product == null) continue;

                if (product.getQuantityInStock() < qty) {
                    return ResponseEntity.status(HttpStatus.CONFLICT)
                            .body(new ApiResponse<>(false, "Insufficient stock for product: " + product.getName()));
                }

                SaleItem item = new SaleItem();
                item.setProduct(product);
                item.setQuantity(qty);
                item.setPriceAtSale(product.getSellingPrice());
                item.setSubtotal(qty * product.getSellingPrice());
                items.add(item);
            }

            if (items.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(new ApiResponse<>(false, "No valid items"));
            }

            Sales sale = new Sales(0.0, paymentMethod, user, customer);
            if (saleService.createSale(sale, items)) {
                return ResponseEntity.status(HttpStatus.CREATED)
                        .body(new ApiResponse<>(true, "Sale recorded successfully"));
            }

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "Failed to create sale"));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "System error"));
        }
    }

    /**
     * Updates details of an existing sale.
     * 
     * @param id The unique identifier of the sale.
     * @param request The data transfer object containing the updated sale details.
     * @return ApiResponse indicating success or failure.
     */
    @PutMapping("/{id}")
    @Operation(summary = "Update sale", description = "Updates an existing sale's payment method or customer.")
    public ResponseEntity<ApiResponse<Void>> updateSale(@PathVariable Long id,
                                                        @RequestBody UpdateSaleRequest request) {
        try {
            Long productId = request.productId();
            Integer quantity = request.quantity();
            String paymentMethod = request.paymentMethod();
            Long customerId = request.customerId();

            if (saleService.updateSale(id, productId, quantity, paymentMethod, customerId)) {
                return ResponseEntity.ok(new ApiResponse<>(true, "Sale updated successfully"));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, "Failed to update sale"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ApiResponse<>(false, "System error"));
        }
    }

    /**
     * Deletes a sale record. 
     * Note: Deleting a sale usually requires reversing stock deductions in the service layer.
     * 
     * @param id The unique identifier of the sale.
     * @return ApiResponse indicating success or failure.
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "Delete sale", description = "Deletes a sale transaction record.")
    public ResponseEntity<ApiResponse<Void>> deleteSale(@PathVariable Long id) {
        if (saleService.deleteSale(id)) {
            return ResponseEntity.ok(new ApiResponse<>(true, "Sale deleted successfully"));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ApiResponse<>(false, "Sale not found"));
    }
}
