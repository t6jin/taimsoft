package com.taim.client;

import com.taim.client.util.BeanMapper;
import com.taim.client.util.PropertiesProcessor;
import com.taim.client.util.RestTemplateFactory;
import com.taim.dto.OrganizationDTO;
import com.taim.model.Organization;
import org.apache.commons.beanutils.BeanMap;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by dragonliu on 2017/8/27.
 */

public class OrganizationClient implements IClient<OrganizationDTO> {
    private static final String ORGANIZATION_PATH=PropertiesProcessor.serverUrl+"/organization";
    private static HttpHeaders headers = new HttpHeaders();
    private static RestTemplate restTemplate = RestTemplateFactory.getRestTemplate();;
    static {
        headers.setContentType(MediaType.APPLICATION_JSON);
    }

    public List<OrganizationDTO> getList(){
        String url = ORGANIZATION_PATH+"/getAll";
        HttpEntity<String> requestEntity = new HttpEntity<String>(headers);

        ResponseEntity<Organization[]> responseEntity = restTemplate.exchange(url, HttpMethod.GET, requestEntity,Organization[].class);
        Organization[] organizations = responseEntity.getBody();
        List<OrganizationDTO> organizationList = new ArrayList<>();
        Arrays.stream(organizations).forEach(p->organizationList.add(BeanMapper.map(p, OrganizationDTO.class)));

        return organizationList;
    }


    public OrganizationDTO add(OrganizationDTO organizationDTO){
        String url = ORGANIZATION_PATH+"/add";
        HttpEntity<Organization> requestEntity = new HttpEntity<Organization>(BeanMapper.map(organizationDTO, Organization.class), headers);
        ResponseEntity<Organization> responseEntity = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Organization.class);
        return BeanMapper.map(responseEntity.getBody(), OrganizationDTO.class);
    }

    public OrganizationDTO getByName(String name){
        String url = ORGANIZATION_PATH+"/getByName"+"?name="+name;
        HttpEntity<String> requestEntity = new HttpEntity<String>(headers);

        ResponseEntity<Organization> responseEntity = restTemplate.exchange(url, HttpMethod.GET, requestEntity,Organization.class);
        return BeanMapper.map(responseEntity.getBody(), OrganizationDTO.class);
    }

    public String deleteByName(String name){
        String url = ORGANIZATION_PATH+"/deleteObject"+"?name="+name;
        HttpEntity<String> requestEntity = new HttpEntity<String>(headers);

        ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.GET, requestEntity,String.class);
        return responseEntity.getBody().replace("\"", "");
    }

    public OrganizationDTO update(OrganizationDTO organizationDTO){
        String url = ORGANIZATION_PATH+"/update";
        HttpEntity<Organization> requestEntity = new HttpEntity<Organization>(BeanMapper.map(organizationDTO, Organization.class), headers);
        ResponseEntity<Organization> responseEntity = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Organization.class);
        return BeanMapper.map(responseEntity.getBody(), OrganizationDTO.class);
    }

    @Override
    public OrganizationDTO getById(Integer id) {
        return null;
    }
}
