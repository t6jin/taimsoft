package com.taim.client;

import com.taim.client.util.BeanMapper;
import com.taim.client.util.PropertiesProcessor;
import com.taim.dto.ProductDTO;
import com.taim.dto.StaffDTO;
import com.taim.model.Staff;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by dragonliu on 2017/8/30.
 */
public class StaffClient {
    private static final String STAFF_PATH= PropertiesProcessor.serverUrl+"/staff";

    public List<StaffDTO> getStaffList(){

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        RestTemplate restTemplate = new RestTemplate();
        String url = STAFF_PATH+"/getAll";
        HttpEntity<String> requestEntity = new HttpEntity<String>(headers);

        ResponseEntity<Staff[]> responseEntity = restTemplate.exchange(url, HttpMethod.GET, requestEntity,Staff[].class);
        Staff[] staffs = responseEntity.getBody();
        List<StaffDTO> staffList = new ArrayList<>();
        Arrays.stream(staffs).forEach(p->staffList.add(BeanMapper.map(p, StaffDTO.class)));

        return staffList;
    }


    public StaffDTO addStaff(StaffDTO staffDTO){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        RestTemplate restTemplate = new RestTemplate();
        String url = STAFF_PATH+"/add";
        HttpEntity<Staff> requestEntity = new HttpEntity<Staff>(BeanMapper.map(staffDTO, Staff.class), headers);
        ResponseEntity<Staff> responseEntity = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Staff.class);
        return BeanMapper.map(responseEntity.getBody(), StaffDTO.class);
    }

    public StaffDTO getStaffByName(String name){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        RestTemplate restTemplate = new RestTemplate();
        String url = STAFF_PATH+"/getByName"+"?name="+name;
        HttpEntity<String> requestEntity = new HttpEntity<String>(headers);

        ResponseEntity<Staff> responseEntity = restTemplate.exchange(url, HttpMethod.GET, requestEntity,Staff.class);
        return BeanMapper.map(responseEntity.getBody(), StaffDTO.class);
    }

    public String deleteStaffByName(String name){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        RestTemplate restTemplate = new RestTemplate();
        String url = STAFF_PATH+"/deleteObject"+"?name="+name;
        HttpEntity<String> requestEntity = new HttpEntity<String>(headers);

        ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.GET, requestEntity,String.class);
        return responseEntity.getBody().replace("\"", "");
    }

    public StaffDTO updateStaff(StaffDTO staffDTO){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        RestTemplate restTemplate = new RestTemplate();
        String url = STAFF_PATH+"/update";
        HttpEntity<Staff> requestEntity = new HttpEntity<Staff>(BeanMapper.map(staffDTO, Staff.class), headers);
        ResponseEntity<Staff> responseEntity = restTemplate.exchange(url, HttpMethod.POST, requestEntity, Staff.class);
        return BeanMapper.map(responseEntity.getBody(), StaffDTO.class);
    }
}