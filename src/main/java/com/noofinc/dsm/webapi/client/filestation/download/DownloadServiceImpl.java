package com.noofinc.dsm.webapi.client.filestation.download;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.noofinc.dsm.webapi.client.core.AbstractDsmServiceImpl;
import com.noofinc.dsm.webapi.client.core.DsmWebapiRequest;
import com.noofinc.dsm.webapi.client.filestation.exception.CouldNotConvertToStringException;
import com.noofinc.dsm.webapi.client.filestation.exception.FileNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.Set;

@Component
public class DownloadServiceImpl extends AbstractDsmServiceImpl implements DownloadService {

    // API Infos
    private static final String API_ID = "SYNO.FileStation.Download";
    private static final String API_VERSION = "1";

    // API Methods
    private static final String METHOD_DOWNLOAD = "download";

    // Parameters
    private static final String PARAMETER_MODE = "mode";
    private static final String PARAMETER_PATH = "path";

    // Parameters values
    private static final String PARAMETER_VALUE_OPEN = "open";
    private static final String PARAMETER_VALUE_DOWNLOAD = "download";

    @Autowired
    @Qualifier("downloadRestTemplate")
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    public DownloadServiceImpl() {
        super(API_ID);
    }

    @Override
    public ResponseEntity<Resource> download(String path) {
        return download(DownloadRequest.createBuilder(path).build());
    }

    @Override
    public ResponseEntity<Resource> download(DownloadRequest downloadRequest) {
        String path = null;

        try {
            path = getPath(downloadRequest.getPaths());

            DsmWebapiRequest request = new DsmWebapiRequest(getApiInfo().getApi(), API_VERSION, getApiInfo().getPath(), METHOD_DOWNLOAD)
                    .parameter(PARAMETER_PATH, path);
            
            switch (downloadRequest.getDownloadMode()) {
                case DOWNLOAD:
                    request.parameter(PARAMETER_MODE, PARAMETER_VALUE_DOWNLOAD);
                    break;
                case OPEN:
                    request.parameter(PARAMETER_MODE, PARAMETER_VALUE_OPEN);
                    break;
            }
            
            URI uri = getDsmWebapiClient().buildUri(request);
            return restTemplate.getForEntity(uri, Resource.class);
        } catch (JsonProcessingException e) {
            throw new CouldNotConvertToStringException(e);
        } catch (HttpClientErrorException e) {
            throw new FileNotFoundException(e, path);
        }
    }

    private String getPath(Set<String> paths) throws JsonProcessingException {
        return objectMapper.writeValueAsString(paths);
    }
}
