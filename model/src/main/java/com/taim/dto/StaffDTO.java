package com.taim.dto;

import com.taim.dto.basedtos.UserBaseModelDTO;
import com.taim.model.Staff;
import javafx.beans.property.*;

import java.util.ArrayList;
import java.util.List;

public class StaffDTO extends UserBaseModelDTO{
    private StringProperty userName;
    private StringProperty password;
    private StringProperty picUrl;
    private ObjectProperty<Staff.Position> position;
    private BooleanProperty checked;

    public StaffDTO(){
        userName = new SimpleStringProperty();
        password = new SimpleStringProperty();
        picUrl = new SimpleStringProperty();
        position = new SimpleObjectProperty<>();
        checked = new SimpleBooleanProperty();
    }

    public String getUserName() {
        return userName.get();
    }

    public StringProperty userNameProperty() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName.set(userName);
    }

    public String getPassword() {
        return password.get();
    }

    public StringProperty passwordProperty() {
        return password;
    }

    public void setPassword(String password) {
        this.password.set(password);
    }

    public String getPicUrl() {
        return picUrl.get();
    }

    public StringProperty picUrlProperty() {
        return picUrl;
    }

    public void setPicUrl(String picUrl) {
        this.picUrl.set(picUrl);
    }

    public Staff.Position getPosition() {
        return position.get();
    }

    public ObjectProperty<Staff.Position> positionProperty() {
        return position;
    }

    public void setPosition(Staff.Position position) {
        this.position.set(position);
    }

    public boolean isChecked() {
        return checked.get();
    }

    public BooleanProperty checkedProperty() {
        return checked;
    }

    public void setChecked(boolean checked) {
        this.checked.set(checked);
    }
}
