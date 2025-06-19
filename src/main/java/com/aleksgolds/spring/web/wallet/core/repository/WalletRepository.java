package com.aleksgolds.spring.web.wallet.core.repository;

import com.aleksgolds.spring.web.wallet.core.model.Wallet;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, UUID> {

     Optional<Wallet> findWalletById(UUID id);

    // Метод с пессимистичной блокировкой для конкурентного доступа
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT w FROM Wallet w WHERE w.id = :id")
    Optional<Wallet> findByIdForUpdate(@Param("id") UUID id);

    // Оптимистичная блокировка через @Version
    @Lock(LockModeType.OPTIMISTIC)
    Optional<Wallet> findWithVersionById(UUID id);
}
