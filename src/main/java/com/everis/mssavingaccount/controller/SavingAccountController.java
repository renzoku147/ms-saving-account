package com.everis.mssavingaccount.controller;

import com.everis.mssavingaccount.entity.SavingAccount;
import com.everis.mssavingaccount.entity.TypeCustomer;
import com.everis.mssavingaccount.service.SavingAccountService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@RestController
@RequestMapping("/savingAccount")
@Slf4j
public class SavingAccountController {

    @Autowired
    SavingAccountService ctaAhorroService;

    @GetMapping("/list")
    public Flux<SavingAccount> list(){
        return ctaAhorroService.findAll();
    }

    @GetMapping("/find/{id}")
    public Mono<SavingAccount> findById(@PathVariable String id){
        return ctaAhorroService.findById(id);
    }

    @PostMapping("/create")
    public Mono<ResponseEntity<SavingAccount>> create(@Valid @RequestBody SavingAccount savingAccount){

        return ctaAhorroService.findCustomer(savingAccount.getCustomer().getId())
            .filter(customer -> customer.getTypeCustomer().getValue().equals(TypeCustomer.EnumTypeCustomer.PERSONAL) && savingAccount.getBalance() >= 0)
            .flatMap(customer -> {
                    return ctaAhorroService.findCustomerAccountBank(savingAccount.getCustomer().getId()) // COUNT CUENTAS AHORRO
                            .filter(count -> count < 1)
                            .flatMap(count -> {
                                switch (customer.getTypeCustomer().getSubType().getValue()) {
                                    case VIP:   return ctaAhorroService.findCreditCardByCustomer(customer.getId())
                                                .count()
                                                .filter(cnt -> cnt > 0
                                                            & savingAccount.getMinAverageVip() != null & savingAccount.getMinAverageVip() > 0.0
                                                            & savingAccount.getBalance() != null & savingAccount.getBalance() >= 0.0
                                                            & savingAccount.getBalance() >= calculateAveregaMin(savingAccount.getMinAverageVip()))
                                                .flatMap(cnt -> {
                                                    savingAccount.setCustomer(customer);
                                                    savingAccount.setDate(LocalDateTime.now());
                                                    return ctaAhorroService.create(savingAccount);
                                                });

                                    case NORMAL: savingAccount.setCustomer(customer);
                                                savingAccount.setDate(LocalDateTime.now());
                                                savingAccount.setBalance(savingAccount.getBalance() != null ? savingAccount.getBalance() : 0.0);
                                                return ctaAhorroService.create(savingAccount);
                                    default: return Mono.empty();
                                }
                            })
                            .map(savedSavingAccount -> new ResponseEntity<>(savedSavingAccount , HttpStatus.CREATED));
            })
            .defaultIfEmpty(new ResponseEntity<>(HttpStatus.BAD_REQUEST));
    }

    @PutMapping("/update")
    public Mono<ResponseEntity<SavingAccount>> update(@RequestBody SavingAccount savingAccount) {

        return ctaAhorroService.findById(savingAccount.getId())
            .filter(saDB -> savingAccount.getBalance() >= 0)
            .flatMap(saDB -> ctaAhorroService.findCustomer(savingAccount.getCustomer().getId())
                        .filter(customer -> customer.getTypeCustomer().getValue().equals(TypeCustomer.EnumTypeCustomer.PERSONAL))
                        .flatMap(customer -> {
                                    log.info("Hola si entro aqui");
                                    switch (customer.getTypeCustomer().getSubType().getValue()) {
                                        case VIP:   return ctaAhorroService.findCreditCardByCustomer(customer.getId())
                                                .count()
                                                .filter(cnt -> cnt > 0
                                                        & savingAccount.getMinAverageVip() != null & savingAccount.getMinAverageVip() > 0.0
                                                        & savingAccount.getBalance() != null & savingAccount.getBalance() >= 0.0
                                                        & savingAccount.getBalance() >= calculateAveregaMin(savingAccount.getMinAverageVip()))
                                                .flatMap(cnt -> {
                                                    savingAccount.setCustomer(customer);
                                                    savingAccount.setDate(LocalDateTime.now());
                                                    return ctaAhorroService.create(savingAccount);
                                                });

                                        case NORMAL: savingAccount.setCustomer(customer);
                                            savingAccount.setDate(LocalDateTime.now());
                                            savingAccount.setBalance(savingAccount.getBalance() != null ? savingAccount.getBalance() : 0.0);
                                            return ctaAhorroService.create(savingAccount);
                                        default: return Mono.empty();
                                    }
                                }
                        )
            )
            .map(savedSavingAccount -> new ResponseEntity<>(savedSavingAccount , HttpStatus.CREATED))
            .defaultIfEmpty(new ResponseEntity<>(HttpStatus.BAD_REQUEST));

    }

    @DeleteMapping("/delete/{id}")
    public Mono<ResponseEntity<String>> delete(@PathVariable String id) {
        return ctaAhorroService.delete(id)
                .filter(deleteSavingAccount -> deleteSavingAccount)
                .map(deleteCustomer -> new ResponseEntity<>("Customer Deleted", HttpStatus.ACCEPTED))
                .defaultIfEmpty(new ResponseEntity<>(HttpStatus.BAD_REQUEST));
    }

    public Double calculateAveregaMin(Double minAverageVip){
        Integer daysRemaining = LocalDate.now().lengthOfMonth() - LocalDate.now().getDayOfMonth();
        return minAverageVip*LocalDate.now().getDayOfMonth()/daysRemaining;
    }

    @GetMapping("/findByAccountNumber/{number}")
    public Mono<SavingAccount> findByAccountNumber(@PathVariable String number){
        return ctaAhorroService.findByCardNumber(number);
    }

    @PutMapping("/updateTransference")
    public Mono<ResponseEntity<SavingAccount>> updateForTransference(@Valid @RequestBody SavingAccount savingAccount) {
        return ctaAhorroService.create(savingAccount)
                .filter(customer -> savingAccount.getBalance() >= 0)
                .map(ft -> new ResponseEntity<>(ft, HttpStatus.CREATED));
    }
}
