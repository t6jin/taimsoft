package com.taimsoft.desktopui.controllers.login;

import com.taim.client.PropertyClient;
import com.taim.client.StaffClient;
import com.taim.dto.PropertyDTO;
import com.taim.dto.StaffDTO;
import com.taimsoft.desktopui.util.RestClientFactory;
import com.taimsoft.desktopui.util.VistaNavigator;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class LoginDialogController {
    private Stage dialogStage;
    private StaffClient staffClient;
    private PropertyClient propertyClient;
    private Executor executor;

    @FXML
    private TextField userNameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Button loginButton;

    @FXML
    private void initialize(){
        userNameField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if(!userNameField.getText().trim().isEmpty()){
                    loginButton.setDisable(false);
                }
                else{
                    loginButton.setDisable(true);
                }
            }
        });
    }

    @FXML
    public void handleLogin(){
        loginButton.setDisable(true);
        String userName = userNameField.getText();
        String password = passwordField.getText();

        Task<List<PropertyDTO>> propertyTask = new Task<List<PropertyDTO>>() {
            @Override
            protected List<PropertyDTO> call() throws Exception {
                return propertyClient.getList();
            }
        };
        propertyTask.setOnSucceeded(event -> {
            if(propertyTask.getValue().size() != 0){
                VistaNavigator.setGlobalProperty(propertyTask.getValue().get(0));
            }
        });

        executor.execute(propertyTask);
    }

    @FXML
    public void handleCancel(){
        dialogStage.close();
    }

    public LoginDialogController() {
        this.propertyClient = RestClientFactory.getPropertyClient();
        this.staffClient = RestClientFactory.getStaffClient();
        executor = Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        });
    }

    public void setDialogStage(Stage dialogStage){
        this.dialogStage = dialogStage;
    }

}