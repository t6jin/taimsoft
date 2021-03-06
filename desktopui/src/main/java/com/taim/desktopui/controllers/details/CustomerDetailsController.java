package com.taim.desktopui.controllers.details;

import com.jfoenix.controls.JFXComboBox;
import com.taim.desktopui.controllers.edit.CustomerEditDialogController;
import com.taim.desktopui.util.AlertBuilder;
import com.taim.desktopui.util.RestClientFactory;
import com.taim.desktopui.util.TransactionPanelLoader;
import com.taim.dto.CustomerDTO;
import com.taim.dto.TransactionDTO;
import com.taim.model.Transaction;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableBooleanValue;
import javafx.beans.value.ObservableStringValue;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class CustomerDetailsController implements IDetailController<CustomerDTO>{
    private static final Logger logger = LoggerFactory.getLogger(CustomerDetailsController.class);
    private static final DateTimeFormatter dtf = DateTimeFormat.forPattern("MMM-dd-yyyy");
    private CustomerDTO customerDTO;
    private List<TransactionDTO> quotationList;
    private List<TransactionDTO> invoiceList;
    private List<TransactionDTO> returnList;
    private Executor executor;
    private Stage stage;

    @FXML
    private Label storeCreditLabel;
    @FXML
    private Label customerClassLabel;
    @FXML
    private Label fullNameLabel;
    @FXML
    private Label phoneLabel;
    @FXML
    private Label emailLabel;
    @FXML
    private Label customerTypeLabel;
    @FXML
    private Label orgNameLabel;
    @FXML
    private Label streetNumLabel;
    @FXML
    private Label streetLabel;
    @FXML
    private Label cityLabel;
    @FXML
    private Label countryLabel;
    @FXML
    private Label postalCodeLabel;
    @FXML
    private Label dateCreatedLabel;
    @FXML
    private JFXComboBox<String> actionComboBox;
    @FXML
    private TabPane transactionTabPane;


    public CustomerDetailsController(){
        quotationList = new ArrayList<>();
        invoiceList = new ArrayList<>();
        returnList = new ArrayList<>();
        executor = Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        });
    }

    @FXML
    public void initialize(){
        actionComboBox.setItems(FXCollections.observableArrayList("EDIT"));
        actionComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
            if(newValue != null){
                if(newValue.equals("EDIT")){
                    CustomerEditDialogController controller = TransactionPanelLoader.showCustomerEditor(customerDTO);
                    if(controller != null && controller.isOKClicked()){
                        initDetailData(controller.getCustomer());
                    }
                }
            }
            Platform.runLater(() -> actionComboBox.getSelectionModel().clearSelection());
        });
    }

    @Override
    public void initDetailData(CustomerDTO obj) {
        this.customerDTO = obj;
        initDataFromDB(customerDTO.getId());
        bindCustomerInfoLabels();
    }

    @Override
    public void setStage(Stage stage){
        this.stage = stage;
    }

    public void initDataFromDB(int id){
        Task<List<TransactionDTO>> transactionTask = new Task<List<TransactionDTO>>() {
            @Override
            protected List<TransactionDTO> call() throws Exception {
                return RestClientFactory.getTransactionClient().getListByCustomerID(id);
            }
        };

        transactionTask.setOnSucceeded(event ->{
            try {
                this.invoiceList = transactionTask.get().stream().filter(t -> t.getTransactionType().equals(Transaction.TransactionType.INVOICE)).collect(Collectors.toList());
                this.quotationList = transactionTask.get().stream().filter(t -> t.getTransactionType().equals(Transaction.TransactionType.QUOTATION)).collect(Collectors.toList());
                this.returnList = transactionTask.get().stream().filter(t -> t.getTransactionType().equals(Transaction.TransactionType.RETURN)).collect(Collectors.toList());
                initTransactionTabPane();
            } catch (InterruptedException | ExecutionException e) {
                logger.error(e.getMessage(), e);
            }
        });
        transactionTask.setOnFailed(event -> {
            logger.error(transactionTask.getException().getMessage());
            new AlertBuilder(stage)
                    .alertType(Alert.AlertType.ERROR)
                    .alertHeaderText("Database Error!")
                    .alertContentText("Unable to fetch transaction information from the database!")
                    .build()
                    .showAndWait();
        });
        executor.execute(transactionTask);

    }

    private void initTransactionTabPane(){
        transactionTabPane.getSelectionModel().clearSelection();
        transactionTabPane.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            try {
                FXMLLoader fXMLLoader = new FXMLLoader();
                AnchorPane root = fXMLLoader.load(this.getClass().getResource("/fxml/details/TransactionTableView.fxml").openStream());
                root.prefHeightProperty().bind(transactionTabPane.heightProperty());
                root.prefWidthProperty().bind(transactionTabPane.widthProperty());
                TransactionTableViewController controller = fXMLLoader.getController();
                if(newValue.getText().equals("Quotation Transactions")){
                    controller.initTableData(quotationList);
                }else if(newValue.getText().equals("Invoice Transactions")){
                    controller.initTableData(invoiceList);
                }else if(newValue.getText().equals("Return Transactions")){
                    controller.initTableData(returnList);
                }
                newValue.setContent(root);
            } catch (IOException ex) {
                logger.error(ex.getMessage(), ex);
            }
        });
        transactionTabPane.getSelectionModel().selectFirst();
    }

    private void bindCustomerInfoLabels(){
        if(customerDTO != null) {
            storeCreditLabel.textProperty().bind(initStringBinding(Bindings.isNull(customerDTO.storeCreditProperty().asObject()),
                    "", customerDTO.storeCreditProperty().asString()));
            customerClassLabel.textProperty().bind(initStringBinding(customerDTO.customerClassProperty().isNull(),
                    "", customerDTO.getCustomerClass().customerClassNameProperty()));
            dateCreatedLabel.textProperty().bind(initStringBinding(customerDTO.dateCreatedProperty().isNull(),
                    "", new SimpleStringProperty(dtf.print(customerDTO.getDateCreated()))));
            fullNameLabel.textProperty().bind(initStringBinding(customerDTO.fullnameProperty().isNull(),
                    "", customerDTO.fullnameProperty()));
            emailLabel.textProperty().bind(initStringBinding(customerDTO.emailProperty().isNull(),
                    "", customerDTO.emailProperty()));
            phoneLabel.textProperty().bind(initStringBinding(customerDTO.phoneProperty().isNull(),
                    "", customerDTO.phoneProperty()));
            customerTypeLabel.textProperty().bind(initStringBinding(customerDTO.userTypeProperty().isNull(),
                    "", new SimpleStringProperty(customerDTO.getUserType().getValue())));
            if(customerDTO.getOrganization() != null){
                orgNameLabel.textProperty().bind(initStringBinding(customerDTO.getOrganization().orgNameProperty().isNull(),
                        "", customerDTO.getOrganization().orgNameProperty()));
                streetNumLabel.textProperty().bind(initStringBinding(customerDTO.getOrganization().streetNumProperty().isNull(),
                        "", customerDTO.getOrganization().streetNumProperty()));
                streetLabel.textProperty().bind(initStringBinding(customerDTO.getOrganization().streetProperty().isNull(),
                        "", customerDTO.getOrganization().streetProperty()));
                cityLabel.textProperty().bind(initStringBinding(customerDTO.getOrganization().cityProperty().isNull(),
                        "", customerDTO.getOrganization().cityProperty()));
                countryLabel.textProperty().bind(initStringBinding(customerDTO.getOrganization().countryProperty().isNull(),
                        "", customerDTO.getOrganization().countryProperty()));
                postalCodeLabel.textProperty().bind(initStringBinding(customerDTO.getOrganization().postalCodeProperty().isNull(),
                        "", customerDTO.getOrganization().postalCodeProperty()));
            }
        }
    }

    private StringBinding initStringBinding(ObservableBooleanValue condition, String thenVal, ObservableStringValue
            otherwiseVal){
        return Bindings.when(condition).then(thenVal).otherwise(otherwiseVal);
    }
}
