package com.everis.mssavingaccount.repository;

import com.everis.mssavingaccount.entity.SavingAccount;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface SavingAccountRepository extends ReactiveMongoRepository<SavingAccount, String> {
    Flux<SavingAccount> findByCustomerId(String id);

    Mono<SavingAccount> findByCardNumber(String number);
}
