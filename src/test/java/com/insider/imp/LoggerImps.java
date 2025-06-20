package com.insider.imp;

import org.apache.log4j.Logger;
import org.junit.jupiter.api.Assertions;
import java.util.HashMap;
import java.util.Map;

public class LoggerImps {
    private static LoggerImps loggerImps;
    private Map<String, String> colorMap;

    private static Logger logger;

    private LoggerImps() {
        initColorMap();
    }

    /**
     * Nesne Singleton Pattern'a göre oluşturulur.
     * @param logger Logger
     * @return Instance
     */
    public static LoggerImps getInstance(Logger logger) {
        LoggerImps.logger = logger;
        if (loggerImps == null) {
            loggerImps = new LoggerImps();
        }
        return loggerImps;
    }

    /**
     * Renk mapi oluşturulur.
     */
    public void initColorMap() {
        colorMap = new HashMap<>();
        colorMap.put("blue", "\u001B[34m-> ");
        colorMap.put("cyan", "\u001B[36m-> ");
        colorMap.put("gray", "\u001B[37m-> ");
        colorMap.put("green", "\u001B[32m-> ");
        colorMap.put("magenta", "\u001B[35m-> ");
        colorMap.put("orange", "\u001B[38;5;136m-> ");
        colorMap.put("red", "\u001B[31m-> ");
        colorMap.put("yellow", "\u001B[33m-> ");
        colorMap.put("white", "\u001B[0m-> ");
    }

    /**
     * Girilen renk değerine göre text renklendirilir.
     *
     * @param color Blue,Cyan,Gray,Green,Magenta,Orange,Red,Yellow,White
     * @return reklendirilmiş text.
     */
    public StringBuilder getColor(String color) {
        StringBuilder cString = new StringBuilder();
        cString.append("\u001B[0m");
        return cString.append(colorMap.getOrDefault(color, "\u001B[0m->"));
    }

    /**
     * Renklendirilmiş log mesajı oluşturulur.
     *
     * @param text  Message
     * @param color Blue,Cyan,Gray,Green,Magenta,Orange,Red,Yellow,White
     * @return renklendirilmiş text.
     */
    public String getLoggerText(String text, String color) {
        StringBuilder cString = getColor(color.toLowerCase());
        return String.valueOf(cString.append(text).append("\u001B[0m"));
    }

    /**
     * Girilen mesaj ile Asserions fail tetiklenir.
     *
     * @param message Message
     */
    public void fail(String message) {
        Assertions.fail(message);
    }

    /**
     * Girilen renge göre info logu ekrana basılır.
     *
     * @param message Message
     * @param color   Blue,Cyan,Gray,Green,Magenta,Orange,Red,Yellow,White
     */
    public void info(String message, String color) {
        logger.info(getLoggerText(message, color));
    }

    /**
     * Yeşil rengi ile info logu ekrana basılır.
     *
     * @param message Message
     */
    public void info(String message) {
        logger.info(getLoggerText(message, "green"));
    }

    /**
     * Girilen renge göre error logu ekrana basılır.
     *
     * @param message Message
     * @param color   Blue,Cyan,Gray,Green,Magenta,Orange,Red,Yellow,White
     */
    public void error(String message, String color) {
        logger.error(getLoggerText(message, color));
    }

    /**
     * Kırmızı rengi ile error logu ekrana basılır.
     *
     * @param message Message
     */
    public void error(String message) {
        logger.error(getLoggerText(message, "red"));
    }

    /**
     * Girilen renge göre error logu ekrana basılır ve fail verilir.
     *
     * @param message Message
     * @param color   Blue,Cyan,Gray,Green,Magenta,Orange,Red,Yellow,White
     */
    public void errorAndFail(String message, String color) {
        logger.error(getLoggerText(message, color));
        fail(message);
    }

    /**
     * Kırmızı rengi ile error logu ekrana basılır ve fail verilir.
     *
     * @param message Message
     */
    public void errorAndFail(String message) {
        logger.error(getLoggerText(message, "red"));
        fail(message);
    }

    /**
     * Girilen renge göre warn logu ekrana basılır.
     *
     * @param message Message
     * @param color   Blue,Cyan,Gray,Green,Magenta,Orange,Red,Yellow,White
     */
    public void warn(String message, String color) {
        logger.warn(getLoggerText(message, color));
    }

    /**
     * Turuncu rengi ile warn logu ekrana basılır.
     *
     * @param message Message
     */
    public void warn(String message) {
        logger.warn(getLoggerText(message, "orange"));
    }

    /**
     * Girilen renge göre warn logu ekrana basılır ve fail verilir.
     *
     * @param message Message
     * @param color   Blue,Cyan,Gray,Green,Magenta,Orange,Red,Yellow,White
     */
    public void warnAndFail(String message, String color) {
        logger.warn(getLoggerText(message, color));
        fail(message);
    }
}
