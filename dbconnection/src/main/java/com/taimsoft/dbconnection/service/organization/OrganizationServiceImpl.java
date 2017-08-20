package com.taimsoft.dbconnection.service.organization;

import com.taimsoft.dbconnection.dao.IDao;
import com.taimsoft.dbconnection.dao.organization.OrganizationDaoImpl;
import com.taimsoft.model.Organization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;

/**
 * Created by tjin on 2017-07-23.
 */
@Service("organizationService")
@Transactional
public class OrganizationServiceImpl implements IOrganizationService{
    @Autowired
    private IDao<Organization> organizationDao;

    @Override
    public List<Organization> getAllOrganizations() {
        return organizationDao.getAll();
    }

    @Override
    public void saveOrganization(Organization organization) {
        organizationDao.save(organization);
    }
}