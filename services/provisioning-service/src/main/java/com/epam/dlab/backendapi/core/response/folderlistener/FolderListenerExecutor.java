package com.epam.dlab.backendapi.core.response.folderlistener;

import com.epam.dlab.backendapi.ProvisioningServiceApplicationConfiguration;
import com.epam.dlab.backendapi.core.response.FileHandler;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.dropwizard.util.Duration;

import java.util.concurrent.CompletableFuture;

/**
 * Created by Alexey Suprun
 */
@Singleton
public class FolderListenerExecutor {
    @Inject
    private ProvisioningServiceApplicationConfiguration configuration;


    public void start(String directory, Duration timeout, FileHandler fileHandler) {
        CompletableFuture.runAsync(new FolderListener(directory, timeout, fileHandler, configuration.getFileLengthCheckDelay()));
    }
}