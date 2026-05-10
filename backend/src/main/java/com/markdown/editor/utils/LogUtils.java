package com.markdown.editor.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 日志工具类
 * 提供中文日志记录功能
 */
public class LogUtils {
    
    /**
     * 获取Logger实例
     * @param clazz 类对象
     * @return Logger实例
     */
    public static Logger getLogger(Class<?> clazz) {
        return LoggerFactory.getLogger(clazz);
    }
    
    /**
     * 记录调试信息
     * @param logger Logger实例
     * @param message 消息
     * @param args 参数
     */
    public static void debug(Logger logger, String message, Object... args) {
        if (logger.isDebugEnabled()) {
            logger.debug(message, args);
        }
    }
    
    /**
     * 记录信息
     * @param logger Logger实例
     * @param message 消息
     * @param args 参数
     */
    public static void info(Logger logger, String message, Object... args) {
        if (logger.isInfoEnabled()) {
            logger.info(message, args);
        }
    }
    
    /**
     * 记录警告
     * @param logger Logger实例
     * @param message 消息
     * @param args 参数
     */
    public static void warn(Logger logger, String message, Object... args) {
        if (logger.isWarnEnabled()) {
            logger.warn(message, args);
        }
    }
    
    /**
     * 记录错误
     * @param logger Logger实例
     * @param message 消息
     * @param args 参数
     */
    public static void error(Logger logger, String message, Object... args) {
        if (logger.isErrorEnabled()) {
            logger.error(message, args);
        }
    }
    
    /**
     * 记录错误
     * @param logger Logger实例
     * @param message 消息
     * @param throwable 异常
     * @param args 参数
     */
    public static void error(Logger logger, String message, Throwable throwable, Object... args) {
        if (logger.isErrorEnabled()) {
            logger.error(message, args, throwable);
        }
    }
}