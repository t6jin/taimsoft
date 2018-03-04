package com.taim.model;

import com.taim.model.basemodels.BaseModel;

import javax.persistence.*;

@Entity
@Table(name = "payment_record")
public class PaymentRecord extends BaseModel {
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "payment_id", nullable = false)
    private Payment payment;
    @Column
    private double amount;

    public PaymentRecord(){}

    public Payment getPayment() {
        return payment;
    }

    public void setPayment(Payment payment) {
        this.payment = payment;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }
}