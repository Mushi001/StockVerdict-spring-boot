package org.henriette.stockverdict.services;

import org.henriette.stockverdict.models.Products;
import org.henriette.stockverdict.repositories.ProductRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    @Autowired
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional
    public boolean addProduct(Products product) {
        try {
            product.setCreatedAt(LocalDateTime.now());
            product.setUpdatedAt(LocalDateTime.now());
            productRepository.save(product);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Transactional
    public boolean updateProduct(Products updatedProduct) {
        try {
            Optional<Products> opt = productRepository.findById(updatedProduct.getId());
            if (opt.isEmpty()) return false;

            Products existing = opt.get();
            existing.setName(updatedProduct.getName());
            existing.setDescription(updatedProduct.getDescription());
            existing.setBarcode(updatedProduct.getBarcode());
            existing.setPurchasePrice(updatedProduct.getPurchasePrice());
            existing.setSellingPrice(updatedProduct.getSellingPrice());
            existing.setQuantityInStock(updatedProduct.getQuantityInStock());
            existing.setReorderLevel(updatedProduct.getReorderLevel());
            existing.setUpdatedAt(LocalDateTime.now());
            productRepository.save(existing);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Transactional
    public boolean deleteProduct(Long productId) {
        try {
            if (!productRepository.existsById(productId)) return false;
            productRepository.deleteById(productId);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public Products getProductById(Long productId) {
        return productRepository.findById(productId).orElse(null);
    }

    public List<Products> getProductsByUser(Long userId) {
        return productRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<Products> getAllProducts() {
        return productRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<Products> getLowStockProducts(Long userId) {
        return productRepository.findLowStockByUser(userId);
    }

    public List<Products> searchProducts(Long userId, String keyword) {
        return productRepository.searchByUserAndKeyword(userId, keyword);
    }

    public boolean isBarcodeExists(String barcode, Long excludeProductId) {
        if (excludeProductId != null) {
            return productRepository.existsByBarcodeAndIdNot(barcode, excludeProductId);
        }
        return productRepository.existsByBarcode(barcode);
    }
}
