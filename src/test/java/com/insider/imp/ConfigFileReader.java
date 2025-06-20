package com.insider.imp;

import org.apache.log4j.Logger;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.util.Properties;

public class ConfigFileReader {
    public static Properties prop;
    private static ConfigFileReader config;
    private final Logger logger;
    private final LoggerImps loggerImps;

    private ConfigFileReader() {
        logger = Logger.getLogger(ConfigFileReader.class);
        loggerImps = LoggerImps.getInstance(logger);
        configRead();
    }

    public static ConfigFileReader getInstance() {
        if (config == null) {
            config = new ConfigFileReader();
        }
        return config;
    }

    /**
     * Properties dosyasını okur ve prop değişkenine atar.
     */
    public void configRead() {
        BufferedReader reader;
        String propPath = "src/test/resources/config.properties";
        try {
            reader = new BufferedReader(new FileReader(propPath));
            prop = new Properties();
            prop.load(reader);
            reader.close();
        } catch (NoSuchFileException e) {
            e.printStackTrace();
            loggerImps.errorAndFail("Properties okunamadı dosya bulunamadı. Path = " + propPath + " " + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            loggerImps.errorAndFail("Properties dosyası açılamadı. Path = " + propPath + " " + e.getMessage());
        }
    }

    /**
     * Properties dosyasındaki değeri getirir.
     *
     * @param key Anahtar kelime
     * @return Anahtar kelimeye karşılık gelen değeri String olarak döner.
     */
    public String getProperty(String key) {
        String value = prop.getProperty(key);
        if (value == null) {
            loggerImps.errorAndFail(key + " keyi ile değer bulunamadı. Lütfen kontrol ediniz.");
        }
        return value;
    }

    /**
     * Properties dosyasındaki değeri getirir.
     *
     * @param key Anahtar kelime
     * @return Anahtar kelimeye karşılık gelen değeri Integer olarak döner.
     */
    public int getInteger(String key) {
        return Integer.parseInt(getProperty(key));
    }

    /**
     * Properties dosyasındaki değeri getirir.
     *
     * @param key Anahtar kelime
     * @return Anahtar kelimeye karşılık gelen değeri Long olarak döner.
     */
    public long getLong(String key) {
        return Long.parseLong(getProperty(key));
    }

    /**
     * Properties dosyasındaki değeri getirir.
     *
     * @param key Anahtar kelime
     * @return Anahtar kelimeye karşılık gelen değeri String olarak döner.
     */
    public String getString(String key) {
        return getProperty(key);
    }

    /**
     * Properties dosyasındaki değeri getirir.
     *
     * @param key Anahtar kelime
     * @return Anahtar kelimeye karşılık gelen değeri Boolean olarak döner.
     */
    public boolean getBoolean(String key) {
        return Boolean.parseBoolean(getProperty(key));
    }

}
