package org.henriette.stockverdict.services;

import org.henriette.stockverdict.models.Supplier;
import org.henriette.stockverdict.models.Users;
import org.henriette.stockverdict.repositories.SupplierRepository;
import org.henriette.stockverdict.repositories.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class SupplierService {

    private final SupplierRepository supplierRepository;
    private final UserRepository userRepository;

    @Autowired
    public SupplierService(SupplierRepository supplierRepository, UserRepository userRepository) {
        this.supplierRepository = supplierRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public boolean addSupplier(Supplier supplier) {
        try {
            if (supplier.getUser() != null && supplier.getUser().getId() != null) {
                Users managedUser = userRepository.findById(supplier.getUser().getId()).orElse(null);
                supplier.setUser(managedUser);
            }
            supplierRepository.save(supplier);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Transactional
    public boolean updateSupplier(Supplier updatedSupplier) {
        try {
            Optional<Supplier> opt = supplierRepository.findById(updatedSupplier.getId());
            if (opt.isEmpty()) return false;

            Supplier existing = opt.get();
            existing.setName(updatedSupplier.getName());
            existing.setPhone(updatedSupplier.getPhone());
            existing.setEmail(updatedSupplier.getEmail());
            existing.setAddress(updatedSupplier.getAddress());
            existing.setContactPerson(updatedSupplier.getContactPerson());
            existing.setBalanceOwed(updatedSupplier.getBalanceOwed());
            existing.setNotes(updatedSupplier.getNotes());

            supplierRepository.save(existing);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Transactional
    public boolean deleteSupplier(Long supplierId) {
        try {
            if (!supplierRepository.existsById(supplierId)) return false;
            supplierRepository.deleteById(supplierId);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public Supplier getSupplierById(Long supplierId) {
        return supplierRepository.findById(supplierId).orElse(null);
    }

    public List<Supplier> getSuppliersByUser(Long userId) {
        return supplierRepository.findByUserIdOrderByNameAsc(userId);
    }

    public List<Supplier> getAllSuppliers() {
        return supplierRepository.findAllByOrderByNameAsc();
    }

    public List<Supplier> searchSuppliers(Long userId, String keyword) {
        return supplierRepository.searchByUserAndKeyword(userId, keyword);
    }

    public boolean isEmailExists(String email, Long userId, Long excludeSupplierId) {
        if (excludeSupplierId != null) {
            return supplierRepository.existsByEmailAndUserIdAndIdNot(email, userId, excludeSupplierId);
        }
        return supplierRepository.existsByEmailAndUserId(email, userId);
    }
}
