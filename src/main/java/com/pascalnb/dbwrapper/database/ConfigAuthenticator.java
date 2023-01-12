package com.pascalnb.dbwrapper.database;

import java.io.*;
import java.util.Properties;

/**
 * Implementation of {@link DatabaseAuthenticator} that reads the credentials from a cfg file.
 *
 * @see DatabaseAuthenticator
 */
@SuppressWarnings("unused")
class ConfigAuthenticator extends DatabaseAuthenticator {

    /**
     * Returns the database credentials from the credentials file.
     */
    public String[] getCredentials() {
        Properties properties = new Properties();
        try {
            File jarPath = new File(getClass().getProtectionDomain()
                .getCodeSource().getLocation().getPath());
            String propertiesPath = jarPath.getParentFile().getAbsolutePath();
            properties.load(new FileInputStream(propertiesPath + "/config.cfg"));

        } catch (FileNotFoundException e) {
            try (InputStream config = getClass().getClassLoader().getResourceAsStream("config.cfg")) {
                properties.load(config);
            } catch (IOException e2) {
                throw new UncheckedIOException(e2);
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
            e.printStackTrace();
            throw new DatabaseException("JDBC driver not loaded");
        }

        return new String[]{username, password, url};
    }

}
