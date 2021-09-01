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
    
    @GetMapping("/findByCustomerId/{idcustomer}")
    public Flux<SavingAccount> findByCustomerId(@PathVariable String idcustomer){
        return ctaAhorroService.findByCustomerId(idcustomer);
    }

    @PostMapping("/create")
    public Mono<ResponseEntity<SavingAccount>> create(@Valid @RequestBody SavingAccount savingAccount){
    	System.out.println(">>> SavingAccount <<<");
    	return ctaAhorroService.findCustomer(savingAccount.getCustomer().getId())
                .filter(customer -> {
                	System.out.println("Encontro al cliente : " + customer.getName());
                	return customer.getTypeCustomer().getValue().equals(TypeCustomer.EnumTypeCustomer.PERSONAL) && savingAccount.getBalance() >= 0;
                })
                .flatMap(customer -> ctaAhorroService.verifyExpiredDebt(savingAccount.getCustomer().getId())
                					.filter(expired -> {
                						System.out.println("Filtro deudas vencidas > " + expired);
                						return expired;
                					})
                					.flatMap(expired -> {	
                						System.out.println("Paso Filtro deudas vencidas > " + expired);
                    	            	return 	ctaAhorroService.verifyAccountNumber(savingAccount.getAccountNumber())
                    	            			.filter(opt -> {
                    	            				System.out.println("Filtro verifyAccountNumber : " + opt.isPresent());
                    	            				return !opt.isPresent();
                	            				})
                    	            			.flatMap(opt -> {
                    	            			return ctaAhorroService.findCustomerAccountBank(savingAccount.getCustomer().getId()) // COUNT CUENTAS AHORRO
                    	                                .filter(count -> {
                    	                                	System.out.println("filtro cantidad de cuentas : " + count);
                    	                                	return count < 1;
                    	                                })
                    	                                .flatMap(count -> {
                    	                                    switch (customer.getTypeCustomer().getSubType().getValue()) {
                    	                                        case VIP:   return ctaAhorroService.findCreditCardByCustomer(customer.getId())
                    	                                                    .count()
                    	                                                    .filter(cnt -> {
                    	                                                    	System.out.println("Cantidad credito = " + cnt );
                    	                                                    	System.out.println("Balance = " + savingAccount.getBalance() );
                    	                                                    	System.out.println("Average = " + calculateAveregaMin(savingAccount.getMinAverageVip()) );
                    	                                                    	return cnt > 0
                    	                                                                & savingAccount.getMinAverageVip() != null & savingAccount.getMinAverageVip() > 0.0
                    	                                                                & savingAccount.getBalance() != null & savingAccount.getBalance() >= 0.0
                    	                                                                & savingAccount.getBalance() >= calculateAveregaMin(savingAccount.getMinAverageVip());
                	                                                    	})
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
                    	            			});
                                })
        		)
                .defaultIfEmpty(new ResponseEntity<>(HttpStatus.BAD_REQUEST));
    }

    @PutMapping("/update")
    public Mono<ResponseEntity<SavingAccount>> update(@RequestBody SavingAccount savingAccount) {

        return ctaAhorroService.findById(savingAccount.getId())
	            .filter(saDB -> savingAccount.getBalance() >= 0)
	            .flatMap(saDB -> ctaAhorroService.create(savingAccount))
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
        Integer daysRemaining = LocalDate.now().lengthOfMonth() - LocalDate.now().getDayOfMonth()+1;
        return minAverageVip*LocalDate.now().getDayOfMonth()/daysRemaining;
    }

    @GetMapping("/findByAccountNumber/{numberAccount}")
    public Mono<SavingAccount> findByAccountNumber(@PathVariable String numberAccount){
        return ctaAhorroService.findByAccountNumber(numberAccount);
    }
    
    @PutMapping("/updateTransference")
    public Mono<ResponseEntity<SavingAccount>> updateForTransference(@Valid @RequestBody SavingAccount savingAccount) {
        return ctaAhorroService.create(savingAccount)
                .filter(customer -> savingAccount.getBalance() >= 0)
                .map(ft -> new ResponseEntity<>(ft, HttpStatus.CREATED));
    }
}
