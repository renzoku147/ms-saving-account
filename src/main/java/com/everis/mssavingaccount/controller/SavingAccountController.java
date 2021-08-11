package com.everis.mssavingaccount.controller;

import com.everis.mssavingaccount.entity.Customer;
import com.everis.mssavingaccount.entity.SavingAccount;
import com.everis.mssavingaccount.entity.TypeCustomer;
import com.everis.mssavingaccount.service.SavingAccountService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/savingAccount")
@Slf4j
public class SavingAccountController {

    WebClient webClient = WebClient.create("http://localhost:8013/customer");

    @Autowired
    SavingAccountService ctaAhorroService;

    @GetMapping("/list")
    public Flux<SavingAccount> list(){
        return ctaAhorroService.findAll();
    }

    @GetMapping("/prueba/{id}")
    public Mono<Customer> list(@PathVariable String id){
        return webClient.get().uri("/find/{id}", id)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(Customer.class);
    }

    @GetMapping("/find/{id}")
    public Mono<SavingAccount> findById(@PathVariable String id){
        return ctaAhorroService.findById(id);
    }

    @PostMapping("/create")
    public Mono<ResponseEntity<SavingAccount>> create(@RequestBody SavingAccount savingAccount){
        //PARA CLIENTES PERSONALES

        Mono<Customer> customer = webClient.get().uri("/find/{id}", savingAccount.getCustomer().getId())
                                    .accept(MediaType.APPLICATION_JSON)
                                    .retrieve()
                                    .bodyToMono(Customer.class); // EXISTE EL CLIENTE?

        return ctaAhorroService.findCustomerAccountBank(savingAccount.getCustomer().getId()) //Mono<Long>
            .filter(count -> {
                return count <1;
            })
            .flatMap(p -> {
                return customer; // Long -> Customer
            }) // Mono<Customer> , Solo si existe
            .filter(c -> {
                return c.getTypeCustomer().equals(TypeCustomer.PERSONAL);
            })
            .map(c -> {
                savingAccount.setCustomer(c);
                savingAccount.setDate(LocalDateTime.now());
                return savingAccount; // Customer -> SavingAccount
            })
            .flatMap(s -> ctaAhorroService.create(s)) // Mono<SavingAccount>
            .map(savedSavingAccount -> {
                return new ResponseEntity<>(savedSavingAccount , HttpStatus.CREATED);
            })
            .defaultIfEmpty(new ResponseEntity<>(HttpStatus.BAD_REQUEST));

    }

    @PutMapping("/update")
    public Mono<ResponseEntity<SavingAccount>> update(@RequestBody SavingAccount c) {
        return ctaAhorroService.update(c)
                .filter(sa -> sa.getBalance() >= 0)
                .map(savedSavingAccount -> new ResponseEntity<>(savedSavingAccount, HttpStatus.CREATED))
                .defaultIfEmpty(new ResponseEntity<>(HttpStatus.BAD_REQUEST));
    }

    @DeleteMapping("/delete/{id}")
    public Mono<ResponseEntity<String>> delete(@PathVariable String id) {
        return ctaAhorroService.delete(id)
                .filter(deleteSavingAccount -> deleteSavingAccount)
                .map(deleteCustomer -> new ResponseEntity<>("Customer Deleted", HttpStatus.ACCEPTED))
                .defaultIfEmpty(new ResponseEntity<>(HttpStatus.BAD_REQUEST));
    }
}
