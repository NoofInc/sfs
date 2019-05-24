package com.noofinc.dsm.webapi.client.filestation.download;

import com.noofinc.dsm.webapi.client.AbstractTest;
import com.noofinc.dsm.webapi.client.filestation.common.DownloadMode;
import com.noofinc.dsm.webapi.client.filestation.exception.FileNotFoundException;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;

import java.io.IOException;

public class DownloadServiceTest extends AbstractTest {

    @Autowired
    private DownloadService downloadService;

    @Test
    public void testDownloadTextFile() throws IOException {
        ResponseEntity<Resource> download = downloadService.download("/noofinc-ws-it/test-1/test-text-file2.txt");

        Assert.assertEquals("This is a test file", IOUtils.toString(download.getBody().getInputStream()));
    }

    @Test
    public void testDownloadTextFileDownloadMode() throws IOException {
        ResponseEntity<Resource> download = downloadService.download(
            DownloadRequest.createBuilder()
                .addPath("/noofinc-ws-it/test-1/test-text-file2.txt")
                .downloadMode(DownloadMode.DOWNLOAD)
                .build()
        );

        Assert.assertEquals(MediaType.APPLICATION_OCTET_STREAM, download.getHeaders().getContentType());
        Assert.assertEquals("This is a test file", IOUtils.toString(download.getBody().getInputStream()));
    }

    @Test
    public void testDownloadTextFileOpenMode() throws IOException {
        ResponseEntity<Resource> download = downloadService.download(
            DownloadRequest.createBuilder()
                .addPath("/noofinc-ws-it/test-1/test-text-file2.txt")
                .downloadMode(DownloadMode.OPEN)
                .build()
        );

        Assert.assertEquals(MediaType.TEXT_PLAIN, download.getHeaders().getContentType());
        Assert.assertEquals("This is a test file", IOUtils.toString(download.getBody().getInputStream()));
    }
    
    @Test
    public void testDownloadMultipleFiles() throws IOException {
        ResponseEntity<Resource> download = downloadService.download(
            DownloadRequest.createBuilder()
                .addPath("/noofinc-ws-it/test-1/test-text-file2.txt")
                .addPath("/noofinc-ws-it/test-1/test-sub-directory/test-text-file3.txt")
                .build()
        );

        Assert.assertEquals("application/zip", download.getHeaders().getContentType().toString());
        Assert.assertEquals(462, download.getBody().contentLength());
    }

    @Test
    public void testDownloadTextPdfFile() throws IOException {
        ResponseEntity<Resource> download = downloadService.download("/noofinc-ws-it/test-2/Test_document_PDF.pdf");
        byte[] downloaded = IOUtils.toByteArray(download.getBody().getInputStream());
        byte[] expected = StreamUtils.copyToByteArray(DownloadServiceTest.class.getResourceAsStream("/file-resources/test-2/Test_document_PDF.pdf"));
        Assert.assertEquals(expected.length, downloaded.length);
        for (int i = 0; i < downloaded.length; i++) {
            Assert.assertEquals(expected[i], downloaded[i]);
        }
    }

    @Test(expected = FileNotFoundException.class)
    public void testDownloadInexistingFile() {
        downloadService.download("/noofinc-ws-it/test-1/inexisting.txt");
    }
}
