/*
 * Copyright 2017-2021 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.testresources.proxy;

import io.micronaut.context.ApplicationContext;
import io.micronaut.context.ApplicationContextBuilder;
import io.micronaut.context.ApplicationContextConfigurer;
import io.micronaut.context.annotation.ContextConfigurer;
import io.micronaut.runtime.Micronaut;
import io.micronaut.runtime.server.EmbeddedServer;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.util.Arrays;

/**
 * Main entry point for the proxy.
 */
@Singleton
public class Application {
    private static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        ApplicationContext context = Micronaut.run(Application.class, args);
        Arrays.stream(args)
            .filter(arg -> arg.startsWith("--port-file="))
            .findFirst()
            .map(arg -> arg.substring("--port-file=".length()))
            .ifPresent(portFile -> {
                try (FileWriter writer = new FileWriter(portFile)) {
                    EmbeddedServer server = context.getBean(EmbeddedServer.class);
                    int port = server.getPort();
                    writer.write(String.valueOf(port));
                    LOGGER.debug("Wrote port {} to {}", port, portFile);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
    }

    /**
     * The application context configurer.
     */
    @ContextConfigurer
    public static class Configurer implements ApplicationContextConfigurer {
        @Override
        public void configure(ApplicationContextBuilder builder) {
            builder.packages("io.micronaut.testresources.proxy")
                .deduceEnvironment(false)
                .banner(false);
        }
    }
}
