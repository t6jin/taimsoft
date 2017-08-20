package com.taimsoft.dbconnection.dao;

import java.util.List;

/**
 * Created by Tjin on 8/19/2017.
 */
public interface IDao<T> {
    void save(T object);

    List<T> getAll();

    T findByID(Integer id);

    void updateObject(T object);
}