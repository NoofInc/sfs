package com.noofinc.dsm.webapi.client.filestation.download;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;

public interface DownloadService {

    ResponseEntity<Resource> download(String path);

    ResponseEntity<Resource> download(DownloadRequest downloadRequest);
}
