package org.acwar.impersonator.service;

import org.acwar.impersonator.enums.IntratimeCommandsEnum;
import org.acwar.impersonator.beans.IntratimeInOutBean;
import org.acwar.impersonator.beans.IntratimeUser;
import org.acwar.impersonator.configuration.IntratimeProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

@Service
public class IntratimeServiceImpl implements IntratimeService {
    private Logger log = LoggerFactory.getLogger(IntratimeServiceImpl.class);

    @Autowired
    private IntratimeProperties properties;
    @Autowired
    private RestTemplate template;

    @Override
    public IntratimeInOutBean launchCommand(Date commandDate, IntratimeCommandsEnum command) {
        log.debug("Attempting command " + command.toString());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.set("token", doLogin().getUSER_TOKEN());
        headers.set("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.117 Safari/537.36");

        MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
        map.add("user_action", command.getCommandParam());
        map.add("user_timestamp", formatDate(new Date()));
        map.add("user_use_server_time", "true");
        map.add("user_gps_coordinates", "40.449355,-3.575887");
        map.add("inout_device_id", "2");
        map.add("expense_amount", "0");
        map.add("from_web", "true");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(map, headers);
        ResponseEntity<IntratimeInOutBean> response;
        try{
            response = template.postForEntity(properties.getCommandsUrl(), request, IntratimeInOutBean.class);
            return response.getBody();
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    private IntratimeUser doLogin() {
        log.debug("Attempting Login");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<String, String>();
        map.add("user", properties.getUser());
        map.add("pin", properties.getPin());

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<MultiValueMap<String, String>>(map, headers);
        ResponseEntity<IntratimeUser> response = template.postForEntity(properties.getLoginUrl(), request, IntratimeUser.class);

        log.debug("login successfull");
        return response.getBody();
    }

    private String formatDate(Date date) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return dateFormat.format(date);
    }
}