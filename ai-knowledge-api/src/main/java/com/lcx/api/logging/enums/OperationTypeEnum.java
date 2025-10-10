package com.lcx.api.logging.enums;

/**
 * 操作类型枚举
 * 
 * <p>定义系统中的各种操作类型，用于操作日志记录和审计。</p>
 * 
 * @author lcx
 * @version 1.0
 */
public enum OperationTypeEnum {
    
    /** 查询操作 */
    QUERY("QUERY", "查询"),
    
    /** 创建操作 */
    CREATE("CREATE", "创建"),
    
    /** 更新操作 */
    UPDATE("UPDATE", "更新"),
    
    /** 删除操作 */
    DELETE("DELETE", "删除"),
    
    /** 上传操作 */
    UPLOAD("UPLOAD", "上传"),
    
    /** 下载操作 */
    DOWNLOAD("DOWNLOAD", "下载"),
    
    /** 导入操作 */
    IMPORT("IMPORT", "导入"),
    
    /** 导出操作 */
    EXPORT("EXPORT", "导出"),
    
    /** 登录操作 */
    LOGIN("LOGIN", "登录"),
    
    /** 登出操作 */
    LOGOUT("LOGOUT", "登出"),
    
    /** 授权操作 */
    AUTHORIZE("AUTHORIZE", "授权"),
    
    /** AI生成操作 */
    AI_GENERATE("AI_GENERATE", "AI生成"),
    
    /** RAG检索操作 */
    RAG_SEARCH("RAG_SEARCH", "RAG检索"),
    
    /** Git仓库分析 */
    GIT_ANALYZE("GIT_ANALYZE", "Git分析"),
    
    /** 其他操作 */
    OTHER("OTHER", "其他");

    private final String code;
    private final String description;

    OperationTypeEnum(String code, String description) {
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

