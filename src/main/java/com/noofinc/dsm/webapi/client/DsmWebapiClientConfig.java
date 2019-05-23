package com.noofinc.dsm.webapi.client;

import java.util.Arrays;
import java.util.Collections;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.noofinc.dsm.webapi.client.core.AuthenticatedDsmWebapiClient;
import com.noofinc.dsm.webapi.client.core.DsmWebapiClient;
import com.noofinc.dsm.webapi.client.core.DsmWebapiClientImpl;
import com.noofinc.dsm.webapi.client.core.LoggingInterceptor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.ResourceHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

@Configuration
@ComponentScan
public class DsmWebapiClientConfig {

    @Bean
    public DsmWebapiClient unauthenticated() {
        return new DsmWebapiClientImpl();
    }

    @Bean
    @Primary
    public DsmWebapiClient dsmRestClient() {
        return new AuthenticatedDsmWebapiClient();
    }

    @Bean
    public RestTemplate dsmWebapiClientRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        MappingJackson2HttpMessageConverter jsonMessageConverter = new MappingJackson2HttpMessageConverter();
        jsonMessageConverter.setSupportedMediaTypes(Collections.singletonList(MediaType.ALL));
        restTemplate.setMessageConverters(Collections.singletonList(jsonMessageConverter));
        restTemplate.setInterceptors(Collections.singletonList(new LoggingInterceptor()));
        restTemplate.setRequestFactory(new BufferingClientHttpRequestFactory(new SimpleClientHttpRequestFactory()));
        return restTemplate;
    }

    @Bean
    public RestTemplate downloadRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        ResourceHttpMessageConverter resourceHttpMessageConverter = new ResourceHttpMessageConverter();
        restTemplate.setMessageConverters(Collections.singletonList(resourceHttpMessageConverter));
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setBufferRequestBody(false);
        restTemplate.setRequestFactory(factory);
        return restTemplate;
    }

    @Bean
    public RestTemplate uploadRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        FormHttpMessageConverter formHttpMessageConverter = new FormHttpMessageConverter();
        MappingJackson2HttpMessageConverter jsonMessageConverter = new MappingJackson2HttpMessageConverter();
        jsonMessageConverter.setSupportedMediaTypes(Collections.singletonList(MediaType.ALL));
        restTemplate.setMessageConverters(Arrays.asList(formHttpMessageConverter, jsonMessageConverter));
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setBufferRequestBody(false);
        restTemplate.setRequestFactory(factory);
        return restTemplate;
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
