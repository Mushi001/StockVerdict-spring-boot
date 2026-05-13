package org.henriette.stockverdict.controllers;

import org.henriette.stockverdict.models.Products;
import org.henriette.stockverdict.models.Supplier;
import org.henriette.stockverdict.models.Users;
import org.henriette.stockverdict.services.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for Dashboard aggregation.
 */
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final ProductService productService;
    private final SupplierService supplierService;
    private final CustomerService customerService;
    private final SaleService saleService;
    private final UserService userService;

    @Autowired
    public DashboardController(ProductService productService, SupplierService supplierService,
                               CustomerService customerService, SaleService saleService,
                               UserService userService) {
        this.productService = productService;
        this.supplierService = supplierService;
        this.customerService = customerService;
        this.saleService = saleService;
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getDashboardData(@RequestParam Long userId) {
        Users user = userService.findById(userId);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "success", false, "message", "User not found"));
        }

        try {
            Map<String, Object> data = new HashMap<>();

            List<Products> productList = productService.getProductsByUser(userId);
            List<Supplier> supplierList = supplierService.getSuppliersByUser(userId);

            data.put("totalProducts", productList.size());
            data.put("totalSuppliers", supplierList.size());
            data.put("totalCustomers", customerService.countCustomersByUser(userId));
            data.put("totalSales", saleService.countSalesByUser(userId));

            data.put("totalRevenue", saleService.getTotalRevenueByUser(userId));

            long lowStockCount = productList.stream()
                    .filter(p -> p.getQuantityInStock() > 0 && p.getQuantityInStock() <= p.getReorderLevel())
                    .count();
            data.put("lowStockCount", lowStockCount);

            long outOfStockCount = productList.stream()
                    .filter(p -> p.getQuantityInStock() == 0)
                    .count();
            data.put("outOfStockCount", outOfStockCount);

            double totalStockValue = productList.stream()
                    .mapToDouble(p -> p.getQuantityInStock() * p.getPurchasePrice())
                    .sum();
            data.put("totalStockValue", totalStockValue);

            double totalOwed = supplierList.stream()
                    .mapToDouble(Supplier::getBalanceOwed)
                    .sum();
            data.put("totalOwed", totalOwed);

            return ResponseEntity.ok(Map.of("success", true, "data", data));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "success", false, "message", "Failed to load dashboard data"));
        }
    }
}
