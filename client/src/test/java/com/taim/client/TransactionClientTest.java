package com.taim.client;

import com.taim.dto.CustomerDTO;
import com.taim.dto.TransactionDTO;
import com.taim.model.Transaction;
import junit.framework.Assert;
import org.joda.time.DateTime;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.List;

/**
 * Created by dragonliu on 2017/8/30.
 */
public class TransactionClientTest {
    private TransactionClient client = new TransactionClient();
    private CustomerClient customerClient = new CustomerClient();
    private static TransactionDTO transaction;

    @BeforeClass
    public static void prepareObject(){
        DateTime d1Created = DateTime.now();
        DateTime d1Modified = DateTime.now();
        transaction = new TransactionDTO();
        transaction.setSaleAmount(100);
        transaction.setGst(7);
        transaction.setPst(8);
        transaction.setTransactionType(Transaction.TransactionType.QUOTATION);
        transaction.setPaymentStatus(Transaction.PaymentStatus.UNPAID);
        transaction.setNote("sample");
        transaction.setDateCreated(d1Created);
        transaction.setDateModified(d1Modified);
    }

    @Test
    public void addTransactionTest()throws Exception{
        CustomerDTO customerDTO = customerClient.getByName("dummy dumb");
        transaction.setCustomer(customerDTO);
        TransactionDTO tran = client.add(transaction);
        Assert.assertEquals(transaction.getSaleAmount(), tran.getSaleAmount());
        Assert.assertEquals(transaction.getGst(),tran.getGst());
        Assert.assertEquals(transaction.getPst(), tran.getPst());
        Assert.assertEquals(transaction.getTransactionType(), tran.getTransactionType());
        Assert.assertEquals(transaction.getPaymentStatus(), tran.getPaymentStatus());
        Assert.assertEquals(transaction.getNote(), tran.getNote());
        Assert.assertEquals(transaction.getDateCreated().getMillis(), tran.getDateCreated().getMillis());
        Assert.assertEquals(transaction.getDateModified().getMillis(), tran.getDateModified().getMillis());
    }

    @Test
    public void getTransactionListTest()throws Exception{
        Thread.sleep(2000);
        List<TransactionDTO> trans = client.getList();
        Assert.assertEquals(1, trans.size());
        TransactionDTO tran = trans.get(0);
        Assert.assertEquals(transaction.getSaleAmount(), tran.getSaleAmount());
        Assert.assertEquals(transaction.getGst(),tran.getGst());
        Assert.assertEquals(transaction.getPst(), tran.getPst());
        Assert.assertEquals(transaction.getTransactionType(), tran.getTransactionType());
        Assert.assertEquals(transaction.getPaymentStatus(), tran.getPaymentStatus());
        Assert.assertEquals(transaction.getNote(), tran.getNote());
        Assert.assertEquals(transaction.getDateCreated().getMillis(), tran.getDateCreated().getMillis());
        Assert.assertEquals(transaction.getDateModified().getMillis(), tran.getDateModified().getMillis());
    }


    @Test
    public void getTransactionByIdTest()throws Exception{
        TransactionDTO tran = client.getById(1);
        Assert.assertEquals(transaction.getSaleAmount(), tran.getSaleAmount());
        Assert.assertEquals(transaction.getGst(),tran.getGst());
        Assert.assertEquals(transaction.getPst(), tran.getPst());
        Assert.assertEquals(transaction.getTransactionType(), tran.getTransactionType());
        Assert.assertEquals(transaction.getPaymentStatus(), tran.getPaymentStatus());
        Assert.assertEquals(transaction.getNote(), tran.getNote());
        Assert.assertEquals(transaction.getDateCreated().getMillis(), tran.getDateCreated().getMillis());
        Assert.assertEquals(transaction.getDateModified().getMillis(), tran.getDateModified().getMillis());
    }

    @Test
    public void updateTransactionTest()throws Exception{

        TransactionDTO tran = client.getById(1);
        tran.setPaymentStatus(Transaction.PaymentStatus.PAID);
        tran.setDateModified(DateTime.now());
        TransactionDTO t1 = client.update(tran);

        Assert.assertEquals(tran.getSaleAmount(), t1.getSaleAmount());
        Assert.assertEquals(tran.getGst(),t1.getGst());
        Assert.assertEquals(tran.getPst(), t1.getPst());
        Assert.assertEquals(tran.getTransactionType(), t1.getTransactionType());
        Assert.assertEquals(tran.getPaymentStatus(), t1.getPaymentStatus());
        Assert.assertEquals(tran.getNote(), t1.getNote());
        Assert.assertEquals(tran.getDateCreated().getMillis(), t1.getDateCreated().getMillis());
        Assert.assertEquals(tran.getDateModified().getMillis(), t1.getDateModified().getMillis());
    }


    @Test
    public void deleteTransactionByIdTest()throws Exception{
        String result = client.deleteById(1);
        Assert.assertEquals("Deleted!", result);
        Thread.sleep(2000);
        List<TransactionDTO> trans = client.getList();
        Assert.assertEquals(0, trans.size());
    }
}