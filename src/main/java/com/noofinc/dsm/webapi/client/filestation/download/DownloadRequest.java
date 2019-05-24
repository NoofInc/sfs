package com.noofinc.dsm.webapi.client.filestation.download;

import java.util.HashSet;
import java.util.Set;

import com.noofinc.dsm.webapi.client.filestation.common.DownloadMode;

public class DownloadRequest {

    private Set<String> paths;
    private DownloadMode downloadMode = DownloadMode.OPEN;

    private DownloadRequest(String... paths) {
        this.paths = new HashSet<>();
        
        for (String path : paths) {
            this.paths.add(path);
        }
    }

    private DownloadRequest(DownloadRequest downloadRequest) {
        this.paths = downloadRequest.paths;
        this.downloadMode = downloadRequest.downloadMode;
    }

    public Set<String> getPaths() {
        return paths;
    }

    public void setPaths(Set<String> paths) {
        this.paths = paths;
    }

    public DownloadMode getDownloadMode() {
        return downloadMode;
    }

    public void setDownloadMode(DownloadMode downloadMode) {
        this.downloadMode = downloadMode;
    }

    public static DownloadRequestBuilder createBuilder(String... paths) {
        return new DownloadRequestBuilder(paths);
    }

    public static class DownloadRequestBuilder {

        private DownloadRequest template;

        private DownloadRequestBuilder(String... paths) {
            this.template = new DownloadRequest(paths);
        }

        public DownloadRequestBuilder addPath(String path) {
            template.paths.add(path);
            return this;
        }

        public DownloadRequestBuilder downloadMode(DownloadMode downloadMode) {
            template.downloadMode = downloadMode;
            return this;
        }

        public DownloadRequest build() {
            return new DownloadRequest(template);
        }
    }
}
