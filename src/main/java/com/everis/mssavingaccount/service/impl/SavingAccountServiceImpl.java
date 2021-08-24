package com.everis.mssavingaccount.service.impl;

import com.everis.mssavingaccount.entity.BankAccount;
import com.everis.mssavingaccount.entity.CreditCard;
import com.everis.mssavingaccount.entity.CurrentAccount;
import com.everis.mssavingaccount.entity.Customer;
import com.everis.mssavingaccount.entity.FixedTerm;
import com.everis.mssavingaccount.entity.SavingAccount;
import com.everis.mssavingaccount.repository.SavingAccountRepository;
import com.everis.mssavingaccount.service.SavingAccountService;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class SavingAccountServiceImpl implements SavingAccountService {

    WebClient webClientCustomer = WebClient.create("http://localhost:8887/ms-customer/customer");

    WebClient webClientCreditCard = WebClient.create("http://localhost:8887/ms-creditcard/creditCard");
    
    WebClient webClientCurrent = WebClient.create("http://localhost:8887/ms-current-account/currentAccount");
    
    WebClient webClientFixed = WebClient.create("http://localhost:8887/ms-fixed-term/fixedTerm");
    
    WebClient webClientCreditCharge = WebClient.create("http://localhost:8887/ms-credit-charge/creditCharge");

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
	public Flux<SavingAccount> findByCustomerId(String idcustomer) {
		return savingAccountRepository.findByCustomerId(idcustomer);
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

    @Override
    public Mono<SavingAccount> findByAccountNumber(String number) {
        return savingAccountRepository.findByAccountNumber(number);
    }

	@Override
	public Mono<Optional<BankAccount>> verifyAccountNumber(String numberAccount) {
		return savingAccountRepository.findByAccountNumber(numberAccount)
				.map(sa -> Optional.of((BankAccount)sa))
				.switchIfEmpty(webClientCurrent.get().uri("/findByAccountNumber/{numberAccount}", numberAccount)
                        .accept(MediaType.APPLICATION_JSON)
                        .retrieve()
                        .bodyToMono(CurrentAccount.class)
                        .map(currentAccount -> {
                            System.out.println("Encontro fixedTerm > " + currentAccount.getId());
                            return Optional.of((BankAccount)currentAccount);
                        })
                        .switchIfEmpty(webClientFixed.get().uri("/findByAccountNumber/{numberAccount}", numberAccount)
                                        .accept(MediaType.APPLICATION_JSON)
                                        .retrieve()
                                        .bodyToMono(FixedTerm.class)
                                        .map(fixedTerm -> {
                                            System.out.println("Encontro savingAccount > " + fixedTerm.getId());
                                            return Optional.of((BankAccount)fixedTerm);
                                        }))
                                        .defaultIfEmpty(Optional.empty())
                        );
	}

	@Override
	public Mono<Boolean> verifyExpiredDebt(String idcustomer) {
		return webClientCreditCharge.get().uri("/verifyExpiredDebt/{idcustomer}", idcustomer)
		        .accept(MediaType.APPLICATION_JSON)
		        .retrieve()
		        .bodyToMono(Boolean.class);
	}



}
