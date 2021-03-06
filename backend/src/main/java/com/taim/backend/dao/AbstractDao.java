package com.taim.backend.dao;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Created by Tjin on 7/15/2017.
 */
public abstract class AbstractDao {
    @Autowired
    private SessionFactory sessionFactory;

    public Session getSession() {
        return sessionFactory.getCurrentSession();
    }

    public void persist(Object entity) {
        getSession().persist(entity);
    }

    public void delete(Object entity) {
        getSession().delete(entity);
    }

    public void update(Object entity){ getSession().update(entity); }

    public void saveOrUpdate(Object entity){
        getSession().saveOrUpdate(entity);
    }

    public void flush(){
        getSession().flush();
    }

    public void refresh(Object entity){
        getSession().refresh(entity);
    }

}
