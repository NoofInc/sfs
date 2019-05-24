package com.noofinc.dsm.webapi.client.filestation.upload;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;

import com.noofinc.dsm.webapi.client.AbstractTest;
import com.noofinc.dsm.webapi.client.filestation.common.OverwriteBehavior;
import com.noofinc.dsm.webapi.client.filestation.exception.FileAlreadyExistsException;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class UploadServiceTest extends AbstractTest {

    @Autowired
    private UploadService uploadService;

    private final ClassLoader classLoader = getClass().getClassLoader();

    private final String fileName = "test-file.txt";

    private final String originalContent = "this is a test file with string content\nHelloWorld!\n";

    private final String newContent = "this is a test file with new content\nHelloWorld!\n";

    @Test
    public void testUpload() throws Exception {
        long currentTimeMillis = System.currentTimeMillis();
        InputStream file = classLoader.getResourceAsStream("file-resources/upload-test/test-file.txt");

        UploadRequest uploadRequest = UploadRequest
                .createBuilder("/noofinc-ws-it/createFiles" + currentTimeMillis, fileName, file)
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
        long currentTimeMillis = System.currentTimeMillis();
        InputStream file = classLoader.getResourceAsStream("file-resources/upload-test/test-file.txt");

        UploadRequest uploadRequest = UploadRequest
                .createBuilder("/noofinc-ws-it/createFiles" + currentTimeMillis, fileName, file)
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

        InputStream newFile = classLoader.getResourceAsStream("file-resources/upload-test/test-file2.txt");

        UploadRequest secondUploadRequest = UploadRequest
                .createBuilder("/noofinc-ws-it/createFiles" + currentTimeMillis, fileName, newFile)
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
        long currentTimeMillis = System.currentTimeMillis();
        InputStream file = classLoader.getResourceAsStream("file-resources/upload-test/test-file.txt");

        UploadRequest uploadRequest = UploadRequest
                .createBuilder("/noofinc-ws-it/createFiles" + currentTimeMillis, fileName, file)
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

        InputStream newFile = classLoader.getResourceAsStream("file-resources/upload-test/test-file2.txt");

        UploadRequest secondUploadRequest = UploadRequest
                .createBuilder("/noofinc-ws-it/createFiles" + currentTimeMillis, fileName, newFile)
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
        long currentTimeMillis = System.currentTimeMillis();
        InputStream file = classLoader.getResourceAsStream("file-resources/upload-test/test-file.txt");

        UploadRequest uploadRequest = UploadRequest
                .createBuilder("/noofinc-ws-it/createFiles" + currentTimeMillis, fileName, file)
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

        InputStream newFile = classLoader.getResourceAsStream("file-resources/upload-test/test-file2.txt");

        UploadRequest secondUploadRequest = UploadRequest
                .createBuilder("/noofinc-ws-it/createFiles" + currentTimeMillis, fileName, newFile)
                .createParents(true)
                .build();
        uploadService.uploadFile(secondUploadRequest);
    }
}
