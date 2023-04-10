package com.example.minibank.controller.request;

import org.springframework.stereotype.Component;

import javax.validation.constraints.Size;

@Component
public class TransferRequest {

    private String receiverAccountCode;

    private double amount;

    @Size(max = 255)
    private String remarks;

    public String getReceiverAccountCode() {
        return receiverAccountCode;
    }

    public void setReceiverAccountCode(String receiverAccountCode) {
        this.receiverAccountCode = receiverAccountCode;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}
