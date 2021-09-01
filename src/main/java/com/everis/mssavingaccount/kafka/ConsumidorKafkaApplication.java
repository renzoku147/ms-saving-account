package com.everis.mssavingaccount.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.config.TopicBuilder;

import com.everis.mssavingaccount.entity.BootCoinRequest;
import com.everis.mssavingaccount.entity.BootCoinTransfer;
import com.everis.mssavingaccount.entity.SavingAccount;
import com.everis.mssavingaccount.service.SavingAccountService;
import com.fasterxml.jackson.databind.ObjectMapper;

import reactor.core.publisher.Flux;

@Configuration
public class ConsumidorKafkaApplication {
	@Autowired
	SavingAccountService savingAccountService;  
	
	ObjectMapper objectMapper = new ObjectMapper();
	
	@Bean
    public NewTopic topic(){
        return TopicBuilder.name("topico-everis5")
                .partitions(10)
                .replicas(1)
                .build();
    }

    @KafkaListener(id="myId", topics = "topico-everis5")
    public void listen(String message) throws Exception{
    	System.out.println(">>>>> topico-everis5 @KafkaListener <<<<<");
    	SavingAccount ca = objectMapper.readValue(message, SavingAccount.class);
    	System.out.println(">>> SavingAccount <<<");
    	System.out.println(ca);
    	
    	savingAccountService.update(ca).subscribe();
        	
    }
    
    @KafkaListener(id="myId2", topics = "topico-everis7")
    public void listen2(String message) throws Exception{
    	System.out.println(">>>>> topico-everis7 @KafkaListener <<<<<");
    	BootCoinRequest bcr = objectMapper.readValue(message, BootCoinRequest.class);
    	System.out.println(">>> SavingAccount <<< " + bcr.getAccountNumber());
    	
    	savingAccountService.findByAccountNumber(bcr.getAccountNumber())
    		.flatMap(savingAccount -> {
    					savingAccount.setBalance(savingAccount.getBalance()+bcr.getAmount()*bcr.getExchangeRate());
    					return savingAccountService.update(savingAccount);
    				})
    		.subscribe();
        	
    }
    @KafkaListener(id="myId3", topics = "topico-everis8")
    public void listen3(String message) throws Exception{
    	System.out.println(">>>>> topico-everis8 @KafkaListener <<<<<");
    	BootCoinTransfer bcr = objectMapper.readValue(message, BootCoinTransfer.class);
    	System.out.println(">>> SavingAccount <<< " + bcr.getAccountNumber());
    	
    	savingAccountService.findByAccountNumber(bcr.getAccountNumber())
    		.flatMap(savingAccount -> {
    			savingAccount.setBalance(savingAccount.getBalance()+bcr.getBuyer().getAmount()*bcr.getBuyer().getExchangeRate());
    					return savingAccountService.update(savingAccount);
    				})
    		.subscribe();
        	
    }
}
