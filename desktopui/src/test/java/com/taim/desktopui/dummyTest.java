package com.taim.desktopui;

import com.taim.dto.*;
import com.taim.model.Staff;
import com.taim.desktopui.util.RestClientFactory;
import org.joda.time.DateTime;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jiawei.liu on 10/4/17.
 */

public class dummyTest {

    @Test
    public void addAdmin()throws Exception{
        StaffDTO admin = new StaffDTO();
        admin.setDateCreated(DateTime.now());
        admin.setDateModified(DateTime.now());
        admin.setPhone("911");
        admin.setEmail("whyme@gmail.com");
        admin.setPosition(Staff.Position.MANAGER);
        admin.setFullname("wormhole");
        admin.setUserName("admin");
        admin.setPassword("123");
        RestClientFactory.getStaffClient().add(admin);
    }

    @Test
    public void addFewProducts()throws Exception{
        ProductDTO product1 = new ProductDTO();
        product1.setSku("p-1");
        product1.setDisplayName("Product1");
        product1.setHeight(10);
        product1.setWidth(10);
        product1.setLength(10);
        product1.setTexture("Unknown");
        product1.setTotalNum(0);
        product1.setVirtualTotalNum(0);
        product1.setUnitPrice(10);
        product1.setPiecePerBox(10);
        product1.setDateCreated(DateTime.now());
        product1.setDateModified(DateTime.now());


        ProductDTO product2 = new ProductDTO();
        product2.setSku("p-2");
        product2.setDisplayName("Product2");
        product2.setHeight(5);
        product2.setWidth(10);
        product2.setLength(20);
        product2.setTexture("TBD");
        product2.setTotalNum(0);
        product2.setVirtualTotalNum(0);
        product2.setUnitPrice(20);
        product2.setDateCreated(DateTime.now());
        product2.setDateModified(DateTime.now());

        RestClientFactory.getProductClient().add(product1);
        RestClientFactory.getProductClient().add(product2);

    }



    @Test
    public void getTransactionDetails()throws Exception{
        TransactionDTO transactionDTO = RestClientFactory.getTransactionClient().getById(1);
        System.out.println(transactionDTO.getSaleAmount()+" -> "+transactionDTO.getTransactionDetails().size());
        //transactionDTO.getTransactionDetails().stream().forEach(d-> System.out.println(d.getProduct().getSku()+": "+d.getQuantity()+" -> "+d.getSaleAmount()));

    }

    @Test
    public void addTransactionDTOTest()throws Exception{
        TransactionDetailDTO detail1 = new TransactionDetailDTO();
        detail1.setProduct(RestClientFactory.getProductClient().getById(1));
        detail1.setQuantity(10);
        detail1.setSaleAmount(100);
        detail1.setDateCreated(DateTime.now());
        detail1.setDateModified(DateTime.now());

        TransactionDetailDTO detail2 = new TransactionDetailDTO();
        detail2.setProduct(RestClientFactory.getProductClient().getById(2));
        detail2.setQuantity(10);
        detail2.setSaleAmount(100);
        detail2.setDateCreated(DateTime.now());
        detail2.setDateModified(DateTime.now());

        List<TransactionDetailDTO> detailList = new ArrayList<>();
        detailList.add(detail1);
        detailList.add(detail2);

        TransactionDTO transaction = new TransactionDTO();
        transaction.getTransactionDetails().addAll(detailList);
        transaction.setSaleAmount(Double.valueOf(100.14));
        transaction.setGst(Double.valueOf(12.1));
        transaction.setPst(Double.valueOf(13.2));
        transaction.setNote("WHY");
        transaction.setDateModified(DateTime.now());
        transaction.setDateCreated(DateTime.now());

        RestClientFactory.getTransactionClient().add(transaction);


    }

    @Test
    public void addCustomers()throws Exception{
        CustomerDTO customer1 = new CustomerDTO();
        customer1.setDateCreated(DateTime.now());
        customer1.setDateModified(DateTime.now());
        customer1.setEmail("tm@gmail.com");
        customer1.setFullname("Tim Hortons");
        customer1.setPhone("5698974624");
//        customer1.setCustomerClass(Customer.CustomerClass.CLASSB);

        CustomerDTO customer2 = new CustomerDTO();
        customer2.setDateCreated(DateTime.now());
        customer2.setDateModified(DateTime.now());
        customer2.setEmail("dummyDum@hotmail.com");
        customer2.setFullname("Dummy dum");
        customer2.setPhone("1234698712");
//        customer2.setCustomerClass(Customer.CustomerClass.CLASSC);

        RestClientFactory.getCustomerClient().add(customer1);
        RestClientFactory.getCustomerClient().add(customer2);


    }

    @Test
    public void addStaff()throws Exception{
        StaffDTO staff = new StaffDTO();
        staff.setDateCreated(DateTime.now());
//        staff.setDateModified(DateTime.now());
        staff.setEmail("staff@gmail.com");
        staff.setFullname("admin");
        staff.setPhone("1234567890");
        staff.setUserName("admin");
        staff.setPassword("admin");
        staff.setPosition(Staff.Position.MANAGER);
        OrganizationDTO organization = new OrganizationDTO();
        organization.setDateCreated(DateTime.now());
        organization.setDateModified(DateTime.now());
        organization.setCountry("Canada");
        organization.setOrgName("TEST ORG");
        organization.setCity("Calgary");
        organization.setStreet("Downtown");
        organization.setStreetNum("123");
        organization.setPostalCode("T3P1A1");
        staff.setOrganization(organization);

    }

//    public void addCustomerTransactionDTOTest()throws Exception{
//        TransactionDetailDTO detail1 = new TransactionDetailDTO();
//
//        detail1.setProduct(RestClientFactory.getProductClient().getById(1));
//        detail1.setQuantity(2);
//        detail1.setSaleAmount(20);
//        detail1.setDateCreated(DateTime.now());
//        detail1.setDateModified(DateTime.now());
//
//        List<TransactionDetailDTO> detailList = new ArrayList<>();
//        detailList.add(detail1);
//
//        TransactionDTO transaction = new TransactionDTO();
//        transaction.getTransactionDetails().addAll(detailList);
//        transaction.setSaleAmount(Double.valueOf(20));
//        transaction.setGst(Double.valueOf(1.2));
//        transaction.setPst(Double.valueOf(1));
//        transaction.setNote("WHY");
//        transaction.setDateModified(DateTime.now());
//        transaction.setDateCreated(DateTime.now());
//
//        CustomerDTO customer = RestClientFactory.getCustomerClient().getByName("Tim Hortons");
//
//        transaction.setCustomer(customer);
//        customer.getTransactionList().add(transaction);
//
//        RestClientFactory.getCustomerClient().update(customer);
//        RestClientFactory.getTransactionClient().add(transaction);
//
//        RestClientFactory.getTransactionClient().add(transaction);
//    }

    @Test
    public void addProperty(){
        PropertyDTO propertyDTO = new PropertyDTO();
        propertyDTO.setCompanyName("TEST COMPANY");
        propertyDTO.setGstNumber("123456");
        propertyDTO.setGstRate(5);
        propertyDTO.setPstRate(7);
        propertyDTO.setProductWarnLimit(200);
        propertyDTO.setDateCreated(DateTime.now());
        propertyDTO.setDateModified(DateTime.now());
        PropertyDTO.CustomerClassDTO customerClassDTO = new PropertyDTO.CustomerClassDTO();
        customerClassDTO.setCustomerClassName("Class A");
        customerClassDTO.setCustomerDiscount(15);
        customerClassDTO.setDateCreated(DateTime.now());
        customerClassDTO.setDateModified(DateTime.now());
        propertyDTO.setCustomerClasses(new ArrayList<PropertyDTO.CustomerClassDTO>(){{add(customerClassDTO);}});

        RestClientFactory.getPropertyClient().add(propertyDTO);
    }

}
