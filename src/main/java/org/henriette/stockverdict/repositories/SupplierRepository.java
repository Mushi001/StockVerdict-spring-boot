package org.henriette.stockverdict.repositories;

import org.henriette.stockverdict.models.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {

    List<Supplier> findByUserIdOrderByNameAsc(Long userId);

    List<Supplier> findAllByOrderByNameAsc();

    @Query("SELECT s FROM Supplier s WHERE s.user.id = :userId AND " +
           "(LOWER(s.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(s.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(s.phone) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY s.name ASC")
    List<Supplier> searchByUserAndKeyword(@Param("userId") Long userId, @Param("keyword") String keyword);

    boolean existsByEmailAndUserIdAndIdNot(String email, Long userId, Long id);

    boolean existsByEmailAndUserId(String email, Long userId);
}
