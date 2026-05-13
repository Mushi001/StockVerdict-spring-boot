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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for Sales management.
 */
@RestController
@RequestMapping("/api/sales")
public class SaleController {

    private final SaleService saleService;
    private final ProductService productService;
    private final CustomerService customerService;
    private final UserService userService;

    @Autowired
    public SaleController(SaleService saleService, ProductService productService,
                          CustomerService customerService, UserService userService) {
        this.saleService = saleService;
        this.productService = productService;
        this.customerService = customerService;
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getSales(@RequestParam Long userId) {
        List<Sales> sales = saleService.getSalesByUser(userId);
        return ResponseEntity.ok(Map.of("success", true, "count", sales.size(), "sales", sales));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getSaleById(@PathVariable Long id) {
        Sales sale = saleService.getSaleById(id);
        if (sale == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false, "message", "Sale not found"));
        }
        List<SaleItem> items = saleService.getSaleItemsBySale(id);
        return ResponseEntity.ok(Map.of("success", true, "sale", sale, "items", items));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<Map<String, Object>> getSalesByCustomer(@PathVariable Long customerId) {
        List<Sales> sales = saleService.getSalesByCustomer(customerId);
        return ResponseEntity.ok(Map.of("success", true, "count", sales.size(), "sales", sales));
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createSale(@RequestBody Map<String, Object> request) {
        try {
            Long userId = Long.valueOf(request.get("userId").toString());
            Users user = userService.findById(userId);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                        "success", false, "message", "User not found"));
            }

            String paymentMethod = (String) request.get("paymentMethod");
            
            Customer customer = null;
            if (request.containsKey("customerId") && request.get("customerId") != null) {
                String customerIdStr = request.get("customerId").toString();
                if ("NEW".equals(customerIdStr)) {
                    String newName = (String) request.get("newCustomerName");
                    String newPhone = (String) request.get("newCustomerPhone");
                    if (newName != null && !newName.isBlank()) {
                        customer = new Customer(newName, newPhone, "", "", user);
                        customerService.addCustomer(customer);
                    }
                } else {
                    customer = customerService.getCustomerById(Long.valueOf(customerIdStr));
                }
            }

            List<Map<String, Object>> itemsData = (List<Map<String, Object>>) request.get("items");
            if (itemsData == null || itemsData.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false, "message", "No items selected for sale"));
            }

            List<SaleItem> items = new ArrayList<>();
            for (Map<String, Object> itemData : itemsData) {
                Long productId = Long.valueOf(itemData.get("productId").toString());
                Integer qty = Integer.valueOf(itemData.get("quantity").toString());

                if (qty <= 0) continue;

                Products product = productService.getProductById(productId);
                if (product == null) continue;

                if (product.getQuantityInStock() < qty) {
                    return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of(
                            "success", false, "message", "Insufficient stock for product: " + product.getName()));
                }

                SaleItem item = new SaleItem();
                item.setProduct(product);
                item.setQuantity(qty);
                item.setPriceAtSale(product.getSellingPrice());
                item.setSubtotal(qty * product.getSellingPrice());
                items.add(item);
            }

            if (items.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false, "message", "No valid items"));
            }

            Sales sale = new Sales(0.0, paymentMethod, user, customer);
            if (saleService.createSale(sale, items)) {
                return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                        "success", true, "message", "Sale recorded successfully"));
            }

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false, "message", "Failed to create sale"));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false, "message", "System error"));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateSale(@PathVariable Long id,
                                                          @RequestBody Map<String, Object> request) {
        try {
            Long productId = Long.valueOf(request.get("productId").toString());
            Integer quantity = Integer.valueOf(request.get("quantity").toString());
            String paymentMethod = (String) request.get("paymentMethod");
            
            Long customerId = null;
            if (request.containsKey("customerId") && request.get("customerId") != null) {
                customerId = Long.valueOf(request.get("customerId").toString());
            }

            if (saleService.updateSale(id, productId, quantity, paymentMethod, customerId)) {
                return ResponseEntity.ok(Map.of("success", true, "message", "Sale updated successfully"));
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "success", false, "message", "Failed to update sale"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false, "message", "System error"));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteSale(@PathVariable Long id) {
        if (saleService.deleteSale(id)) {
            return ResponseEntity.ok(Map.of("success", true, "message", "Sale deleted successfully"));
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "success", false, "message", "Sale not found"));
    }
}
