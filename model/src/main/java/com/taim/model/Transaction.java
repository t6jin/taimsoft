package com.taim.model;


import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.taim.model.basemodels.BaseModel;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Type;
import org.joda.time.DateTime;

import javax.persistence.*;
import java.util.List;

/**
 * Created by tjin on 2017-07-28.
 */
@Entity
@Table(name = "transaction")
@JsonIdentityInfo(scope = Transaction.class,
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id")
public class Transaction extends BaseModel {
    /**
     *
     * Indicate the transaction type
     */
    public enum TransactionType{
        QUOTATION("Quotation"),
        CONTRACT("Contract"),
        INVOICE("Invoice"),
        STOCK("Stock"),
        RETURN("Return");

        TransactionType(String vvalue){
            this.value = vvalue;
        }

        private String value;

        public String getValue() {
            return value;
        }
    }

    /**
     * Indicate the payment status
     */
    public enum PaymentStatus{
        UNPAID("Unpaid"),
        PARTIALLY_PAID("Partially Paid"),
        PAID("Fully Paid");

        private String value;

        PaymentStatus(String vvalue){
            this.value = vvalue;
        }

        public String getValue() {
            return value;
        }

        public static PaymentStatus getStatus(String value){
            for (PaymentStatus ps : PaymentStatus.values()){
                if (value.equalsIgnoreCase(ps.name())){
                    return ps;
                }
            }
            return null;
        }
    }

    public enum DeliveryStatus{
        UNDELIVERED("Undelivered"),
        DELIVERING("Delivering"),
        DELIVERED("Delivered");

        private String value;

        DeliveryStatus(String vvalue){
            this.value = vvalue;
        }

        public String getValue() {
            return value;
        }

        public static DeliveryStatus getStatus(String value){
            for (DeliveryStatus ps : DeliveryStatus.values()){
                if (value.equalsIgnoreCase(ps.name())){
                    return ps;
                }
            }
            return null;
        }
    }

    @Column(name = "sale_amount", nullable = false)
    private double saleAmount;
    @Column
    private double gst;
    @Column
    private double pst;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "staff_id", nullable = false)
    private Staff staff;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "customer_id")
    private Customer customer;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "vendor_id")
    private Vendor vendor;
    @Column(name = "transaction_type", nullable = false)
    private TransactionType transactionType;
    @Column(name = "payment_status")
    private PaymentStatus paymentStatus;
    @Column(name = "delivery_status")
    private DeliveryStatus deliveryStatus;
    @Column(name = "payment_due_date")
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime paymentDueDate;
    @Column(name = "delivery_due_date")
    @Type(type = "org.jadira.usertype.dateandtime.joda.PersistentDateTime")
    private DateTime deliveryDueDate;
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @Fetch(FetchMode.SUBSELECT)
    @JoinColumn(name = "transaction_id")
    private List<TransactionDetail> transactionDetails;
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @Fetch(FetchMode.SUBSELECT)
    @JoinColumn(name = "transaction_id")
    private List<Payment> payments;
    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @Fetch(FetchMode.SUBSELECT)
    @JoinColumn(name = "transaction_id")
    private List<Delivery> deliveries;
    @Column(name = "ref_id")
    private int refId;
    @Column(name = "is_finalized", nullable = false)
    private boolean finalized;
    @Column
    private String note;
    @Column(name = "address_note ")
    private String addressNote;

    public Transaction(){}

    public double getSaleAmount() {
        return saleAmount;
    }

    public void setSaleAmount(double saleAmount) {
        this.saleAmount = saleAmount;
    }

    public Staff getStaff() {
        return staff;
    }

    public void setStaff(Staff staff) {
        this.staff = staff;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public Vendor getVendor() {
        return vendor;
    }

    public void setVendor(Vendor vendor) {
        this.vendor = vendor;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public List<TransactionDetail> getTransactionDetails() {
        return transactionDetails;
    }

    public void setTransactionDetails(List<TransactionDetail> transactionDetails) {
        this.transactionDetails = transactionDetails;
    }

    public List<Payment> getPayments() {
        return payments;
    }

    public void setPayments(List<Payment> payments) {
        this.payments = payments;
    }

    public PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public DeliveryStatus getDeliveryStatus() {
        return deliveryStatus;
    }

    public void setDeliveryStatus(DeliveryStatus deliveryStatus) {
        this.deliveryStatus = deliveryStatus;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public double getGst() {
        return gst;
    }

    public void setGst(double gst) {
        this.gst = gst;
    }

    public double getPst() {
        return pst;
    }

    public void setPst(double pst) {
        this.pst = pst;
    }

    public int getRefId() {
        return refId;
    }

    public void setRefId(int refId) {
        this.refId = refId;
    }

    public DateTime getPaymentDueDate() {
        return paymentDueDate;
    }

    public void setPaymentDueDate(DateTime paymentDueDate) {
        this.paymentDueDate = paymentDueDate;
    }

    public DateTime getDeliveryDueDate() {
        return deliveryDueDate;
    }

    public void setDeliveryDueDate(DateTime deliveryDueDate) {
        this.deliveryDueDate = deliveryDueDate;
    }

    public boolean isFinalized() {
        return finalized;
    }

    public void setFinalized(boolean finalized) {
        this.finalized = finalized;
    }

    public List<Delivery> getDeliveries() {
        return deliveries;
    }

    public void setDeliveries(List<Delivery> deliveries) {
        this.deliveries = deliveries;
    }

    public String getAddressNote() {
        return addressNote;
    }

    public void setAddressNote(String addressNote) {
        this.addressNote = addressNote;
    }
}
