package org.henriette.stockverdict.repositories;

import org.henriette.stockverdict.models.Sales;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SalesRepository extends JpaRepository<Sales, Long> {

    @Query("SELECT DISTINCT s FROM Sales s LEFT JOIN FETCH s.saleItems si LEFT JOIN FETCH si.product " +
           "WHERE s.user.id = :userId ORDER BY s.saleDate DESC")
    List<Sales> findByUserIdWithItems(@Param("userId") Long userId);

    List<Sales> findByCustomerIdOrderBySaleDateDesc(Long customerId);

    @Query("SELECT s FROM Sales s WHERE s.user.id = :userId AND s.saleDate >= :from AND s.saleDate <= :to ORDER BY s.saleDate DESC")
    List<Sales> findByUserIdAndDateRange(@Param("userId") Long userId, @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    @Query("SELECT SUM(s.totalAmount) FROM Sales s WHERE s.user.id = :userId")
    Double getTotalRevenueByUser(@Param("userId") Long userId);

    @Query("SELECT SUM(s.totalAmount) FROM Sales s WHERE s.user.id = :userId AND s.saleDate >= :from AND s.saleDate <= :to")
    Double getTotalRevenueByUserAndDateRange(@Param("userId") Long userId, @Param("from") LocalDateTime from, @Param("to") LocalDateTime to);

    Long countByUserId(Long userId);

    @Query("SELECT SUM(s.totalAmount) FROM Sales s")
    Double getSystemWideTotalRevenue();

    @Query("SELECT si.product.name, SUM(si.quantity) as totalQty FROM SaleItem si " +
           "WHERE si.sale.user.id = :userId GROUP BY si.product.name ORDER BY totalQty DESC")
    List<Object[]> getTopSellingProductsByUser(@Param("userId") Long userId);

    @Query("SELECT si.product.name, SUM(si.quantity) as totalQty FROM SaleItem si " +
           "GROUP BY si.product.name ORDER BY totalQty DESC")
    List<Object[]> getSystemWideTopSellingProducts();

    @Query("SELECT s.user.name, SUM(s.totalAmount) as revenue FROM Sales s " +
           "GROUP BY s.user.name ORDER BY revenue DESC")
    List<Object[]> getSystemWideTopTraders();
}
