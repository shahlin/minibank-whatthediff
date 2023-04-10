package com.example.minibank.model;

import com.example.minibank.exception.AccountTransactionException;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "accounts")
public class Account {

    @JsonIgnore
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @Column(name = "code", nullable = false)
    private String code;

    @Column(name = "balance", nullable = false)
    private double balance;

    @Column(name = "updated_at", nullable = false)
    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Column(name = "created_at", nullable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;

    @OneToOne
    @JoinColumn(name = "customer_id", referencedColumnName = "id")
    private Customer customer;

    @JsonManagedReference
    @OneToMany(mappedBy = "senderAccount")
    private List<Transfer> sentTransfers;

    @JsonManagedReference
    @OneToMany(mappedBy = "receiverAccount")
    private List<Transfer> receivedTransfers;

    public synchronized void deposit(double amount) {
        if (amount < 0) {
            throw new AccountTransactionException("Cannot deposit negative amount");
        }

        balance += amount;
    }

    public synchronized void withdraw(double amount) {
        if (amount > balance) {
            throw new AccountTransactionException("Insufficient funds");
        }

        balance -= amount;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public List<Transfer> getSentTransfers() {
        return (sentTransfers != null) ? sentTransfers : Collections.emptyList();
    }

    public void setSentTransfers(List<Transfer> sentTransfers) {
        this.sentTransfers = sentTransfers;
    }

    public List<Transfer> getReceivedTransfers() {
        return (receivedTransfers != null) ? receivedTransfers : Collections.emptyList();
    }

    public void setReceivedTransfers(List<Transfer> receivedTransfers) {
        this.receivedTransfers = receivedTransfers;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }
}
