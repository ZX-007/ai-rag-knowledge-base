package com.lcx.api.logging;

/**
 * 日志常量定义
 * 
 * <p>定义企业级日志系统的所有常量，包括MDC键、日志类型、操作类型等。</p>
 * <p>统一管理日志相关常量，便于维护和扩展。</p>
 * 
 * @author lcx
 * @version 1.0
 */
public final class LogConstants {

    private LogConstants() {
        throw new UnsupportedOperationException("LogConstants is a utility class");
    }

    /**
     * MDC上下文键常量
     */
    public static final class MdcKey {
        /** 追踪ID */
        public static final String TRACE_ID = "traceId";
        /** 服务ID */
        public static final String SERVICE_ID = "serviceId";
        /** 用户ID */
        public static final String USER_ID = "userId";
        /** 用户名 */
        public static final String USERNAME = "username";
        /** 客户端IP */
        public static final String CLIENT_IP = "clientIp";
        /** 请求方法 */
        public static final String HTTP_METHOD = "httpMethod";
        /** 请求URI */
        public static final String REQUEST_URI = "requestUri";
        /** 会话ID */
        public static final String SESSION_ID = "sessionId";
        /** 租户ID（多租户场景） */
        public static final String TENANT_ID = "tenantId";
        /** 业务模块 */
        public static final String MODULE = "module";
        /** 业务操作 */
        public static final String OPERATION = "operation";
        
        private MdcKey() {}
    }

    /**
     * HTTP请求头常量
     */
    public static final class HttpHeader {
        /** 追踪ID请求头 */
        public static final String TRACE_ID = "X-Trace-Id";
        /** 用户代理 */
        public static final String USER_AGENT = "User-Agent";
        /** 真实IP（代理转发） */
        public static final String X_FORWARDED_FOR = "X-Forwarded-For";
        /** 真实IP（Nginx等） */
        public static final String X_REAL_IP = "X-Real-IP";
        
        private HttpHeader() {}
    }

    /**
     * 日志类型常量
     */
    public static final class LogType {
        /** 访问日志 */
        public static final String ACCESS = "ACCESS";
        /** 业务日志 */
        public static final String BUSINESS = "BUSINESS";
        /** 操作日志 */
        public static final String OPERATION = "OPERATION";
        /** 系统日志 */
        public static final String SYSTEM = "SYSTEM";
        /** 性能日志 */
        public static final String PERFORMANCE = "PERFORMANCE";
        /** 安全日志 */
        public static final String SECURITY = "SECURITY";
        /** 错误日志 */
        public static final String ERROR = "ERROR";
        
        private LogType() {}
    }

    /**
     * 服务名称常量
     */
    public static final class ServiceName {
        /** 应用服务 */
        public static final String APP = "ai-knowledge-app";
        /** AI服务 */
        public static final String AI_SERVICE = "ai-service";
        /** RAG服务 */
        public static final String RAG_SERVICE = "rag-service";
        
        private ServiceName() {}
    }

    /**
     * 日志模板常量
     */
    public static final class Template {
        /** 请求开始 */
        public static final String REQUEST_BEGIN = "REQUEST_BEGIN";
        /** 请求结束 */
        public static final String REQUEST_END = "REQUEST_END";
        /** 方法开始 */
        public static final String METHOD_BEGIN = "METHOD_BEGIN";
        /** 方法结束 */
        public static final String METHOD_END = "METHOD_END";
        /** 方法错误 */
        public static final String METHOD_ERROR = "METHOD_ERROR";
        /** 业务操作 */
        public static final String BIZ_OPERATION = "BIZ_OPERATION";
        /** 外部调用 */
        public static final String EXTERNAL_CALL = "EXTERNAL_CALL";
        
        private Template() {}
    }

    /**
     * 敏感信息类型
     */
    public static final class SensitiveType {
        /** 手机号 */
        public static final String MOBILE = "mobile";
        /** 邮箱 */
        public static final String EMAIL = "email";
        /** 身份证 */
        public static final String ID_CARD = "idCard";
        /** 银行卡 */
        public static final String BANK_CARD = "bankCard";
        /** 密码 */
        public static final String PASSWORD = "password";
        /** 令牌 */
        public static final String TOKEN = "token";
        /** 姓名 */
        public static final String NAME = "name";
        /** 地址 */
        public static final String ADDRESS = "address";
        
        private SensitiveType() {}
    }

    /**
     * 特殊标记
     */
    public static final class Marker {
        /** 未知值 */
        public static final String UNKNOWN = "unknown";
        /** 匿名用户 */
        public static final String ANONYMOUS = "anonymous";
        /** 系统用户 */
        public static final String SYSTEM = "system";
        /** 空值 */
        public static final String NULL = "null";
        /** 脱敏标记 */
        public static final String MASKED = "***";
        
        private Marker() {}
    }
}

