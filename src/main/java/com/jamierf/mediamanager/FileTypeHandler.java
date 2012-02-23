package com.jamierf.mediamanager;

import java.io.File;
import java.io.IOException;

public interface FileTypeHandler {

	public String[] getHandledExtensions();
	public void handleFile(String relativePath, File file) throws IOException;
}
