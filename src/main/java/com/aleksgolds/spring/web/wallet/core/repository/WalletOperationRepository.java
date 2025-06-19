package com.aleksgolds.spring.web.wallet.core.repository;

import com.aleksgolds.spring.web.wallet.core.model.WalletOperation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WalletOperationRepository extends JpaRepository<WalletOperation, UUID> {

//    List<WalletOperation> findAllWalletOperations();
    List<WalletOperation> findAllOperationByWalletId(UUID walletId);
}
