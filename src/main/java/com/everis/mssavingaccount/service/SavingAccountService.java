package com.everis.mssavingaccount.service;

import java.util.Optional;

import com.everis.mssavingaccount.entity.BankAccount;
import com.everis.mssavingaccount.entity.CreditCard;
import com.everis.mssavingaccount.entity.Customer;
import com.everis.mssavingaccount.entity.SavingAccount;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface SavingAccountService {

    Mono<SavingAccount> create(SavingAccount savingAccount);

    Flux<SavingAccount> findAll();

    Mono<SavingAccount> findById(String id);
    
    Flux<SavingAccount> findByCustomerId(String idcustomer);

    Mono<SavingAccount> update(SavingAccount savingAccount);

    Mono<Boolean> delete(String id);

    Mono<Long>  findCustomerAccountBank(String id);

    Mono<Customer>  findCustomer(String id);

    Flux<CreditCard> findCreditCardByCustomer(String t);

    Mono<SavingAccount> findByAccountNumber(String number);
    
    public Mono<Optional<BankAccount>> verifyAccountNumber(String numberAccount);
    
    Mono<Boolean> verifyExpiredDebt(String idcustomer);
}
