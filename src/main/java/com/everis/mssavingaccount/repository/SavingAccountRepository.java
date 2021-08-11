package com.everis.mssavingaccount.repository;

import com.everis.mssavingaccount.entity.SavingAccount;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

public interface SavingAccountRepository extends ReactiveMongoRepository<SavingAccount, String> {
    Flux<SavingAccount> findByCustomerId(String id);
}
