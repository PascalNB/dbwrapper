package com.pascalnb.dbwrapper;

import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.Properties;

/**
 * Implementation of [DatabaseAuthenticator] that reads the credentials from a cfg file.
 *
 * @see DatabaseAuthenticator
 */
class ConfigAuthenticator extends DatabaseAuthenticator {

    private final String[] credentialsArray;

    protected ConfigAuthenticator(String configFile) {
        Properties properties = new Properties();
        try {
            File jarPath = new File(getClass().getProtectionDomain().getCodeSource().getLocation().getPath());
            String propertiesPath = jarPath.getParentFile().getAbsolutePath();
            properties.load(new FileInputStream(propertiesPath + "/" + configFile));
        } catch (FileNotFoundException __) {
            try (InputStream config = getClass().getClassLoader().getResourceAsStream(configFile)) {
                properties.load(config);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        String username = properties.getProperty("username");
        String password = properties.getProperty("password");
        String url = properties.getProperty("host");

        try {
            Class.forName(properties.getProperty("driver"));
        } catch (ClassNotFoundException e) {
            throw new DatabaseException("JDBC driver not loaded");
        }

        this.credentialsArray = new String[]{username, password, url};
    }

    /**
     * Returns the database credentials from the credentials file.
     */
    @NotNull
    @Override
    protected String @NotNull [] getCredentials() {
        return credentialsArray;
    }

}