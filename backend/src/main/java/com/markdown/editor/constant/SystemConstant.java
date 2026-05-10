package com.markdown.editor.constant;

/**
 * 系统常量类
 */
public class SystemConstant {

    /**
     * 日志记录
     */
    public static final String LOG_REQUEST = "请求参数: {}";
    public static final String LOG_RESPONSE = "响应结果: {}";
    public static final String LOG_ERROR = "错误信息: {}";

    /**
     * 评论相关
     */
    public static final int MAX_COMMENT_LENGTH = 150;

    /**
     * 分页相关
     */
    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final int MAX_PAGE_SIZE = 100;

    /**
     * 响应消息
     */
    public static final String SUCCESS_MESSAGE = "操作成功";
    public static final String ERROR_MESSAGE = "操作失败";
    public static final String NOT_FOUND_MESSAGE = "资源不存在";
    public static final String UNAUTHORIZED_MESSAGE = "未授权";
    public static final String FORBIDDEN_MESSAGE = "无权限";

    /**
     * 文档权限
     */
    public static final String PERMISSION_PUBLIC = "public";
    public static final String PERMISSION_PRIVATE = "private";
    public static final String PERMISSION_SECRET = "secret";

    /**
     * 文档分类
     */
    public static final String CATEGORY_TECH = "tech";
    public static final String CATEGORY_LIFE = "life";
    public static final String CATEGORY_WORK = "work";
    public static final String CATEGORY_ENTERTAINMENT = "entertainment";
    public static final String CATEGORY_OTHER = "other";

    /**
     * 排序方式
     */
    public static final String SORT_BY_CREATED_AT = "createdAt";
    public static final String SORT_BY_UPDATED_AT = "updatedAt";
    public static final String SORT_BY_VIEW_COUNT = "viewCount";
    public static final String SORT_BY_LIKE_COUNT = "likeCount";

    /**
     * 时间格式
     */
    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String TIME_FORMAT = "HH:mm:ss";

    /**
     * 正则表达式
     */
    public static final String EMAIL_REGEX = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
    public static final String PHONE_REGEX = "^1[3-9]\\d{9}$";
    public static final String PASSWORD_REGEX = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d@$!%*?&]{6,40}$";

    /**
     * 缓存键
     */
    public static final String CACHE_KEY_USER_STATS = "user:stats:{}";
    public static final String CACHE_KEY_DOCUMENT = "document:{}";
    public static final String CACHE_KEY_CATEGORIES = "categories";

    /**
     * 限流配置
     */
    public static final int RATE_LIMIT_REQUESTS_PER_MINUTE = 60;

    /**
     * 文件上传
     */
    public static final long MAX_FILE_SIZE = 10485760; // 10MB
    public static final String ALLOWED_FILE_TYPES = "image/jpeg,image/png,image/gif,image/webp";
    public static final String UPLOAD_DIR = "uploads";

    /**
     * 安全相关
     */
    public static final String JWT_SECRET_KEY = "markdown_editor_secret_key";
    public static final long JWT_EXPIRATION_TIME = 86400000; // 24 hours
    public static final String JWT_HEADER = "Authorization";
    public static final String JWT_PREFIX = "Bearer ";

    /**
     * 角色
     */
    public static final String ROLE_ADMIN = "admin";
    public static final String ROLE_USER = "user";

    /**
     * 权限
     */
    public static final String PERMISSION_READ = "read";
    public static final String PERMISSION_WRITE = "write";
    public static final String PERMISSION_DELETE = "delete";
    public static final String PERMISSION_ADMIN = "admin";
}
