package org.henriette.stockverdict.repositories;

import org.henriette.stockverdict.models.Products;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Products, Long> {

    List<Products> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Products> findAllByOrderByCreatedAtDesc();

    @Query("SELECT p FROM Products p WHERE p.user.id = :userId AND p.quantityInStock <= p.reorderLevel ORDER BY p.quantityInStock ASC")
    List<Products> findLowStockByUser(@Param("userId") Long userId);

    @Query("SELECT p FROM Products p WHERE p.user.id = :userId AND " +
           "(LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.barcode) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY p.name ASC")
    List<Products> searchByUserAndKeyword(@Param("userId") Long userId, @Param("keyword") String keyword);

    boolean existsByBarcodeAndIdNot(String barcode, Long id);

    boolean existsByBarcode(String barcode);
}
