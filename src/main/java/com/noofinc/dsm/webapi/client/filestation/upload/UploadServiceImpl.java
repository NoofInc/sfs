package com.noofinc.dsm.webapi.client.filestation.upload;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.noofinc.dsm.webapi.client.core.AbstractDsmServiceImpl;
import com.noofinc.dsm.webapi.client.core.DsmUrlProvider;
import com.noofinc.dsm.webapi.client.core.DsmWebapiResponse;
import com.noofinc.dsm.webapi.client.core.authentication.AuthenticationHolder;
import com.noofinc.dsm.webapi.client.core.exception.DsmWebApiClientException;
import com.noofinc.dsm.webapi.client.core.timezone.TimeZoneUtil;
import com.noofinc.dsm.webapi.client.filestation.common.OverwriteBehavior;
import com.noofinc.dsm.webapi.client.filestation.exception.FileAlreadyExistsException;
import com.noofinc.dsm.webapi.client.filestation.exception.FileNotFoundException;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Could not succeed to implement this with RestTemplate
 * Implemented with Apache's HttpClient and Jackson's object mapper
 */
@Component
public class UploadServiceImpl extends AbstractDsmServiceImpl implements UploadService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UploadServiceImpl.class);

    private static final String API_ID = "SYNO.FileStation.Upload";

    @Value("${dsm.webapi.username}")
    private String username;
    @Value("${dsm.webapi.password}")
    private String password;
    @Value("${dsm.webapi.host}")
    private String host;

    @Autowired
    private DsmUrlProvider dsmUrlProvider;

    @Autowired
    private AuthenticationHolder authenticationHolder;

    @Autowired
    private TimeZoneUtil timeZoneUtil;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    @Qualifier("uploadRestTemplate")
    private RestTemplate restTemplate;

    FTPClient ftp = new FTPClient();

    public UploadServiceImpl() {
        super(API_ID);
    }

    @PostConstruct
    public void initUploadService() {
        try {
            ftp.connect(this.host);
            ftp.login(this.username, this.password);
            ftp.setFileType(FTP.BINARY_FILE_TYPE);
            ftp.enterLocalPassiveMode();
        } catch (IOException e) {
            LOGGER.warn("Could not connect to ftp server.", e);
        }
    }

    @PreDestroy
    public void destroy() {
        try {
            ftp.logout();
            ftp.disconnect();
        } catch (IOException ioe) {
            //try to disconnect but eat it if fail
        }
    }

    @Override
    public void uploadFile(String parentPath, String name, InputStream content) {
        uploadFile(UploadRequest.createBuilder(parentPath, name, content).build());
    }

    @Override
    public void uploadFile(UploadRequest uploadRequest) {
        DsmWebapiResponse response = doUploadRequest(uploadRequest);
        if(!response.isSuccess()) {
            switch (response.getError().getCode()) {
                case 1805:
                    throw new FileAlreadyExistsException(response.getError(), uploadRequest.getParentFolderPath(), uploadRequest.getFileName());
                case 414:
                    throw new FileAlreadyExistsException(response.getError(), uploadRequest.getParentFolderPath(), uploadRequest.getFileName());
                case 408:
                    throw new FileNotFoundException(uploadRequest.getParentFolderPath().toString(), response.getError());
                    // TODO handle other cases
            }
        }
    }


    @Override
    public void uploadFtpFile(String parentPath, String name, InputStream content) {
        try {
            ftp.storeFile("/" + parentPath + "/" + name, content);

        } catch (IOException e) {
            throw new DsmWebApiClientException("Unable to upload file via FTP", e);
        }
    }

    @Override
    public void uploadFileWithFtpFailOver(String parentPath, String name, InputStream content) {
        try {
            uploadFile(parentPath, name, content);
        } catch (Exception e) {
            LOGGER.info("RESTful file upload failed, attempting via FTP");
            uploadFtpFile(parentPath, name, content);
        }
    }

    private DsmWebapiResponse doUploadRequest(UploadRequest uploadRequest) {
        try {            
            Resource content = new InputStreamResource(uploadRequest.getContent());
            HttpHeaders fileHeaders = new HttpHeaders();
            fileHeaders.setContentDispositionFormData("file", uploadRequest.getFileName());

            HttpEntity<Resource> fileEntity = new HttpEntity<>(content, fileHeaders);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", fileEntity);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<DsmWebapiResponse> response = restTemplate.exchange(createUrl(uploadRequest), HttpMethod.PUT, requestEntity, DsmWebapiResponse.class);
            
            LOGGER.debug("Response body: {}", objectMapper.writeValueAsString(response.getBody()));

            return response.getBody();

        } catch (Exception e) {
            throw new DsmWebApiClientException("Could not upload file.", e);
        }
    }

    private String createUrl(UploadRequest uploadRequest) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(dsmUrlProvider.getDsmUrl())
                .path("/webapi/entry.cgi")
                .queryParam("api", getApiId())
                .queryParam("version", "2")
                .queryParam("method", "upload")
                .queryParam("_sid", authenticationHolder.getLoginInformation().getSid())
                .queryParam("path", uploadRequest.getParentFolderPath())
                .queryParam("create_parents", Boolean.toString(uploadRequest.isCreateParents()));

        appendOverwriteBehaviorIfNeeded(builder, uploadRequest.getOverwriteBehavior());
        appendTimeParameterIfNeeded(builder, "mtime", uploadRequest.getLastModificationTime());
        appendTimeParameterIfNeeded(builder, "crtime", uploadRequest.getCreationTime());
        appendTimeParameterIfNeeded(builder, "atime", uploadRequest.getLastAccessTime());

        String url = builder.toUriString();

        LOGGER.debug("Upload Request URL: {}", url);

        return url;
    }

    private void appendOverwriteBehaviorIfNeeded(UriComponentsBuilder builder, OverwriteBehavior overwriteBehavior) {
        switch (overwriteBehavior) {
            case OVERWRITE:
                builder.queryParam("overwrite", "true");
                break;
            case SKIP:
                builder.queryParam("overwrite", "false");
                break;
            case ERROR:
                // Default behavior: no parameter to add
                break;
            default:
                throw new AssertionError("Cannot happen");
        }
    }

    private void appendTimeParameterIfNeeded(UriComponentsBuilder builder, String parameterName, Optional<LocalDateTime> time) {
        if(time.isPresent()) {
            long unixTime = time.get().toEpochSecond(timeZoneUtil.getZoneOffset()) * 1000;
            builder.queryParam(parameterName, Long.toString(unixTime));
        }
    }

}
