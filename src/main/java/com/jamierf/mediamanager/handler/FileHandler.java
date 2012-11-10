package com.jamierf.mediamanager.handler;

import java.io.File;
import java.io.IOException;

public interface FileHandler {
    public void handleFile(String relativePath, File file) throws IOException;
}
