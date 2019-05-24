package com.noofinc.dsm.webapi.client.filestation.upload;

import java.io.InputStream;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.noofinc.dsm.webapi.client.core.AbstractDsmServiceImpl;
import com.noofinc.dsm.webapi.client.core.DsmWebapiRequest;
import com.noofinc.dsm.webapi.client.core.DsmWebapiResponse;
import com.noofinc.dsm.webapi.client.core.exception.DsmWebApiClientException;
import com.noofinc.dsm.webapi.client.core.timezone.TimeZoneUtil;
import com.noofinc.dsm.webapi.client.filestation.common.OverwriteBehavior;
import com.noofinc.dsm.webapi.client.filestation.exception.FileAlreadyExistsException;
import com.noofinc.dsm.webapi.client.filestation.exception.FileNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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

@Component
public class UploadServiceImpl extends AbstractDsmServiceImpl implements UploadService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UploadServiceImpl.class);

    // API Infos
    private static final String API_ID = "SYNO.FileStation.Upload";
    private static final String API_VERSION = "2";

    // API Methods
    private static final String METHOD_UPLOAD = "upload";

    // Parameters
    private static final String PARAMETER_PATH = "path";
    private static final String PARAMETER_CREATE_PARENTS = "create_parents";
    private static final String PARAMETER_OVERWRITE = "overwrite";
    private static final String PARAMETER_MODIFIED_TIME = "mtime";
    private static final String PARAMETER_CREATED_TIME = "crtime";
    private static final String PARAMETER_ACCESSED_TIME = "atime";

    // Parameters values
    private static final String PARAMETER_VALUE_OVERWRITE = "true";
    private static final String PARAMETER_VALUE_SKIP = "false";
    
    @Autowired
    private TimeZoneUtil timeZoneUtil;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    @Qualifier("uploadRestTemplate")
    private RestTemplate restTemplate;

    public UploadServiceImpl() {
        super(API_ID);
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

            ResponseEntity<DsmWebapiResponse> response = restTemplate.exchange(createUri(uploadRequest), HttpMethod.PUT, requestEntity, DsmWebapiResponse.class);
            
            LOGGER.debug("Response body: {}", objectMapper.writeValueAsString(response.getBody()));

            return response.getBody();

        } catch (Exception e) {
            throw new DsmWebApiClientException("Could not upload file.", e);
        }
    }

    private URI createUri(UploadRequest uploadRequest) {
        DsmWebapiRequest request = new DsmWebapiRequest(getApiInfo().getApi(), API_VERSION, getApiInfo().getPath(), METHOD_UPLOAD)
                .parameter(PARAMETER_PATH, uploadRequest.getParentFolderPath())
                .parameter(PARAMETER_CREATE_PARENTS, Boolean.toString(uploadRequest.isCreateParents()));

        appendOverwriteBehaviorIfNeeded(request, uploadRequest.getOverwriteBehavior());
        appendTimeParameterIfNeeded(request, PARAMETER_MODIFIED_TIME, uploadRequest.getLastModificationTime());
        appendTimeParameterIfNeeded(request, PARAMETER_CREATED_TIME, uploadRequest.getCreationTime());
        appendTimeParameterIfNeeded(request, PARAMETER_ACCESSED_TIME, uploadRequest.getLastAccessTime());

        URI uri = getDsmWebapiClient().buildUri(request);

        LOGGER.debug("Upload Request URI: {}", uri);

        return uri;
    }

    private void appendOverwriteBehaviorIfNeeded(DsmWebapiRequest request, OverwriteBehavior overwriteBehavior) {
        switch (overwriteBehavior) {
            case OVERWRITE:
                request.parameter(PARAMETER_OVERWRITE, PARAMETER_VALUE_OVERWRITE);
                break;
            case SKIP:
                request.parameter(PARAMETER_OVERWRITE, PARAMETER_VALUE_SKIP);
                break;
            case ERROR:
                // Default behavior: no parameter to add
                break;
        }
    }

    private void appendTimeParameterIfNeeded(DsmWebapiRequest request, String parameterName, Optional<LocalDateTime> time) {
        if(time.isPresent()) {
            long unixTime = time.get().toEpochSecond(timeZoneUtil.getZoneOffset()) * 1000;
            request.parameter(parameterName, Long.toString(unixTime));
        }
    }

}
