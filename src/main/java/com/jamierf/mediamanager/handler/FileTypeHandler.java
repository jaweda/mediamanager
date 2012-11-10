package com.jamierf.mediamanager.handler;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

public interface FileTypeHandler extends FileHandler {
	public Collection<String> getHandledExtensions();
}
