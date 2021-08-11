package com.everis.mssavingaccount.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@Document("SavingAccount")
@AllArgsConstructor
@NoArgsConstructor
public class SavingAccount {
    @Id
    String id;

    private Customer customer;

    private String cardNumber;

    @NotNull
    private List<Person> holders;

    private List<Person> signers;

    @NotNull
    private Double balance;

    @NotNull
    private Integer limitMovements;

    private LocalDateTime date;



}
