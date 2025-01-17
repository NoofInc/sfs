package com.noofinc.dsm.webapi.client.filestation.upload;

import java.io.InputStream;

public interface UploadService {

    void uploadFile(String parentPath, String name, InputStream content);

    void uploadFile(UploadRequest uploadRequest);

}
