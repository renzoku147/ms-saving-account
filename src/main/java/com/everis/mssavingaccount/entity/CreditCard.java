package com.everis.mssavingaccount.entity;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class CreditCard {

    private String id;

    private String cardNumber;

    private Customer customer;

    private Double limitCredit;

    private LocalDate expirationDate;

    private LocalDateTime date;
}
