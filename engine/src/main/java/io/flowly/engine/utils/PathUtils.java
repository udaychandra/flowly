/*
 * Copyright (c) 2015 The original author or authors.
 *
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Apache License v2.0
 *  which accompanies this distribution.
 *
 *  The Apache License v2.0 is available at
 *  http://opensource.org/licenses/Apache-2.0
 *
 *  You may elect to redistribute this code under this license.
 */

package io.flowly.engine.utils;

import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.io.File;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility methods to create and manipulate file paths.
 *
 * @author <a>Uday Tatiraju</a>
 */
public class PathUtils {
    public static final String PROCESSES_FOLDER = "processes";
    public static final String INTERACTIVE_SERVICES_FOLDER = "interactiveServices";
    public static final String MICRO_SERVICES_FOLDER = "microServices";
    public static final String SCRIPTS_FOLDER = "scripts";
    public static final String VERTICLES_FOLDER = "verticles";

    public static final String DOT = ".";
    public static final String DOT_JS_SUFFIX = ".js";
    public static final String DOT_JSON_SUFFIX = ".json";
    public static final String APP_CONFIG_FILE_NAME = "app.json";

    private static final Logger logger = LoggerFactory.getLogger(PathUtils.class);

    /**
     * Concatenate all tokens using the file separator to create a path.
     * Adds a file separator to the beginning of the path.
     *
     * @param tokens the tokens that are to be joined to create the relative path.
     * @return a relative file path.
     */
    public static String createPathWithPrefix(String... tokens) {
        return File.separator + createPath(false, tokens);
    }

    /**
     * Concatenate all tokens using the file separator to create a path.
     * Adds a file separator to the beginning of the path.
     *
     * @param skipEmpty empty or null tokens will be ignored during path creation.
     * @param tokens the tokens that are to be joined to create the relative path.
     * @return a relative file path.
     */
    public static String createPathWithPrefix(boolean skipEmpty, String... tokens) {
        return File.separator + createPath(skipEmpty, tokens);
    }

    /**
     * Concatenate all tokens using the file separator to create a path.
     *
     * @param tokens the tokens that are to be joined to create the path.
     * @return a file path.
     */
    public static String createPath(String... tokens) {
        return createPath(false, tokens);
    }

    /**
     * Concatenate all tokens using the file separator to create a path.
     *
     * @param skipEmpty empty or null tokens will be ignored during path creation.
     * @param tokens the tokens that are to be joined to create the path.
     * @return a file path.
     */
    public static String createPath(boolean skipEmpty, String... tokens) {
        StringBuilder path = new StringBuilder();

        for (int i=0; i<tokens.length - 1; i++) {
            appendToken(path, tokens[i], skipEmpty, false);
        }

        appendToken(path, tokens[tokens.length - 1], skipEmpty, true);
        return path.toString();
    }

    public static List<Path> findAppConfigFilePaths(String appsDirectory) {
        final List<Path> appConfigFilePaths = new ArrayList<>();

        try {
            Path startPath = Paths.get(appsDirectory);
            Files.walkFileTree(startPath, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
                    if (dir.endsWith(PROCESSES_FOLDER) || dir.endsWith(INTERACTIVE_SERVICES_FOLDER) ||
                            dir.endsWith(MICRO_SERVICES_FOLDER) || dir.endsWith(SCRIPTS_FOLDER) ||
                            dir.endsWith(VERTICLES_FOLDER)) {
                        return FileVisitResult.SKIP_SUBTREE;
                    }

                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                    if (file.endsWith(APP_CONFIG_FILE_NAME)){
                        logger.info("Found app file at: " + file);
                        appConfigFilePaths.add(file);

                        return FileVisitResult.SKIP_SIBLINGS;
                    }

                    return FileVisitResult.CONTINUE;
                }
            });
        }
        catch (Exception ex) {
            appConfigFilePaths.clear();
            logger.error("Unable to scan directory.", ex);
        }

        return appConfigFilePaths;
    }

    private static void appendToken(StringBuilder path, String token, boolean skipEmpty, boolean skipSeparator) {
        if (skipEmpty && (token == null || token.length() == 0)) {
            // If this is the last token, remove the leading file separator
            if (skipSeparator) {
                path.replace(path.length() - 1, path.length(), "");
            }
            return;
        }

        path.append(token);
        if (!skipSeparator) path.append(File.separator);
    }
}
