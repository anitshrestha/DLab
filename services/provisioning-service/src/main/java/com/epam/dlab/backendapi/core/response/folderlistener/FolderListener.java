/***************************************************************************

Copyright (c) 2016, EPAM SYSTEMS INC

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

****************************************************************************/

package com.epam.dlab.backendapi.core.response.folderlistener;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.epam.dlab.backendapi.core.FileHandlerCallback;
import com.epam.dlab.backendapi.core.commands.DockerCommands;
import com.epam.dlab.exceptions.DlabException;

import io.dropwizard.util.Duration;

public class FolderListener implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(FolderListener.class);

    private final String directory;
    private final Duration timeout;
    private final FileHandlerCallback fileHandlerCallback;
    private final Duration fileLengthCheckDelay;
    //private volatile boolean success;
    private long timeoutExpired;

    public FolderListener(String directory, Duration timeout, FileHandlerCallback fileHandlerCallback, Duration fileLengthCheckDelay) {
        this.directory = directory;
        this.timeout = timeout;
        this.fileHandlerCallback = fileHandlerCallback;
        this.fileLengthCheckDelay = fileLengthCheckDelay;
    }

    @Override
    public void run() {
        pollFile();
    }
    
    
    private boolean waitForDirectory() throws InterruptedException {
    	File file = new File(directory);
		if (file.exists()) {
    		return true;
    	} else {
    		LOGGER.debug("FolderListener for directory {} expect to create it", directory);
    	}

		while (timeoutExpired >= System.currentTimeMillis()) {
    		Thread.sleep(1000);
    		if (file.exists()) {
        		return true;
        	}
    	}
    	return false;
    }

    private void pollFile() {
        Path directoryPath = Paths.get(directory);
        String directoryName = directoryPath.toAbsolutePath().toString();
        boolean fileHandled = false;
    	long timeoutMillis = timeout.toMilliseconds();
    	timeoutExpired = System.currentTimeMillis() + timeoutMillis;
    	
    	try {
    		if (!waitForDirectory()) {
    			LOGGER.error("FolderListener error. Timeout expired and directory {} not exists", directoryName);
    			fileHandlerCallback.handleError();
    			return;
    		}
    	} catch (InterruptedException e) {
    		LOGGER.debug("FolderListener for directory {} has been interrupted", directoryName);
    	}
    	
        LOGGER.debug("Registers a new watcher for directory {} with timeout {} sec", directoryName, timeout.toSeconds());
		try (WatchService watcher = directoryPath.getFileSystem().newWatchService()) {
			directoryPath.register(watcher, ENTRY_CREATE);
			LOGGER.debug("Registered a new watcher for directory {}", directoryName);

			while (true) {
				final WatchKey watchKey = watcher.poll(timeout.toSeconds(), TimeUnit.SECONDS);
				if (watchKey != null) {
					for (WatchEvent<?> watchEvent : watchKey.pollEvents()) {
						final WatchEvent.Kind<?> kind = watchEvent.kind();
						String fileName = watchEvent.context().toString();

						if (kind == ENTRY_CREATE) {
							if (fileHandlerCallback.checkUUID(DockerCommands.extractUUID(fileName))) {
								LOGGER.debug("Folder listener {} handle file {}", directoryName, fileName);
								handleFileAsync(fileName);
								fileHandled = true;
							}
						}
					}
					watchKey.reset();
				}
				if (timeoutExpired < System.currentTimeMillis()) {
					if ( fileHandled ) {
						LOGGER.debug("Timeout expired for FolderListener directory {}", directoryName);
						break;
					} else {
						LOGGER.warn("Timeout expired for FolderListener directory {}", directoryName);
						fileHandlerCallback.handleError();
		    			break;
					}
				}
			}
			LOGGER.debug("Closing a watcher for directory {}", directoryName);
		} catch (InterruptedException e) {
			LOGGER.debug("FolderListener for directory {} has been interrupted", directoryName);
		} catch (Exception e) {
			LOGGER.warn("FolderListenerExecutor exception for folder {}", directoryName, e);
			throw new DlabException("FolderListenerExecutor exception for folder " + directoryName, e);
		}
    }

    private void handleFileAsync(String fileName) {
        CompletableFuture
                .supplyAsync(new AsyncFileHandler(fileName, directory, fileHandlerCallback, fileLengthCheckDelay))
                /*.thenAccept(result -> success = success || result)*/;
    }
}
