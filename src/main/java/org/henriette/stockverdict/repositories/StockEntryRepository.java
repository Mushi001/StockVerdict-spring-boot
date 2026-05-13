package org.henriette.stockverdict.repositories;

import org.henriette.stockverdict.models.StockEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StockEntryRepository extends JpaRepository<StockEntry, Long> {

    List<StockEntry> findByUserIdOrderByDateAddedDesc(Long userId);

    List<StockEntry> findByProductIdOrderByDateAddedDesc(Long productId);

    List<StockEntry> findBySupplierIdOrderByDateAddedDesc(Long supplierId);

    @Query("SELECT se FROM StockEntry se WHERE se.user.id = :userId AND se.dateAdded >= :from AND se.dateAdded <= :to ORDER BY se.dateAdded DESC")
    List<StockEntry> findByUserIdAndDateRange(@Param("userId") Long userId, @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("SELECT SUM(se.quantityAdded * se.purchasePrice) FROM StockEntry se WHERE se.user.id = :userId")
    Double getTotalStockValueByUser(@Param("userId") Long userId);
}
