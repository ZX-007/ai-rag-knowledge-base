package com.lcx.api.logging.enums;

/**
 * 业务模块枚举
 * 
 * <p>定义系统中的业务模块，用于日志分类和统计分析。</p>
 * 
 * @author lcx
 * @version 1.0
 */
public enum BusinessModuleEnum {
    
    /** AI对话模块 */
    AI_CHAT("AI_CHAT", "AI对话"),
    
    /** RAG检索模块 */
    RAG("RAG", "RAG检索"),
    
    /** 文件管理模块 */
    FILE("FILE", "文件管理"),
    
    /** Git仓库模块 */
    GIT("GIT", "Git仓库"),
    
    /** 向量数据库模块 */
    VECTOR_DB("VECTOR_DB", "向量数据库"),
    
    /** 缓存模块 */
    CACHE("CACHE", "缓存"),
    
    /** 系统管理模块 */
    SYSTEM("SYSTEM", "系统管理"),
    
    /** 用户管理模块 */
    USER("USER", "用户管理"),
    
    /** 配置管理模块 */
    CONFIG("CONFIG", "配置管理"),
    
    /** 其他模块 */
    OTHER("OTHER", "其他");

    private final String code;
    private final String description;

    BusinessModuleEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}

