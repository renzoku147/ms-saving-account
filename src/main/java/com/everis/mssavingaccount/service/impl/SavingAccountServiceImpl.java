package com.everis.mssavingaccount.service.impl;

import com.everis.mssavingaccount.entity.CreditCard;
import com.everis.mssavingaccount.entity.Customer;
import com.everis.mssavingaccount.entity.SavingAccount;
import com.everis.mssavingaccount.repository.SavingAccountRepository;
import com.everis.mssavingaccount.service.SavingAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class SavingAccountServiceImpl implements SavingAccountService {

    WebClient webClientCustomer = WebClient.create("http://localhost:8887/ms-customer/customer/customer");

    WebClient webClientCreditCard = WebClient.create("http://localhost:8887/ms-creditcard/creditcard/creditcard");

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

    @Override
    public Mono<Customer> findCustomer(String id) {
        return webClientCustomer.get().uri("/find/{id}", id)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(Customer.class);
    }

    @Override
    public Flux<CreditCard> findCreditCardByCustomer(String id) {
        return webClientCreditCard.get().uri("/findCreditCards/{id}", id)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToFlux(CreditCard.class);
    }


}
