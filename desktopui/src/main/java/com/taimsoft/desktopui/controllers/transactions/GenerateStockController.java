package com.taimsoft.desktopui.controllers.transactions;

import com.taim.dto.*;
import com.taim.model.DeliveryStatus;
import com.taim.model.PackageInfo;
import com.taim.model.Transaction;
import com.taimsoft.desktopui.controllers.edit.VendorEditDialogController;
import com.taimsoft.desktopui.util.*;
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
import org.joda.time.DateTime;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * Created by jiawei.liu
 * on 10/25/17.
 */

public class GenerateStockController {

    private Stage dialogStage;

    private VendorDTO vendor;
    private StaffDTO staff;
    private List<VendorDTO> vendorList;
    private List<ProductDTO> productList;
    private ObservableList<TransactionDetailDTO> transactionDetailDTOObservableList;
    private TransactionDTO transaction;
    private StringBuffer errorMsgBuilder;
    private boolean confirmedClicked;
    private BooleanBinding confimButtonBinding;
    private Executor executor;
    private Mode mode;
    //private Map<Integer, Double> oldProductQuantityMap;
    private ObservableList<String> paymentDue;
    private ObservableList<String> deliveryDue;
    private DeliveryStatus.Status prevStats;


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


    //Transaction Information Labels
    @FXML
    private Label typeLabel;
    @FXML
    private Label dateLabel;

    //transaction payment/delivery due Labels
    @FXML
    private DatePicker paymentDueDatePicker;
    @FXML
    private DatePicker deliveryDueDatePicker;
    @FXML
    private ChoiceBox<String> deliveryStatusChoiceBox;
    @FXML
    private ChoiceBox<String> paymentStatusChoiceBox;

    //Staff Information Labels
    @FXML
    private Label staffFullNameLabel;
    @FXML
    private Label staffPhoneLabel;
    @FXML
    private Label staffPositionLabel;
    @FXML
    private Label staffEmail;

    //Vendor Details Labels
    @FXML
    private Label fullNameLabel;
    @FXML
    private Label userTypeLabel;
    @FXML
    private Label emailLabel;
    @FXML
    private Label phoneLabel;


    //Items Information Labels
    @FXML
    private Label itemsCountLabel;
    @FXML
    private Label subTotalLabel;
    @FXML
    private Label pstTaxLabel;
    @FXML
    private Label gstTaxLabel;
    @FXML
    private Label totalLabel;

    @FXML
    private Button addItemButton;
    @FXML
    private Button confirmButton;
    @FXML
    private Button cancelButton;

    @FXML
    private ComboBox<String> productComboBox;
    @FXML
    private ComboBox<String> vendorComboBox;
    @FXML
    private ComboBox<String> vendorPhoneComboBox;
    @FXML
    private TextArea textArea;


    @FXML
    private void initialize(){
        confimButtonBinding = Bindings.size(transactionTableView.getItems()).greaterThan(0);
        confirmButton.disableProperty().bind(confimButtonBinding);
        productIdCol.setCellValueFactory(p->new SimpleStringProperty(p.getValue().getProduct().getSku()));
        unitPriceCol.setCellValueFactory(u->new SimpleFloatProperty(new BigDecimal(u.getValue().getProduct().getUnitPrice()).floatValue()));
        qtyCol.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        qtyCol.setCellFactory(TextFieldTableCell.forTableColumn(new StringConverter<Number>() {
            @Override
            public String toString(Number object) {
                return String.valueOf(object);
            }

            @Override
            public Float fromString(String string) {
                return Float.valueOf(string);
            }
        }));

        qtyCol.setOnEditCommit(event -> {
            int oldValue = event.getOldValue().intValue();
            TransactionDetailDTO p = event.getTableView().getItems().get(event.getTablePosition().getRow());
            p.setQuantity(event.getNewValue().floatValue());
            p.setSaleAmount(new BigDecimal(p.getQuantity() * p.getProduct().getUnitPrice()).setScale(2, BigDecimal.ROUND_HALF_EVEN).floatValue());
            p.getPackageInfo().setBox(new BigDecimal(p.getQuantity()/p.getProduct().getPiecePerBox()).intValue());
            p.getPackageInfo().setPieces(new BigDecimal(p.getQuantity()-p.getProduct().getPiecePerBox()*p.getPackageInfo().getBox()).intValue());
            showPaymentDetails();
            p.getProduct().setVirtualTotalNum(p.getProduct().getVirtualTotalNum()-oldValue+event.getNewValue().intValue());
            refreshTable();
        });

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
        sizeCol.setCellValueFactory(s->new SimpleStringProperty(SizeHelper.getSizeString(s.getValue().getProduct())));
        pkgBoxCol.setCellValueFactory(u->new SimpleFloatProperty(new BigDecimal(u.getValue().getPackageInfo().getBox()).floatValue()));
        pkgPieceCol.setCellValueFactory(u->new SimpleFloatProperty(new BigDecimal(u.getValue().getPackageInfo().getPieces()).floatValue()));
        remarkCol.setCellValueFactory(new PropertyValueFactory<>("note"));
        remarkCol.setOnEditCommit(event ->
                (event.getTableView().getItems().get(event.getTablePosition().getRow())).setNote(event.getNewValue()));
        remarkCol.setCellFactory(TextFieldTableCell.forTableColumn());
        totalCol.setCellValueFactory(param ->
                new SimpleFloatProperty(new BigDecimal(param.getValue().getSaleAmount())
                        .setScale(2, RoundingMode.HALF_EVEN).floatValue()));
        deleteCol.setCellValueFactory(
                new Callback<TableColumn.CellDataFeatures<TransactionDetailDTO, Boolean>,
                                        ObservableValue<Boolean>>() {
                    @Override
                    public ObservableValue<Boolean> call(TableColumn.CellDataFeatures<TransactionDetailDTO, Boolean> p) {
                        return new SimpleBooleanProperty(p.getValue() != null);
                    }
                });
        deleteCol.setCellFactory(
                new Callback<TableColumn<TransactionDetailDTO, Boolean>, TableCell<TransactionDetailDTO, Boolean>>() {
                    @Override
                    public TableCell<TransactionDetailDTO, Boolean> call(TableColumn<TransactionDetailDTO, Boolean> p) {
                        return new ButtonCell(transactionTableView);
                    }

                });
        deliveryStatusChoiceBox.setItems(deliveryDue);
        deliveryStatusChoiceBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue,String newValue) {
                transaction.getDeliveryStatus().setStatus(DeliveryStatus.getStatus(newValue));
                transaction.getDeliveryStatus().setDateModified(DateTime.now());
                if (DeliveryStatus.getStatus(newValue)== DeliveryStatus.Status.DELIVERED && oldValue!=null){
                    transactionTableView.getItems().forEach(e->e.getProduct().setTotalNum(e.getProduct().getTotalNum()+e.getQuantity()));
                    qtyCol.setEditable(false);
                    refreshTable();
                }else if (DeliveryStatus.getStatus(oldValue)== DeliveryStatus.Status.DELIVERED){
                    transactionTableView.getItems().forEach(e->e.getProduct().setTotalNum(e.getProduct().getTotalNum()-e.getQuantity()));
                    qtyCol.setEditable(true);
                    refreshTable();
                }
            }
        });
        paymentStatusChoiceBox.setItems(paymentDue);
        paymentStatusChoiceBox.valueProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue,String newValue) {
                transaction.setPaymentStatus(Transaction.PaymentStatus.getStatus(newValue));
            }
        });

        paymentDueDatePicker.setOnAction(event ->{
            this.transaction.setPaymentDueDate(DateUtils.toDateTime(paymentDueDatePicker.getValue()));
        });
        paymentDueDatePicker.setPromptText(DATE_PATTERN.toLowerCase());
        paymentDueDatePicker.setConverter(new StringConverter<LocalDate>() {
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
            new AlertBuilder()
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
            newProductTransaction.setSaleAmount(selectedProduct.getUnitPrice()*newProductTransaction.getQuantity());
            newProductTransaction.setPackageInfo(initiatePkgInfo(newProductTransaction));
            transactionDetailDTOObservableList.add(newProductTransaction);
        }
    }

    @FXML
    public void handleAddVendor(){
        VendorDTO newVendor = new VendorDTO();
        newVendor.setDateCreated(DateTime.now());
        newVendor.setDateModified(DateTime.now());
        newVendor.setOrganization(new OrganizationDTO());
        newVendor.getOrganization().setDateCreated(DateTime.now());
        newVendor.getOrganization().setDateModified(DateTime.now());
        VendorEditDialogController controller = TransactionPanelLoader.showVendorEditor(newVendor);
        if(controller != null && controller.isOKClicked()){
            this.vendor = RestClientFactory.getVendorClient().getByName(controller.getVendor().getFullname());
            vendorList.add(this.vendor);
            showVendorDetails();
        }
    }

    @FXML
    public void handleCancelButton(){
        this.dialogStage.close();
    }

    @FXML
    public TransactionDTO handleConfirmButton() throws IOException, SQLException {
        generateTransaction();
        if(isConfirmedClicked()) {
            dialogStage.close();
        }
        return this.transaction;
    }


    public GenerateStockController(){
        confirmedClicked = false;
        paymentDue = FXCollections.observableArrayList(
                Arrays.stream(Transaction.PaymentStatus.values()).map(Transaction.PaymentStatus::name).collect(Collectors.toList()));
        deliveryDue = FXCollections.observableArrayList(
                Arrays.stream(DeliveryStatus.Status.values()).map(DeliveryStatus.Status::getValue).collect(Collectors.toList()));
    }


    public void setDialogStage(Stage dialogStage){
        this.dialogStage = dialogStage;
    }


    /**
     * Initialize the main class for this class
     */

    public void setMainClass(TransactionDTO transactionFromAbove){

        //either edit existing stock or create new stock
        if (transactionFromAbove==null) {
            this.staff = VistaNavigator.getGlobalStaff();
            this.mode= Mode.CREATE;
            this.transaction = new TransactionDTO();
            transaction.setTransactionType(Transaction.TransactionType.STOCK);
            transaction.setFinalized(false);
            transaction.setStaff(staff);
            transaction.setDateCreated(DateTime.now());

            DeliveryStatusDTO currentDeliveryStatus = new DeliveryStatusDTO();
            currentDeliveryStatus.setStatus(DeliveryStatus.Status.UNDELIVERED);
            currentDeliveryStatus.setDateCreated(DateTime.now());
            currentDeliveryStatus.setDateModified(DateTime.now());
            this.transaction.setDeliveryStatus(currentDeliveryStatus);
            this.transaction.setPaymentDueDate(transaction.getDateCreated());
            this.transaction.setDeliveryDueDate(transaction.getDateCreated());
            this.transaction.setPaymentStatus(Transaction.PaymentStatus.UNPAID);

        }else{
            this.mode= Mode.EDIT;
            prevStats = transactionFromAbove.getDeliveryStatus().getStatus();
            this.transaction = transactionFromAbove;
            this.staff = transactionFromAbove.getStaff();
            this.vendor = transactionFromAbove.getVendor();
            //updatePrevProductCount();

            if (prevStats== DeliveryStatus.Status.DELIVERED){
                qtyCol.setEditable(false);
            }
            if(transaction.isFinalized()){
                System.out.println("This transaction is already finalized! You cannot edit on it anymore.");
                confirmButton.setDisable(true);
            }
        }
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
        //load list of products and vendors
        Task<List<VendorDTO>> vendorsTask = new Task<List<VendorDTO>>() {
            @Override
            protected List<VendorDTO> call() throws Exception {
                return RestClientFactory.getVendorClient().getList();
            }
        };
        Task<List<ProductDTO>> productsTask = new Task<List<ProductDTO>>() {
            @Override
            protected List<ProductDTO> call() throws Exception {
                return RestClientFactory.getProductClient().getList();
            }
        };

        vendorsTask.setOnSucceeded(event ->{
            this.vendorList = vendorsTask.getValue();

            if(this.transaction.getVendor()!=null && this.transaction.getVendor().getFullname() != null){
                Optional<VendorDTO> vendor =  vendorList.stream().filter(p -> p.getFullname().equals(transaction.getVendor().getFullname())).findFirst();
                if(vendor.isPresent()){
                    this.vendor = vendor.get();
                    showVendorDetails();
                }
            }
            List<String> tmpVendorList = new ArrayList<>();
            for(VendorDTO vendor: this.vendorList){
                tmpVendorList.add(vendor.getFullname());
            }
            vendorComboBox.setItems(FXCollections.observableArrayList(tmpVendorList));
            vendorComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
                for(VendorDTO tmpVendor: this.vendorList){
                    if(tmpVendor.getFullname().equals(newValue)){
                        vendor = tmpVendor;
                        showVendorDetails();
                        break;
                    }
                }
            });
            List<String> tmpVendorPhoneList = new ArrayList<>();
            for(VendorDTO vendor: this.vendorList){

                tmpVendorPhoneList.add(vendor.getPhone());
            }
            vendorPhoneComboBox.setItems(FXCollections.observableArrayList(tmpVendorPhoneList));
            vendorPhoneComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
                for(VendorDTO tmpVendor: this.vendorList){
                    if(tmpVendor.getPhone() != null && tmpVendor.getPhone().equals(newValue)){
                        vendor = tmpVendor;
                        showVendorDetails();
                        break;
                    }
                }
            });
            new AutoCompleteComboBoxListener<>(vendorComboBox);
            new AutoCompleteComboBoxListener<>(vendorPhoneComboBox);
        });

        vendorsTask.setOnFailed(event -> {
            System.out.println((event.getSource().getMessage()));
            new AlertBuilder()
                    .alertType(Alert.AlertType.ERROR)
                    .alertHeaderText("Database Error!")
                    .alertContentText("Unable to fetch vendor information from the database!")
                    .build()
                    .showAndWait();
            dialogStage.close();
        });

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
            System.out.println(event.getSource().getMessage());
            new AlertBuilder()
                    .alertType(Alert.AlertType.ERROR)
                    .alertHeaderText("Database Error!")
                    .alertContentText("Unable to fetch product information from the database!")
                    .build()
                    .showAndWait();
            dialogStage.close();
        });
        executor.execute(vendorsTask);
        executor.execute(productsTask);
    }

    /**
     * Initial Panel Details
     */
    public void initPanelDetails(){
        showTransactionDetails();
        showStaffDetails();
        showVendorDetails();
        showPaymentDetails();
        showPaymentDeliveryDetail();

    }


    private void showTransactionDetails(){
        typeLabel.setText(transaction.getTransactionType().getValue());
        dateLabel.setText(new SimpleDateFormat("yyyy-MM-dd").format(transaction.getDateCreated().toDate()));
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
        deliveryStatusChoiceBox.getSelectionModel().select(transaction.getDeliveryStatus().getStatus().getValue());
        paymentStatusChoiceBox.getSelectionModel().select(transaction.getPaymentStatus().name());
    }

    /**
     * Show customer details grid pane
     */

    private void showVendorDetails(){
        if(this.vendor != null){
            addItemButton.setDisable(false);
            fullNameLabel.setText(vendor.getFullname());
            userTypeLabel.setText(vendor.getUserType().getValue());
            emailLabel.setText(this.vendor.getEmail());
            phoneLabel.setText(this.vendor.getPhone());
        }
        else{
            addItemButton.setDisable(true);
            fullNameLabel.setText("");
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
            BigDecimal subTotal = new BigDecimal(0.00);
            while(iterator.hasNext()){
                TransactionDetailDTO tmp = iterator.next();
                subTotal = subTotal.add(new BigDecimal(tmp.getSaleAmount()));
            }

            BigDecimal pstTax = new BigDecimal(pstNum).multiply(subTotal).divide(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_EVEN);
            BigDecimal gstTax = new BigDecimal(gstNum).multiply(subTotal).divide(new BigDecimal(100)).setScale(2, BigDecimal.ROUND_HALF_EVEN);

            BigDecimal total = subTotal.add(pstTax).add(gstTax).setScale(2, BigDecimal.ROUND_HALF_EVEN);
            subTotal.setScale(2, RoundingMode.HALF_EVEN);

            itemsCountLabel.setText(String.valueOf(this.transactionDetailDTOObservableList.size()));
            subTotalLabel.setText(String.valueOf(subTotal.floatValue()));
            pstTaxLabel.setText(String.valueOf(pstTax.floatValue()));
            gstTaxLabel.setText(String.valueOf(gstTax.floatValue()));
            totalLabel.setText(String.valueOf(total.floatValue()));
        }
        else{
            itemsCountLabel.setText("");
            subTotalLabel.setText("");
            pstTaxLabel.setText("");
            gstTaxLabel.setText("");
            totalLabel.setText("");
        }
    }


    private void generateTransaction() throws IOException, SQLException{
        transaction.getTransactionDetails().clear();
        transactionDetailDTOObservableList.forEach(t->{
            if (t.getDateCreated()==null){
                t.setDateCreated(DateTime.now());
            }
            t.setDateModified(DateTime.now());
        });


        transaction.getTransactionDetails().addAll(transactionDetailDTOObservableList);
        transaction.setSaleAmount(Double.valueOf(totalLabel.getText()));
        transaction.setGst(Double.valueOf(gstTaxLabel.getText()));
        transaction.setPst(Double.valueOf(pstTaxLabel.getText()));
        transaction.setNote(textArea.getText());
        transaction.setVendor(vendor);
        transaction.setStaff(staff);
        transaction.setDateModified(DateTime.now());

        Optional<ButtonType> result = new AlertBuilder()
                .alertType(Alert.AlertType.CONFIRMATION)
                .alertTitle("Transaction Confirmation")
                .alertContentText("Are you sure you want to submit this transaction?\n")
                .alertHeaderText(null)
                .build()
                .showAndWait();
        if(result.isPresent() && result.get() == ButtonType.OK){
            if (transaction.getDeliveryStatus().getStatus()== DeliveryStatus.Status.DELIVERED && transaction.getPaymentStatus()== Transaction.PaymentStatus.PAID){
                transaction.setFinalized(true);
            }
            if(mode==Mode.CREATE) {
                RestClientFactory.getTransactionClient().add(transaction);
            }else{
                RestClientFactory.getTransactionClient().update(transaction);
            }
            updateProduct();
            confirmedClicked = true;
        }
    }

    private void updateProduct() {
        transaction.getTransactionDetails().forEach(p->{
            RestClientFactory.getProductClient().update(p.getProduct());
        });
//        if (transaction.getDeliveryStatus().getStatus() == DeliveryStatus.Status.DELIVERED) {
//            if (mode == Mode.CREATE) {
//                transaction.getTransactionDetails().forEach(p -> {
//                    double newVirtualNum = p.getProduct().getVirtualTotalNum() + p.getQuantity();
//                    p.getProduct().setVirtualTotalNum(newVirtualNum);
//                    double newActualNum = p.getProduct().getTotalNum() + p.getQuantity();
//                    p.getProduct().setTotalNum(newActualNum);
//                    RestClientFactory.getProductClient().update(p.getProduct());
//                });
//            } else {
//                transaction.getTransactionDetails().forEach(p -> {
//                    double newVirtualNum = p.getProduct().getVirtualTotalNum() + p.getQuantity();
//                    if (oldProductQuantityMap.containsKey(p.getProduct().getId())) {
//                        newVirtualNum -= oldProductQuantityMap.get(p.getProduct().getId());
//                    }
//                    p.getProduct().setVirtualTotalNum(newVirtualNum);
//                    if (prevStats != DeliveryStatus.Status.DELIVERED) {
//                        double newActualNum = p.getProduct().getTotalNum() + p.getQuantity();
//                        p.getProduct().setTotalNum(newActualNum);
//                    }
//                    RestClientFactory.getProductClient().update(p.getProduct());
//                });
//            }
//        } else if (transaction.getDeliveryStatus().getStatus() == DeliveryStatus.Status.UNDELIVERED) {
//            if (mode==Mode.EDIT){
//                transaction.getTransactionDetails().forEach(p->{
//                    double newVirtualNum = p.getProduct().getVirtualTotalNum() + p.getQuantity();
//                    if (oldProductQuantityMap.containsKey(p.getProduct().getId())){
//                        newVirtualNum -=oldProductQuantityMap.get(p.getProduct().getId());
//                    }
//                    p.getProduct().setVirtualTotalNum(newVirtualNum);
//                    RestClientFactory.getProductClient().update(p.getProduct());
//                });
//            }else{
//                transaction.getTransactionDetails().forEach(p->{
//                    double newVirtualNum = p.getProduct().getVirtualTotalNum() + p.getQuantity();
//                    p.getProduct().setVirtualTotalNum(newVirtualNum);
//                    RestClientFactory.getProductClient().update(p.getProduct());
//                });
//            }
//
//        }
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




    private PackageInfoDTO initiatePkgInfo(TransactionDetailDTO detailDTO){
        PackageInfoDTO pkgInfo = new PackageInfoDTO();
        pkgInfo.setDateCreated(DateTime.now());
        pkgInfo.setDateModified(DateTime.now());
        pkgInfo.setBox((int)detailDTO.getQuantity()/detailDTO.getProduct().getPiecePerBox());
        pkgInfo.setPieces((int)detailDTO.getQuantity()-detailDTO.getProduct().getPiecePerBox()* pkgInfo.getBox());
        return pkgInfo;
    }




//    private void updatePrevProductCount(){
//        oldProductQuantityMap = new HashMap<>();
//        this.transaction.getTransactionDetails().forEach(t->{
//            oldProductQuantityMap.put(t.getProduct().getId(),t.getQuantity());
//        });
//    }
}
