package org.henriette.stockverdict.repositories;

import org.henriette.stockverdict.models.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    List<Customer> findByUserIdOrderByNameAsc(Long userId);

    List<Customer> findAllByOrderByNameAsc();

    @Query("SELECT c FROM Customer c WHERE c.user.id = :userId AND " +
           "(LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(c.phone) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY c.name ASC")
    List<Customer> searchByUserAndKeyword(@Param("userId") Long userId, @Param("keyword") String keyword);

    boolean existsByEmailAndIdNot(String email, Long id);

    boolean existsByEmail(String email);

    Long countByUserId(Long userId);
}
