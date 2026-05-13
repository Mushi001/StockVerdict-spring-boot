package org.henriette.stockverdict.services;

import org.henriette.stockverdict.models.Products;
import org.henriette.stockverdict.models.StockEntry;
import org.henriette.stockverdict.repositories.ProductRepository;
import org.henriette.stockverdict.repositories.StockEntryRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class StockEntryService {

    private final StockEntryRepository stockEntryRepository;
    private final ProductRepository productRepository;

    @Autowired
    public StockEntryService(StockEntryRepository stockEntryRepository, ProductRepository productRepository) {
        this.stockEntryRepository = stockEntryRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public boolean addStockEntry(StockEntry entry) {
        try {
            entry.setDateAdded(LocalDateTime.now());

            Products product = productRepository.findById(entry.getProduct().getId()).orElse(null);
            if (product == null) return false;

            product.setQuantityInStock(product.getQuantityInStock() + entry.getQuantityAdded());
            product.setPurchasePrice(entry.getPurchasePrice());
            product.setUpdatedAt(LocalDateTime.now());

            productRepository.save(product);
            stockEntryRepository.save(entry);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Transactional
    public boolean deleteStockEntry(Long entryId) {
        try {
            StockEntry entry = stockEntryRepository.findById(entryId).orElse(null);
            if (entry == null) return false;

            Products product = productRepository.findById(entry.getProduct().getId()).orElse(null);
            if (product != null) {
                int newQty = product.getQuantityInStock() - entry.getQuantityAdded();
                product.setQuantityInStock(Math.max(newQty, 0));
                product.setUpdatedAt(LocalDateTime.now());
                productRepository.save(product);
            }

            stockEntryRepository.delete(entry);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public StockEntry getStockEntryById(Long entryId) {
        return stockEntryRepository.findById(entryId).orElse(null);
    }

    public List<StockEntry> getStockEntriesByUser(Long userId) {
        return stockEntryRepository.findByUserIdOrderByDateAddedDesc(userId);
    }

    public List<StockEntry> getStockEntriesByProduct(Long productId) {
        return stockEntryRepository.findByProductIdOrderByDateAddedDesc(productId);
    }

    public List<StockEntry> getStockEntriesBySupplier(Long supplierId) {
        return stockEntryRepository.findBySupplierIdOrderByDateAddedDesc(supplierId);
    }

    public List<StockEntry> getStockEntriesByDateRange(Long userId, LocalDateTime from, LocalDateTime to) {
        return stockEntryRepository.findByUserIdAndDateRange(userId, from, to);
    }

    public Double getTotalStockValueByUser(Long userId) {
        Double result = stockEntryRepository.getTotalStockValueByUser(userId);
        return result != null ? result : 0.0;
    }
}
