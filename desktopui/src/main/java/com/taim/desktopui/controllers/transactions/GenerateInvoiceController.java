package com.taim.desktopui.controllers.transactions;

import com.jfoenix.controls.*;
import com.taim.desktopui.util.*;
import com.taim.dto.*;
import com.taim.model.*;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleFloatProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.StringConverter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.joda.time.DateTime;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;


import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * Created by jiawei.liu on 9/17/17.
 */

public class GenerateInvoiceController {
    private static final Logger logger = LoggerFactory.getLogger(GenerateInvoiceController.class);

    private Stage dialogStage;

    private CustomerDTO customer;
    private StaffDTO staff;
    private List<ProductDTO> productList;
    private ObservableList<TransactionDetailDTO> transactionDetailDTOObservableList;
    private TransactionDTO transaction;
    private TransactionDTO oldTransaction;
    private PaymentDTO payment;
    private boolean confirmedClicked;
    private BooleanBinding confimButtonBinding;
    private int discount;
    private Executor executor;
    private Mode mode;
    private Map<Integer, Double> oldProductVirtualNumMap;
    private Map<Integer, Double> oldProductActualNumMap;
    private double oldStoreCredit;

    private Transaction.DeliveryStatus prevStats;

    private static final String DATE_PATTERN = "yyyy-MM-dd";

    @FXML
    private TableView<TransactionDetailDTO> transactionTableView;
    @FXML
    private TableColumn<TransactionDetailDTO, String> productIdCol;
    @FXML
    private TableColumn<TransactionDetailDTO, Number> unitPriceCol;
    @FXML
    private TableColumn<TransactionDetailDTO, Number> virtualNumCol;
    @FXML
    private TableColumn<TransactionDetailDTO, Number> actualNumCol;
    @FXML
    private TableColumn<TransactionDetailDTO, Number> qtyCol;
    @FXML
    private TableColumn<TransactionDetailDTO, Number> subTotalCol;
    @FXML
    private TableColumn<TransactionDetailDTO, Number> discountCol;
    @FXML
    private TableColumn<TransactionDetailDTO, Number> totalCol;
    @FXML
    private TableColumn<TransactionDetailDTO, String> sizeCol;
    @FXML
    private TableColumn<TransactionDetailDTO, Number> pkgBoxCol;
    @FXML
    private TableColumn<TransactionDetailDTO, Number> pkgPieceCol;
    @FXML
    private TableColumn deleteCol;
    @FXML
    private TableColumn<TransactionDetailDTO, String> remarkCol;
    @FXML
    private TableColumn<TransactionDetailDTO, Number> deliveredCol;
    @FXML
    private TableColumn<TransactionDetailDTO, Number> toDeliverCol;


    //Transaction Information Labels
    @FXML
    private Label typeLabel;
    @FXML
    private Label dateLabel;

    //transaction payment/delivery due Labels
    @FXML
    private JFXDatePicker paymentDueDatePicker;
    @FXML
    private JFXDatePicker deliveryDueDatePicker;
    @FXML
    private Label deliveryStatusLabel;
    @FXML
    private Label paymentStatusLabel;

    //Staff Information Labels
    @FXML
    private Label staffFullNameLabel;
    @FXML
    private Label staffPhoneLabel;
    @FXML
    private Label staffPositionLabel;
    @FXML
    private Label staffEmail;

    //Customer Details Labels
    @FXML
    private Label fullNameLabel;
    @FXML
    private Label storeCreditLabel;
    @FXML
    private Label userTypeLabel;
    @FXML
    private Label emailLabel;
    @FXML
    private Label phoneLabel;

    //payment details
    @FXML
    private Label balanceLabel;
    @FXML
    private ChoiceBox<String> paymentTypeChoiceBox;
    @FXML
    private JFXTextField paymentField;
    @FXML
    private Label paidAmountLabel;
    @FXML
    private JFXCheckBox isDepositCheckBox;

    //Items Information Labels
    @FXML
    private Label itemsCountLabel;
    @FXML
    private Label subTotalLabel;
    @FXML
    private Label paymentDiscountLabel;
    @FXML
    private JFXTextField pstTaxField;
    @FXML
    private JFXTextField gstTaxField;
    @FXML
    private Label totalLabel;

    @FXML
    private JFXButton addItemButton;
    @FXML
    private JFXButton confirmButton;
    @FXML
    private JFXButton cancelButton;

    @FXML
    private JFXComboBox<String> productComboBox;
    @FXML
    private JFXTextArea textArea;


    @FXML
    private void initialize(){
        confimButtonBinding = Bindings.size(transactionTableView.getItems()).greaterThan(0);
        confirmButton.disableProperty().bind(confimButtonBinding);
        productIdCol.setCellValueFactory(p->new SimpleStringProperty(p.getValue().getProduct().getSku()));
        unitPriceCol.setCellValueFactory(u->new SimpleFloatProperty(new BigDecimal(u.getValue().getProduct().getUnitPrice()).floatValue()));
        qtyCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        sizeCol.setCellValueFactory(s->new SimpleStringProperty(SizeHelper.getSizeString(s.getValue().getProduct())));
        pkgBoxCol.setCellValueFactory(u->new SimpleFloatProperty(new BigDecimal(u.getValue().getPackageInfo().getBox()).floatValue()));
        pkgPieceCol.setCellValueFactory(u->new SimpleFloatProperty(new BigDecimal(u.getValue().getPackageInfo().getPieces()).floatValue()));
        deliveredCol.setCellValueFactory(d->new SimpleFloatProperty(new BigDecimal(d.getValue().getDeliveredQuantity()).floatValue()));
        toDeliverCol.setCellValueFactory(d->new SimpleFloatProperty(new BigDecimal(d.getValue().getToDeliveryQuantity()).floatValue()));
        discountCol.setCellValueFactory(new PropertyValueFactory<>("discount"));

        remarkCol.setCellValueFactory(new PropertyValueFactory<>("note"));
        remarkCol.setOnEditCommit(event ->
            (event.getTableView().getItems().get(event.getTablePosition().getRow())).setNote(event.getNewValue()));

        remarkCol.setCellFactory(TextFieldTableCell.forTableColumn());

        actualNumCol.setCellValueFactory(param ->
                new SimpleFloatProperty(new BigDecimal(param.getValue().getProduct().getTotalNum()).floatValue()));

        virtualNumCol.setCellValueFactory(param ->
                new SimpleFloatProperty(new BigDecimal(param.getValue().getProduct().getVirtualTotalNum()).floatValue()));


        pkgBoxCol.setCellFactory(TextFieldTableCell.forTableColumn(new StringConverter<Number>() {
            @Override
            public String toString(Number object) {
                return String.valueOf(object);
            }

            @Override
            public Float fromString(String string) {
                return Float.valueOf(string);
            }
        }));
        pkgPieceCol.setCellFactory(TextFieldTableCell.forTableColumn(new StringConverter<Number>() {
            @Override
            public String toString(Number object) {
                return String.valueOf(object);
            }

            @Override
            public Float fromString(String string) {
                return Float.valueOf(string);
            }
        }));
        pkgBoxCol.setOnEditCommit(event -> {
            TransactionDetailDTO p = event.getTableView().getItems().get(event.getTablePosition().getRow());
            p.getPackageInfo().setBox(event.getNewValue().intValue());
            refreshTable();
        });

        pkgPieceCol.setOnEditCommit(event -> {
            TransactionDetailDTO p = event.getTableView().getItems().get(event.getTablePosition().getRow());
            p.getPackageInfo().setPieces(event.getNewValue().intValue());
            refreshTable();
        });

        subTotalCol.setCellValueFactory(param ->
                new SimpleFloatProperty(new BigDecimal(param.getValue().getSaleAmount()).floatValue()));

        totalCol.setCellValueFactory(param ->
                new SimpleFloatProperty(new BigDecimal(param.getValue().getSaleAmount()* (100 - param.getValue().getDiscount()) / 100)
                        .setScale(2, RoundingMode.HALF_EVEN).floatValue()));

        deleteCol.setCellValueFactory(
                (Callback<TableColumn.CellDataFeatures<TransactionDetailDTO, Boolean>, ObservableValue<Boolean>>) p -> new SimpleBooleanProperty(p.getValue() != null));

        deleteCol.setCellFactory(
                (Callback<TableColumn<TransactionDetailDTO, Boolean>, TableCell<TransactionDetailDTO, Boolean>>) p -> new ButtonCell(transactionTableView,oldProductVirtualNumMap,true,true));
        deliveryStatusLabel.textProperty().addListener((observable, oldValue, newValue) -> {
            transaction.setDeliveryStatus(Transaction.DeliveryStatus.getStatus(newValue));
            //transaction.getDeliveryStatus().setDateModified(DateTime.now());
            if (Transaction.DeliveryStatus.getStatus(newValue)== Transaction.DeliveryStatus.DELIVERING && Transaction.DeliveryStatus.getStatus(oldValue)== Transaction.DeliveryStatus.UNDELIVERED){
                //transactionTableView.getItems().forEach(e->e.getProduct().setTotalNum(e.getProduct().getTotalNum()-e.getQuantity()));
                qtyCol.setEditable(false);
                refreshTable();
            }else if ((Transaction.DeliveryStatus.getStatus(oldValue)== Transaction.DeliveryStatus.DELIVERING || Transaction.DeliveryStatus.getStatus(oldValue)== Transaction.DeliveryStatus.DELIVERED)
                    && Transaction.DeliveryStatus.getStatus(newValue)== Transaction.DeliveryStatus.UNDELIVERED){
                //transactionTableView.getItems().forEach(e->e.getProduct().setTotalNum(e.getProduct().getTotalNum()+e.getQuantity()));
                qtyCol.setEditable(true);
                refreshTable();
            }
        });
        toDeliverCol.setCellFactory(TextFieldTableCell.forTableColumn(new StringConverter<Number>() {
            @Override
            public String toString(Number object) {
                return String.valueOf(object);
            }

            @Override
            public Float fromString(String string) {
                return Float.valueOf(string);
            }
        }));
        toDeliverCol.setOnEditCommit(event -> {
            int oldValue = event.getOldValue().intValue();
            TransactionDetailDTO p = event.getTableView().getItems().get(event.getTablePosition().getRow());
            p.setToDeliveryQuantity(event.getNewValue().doubleValue());
            p.getProduct().setTotalNum(p.getProduct().getTotalNum()+oldValue-event.getNewValue().intValue());
            if (transaction.getTransactionDetails().stream().allMatch(t->t.getQuantity()==t.getDeliveredQuantity()+t.getToDeliveryQuantity())) {
                deliveryStatusLabel.setText(Transaction.DeliveryStatus.DELIVERED.getValue());
            }else if (transaction.getTransactionDetails().stream().allMatch(t->t.getDeliveredQuantity()+t.getToDeliveryQuantity()==0)){
                deliveryStatusLabel.setText(Transaction.DeliveryStatus.UNDELIVERED.getValue());
            }else{
                if(transaction.getDeliveryStatus()!= Transaction.DeliveryStatus.DELIVERING){
                    deliveryStatusLabel.setText(Transaction.DeliveryStatus.DELIVERING.getValue());
                }
            }
            refreshTable();
        });

        paymentField.textProperty().addListener((observable, oldValue, newValue) -> showBalanceDetails());

        paymentTypeChoiceBox.getSelectionModel().selectFirst();
        paymentTypeChoiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> payment.setPaymentType(Payment.PaymentType.getValue(newValue)));
        paymentDueDatePicker.setOnAction(event ->{
            this.transaction.setPaymentDueDate(DateUtils.toDateTime(paymentDueDatePicker.getValue()));
        });
        paymentDueDatePicker.setPromptText(DATE_PATTERN.toLowerCase());
        paymentDueDatePicker.setConverter(new StringConverter <LocalDate>() {
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(DATE_PATTERN);
            @Override
            public String toString(LocalDate date) {
                if (date != null) {
                    return dateFormatter.format(date);
                } else {
                    return "";
                }
            }
            @Override
            public LocalDate fromString(String string) {
                if (string != null && !string.isEmpty()) {
                    return LocalDate.parse(string, dateFormatter);
                } else {
                    return null;
                }
            }
        });
        deliveryDueDatePicker.setOnAction(event ->{
            this.transaction.setDeliveryDueDate(DateUtils.toDateTime(deliveryDueDatePicker.getValue()));
        });
        deliveryDueDatePicker.setPromptText(DATE_PATTERN.toLowerCase());
        deliveryDueDatePicker.setConverter(new StringConverter<LocalDate>() {
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(DATE_PATTERN);
            @Override
            public String toString(LocalDate date) {
                if (date != null) {
                    return dateFormatter.format(date);
                } else {
                    return "";
                }
            }
            @Override
            public LocalDate fromString(String string) {
                if (string != null && !string.isEmpty()) {
                    return LocalDate.parse(string, dateFormatter);
                } else {
                    return null;
                }
            }
        });


        executor = Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        });
    }




    @FXML
    public void handleAddItem(){
        ProductDTO selectedProduct = productList
                .stream()
                .filter(product -> product.getSku().equals(productComboBox.getSelectionModel().getSelectedItem()))
                .findFirst()
                .get();
        List<String> productIdList = transactionDetailDTOObservableList.stream()
                .map(t -> t.getProduct().getSku())
                .collect(Collectors.toList());
        if(productIdList.contains(selectedProduct.getSku())){
            new AlertBuilder(dialogStage)
                    .alertType(Alert.AlertType.ERROR)
                    .alertContentText("Product Add Error")
                    .alertContentText(selectedProduct.getSku() + " has already been added!")
                    .build()
                    .showAndWait();
        }else{
            TransactionDetailDTO newProductTransaction = new TransactionDetailDTO();
            newProductTransaction.setProduct(selectedProduct);
            newProductTransaction.setDiscount(0);
            newProductTransaction.setQuantity(0);
            newProductTransaction.setDeliveredQuantity(0);
            newProductTransaction.setToDeliveryQuantity(0);
            newProductTransaction.setSaleAmount(selectedProduct.getUnitPrice()*newProductTransaction.getQuantity());
            newProductTransaction.setPackageInfo(initiatePkgInfo(newProductTransaction));
            transactionDetailDTOObservableList.add(newProductTransaction);
            oldProductActualNumMap.put(selectedProduct.getId(),selectedProduct.getTotalNum());
            oldProductVirtualNumMap.put(selectedProduct.getId(),selectedProduct.getVirtualTotalNum());
        }
    }


    @FXML
    public void handleCancelButton(){
        this.dialogStage.close();
    }

//    @FXML
//    public TransactionDTO handleConfirmButton() throws IOException, SQLException {
//        generateTransaction();
//        if(isConfirmedClicked()) {
//            dialogStage.close();
//        }
//        return this.transaction;
//    }


    public GenerateInvoiceController(){
        confirmedClicked = false;
        discount = 100;
    }


    public void setDialogStage(Stage dialogStage){
        this.dialogStage = dialogStage;
    }


    /**
     * Initialize the main class for this class
     */

    public void setMainClass(TransactionDTO transactionFromAbove){

        //either edit existing invoice or generate new invoice from transaction
        if (transactionFromAbove.getTransactionType()== Transaction.TransactionType.QUOTATION) {
            this.mode= Mode.CREATE;
            this.oldTransaction = transactionFromAbove;
            this.transaction = copyTransaction(transactionFromAbove);
            this.transaction.setDeliveryStatus(Transaction.DeliveryStatus.UNDELIVERED);
            this.transaction.setPaymentDueDate(transaction.getDateCreated());
            this.transaction.setDeliveryDueDate(transaction.getDateCreated());
            this.transaction.setPayments(new ArrayList<>());
            this.transaction.setDeliveries(new ArrayList<>());
            this.transaction.setPaymentStatus(Transaction.PaymentStatus.UNPAID);

        }else{
            this.mode= Mode.EDIT;
            prevStats = transactionFromAbove.getDeliveryStatus();
            this.transaction = transactionFromAbove;
            if(transaction.isFinalized()){
                confirmButton.setDisable(true);
            }
            if (prevStats== Transaction.DeliveryStatus.DELIVERING || prevStats== Transaction.DeliveryStatus.DELIVERED){
                qtyCol.setEditable(false);
                discountCol.setEditable(false);
            }
        }
        this.staff = transactionFromAbove.getStaff();
        this.payment = new PaymentDTO();
        this.customer = transactionFromAbove.getCustomer();
        updatePrevProductNum();
        transaction.getTransactionDetails().forEach(t->t.setToDeliveryQuantity(0));
        this.transactionDetailDTOObservableList = FXCollections.observableArrayList(transaction.getTransactionDetails());
        transactionTableView.setItems(transactionDetailDTOObservableList);
        transactionDetailDTOObservableList.addListener(new ListChangeListener<TransactionDetailDTO>() {
            @Override
            public void onChanged(Change<? extends TransactionDetailDTO> c) {
                while(c.next()){
                    if( c.wasAdded() || c.wasRemoved()){
                        showPaymentDetails();
                    }
                }
            }
        });
    }

    /**
     * Load Data From DB (Customer and Product)
     */
    public void initDataFromDB(){
        //load list of products
        Task<List<ProductDTO>> productsTask = new Task<List<ProductDTO>>() {
            @Override
            protected List<ProductDTO> call() throws Exception {
                return RestClientFactory.getProductClient().getList();
            }
        };

        //product
        productsTask.setOnSucceeded(event ->{
            this.productList = productsTask.getValue();
            List<String> tmpProductList = productList
                    .stream()
                    .map(product -> product.getSku())
                    .collect(Collectors.toList());
            productComboBox.setItems(FXCollections.observableArrayList(tmpProductList));
            new AutoCompleteComboBoxListener<>(productComboBox);
        });
        productsTask.setOnFailed(event -> {
            logger.error(productsTask.getException().getMessage());
            new AlertBuilder(dialogStage)
                    .alertType(Alert.AlertType.ERROR)
                    .alertHeaderText("Database Error!")
                    .alertContentText("Unable to fetch product information from the database!")
                    .build()
                    .showAndWait();
            dialogStage.close();
        });
        executor.execute(productsTask);
    }

    /**
     * Initial Panel Details
     */
    public void initPanelDetails(){
        showTransactionDetails();
        showStaffDetails();
        showCustomerDetails();
        showPaymentDetails();
        showPaymentDeliveryDetail();

    }


    private void showTransactionDetails(){
        typeLabel.setText(transaction.getTransactionType().getValue());
        dateLabel.setText(new SimpleDateFormat("yyyy-MM-dd").format(transaction.getDateCreated().toDate()));
        textArea.setText(transaction.getNote());
    }

    private void showStaffDetails(){
        if(this.staff != null){
            staffFullNameLabel.setText(this.staff.getFullname());
            staffPhoneLabel.setText(this.staff.getPhone());
            staffPositionLabel.setText(this.staff.getPosition().toString());
            staffEmail.setText(this.staff.getEmail());
        }else{
            staffFullNameLabel.setText("");
            staffPhoneLabel.setText("");
            staffPositionLabel.setText("");
            staffEmail.setText("");
        }
    }

    private void showPaymentDeliveryDetail(){
        paymentDueDatePicker.setValue(DateUtils.toLocalDate(this.transaction.getPaymentDueDate()));
        deliveryDueDatePicker.setValue(DateUtils.toLocalDate(this.transaction.getDeliveryDueDate()));
        deliveryStatusLabel.setText(transaction.getDeliveryStatus().getValue());
        paymentStatusLabel.setText(this.transaction.getPaymentStatus().name());
    }

    /**
     * Show customer details grid pane
     */

    private void showCustomerDetails(){
        if(this.customer != null){
            addItemButton.setDisable(false);
            fullNameLabel.setText(this.customer.getFullname());
            storeCreditLabel.setText(String.valueOf(this.customer.getStoreCredit()));
            userTypeLabel.setText(this.customer.getUserType().getValue());
            emailLabel.setText(this.customer.getEmail());
            phoneLabel.setText(this.customer.getPhone());
        }
        else{
            addItemButton.setDisable(true);
            fullNameLabel.setText("");
            storeCreditLabel.setText("");
            userTypeLabel.setText("");
            emailLabel.setText("");
            phoneLabel.setText("");
        }
    }

/**
     * Show payment details grid pane
     */

    private void showPaymentDetails(){
        if(this.transactionDetailDTOObservableList != null ){
            int pstNum = VistaNavigator.getGlobalProperty().getPstRate();
            int gstNum = VistaNavigator.getGlobalProperty().getGstRate();
            Iterator<TransactionDetailDTO> iterator = this.transactionDetailDTOObservableList.iterator();
            BigDecimal subTotalAfterDiscount = new BigDecimal(0.00);
            BigDecimal subTotalBeforeDiscount = new BigDecimal(0.00);
            while(iterator.hasNext()){
                TransactionDetailDTO tmp = iterator.next();
                subTotalBeforeDiscount = subTotalBeforeDiscount.add(new BigDecimal(tmp.getSaleAmount()));
                subTotalAfterDiscount = subTotalAfterDiscount.add(new BigDecimal(tmp.getSaleAmount()* (100 - tmp.getDiscount()) / 100));
            }
            BigDecimal paymentDiscount = subTotalBeforeDiscount.subtract(subTotalAfterDiscount).setScale(2, BigDecimal.ROUND_HALF_EVEN);

            BigDecimal pstTax = new BigDecimal(pstNum).multiply(subTotalAfterDiscount).divide(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_EVEN);
            BigDecimal gstTax = new BigDecimal(gstNum).multiply(subTotalAfterDiscount).divide(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_EVEN);

            BigDecimal total = subTotalAfterDiscount.add(pstTax).add(gstTax).setScale(2, BigDecimal.ROUND_HALF_EVEN);
            subTotalBeforeDiscount.setScale(2, RoundingMode.HALF_EVEN);

            itemsCountLabel.setText(String.valueOf(this.transactionDetailDTOObservableList.size()));
            subTotalLabel.setText(String.valueOf(subTotalBeforeDiscount.floatValue()));
            paymentDiscountLabel.setText(String.valueOf(paymentDiscount.floatValue()));
            pstTaxField.setText(String.valueOf(pstTax.floatValue()));
            gstTaxField.setText(String.valueOf(gstTax.floatValue()));
            totalLabel.setText(String.valueOf(total.floatValue()));
            showBalanceDetails();
        }
        else{
            itemsCountLabel.setText("");
            subTotalLabel.setText("");
            paymentDiscountLabel.setText("");
            pstTaxField.setText("");
            gstTaxField.setText("");
            totalLabel.setText("");
            balanceLabel.setText("");
        }
    }

    private void showBalanceDetails(){
        BigDecimal balance = new BigDecimal(totalLabel.getText()).setScale(2, BigDecimal.ROUND_HALF_EVEN);
        BigDecimal paid = new BigDecimal(BigInteger.ZERO);
        for (PaymentDTO prevPayment : transaction.getPayments()){
            paid = paid.add(new BigDecimal(prevPayment.getPaymentAmount()));
        }
        balance = balance.subtract(paid);
        paidAmountLabel.setText(paid.toString());

        if((payment.getPaymentType()== Payment.PaymentType.STORE_CREDIT && !paymentField.getText().trim().isEmpty() && isStoreCreditValidNoCustomer())){
            balance = balance.subtract(new BigDecimal(paymentField.getText()));
        } else if(payment.getPaymentType()!= Payment.PaymentType.STORE_CREDIT && !paymentField.getText().trim().isEmpty() && NumberUtils.isNumber(paymentField.getText())){
            balance = balance.subtract(new BigDecimal(paymentField.getText()));
        }

        balanceLabel.setText(balance.toString());
    }

    @FXML
    public void handleConfirmButton() throws IOException, SQLException{

        transaction.getTransactionDetails().clear();
        transactionDetailDTOObservableList.forEach(t->{
            if (t.getDateCreated()==null){
                t.setDateCreated(DateTime.now());
            }
            //t.setDateModified(DateTime.now());
            double newDeliver = t.getToDeliveryQuantity();
            if (newDeliver >0){
                t.setDeliveredQuantity(t.getDeliveredQuantity()+newDeliver);
                DeliveryDTO deliveryDTO = new DeliveryDTO();
                deliveryDTO.setDeliveryAmount(newDeliver);
                deliveryDTO.setProduct(t.getProduct());
                deliveryDTO.setDateCreated(DateTime.now());
                transaction.getDeliveries().add(deliveryDTO);
            }
        });

        if(!paymentField.getText().trim().isEmpty()){
            if(payment.getPaymentType()== Payment.PaymentType.STORE_CREDIT){
                customer.setStoreCredit(new BigDecimal(customer.getStoreCredit()).setScale(2,BigDecimal.ROUND_HALF_EVEN).subtract(new BigDecimal(paymentField.getText())).doubleValue());
            }
            payment.setDateCreated(DateTime.now());
            payment.setDateModified(DateTime.now());
            payment.setPaymentAmount(new BigDecimal(paymentField.getText()).doubleValue());
            payment.setDeposit(isDepositCheckBox.isSelected());
            payment.setPaymentType(Payment.PaymentType.getValue(paymentTypeChoiceBox.getValue()));

        }
        transaction.getTransactionDetails().addAll(transactionDetailDTOObservableList);
        transaction.setSaleAmount(Double.valueOf(totalLabel.getText()));
        transaction.setGst(Double.valueOf(gstTaxField.getText()));
        transaction.setPst(Double.valueOf(pstTaxField.getText()));
        transaction.setNote(textArea.getText());
        transaction.setCustomer(customer);
        transaction.setStaff(staff);
        //transaction.setDateModified(DateTime.now());
        if (this.payment.getPaymentAmount()!=0){
            transaction.getPayments().add(payment);
            double paid = 0.0;
            for (PaymentDTO p : transaction.getPayments()){
                paid+=p.getPaymentAmount();
            }
            if (paid >= transaction.getSaleAmount()){
                transaction.setPaymentStatus(Transaction.PaymentStatus.PAID);
            }else if (paid >0){
                transaction.setPaymentStatus(Transaction.PaymentStatus.PARTIALLY_PAID);
            }
        }


        Optional<ButtonType> result = new AlertBuilder(dialogStage)
                .alertType(Alert.AlertType.CONFIRMATION)
                .alertHeaderText("Transaction Confirmation")
                .alertContentText("Are you sure you want to submit this transaction?\n")
                .build()
                .showAndWait();
        if(result.isPresent() && result.get() == ButtonType.OK){
            CustomerDTO tmpCustomer = RestClientFactory.getCustomerClient().getByName(customer.getFullname());
            tmpCustomer.setStoreCredit(this.customer.getStoreCredit()-oldStoreCredit+tmpCustomer.getStoreCredit());
            this.customer = tmpCustomer;
            transaction.setCustomer(customer);
            transaction.setStaff(RestClientFactory.getStaffClient().getByName(staff.getUserName()));
            transaction.getTransactionDetails().forEach(t->{
                ProductDTO tmpProduct = RestClientFactory.getProductClient().getById(t.getProduct().getId());
                double newVirtNum = t.getProduct().getVirtualTotalNum()-oldProductVirtualNumMap.get(tmpProduct.getId())+tmpProduct.getVirtualTotalNum();
                double newActualNum = t.getProduct().getTotalNum()-oldProductActualNumMap.get(tmpProduct.getId())+tmpProduct.getTotalNum();
                tmpProduct.setVirtualTotalNum(newVirtNum);
                tmpProduct.setTotalNum(newActualNum);
                t.setProduct(tmpProduct);
            });
            if (transaction.getDeliveryStatus() == Transaction.DeliveryStatus.DELIVERED && transaction.getPaymentStatus()== Transaction.PaymentStatus.PAID){
                transaction.setFinalized(true);
            }
            if(mode== Mode.CREATE) {
                transaction.setRefId(oldTransaction.getId());
            }

            Task<TransactionDTO> saveUpdateTransactionTask = new Task<TransactionDTO>() {
                @Override
                protected TransactionDTO call() throws Exception {
                    return RestClientFactory.getTransactionClient().saveOrUpdateAll(transaction);
                }
            };
            saveUpdateTransactionTask.setOnSucceeded(event -> {
                this.transaction = saveUpdateTransactionTask.getValue();
                if(mode== Mode.CREATE) {
                    int refNum = transaction.getId();
                    oldTransaction.setRefId(refNum);
                    oldTransaction.setFinalized(true);
                    RestClientFactory.getTransactionClient().update(oldTransaction);
                }
                confirmedClicked = true;
                dialogStage.close();
            });
            saveUpdateTransactionTask.exceptionProperty().addListener((observable, oldValue, newValue) -> {
                if(newValue != null) {
                    Exception ex = (Exception) newValue;
                    logger.error(ExceptionUtils.getRootCause(ex).getMessage());
                    JSONObject errorMsg = new JSONObject(ExceptionUtils.getRootCause(ex).getMessage());
                    new AlertBuilder(dialogStage).alertType(Alert.AlertType.ERROR)
                            .alertContentText(errorMsg.getString("taimErrorMessage"))
                            .build()
                            .showAndWait();
                    if(errorMsg.getInt("taimErrorCode") == 1){
                        Task<TransactionDTO> getTransactionTask = new Task<TransactionDTO>() {
                            @Override
                            protected TransactionDTO call() throws Exception {
                                return RestClientFactory.getTransactionClient().getById(transaction.getId());
                            }
                        };
                        getTransactionTask.setOnSucceeded(event -> {
                            setMainClass(getTransactionTask.getValue());
                            initPanelDetails();
                        });
                        getTransactionTask.exceptionProperty().addListener((observable1, oldValue1, newValue1) -> {
                            if(newValue1 != null) {
                                Exception newEx = (Exception) newValue1;
                                String newExMsg = ExceptionUtils.getRootCause(newEx).getMessage();
                                logger.error(newExMsg);
                                JSONObject newErrorMessage = new JSONObject(newExMsg);
                                new AlertBuilder(dialogStage).alertType(Alert.AlertType.ERROR)
                                        .alertContentText(newErrorMessage.getString("taimErrorMessage"))
                                        .build()
                                        .showAndWait();
                            }});
                        executor.execute(getTransactionTask);
                    }
                }
            });
            executor.execute(saveUpdateTransactionTask);
        }
    }



    public boolean isConfirmedClicked(){
        return this.confirmedClicked;
    }

    private void refreshTable(){
        transactionTableView.getColumns().get(0).setVisible(false);
        transactionTableView.getColumns().get(0).setVisible(true);
    }


    public TransactionDTO getTransaction(){return this.transaction;}

    private enum Mode{
        CREATE,EDIT;
    }



    private int validateDiscountEntered(int oldValue, int newValue){
        if (this.customer!=null && this.customer.getCustomerClass()!=null) {
            if(newValue <= this.customer.getCustomerClass().getCustomerDiscount()){
                return newValue;
            }else{
                new AlertBuilder(dialogStage)
                        .alertType(Alert.AlertType.ERROR)
                        .alertHeaderText("Discount Error!")
                        .alertContentText("Exceed Max discount rate for this customer!")
                        .build()
                        .showAndWait();
            }
        }
        return oldValue;
    }

    private PackageInfoDTO initiatePkgInfo(TransactionDetailDTO detailDTO){
        PackageInfoDTO pkgInfo = new PackageInfoDTO();
        pkgInfo.setDateCreated(DateTime.now());
        pkgInfo.setDateModified(DateTime.now());
        pkgInfo.setBox((int)detailDTO.getQuantity()/detailDTO.getProduct().getPiecePerBox());
        pkgInfo.setPieces((int)detailDTO.getQuantity()-detailDTO.getProduct().getPiecePerBox()* pkgInfo.getBox());
        return pkgInfo;
    }

    private TransactionDetailDTO copyDetails(TransactionDetailDTO oldDetail){
        TransactionDetailDTO transactionDetailDTO = new TransactionDetailDTO();
        transactionDetailDTO.setDateCreated(DateTime.now());
        transactionDetailDTO.setDateModified(DateTime.now());
        transactionDetailDTO.setPackageInfo(initiatePkgInfo(oldDetail));
        transactionDetailDTO.setProduct(oldDetail.getProduct());
        transactionDetailDTO.setNote(oldDetail.getNote());
        transactionDetailDTO.setSaleAmount(oldDetail.getSaleAmount());
        transactionDetailDTO.setQuantity(oldDetail.getQuantity());
        transactionDetailDTO.setDiscount(oldDetail.getDiscount());
        return transactionDetailDTO;
    }


    private TransactionDTO copyTransaction(TransactionDTO oldTransaction){
        TransactionDTO newTransaction = new TransactionDTO();
        newTransaction.setDateCreated(DateTime.now());
        newTransaction.setTransactionType(Transaction.TransactionType.INVOICE);
        newTransaction.setCustomer(oldTransaction.getCustomer());
        newTransaction.setFinalized(false);
        newTransaction.setTransactionDetails(oldTransaction.getTransactionDetails().stream().map(d->copyDetails(d)).collect(Collectors.toList()));
        newTransaction.setSaleAmount(oldTransaction.getSaleAmount());
        newTransaction.setPst(oldTransaction.getPst());
        newTransaction.setGst(oldTransaction.getGst());
        newTransaction.setStaff(oldTransaction.getStaff());
        newTransaction.setVendor(oldTransaction.getVendor());
        newTransaction.setNote(oldTransaction.getNote());

        return newTransaction;
    }


    private boolean isStoreCreditValidNoCustomer(){
        try{
            double credit  = Double.parseDouble(paymentField.getText());
            if(credit > customer.getStoreCredit()){
                return false;
            }
        }catch(NumberFormatException e){
            return false;
        }
        return true;
    }

    private void updatePrevProductNum(){
        oldProductVirtualNumMap= new HashMap<>();
        oldProductActualNumMap = new HashMap<>();
        oldStoreCredit=customer.getStoreCredit();
        this.transaction.getTransactionDetails().forEach(t->{
            oldProductVirtualNumMap.put(t.getProduct().getId(),t.getProduct().getVirtualTotalNum());
            oldProductActualNumMap.put(t.getProduct().getId(),t.getProduct().getTotalNum());
        });
    }
}

