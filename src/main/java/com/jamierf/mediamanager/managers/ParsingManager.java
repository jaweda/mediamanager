package com.jamierf.mediamanager.managers;

import com.jamierf.mediamanager.io.HttpParser;

import java.util.Collection;

public interface ParsingManager {

    public Collection<? extends HttpParser<?>> getParsers();
}
