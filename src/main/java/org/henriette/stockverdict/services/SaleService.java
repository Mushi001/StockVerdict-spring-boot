package org.henriette.stockverdict.services;

import org.henriette.stockverdict.models.Products;
import org.henriette.stockverdict.models.SaleItem;
import org.henriette.stockverdict.models.Sales;
import org.henriette.stockverdict.repositories.ProductRepository;
import org.henriette.stockverdict.repositories.SaleItemRepository;
import org.henriette.stockverdict.repositories.SalesRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
public class SaleService {

    private final SalesRepository salesRepository;
    private final SaleItemRepository saleItemRepository;
    private final ProductRepository productRepository;

    @Autowired
    public SaleService(SalesRepository salesRepository, SaleItemRepository saleItemRepository,
                       ProductRepository productRepository) {
        this.salesRepository = salesRepository;
        this.saleItemRepository = saleItemRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public boolean createSale(Sales sale, List<SaleItem> items) {
        try {
            // Validate stock
            for (SaleItem item : items) {
                Products product = productRepository.findById(item.getProduct().getId()).orElse(null);
                if (product == null) return false;
                if (product.getQuantityInStock() < item.getQuantity()) return false;
            }

            sale.setSaleDate(LocalDateTime.now());
            salesRepository.save(sale);

            double total = 0.0;
            for (SaleItem item : items) {
                Products product = productRepository.findById(item.getProduct().getId()).get();
                item.setSale(sale);
                item.setPriceAtSale(product.getSellingPrice());
                item.setSubtotal(item.getQuantity() * product.getSellingPrice());
                total += item.getSubtotal();

                product.setQuantityInStock(product.getQuantityInStock() - item.getQuantity());
                product.setUpdatedAt(LocalDateTime.now());
                productRepository.save(product);
                saleItemRepository.save(item);
            }

            sale.setTotalAmount(total);
            salesRepository.save(sale);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Transactional
    public boolean updateSale(Long saleId, Long productId, int quantity, String payment, Long customerId) {
        try {
            Sales sale = salesRepository.findById(saleId).orElse(null);
            if (sale == null) return false;

            // Reverse old stock
            List<SaleItem> oldItems = saleItemRepository.findBySaleId(saleId);
            for (SaleItem item : oldItems) {
                Products p = productRepository.findById(item.getProduct().getId()).orElse(null);
                if (p != null) {
                    p.setQuantityInStock(p.getQuantityInStock() + item.getQuantity());
                    productRepository.save(p);
                }
            }

            // Validate new stock
            Products newProduct = productRepository.findById(productId).orElse(null);
            if (newProduct == null || newProduct.getQuantityInStock() < quantity) return false;

            sale.setPaymentMethod(payment);
            double total = quantity * newProduct.getSellingPrice();
            sale.setTotalAmount(total);
            salesRepository.save(sale);

            // Delete old items and create new
            saleItemRepository.deleteAll(oldItems);

            SaleItem newItem = new SaleItem();
            newItem.setSale(sale);
            newItem.setProduct(newProduct);
            newItem.setQuantity(quantity);
            newItem.setPriceAtSale(newProduct.getSellingPrice());
            newItem.setSubtotal(total);
            saleItemRepository.save(newItem);

            newProduct.setQuantityInStock(newProduct.getQuantityInStock() - quantity);
            newProduct.setUpdatedAt(LocalDateTime.now());
            productRepository.save(newProduct);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Transactional
    public boolean deleteSale(Long saleId) {
        try {
            Sales sale = salesRepository.findById(saleId).orElse(null);
            if (sale == null) return false;

            List<SaleItem> items = saleItemRepository.findBySaleId(saleId);
            for (SaleItem item : items) {
                Products product = productRepository.findById(item.getProduct().getId()).orElse(null);
                if (product != null) {
                    product.setQuantityInStock(product.getQuantityInStock() + item.getQuantity());
                    product.setUpdatedAt(LocalDateTime.now());
                    productRepository.save(product);
                }
            }
            salesRepository.delete(sale);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public Sales getSaleById(Long saleId) {
        return salesRepository.findById(saleId).orElse(null);
    }

    public List<Sales> getSalesByUser(Long userId) {
        return salesRepository.findByUserIdWithItems(userId);
    }

    public List<SaleItem> getSaleItemsBySale(Long saleId) {
        return saleItemRepository.findBySaleId(saleId);
    }

    public List<Sales> getSalesByCustomer(Long customerId) {
        return salesRepository.findByCustomerIdOrderBySaleDateDesc(customerId);
    }

    public List<Sales> getSalesByDateRange(Long userId, LocalDateTime from, LocalDateTime to) {
        return salesRepository.findByUserIdAndDateRange(userId, from, to);
    }

    public Double getTotalRevenueByUser(Long userId) {
        Double result = salesRepository.getTotalRevenueByUser(userId);
        return result != null ? result : 0.0;
    }

    public Double getTotalRevenueByDateRange(Long userId, LocalDateTime from, LocalDateTime to) {
        Double result = salesRepository.getTotalRevenueByUserAndDateRange(userId, from, to);
        return result != null ? result : 0.0;
    }

    public Long countSalesByUser(Long userId) {
        return salesRepository.countByUserId(userId);
    }

    public List<Object[]> getTopSellingProducts(Long userId, int limit) {
        List<Object[]> results = salesRepository.getTopSellingProductsByUser(userId);
        return results.size() > limit ? results.subList(0, limit) : results;
    }

    public List<Object[]> getSystemWideTopSellingProducts(int limit) {
        List<Object[]> results = salesRepository.getSystemWideTopSellingProducts();
        return results.size() > limit ? results.subList(0, limit) : results;
    }

    public List<Object[]> getSystemWideTopTraders(int limit) {
        List<Object[]> results = salesRepository.getSystemWideTopTraders();
        return results.size() > limit ? results.subList(0, limit) : results;
    }

    public Double getSystemWideTotalRevenue() {
        Double result = salesRepository.getSystemWideTotalRevenue();
        return result != null ? result : 0.0;
    }
}
