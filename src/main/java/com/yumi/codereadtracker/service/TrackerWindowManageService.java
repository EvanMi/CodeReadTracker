package com.yumi.codereadtracker.service;

import com.intellij.openapi.components.Service;
import com.yumi.codereadtracker.window.TrackerWindow;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public final class TrackerWindowManageService {
    private ConcurrentHashMap<String, TrackerWindow> project2WindowMap = new ConcurrentHashMap<>();

    public void add(String key, TrackerWindow trackerWindow) {
        this.project2WindowMap.put(key, trackerWindow);
    }

    public Optional<TrackerWindow> get(String key) {
        return Optional.ofNullable(project2WindowMap.get(key));
    }

    public void del(String key) {
        project2WindowMap.remove(key);
    }
}
