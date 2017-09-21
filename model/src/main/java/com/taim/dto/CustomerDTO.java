package com.taim.dto;

import com.taim.dto.basedtos.UserBaseModelDTO;
import com.taim.model.Customer;
import javafx.beans.property.*;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.List;

public class CustomerDTO extends UserBaseModelDTO{
    private DoubleProperty storeCredit;
    private BooleanProperty isChecked;
    private ObjectProperty<Customer.CustomerClass> customerClass;
    private List<TransactionDTO> transactionList;

    public CustomerDTO(){
        storeCredit = new SimpleDoubleProperty();
        isChecked = new SimpleBooleanProperty();
        transactionList = new ArrayList<TransactionDTO>();
        customerClass = new SimpleObjectProperty<>();
    }

    public double getStoreCredit() {
        return storeCredit.get();
    }

    public DoubleProperty storeCreditProperty() {
        return storeCredit;
    }

    public void setStoreCredit(double storeCredit) {
        this.storeCredit.set(storeCredit);
    }

    public List<TransactionDTO> getTransactionList() {
        return transactionList;
    }

    public void setTransactionList(List<TransactionDTO> transactionList) {
        this.transactionList = transactionList;
    }

    public boolean isIsChecked() {
        return isChecked.get();
    }

    public BooleanProperty isCheckedProperty() {
        return isChecked;
    }

    public void setIsChecked(boolean isChecked) {
        this.isChecked.set(isChecked);
    }

    public Customer.CustomerClass getCustomerClass() {
        return customerClass.get();
    }

    public ObjectProperty<Customer.CustomerClass> customerClassProperty() {
        return customerClass;
    }

    public void setCustomerClass(Customer.CustomerClass customerClass) {
        this.customerClass.set(customerClass);
    }
}
