package com.jamierf.mediamanager.handler;

import java.util.Collection;

public interface FileTypeHandler extends FileHandler {
	public Collection<String> getHandledExtensions();
}
