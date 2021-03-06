package com.taim.model;


import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.taim.model.basemodels.UserBaseModel;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;


import javax.persistence.*;

import java.util.List;

/**
 * Created by Tjin on 7/20/2017.
 */
@Entity
@Table(name = "vendor")
@JsonIdentityInfo(scope = Vendor.class,
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id")
public class Vendor extends UserBaseModel {
    @Column(name = "user_type", nullable = false)
    private UserType userType;

    public Vendor(){}

    public UserType getUserType() {
        return userType;
    }

    public void setUserType(UserType userType) {
        this.userType = userType;
    }
}
