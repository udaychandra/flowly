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

package io.flowly.core.security;

import io.vertx.core.file.FileSystem;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.KeyStore;

/**
 * Store and retrieve sensitive data as key value pair to/from the key store.
 *
 * @author <a>Uday Tatiraju</a>
 */
public class Vault {
    public static final String PKCS12_KEY_STORE = "PKCS12";
    public static final String PBE_SECRET_KEY_FACTORY = "PBE";

    private static final Logger logger = LoggerFactory.getLogger(Vault.class);

    private KeyStore keyStore;
    private SecretKeyFactory secretKeyFactory;
    private KeyStore.PasswordProtection passwordProtection;
    private String vaultKey;
    private String keyStoreFilePath;
    private FileSystem fileSystem;

    /**
     * Initialize a key store and key factory.
     *
     * @param vaultKey the key used to check the integrity of the key store.
     * @param keyStoreFilePath the absolute path of the key store on the file system.
     * @param fileSystem vert.x file system.
     */
    public Vault(String vaultKey, String keyStoreFilePath, FileSystem fileSystem) {
        try {
            this.vaultKey = vaultKey;
            this.keyStoreFilePath = keyStoreFilePath;
            this.fileSystem = fileSystem;

            keyStore = KeyStore.getInstance(PKCS12_KEY_STORE);
            secretKeyFactory = SecretKeyFactory.getInstance(PBE_SECRET_KEY_FACTORY);
            passwordProtection = new KeyStore.PasswordProtection(vaultKey.toCharArray());
        }
        catch (Exception ex) {
            logger.error("Unable to create key store.", ex);
        }
    }

    /**
     * Save the data using the specified key in the key store.
     *
     * @param key used to save and retrieve data.
     * @param data the data to be stored in the key store.
     */
    public boolean saveData(String key, String data) {
        try {
            loadKeyStore();

            SecretKey secretKey = secretKeyFactory.generateSecret(new PBEKeySpec(data.toCharArray()));
            keyStore.setEntry(key, new KeyStore.SecretKeyEntry(secretKey), passwordProtection);
            keyStore.store(new FileOutputStream(keyStoreFilePath), vaultKey.toCharArray());
            return true;
        }
        catch (Exception ex) {
            logger.error("Unable to save key/value pair to key store.", ex);
            return false;
        }
    }

    /**
     * Get the data saved in the key store based on the given key.
     *
     * @param key used to retrieve data from the key store.
     * @return the stored data as a string.
     */
    public String getData(String key) {
        try {
            loadKeyStore();

            KeyStore.SecretKeyEntry secretKeyEntry = (KeyStore.SecretKeyEntry) keyStore.
                    getEntry(key, passwordProtection);
            PBEKeySpec keySpec = (PBEKeySpec) secretKeyFactory.
                    getKeySpec(secretKeyEntry.getSecretKey(), PBEKeySpec.class);

            return new String(keySpec.getPassword());
        }
        catch (Exception ex) {
            logger.error("Unable to get key/value pair from key store.", ex);
            return null;
        }
    }

    private void loadKeyStore() throws Exception {
        if (fileSystem.existsBlocking(keyStoreFilePath)) {
            keyStore.load(new FileInputStream(keyStoreFilePath), vaultKey.toCharArray());
        }
        else {
            fileSystem.createFileBlocking(keyStoreFilePath);
            keyStore.load(null, null);
        }
    }
}
