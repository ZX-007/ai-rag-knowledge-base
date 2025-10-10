package com.lcx.api.logging.enums;

/**
 * 日志级别枚举
 * 
 * <p>定义标准的日志级别，用于日志记录和过滤。</p>
 * 
 * @author lcx
 * @version 1.0
 */
public enum LogLevelEnum {
    
    /** 追踪级别 - 最详细的日志信息 */
    TRACE("TRACE", "追踪"),
    
    /** 调试级别 - 调试信息 */
    DEBUG("DEBUG", "调试"),
    
    /** 信息级别 - 常规信息 */
    INFO("INFO", "信息"),
    
    /** 警告级别 - 警告信息 */
    WARN("WARN", "警告"),
    
    /** 错误级别 - 错误信息 */
    ERROR("ERROR", "错误");

    private final String level;
    private final String description;

    LogLevelEnum(String level, String description) {
        this.level = level;
        this.description = description;
    }

    public String getLevel() {
        return level;
    }

    public String getDescription() {
        return description;
    }
}

