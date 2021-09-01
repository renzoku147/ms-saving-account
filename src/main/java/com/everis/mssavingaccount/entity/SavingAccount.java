package com.everis.mssavingaccount.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@Document("SavingAccount")
@AllArgsConstructor
@NoArgsConstructor
public class SavingAccount implements BankAccount {
    @Id
    String id;

    @NotNull
    private Customer customer;

    @NotNull
    private String accountNumber;

    private List<Person> holders;

    private List<Person> signers;

    @NotNull
    private Integer limitTransactions;

    @NotNull
    private Integer freeTransactions;

    @NotNull
    private Double commissionTransactions;

    @NotNull
    private Double balance;

    private Double minAverageVip;
    
    private DebitCard debitCard;

    @JsonDeserialize(using=LocalDateTimeDeserializer.class)
    @JsonSerialize(using=LocalDateTimeSerializer.class)
    private LocalDateTime date;

}
