package com.everis.mssavingaccount.service.impl;

import com.everis.mssavingaccount.entity.SavingAccount;
import com.everis.mssavingaccount.repository.SavingAccountRepository;
import com.everis.mssavingaccount.service.SavingAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class SavingAccountServiceImpl implements SavingAccountService {

    @Autowired
    SavingAccountRepository savingAccountRepository;

    @Override
    public Mono<SavingAccount> create(SavingAccount savingAccount) {
        return savingAccountRepository.save(savingAccount);
    }

    @Override
    public Flux<SavingAccount> findAll() {
        return savingAccountRepository.findAll();
    }

    @Override
    public Mono<SavingAccount> findById(String id) {
        return savingAccountRepository.findById(id) ;
    }

    @Override
    public Mono<SavingAccount> update(SavingAccount savingAccount) {
        return savingAccountRepository.save(savingAccount);
    }

    @Override
    public Mono<Boolean> delete(String id) {
        return savingAccountRepository.findById(id)
                .flatMap(
                        deletectaAhorro -> savingAccountRepository.delete(deletectaAhorro)
                                .then(Mono.just(Boolean.TRUE))
                )
                .defaultIfEmpty(Boolean.FALSE);
    }

    @Override
    public Mono<Long> findCustomerAccountBank(String id) {
        return savingAccountRepository.findByCustomerId(id).count();
    }
}
