package com.noofinc.dsm.webapi.client.filestation.upload;

import com.noofinc.dsm.webapi.client.AbstractTest;
import com.noofinc.dsm.webapi.client.filestation.common.OverwriteBehavior;
import com.noofinc.dsm.webapi.client.filestation.exception.FileAlreadyExistsException;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;

public class UploadServiceTest extends AbstractTest {

    @Autowired
    private UploadService uploadService;

    @Test
    public void testUpload() throws Exception {
        String fileName = "test-file.txt";
        long currentTimeMillis = System.currentTimeMillis();
        String originalContent = "this is a test file with strsing content\nHelloWorld!\n";
        UploadRequest uploadRequest = UploadRequest
                .createBuilder("/noofinc-ws-it/createFiles" + currentTimeMillis, fileName, originalContent.getBytes("UTF-8"))
                .createParents(true).creationTime(LocalDateTime.of(1984, 3, 9, 10, 0))
                .lastAccessTime(LocalDateTime.of(1984, 3, 9, 10, 0))
                .lastModificationTime(LocalDateTime.of(1984, 3, 9, 10, 0)).build();
        uploadService.uploadFile(uploadRequest);

        File f = new File(getShareMountPoint() + "/createFiles" + currentTimeMillis + "/" + fileName);
        int seconds = 0;
        // check for existence up to 10 sec
        while (!f.exists() && seconds < 10) {
            try {
                Thread.sleep(1000L);
            } catch (InterruptedException e) {
            }
            seconds++;
        }

        assertTrue(f.exists());
        assertFalse(f.isDirectory());
        assertEquals(originalContent, FileUtils.readFileToString(f));
    }

    @Test
    public void testUploadOverwrite() throws UnsupportedEncodingException, IOException {
        String fileName = "test-file.txt";
        long currentTimeMillis = System.currentTimeMillis();
		String originalContent = "this is a test file with strsing content\nHelloWorld!\n";
        UploadRequest uploadRequest = UploadRequest
                .createBuilder("/noofinc-ws-it/createFiles" + currentTimeMillis, fileName, originalContent.getBytes("UTF-8"))
                .createParents(true)
                .creationTime(LocalDateTime.of(1984, 3, 9, 10, 0))
                .lastAccessTime(LocalDateTime.of(1984, 3, 9, 10, 0))
                .lastModificationTime(LocalDateTime.of(1984, 3, 9, 10, 0))
                .build();
        uploadService.uploadFile(uploadRequest);

        File f = new File(getShareMountPoint() + "/createFiles" + currentTimeMillis + "/" + fileName);
        int seconds = 0;
        //check for existence up to 10 sec
        while (!f.exists()  && seconds < 10) {
            try { Thread.sleep(1000L); } catch (InterruptedException e) { }
            seconds++;
        }

        assertTrue(f.exists());
        assertFalse(f.isDirectory());
        assertEquals(originalContent, FileUtils.readFileToString(f));

        long originalModified = f.lastModified();

        String newContent = "New content!";
        UploadRequest secondUploadRequest = UploadRequest
                .createBuilder("/noofinc-ws-it/createFiles" + currentTimeMillis, fileName, newContent.getBytes("UTF-8"))
                .createParents(true)
                .overwriteBehavior(OverwriteBehavior.OVERWRITE)
                .build();
        uploadService.uploadFile(secondUploadRequest);

        try { Thread.sleep(5000L); } catch (InterruptedException e) { }

        assertTrue(f.exists());
        assertFalse(f.isDirectory());
        assertTrue(f.lastModified() > originalModified);
        assertEquals(newContent, FileUtils.readFileToString(f));
    }

    @Test
    public void testUploadOverwriteSkip() throws UnsupportedEncodingException, IOException {
        String fileName = "test-file.txt";
        long currentTimeMillis = System.currentTimeMillis();
		String originalContent = "this is a test file with strsing content\nHelloWorld!\n";
        UploadRequest uploadRequest = UploadRequest
                .createBuilder("/noofinc-ws-it/createFiles" + currentTimeMillis, fileName, originalContent.getBytes("UTF-8"))
                .createParents(true)
                .creationTime(LocalDateTime.of(1984, 3, 9, 10, 0))
                .lastAccessTime(LocalDateTime.of(1984, 3, 9, 10, 0))
                .lastModificationTime(LocalDateTime.of(1984, 3, 9, 10, 0))
                .build();
        uploadService.uploadFile(uploadRequest);

        File f = new File(getShareMountPoint() + "/createFiles" + currentTimeMillis + "/" + fileName);
        int seconds = 0;
        //check for existence up to 10 sec
        while (!f.exists()  && seconds < 10) {
            try { Thread.sleep(1000L); } catch (InterruptedException e) { }
            seconds++;
        }

        assertTrue(f.exists());
        assertFalse(f.isDirectory());
        assertEquals(originalContent, FileUtils.readFileToString(f));

        long originalModified = f.lastModified();

        String newContent = "New content!";
        UploadRequest secondUploadRequest = UploadRequest
                .createBuilder("/noofinc-ws-it/createFiles" + currentTimeMillis, fileName, newContent.getBytes("UTF-8"))
                .createParents(true)
                .overwriteBehavior(OverwriteBehavior.SKIP)
                .build();
        uploadService.uploadFile(secondUploadRequest);

        try { Thread.sleep(5000L); } catch (InterruptedException e) { }

        assertTrue(f.exists());
        assertFalse(f.isDirectory());
        assertEquals(f.lastModified(), originalModified);
        assertEquals(originalContent, FileUtils.readFileToString(f));
    }

    @Test(expected = FileAlreadyExistsException.class)
    public void testUploadOverwriteError() throws UnsupportedEncodingException, IOException {
        String fileName = "test-file.txt";
        long currentTimeMillis = System.currentTimeMillis();
		String originalContent = "this is a test file with strsing content\nHelloWorld!\n";
        UploadRequest uploadRequest = UploadRequest
                .createBuilder("/noofinc-ws-it/createFiles" + currentTimeMillis, fileName, originalContent.getBytes("UTF-8"))
                .createParents(true)
                .creationTime(LocalDateTime.of(1984, 3, 9, 10, 0))
                .lastAccessTime(LocalDateTime.of(1984, 3, 9, 10, 0))
                .lastModificationTime(LocalDateTime.of(1984, 3, 9, 10, 0))
                .build();
        uploadService.uploadFile(uploadRequest);

        File f = new File(getShareMountPoint() + "/createFiles" + currentTimeMillis + "/" + fileName);
        int seconds = 0;
        //check for existence up to 10 sec
        while (!f.exists()  && seconds < 10) {
            try { Thread.sleep(1000L); } catch (InterruptedException e) { }
            seconds++;
        }

        assertTrue(f.exists());
        assertFalse(f.isDirectory());
        assertEquals(originalContent, FileUtils.readFileToString(f));

        String newContent = "New content!";
        UploadRequest secondUploadRequest = UploadRequest
                .createBuilder("/noofinc-ws-it/createFiles" + currentTimeMillis, fileName, newContent.getBytes("UTF-8"))
                .createParents(true)
                .build();
        uploadService.uploadFile(secondUploadRequest);
    }

    @Test
    public void testFtpUpload() {
        String fileName = "uploadTestFileFtp.txt";
        uploadService.uploadFtpFile("/noofinc-ws-it", fileName, "Upload testFtpUpload file contents".getBytes());


        File f = new File(getShareMountPoint() + "/" + fileName);
        int seconds = 0;
        //check for existence up to 10 sec
        while (!f.exists()  && seconds < 10) {
            try { Thread.sleep(1000L); } catch (InterruptedException e) { }
            seconds++;
        }
        assertTrue(f.exists());
        assertFalse(f.isDirectory());
    }

    @Test
    public void testUploadFileWithFtpFailOver() {
        String fileName = "uploadTestFileFtpFailover.txt";
        uploadService.uploadFileWithFtpFailOver("/noofinc-ws-it", fileName, "Upload testUploadFileWithFtpFailOver file contents".getBytes());

        File f = new File(getShareMountPoint() + "/" + fileName);
        int seconds = 0;
        //check for existence up to 10 sec
        while (!f.exists()  && seconds < 10) {
            try { Thread.sleep(1000L); } catch (InterruptedException e) { }
            seconds++;
        }
        assertTrue(f.exists());
        assertFalse(f.isDirectory());
    }
}
