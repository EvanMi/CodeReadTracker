/*
 * Copyright [2024] [EvanMi]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
