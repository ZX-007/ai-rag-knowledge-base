package com.lcx.api.logging.util;

import com.lcx.api.logging.LogConstants;

import java.util.regex.Pattern;

/**
 * 敏感信息脱敏工具类
 * 
 * <p>提供各种敏感信息的脱敏处理，确保日志中不会泄露敏感数据。</p>
 * <p>支持手机号、邮箱、身份证、银行卡、密码、地址等多种敏感信息类型。</p>
 * 
 * <p>使用示例：</p>
 * <pre>{@code
 * // 脱敏手机号
 * String maskedPhone = SensitiveDataMasker.maskMobile("13812345678");
 * // 结果: 138****5678
 * 
 * // 脱敏邮箱
 * String maskedEmail = SensitiveDataMasker.maskEmail("example@email.com");
 * // 结果: exa***@email.com
 * 
 * // 脱敏身份证
 * String maskedIdCard = SensitiveDataMasker.maskIdCard("110101199001011234");
 * // 结果: 110101********1234
 * 
 * // 通用脱敏（保留前后各3位）
 * String masked = SensitiveDataMasker.mask("sensitive_data");
 * // 结果: sen***ata
 * }</pre>
 * 
 * @author lcx
 * @version 1.0
 */
public final class SensitiveDataMasker {

    private SensitiveDataMasker() {
        throw new UnsupportedOperationException("SensitiveDataMasker is a utility class");
    }

    /** 手机号正则表达式 */
    private static final Pattern MOBILE_PATTERN = Pattern.compile("1[3-9]\\d{9}");
    
    /** 邮箱正则表达式 */
    private static final Pattern EMAIL_PATTERN = Pattern.compile("\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*");
    
    /** 身份证正则表达式 */
    private static final Pattern ID_CARD_PATTERN = Pattern.compile("(\\d{6})(\\d{8})(\\d{4})");
    
    /** 银行卡正则表达式 */
    private static final Pattern BANK_CARD_PATTERN = Pattern.compile("(\\d{4})(\\d+)(\\d{4})");

    /**
     * 脱敏手机号
     * <p>格式：保留前3位和后4位，中间用****代替</p>
     * <p>示例：13812345678 -> 138****5678</p>
     * 
     * @param mobile 手机号
     * @return 脱敏后的手机号
     */
    public static String maskMobile(String mobile) {
        if (mobile == null || mobile.isEmpty()) {
            return mobile;
        }
        if (mobile.length() != 11 || !MOBILE_PATTERN.matcher(mobile).matches()) {
            return mask(mobile);
        }
        return mobile.substring(0, 3) + "****" + mobile.substring(7);
    }

    /**
     * 脱敏邮箱
     * <p>格式：保留邮箱前3位和@后面的内容，中间用***代替</p>
     * <p>示例：example@email.com -> exa***@email.com</p>
     * 
     * @param email 邮箱地址
     * @return 脱敏后的邮箱
     */
    public static String maskEmail(String email) {
        if (email == null || email.isEmpty()) {
            return email;
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            return mask(email);
        }
        int atIndex = email.indexOf('@');
        if (atIndex <= 0) {
            return mask(email);
        }
        String prefix = email.substring(0, atIndex);
        String suffix = email.substring(atIndex);
        
        if (prefix.length() <= 3) {
            return prefix.charAt(0) + "***" + suffix;
        }
        return prefix.substring(0, 3) + "***" + suffix;
    }

    /**
     * 脱敏身份证号
     * <p>格式：保留前6位和后4位，中间用********代替</p>
     * <p>示例：110101199001011234 -> 110101********1234</p>
     * 
     * @param idCard 身份证号
     * @return 脱敏后的身份证号
     */
    public static String maskIdCard(String idCard) {
        if (idCard == null || idCard.isEmpty()) {
            return idCard;
        }
        if (idCard.length() < 15) {
            return mask(idCard);
        }
        if (idCard.length() == 15) {
            return idCard.substring(0, 6) + "******" + idCard.substring(12);
        }
        if (idCard.length() == 18) {
            return idCard.substring(0, 6) + "********" + idCard.substring(14);
        }
        return mask(idCard);
    }

    /**
     * 脱敏银行卡号
     * <p>格式：保留前4位和后4位，中间用****代替</p>
     * <p>示例：6222021234567890123 -> 6222************0123</p>
     * 
     * @param bankCard 银行卡号
     * @return 脱敏后的银行卡号
     */
    public static String maskBankCard(String bankCard) {
        if (bankCard == null || bankCard.isEmpty()) {
            return bankCard;
        }
        if (bankCard.length() < 8) {
            return mask(bankCard);
        }
        int starCount = bankCard.length() - 8;
        return bankCard.substring(0, 4) + "*".repeat(Math.max(4, starCount)) + bankCard.substring(bankCard.length() - 4);
    }

    /**
     * 脱敏密码
     * <p>完全隐藏，返回固定的******</p>
     * 
     * @param password 密码
     * @return 脱敏后的密码（固定为******）
     */
    public static String maskPassword(String password) {
        if (password == null || password.isEmpty()) {
            return password;
        }
        return "******";
    }

    /**
     * 脱敏令牌
     * <p>格式：保留前8位，后面用...代替</p>
     * <p>示例：abcdef1234567890xyz -> abcdef12...</p>
     * 
     * @param token 令牌
     * @return 脱敏后的令牌
     */
    public static String maskToken(String token) {
        if (token == null || token.isEmpty()) {
            return token;
        }
        if (token.length() <= 8) {
            return token.substring(0, Math.min(4, token.length())) + "...";
        }
        return token.substring(0, 8) + "...";
    }

    /**
     * 脱敏姓名
     * <p>格式：保留姓氏，名字用*代替</p>
     * <p>示例：张三 -> 张*，李四四 -> 李**</p>
     * 
     * @param name 姓名
     * @return 脱敏后的姓名
     */
    public static String maskName(String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }
        if (name.length() == 1) {
            return "*";
        }
        if (name.length() == 2) {
            return name.charAt(0) + "*";
        }
        return name.charAt(0) + "*".repeat(name.length() - 1);
    }

    /**
     * 脱敏地址
     * <p>格式：保留省市区，详细地址用****代替</p>
     * <p>示例：北京市朝阳区某某街道123号 -> 北京市朝阳区****</p>
     * 
     * @param address 地址
     * @return 脱敏后的地址
     */
    public static String maskAddress(String address) {
        if (address == null || address.isEmpty()) {
            return address;
        }
        // 简单处理：保留前9个字符（大约是省市区），后面用****代替
        if (address.length() <= 9) {
            return address.substring(0, Math.min(3, address.length())) + "****";
        }
        return address.substring(0, 9) + "****";
    }

    /**
     * 通用脱敏方法
     * <p>格式：保留前3位和后3位，中间用***代替</p>
     * <p>如果字符串长度小于等于6，则保留前1位，其余用***代替</p>
     * <p>示例：sensitive_data -> sen***ata</p>
     * 
     * @param data 待脱敏数据
     * @return 脱敏后的数据
     */
    public static String mask(String data) {
        if (data == null || data.isEmpty()) {
            return data;
        }
        int length = data.length();
        if (length <= 1) {
            return LogConstants.Marker.MASKED;
        }
        if (length <= 6) {
            return data.charAt(0) + LogConstants.Marker.MASKED;
        }
        return data.substring(0, 3) + LogConstants.Marker.MASKED + data.substring(length - 3);
    }

    /**
     * 根据类型脱敏
     * 
     * @param data 待脱敏数据
     * @param type 敏感信息类型
     * @return 脱敏后的数据
     */
    public static String maskByType(String data, String type) {
        if (data == null || type == null) {
            return data;
        }
        
        return switch (type) {
            case LogConstants.SensitiveType.MOBILE -> maskMobile(data);
            case LogConstants.SensitiveType.EMAIL -> maskEmail(data);
            case LogConstants.SensitiveType.ID_CARD -> maskIdCard(data);
            case LogConstants.SensitiveType.BANK_CARD -> maskBankCard(data);
            case LogConstants.SensitiveType.PASSWORD -> maskPassword(data);
            case LogConstants.SensitiveType.TOKEN -> maskToken(data);
            case LogConstants.SensitiveType.NAME -> maskName(data);
            case LogConstants.SensitiveType.ADDRESS -> maskAddress(data);
            default -> mask(data);
        };
    }

    /**
     * 判断字符串是否包含手机号
     * 
     * @param text 文本
     * @return 是否包含手机号
     */
    public static boolean containsMobile(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        return MOBILE_PATTERN.matcher(text).find();
    }

    /**
     * 判断字符串是否包含邮箱
     * 
     * @param text 文本
     * @return 是否包含邮箱
     */
    public static boolean containsEmail(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(text).find();
    }

    /**
     * 判断字符串是否包含身份证号
     * 
     * @param text 文本
     * @return 是否包含身份证号
     */
    public static boolean containsIdCard(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }
        return ID_CARD_PATTERN.matcher(text).find();
    }

    /**
     * 自动检测并脱敏文本中的敏感信息
     * <p>会自动识别文本中的手机号、邮箱、身份证等敏感信息并进行脱敏</p>
     * 
     * @param text 原始文本
     * @return 脱敏后的文本
     */
    public static String autoMask(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        
        String result = text;
        
        // 脱敏手机号
        result = MOBILE_PATTERN.matcher(result).replaceAll(matchResult -> {
            String mobile = matchResult.group();
            return maskMobile(mobile);
        });
        
        // 脱敏邮箱
        result = EMAIL_PATTERN.matcher(result).replaceAll(matchResult -> {
            String email = matchResult.group();
            return maskEmail(email);
        });
        
        // 脱敏身份证
        result = ID_CARD_PATTERN.matcher(result).replaceAll(matchResult -> {
            String idCard = matchResult.group();
            return maskIdCard(idCard);
        });
        
        return result;
    }
}

