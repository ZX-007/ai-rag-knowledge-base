package com.lcx.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Git 仓库分析请求数据传输对象
 * <p>
 * 用于接收和验证 Git 仓库分析请求的参数。该 DTO 包含了进行 Git 仓库克隆和文档分析所需的基本信息，
 * 包括仓库地址和访问令牌。所有字段都经过严格的参数验证，确保数据的完整性和有效性。
 * </p>
 * <p>
 * 主要功能：
 * <ul>
 *   <li>参数验证 - 确保仓库地址和令牌符合要求</li>
 *   <li>数据传输 - 在客户端和服务端之间传递 Git 仓库分析请求</li>
 *   <li>类型安全 - 提供强类型的数据结构</li>
 * </ul>
 * </p>
 * <p>
 * 验证规则：
 * <ul>
 *   <li>仓库地址：不能为空，必须是有效的 Git 仓库 URL 格式</li>
 *   <li>用户名：长度 1-64 个字符，用于访问私有仓库</li>
 *   <li>访问令牌：长度 1-256 个字符，用于访问私有仓库</li>
 * </ul>
 *
 * @author lcx
 * @version 1.0
 * @since 2024
 * @see jakarta.validation.constraints.NotBlank 非空验证注解
 * @see jakarta.validation.constraints.Size 长度验证注解
 * @see jakarta.validation.constraints.Pattern 正则表达式验证注解
 */
@Data
public class GitRepositoryRequest {

    /**
     * Git 仓库地址
     * <p>
     * 支持 HTTPS 和 SSH 协议的 Git 仓库地址，如：
     * <ul>
     *   <li>https://github.com/username/repository.git</li>
     *   <li>https://gitlab.com/username/repository.git</li>
     *   <li>git@github.com:username/repository.git</li>
     * </ul>
     * </p>
     * <p>
     * 验证规则：
     * <ul>
     *   <li>不能为空或空白字符串</li>
     *   <li>必须符合 Git 仓库 URL 格式</li>
     * </ul>
     * </p>
     */
    @NotBlank(message = "Git仓库地址不能为空")
    @Pattern(regexp = "^(https?://[\\w\\.-]+/[\\w\\.-]+/[\\w\\.-]+\\.git|git@[\\w\\.-]+:[\\w\\.-]+/[\\w\\.-]+\\.git)$",
            message = "Git仓库地址格式不正确")
    private String repoUrl;

    /**
     * Git 用户名
     * <p>
     * 用于访问 Git 仓库的用户名，通常是 GitHub、GitLab 等平台的用户名。
     * 对于私有仓库，此字段是必需的；对于公开仓库，则不需要。
     * </p>
     * <p>
     * 验证规则：
     * <ul>
     *   <li>长度必须在 1-64 个字符之间</li>
     * </ul>
     * </p>
     */
    @Size(max = 64, message = "userName长度必须在1-64个字符之间")
    private String userName = "";

    /**
     * 访问令牌
     * <p>
     * 用于访问私有仓库的访问令牌，如 GitHub Personal Access Token、GitLab Access Token 等。
     * 对于私有仓库，此字段是必需的；对于公开仓库，则不需要。
     * </p>
     * <p>
     * 验证规则：
     * <ul>
     *   <li>长度必须在 1-256 个字符之间</li>
     * </ul>
     * </p>
     * <p>
     * 安全提示：
     * <ul>
     *   <li>令牌应具有适当的权限范围，仅授予必要的仓库访问权限</li>
     *   <li>定期更换访问令牌以确保安全</li>
     *   <li>不要在日志中记录令牌信息</li>
     * </ul>
     * </p>
     */
    @Size(max = 256, message = "token长度必须在1-256个字符之间")
    private String token = "";
}