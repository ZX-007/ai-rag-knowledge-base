package com.lcx.app;

import com.lcx.api.dto.ChatRequest;
import com.lcx.api.dto.FileUploadRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.Size;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 参数校验测试类
 * <p>
 * 测试各种参数校验注解的功能
 * </p>
 *
 * @author lcx
 * @version 1.0
 * @since 1.0
 */
@Slf4j
@SpringBootTest
public class ValidationTest {

    @Autowired
    private Validator validator;

    /**
     * 测试ChatRequest参数校验
     */
    @Test
    public void testChatRequestValidation() {
        // 测试正常情况
        ChatRequest validRequest = new ChatRequest();
        validRequest.setModel("llama2");
        validRequest.setMessage("Hello, how are you?");
        
        Set<ConstraintViolation<ChatRequest>> violations = validator.validate(validRequest);
        assertTrue(violations.isEmpty(), "正常请求应该通过校验");

        // 测试模型名称为空
        ChatRequest invalidModelRequest = new ChatRequest();
        invalidModelRequest.setModel("");
        invalidModelRequest.setMessage("Hello");
        
        violations = validator.validate(invalidModelRequest);
        assertFalse(violations.isEmpty(), "空模型名称应该校验失败");
        log.info("模型名称为空校验失败: {}", violations.iterator().next().getMessage());

        // 测试消息内容过长
        ChatRequest longMessageRequest = new ChatRequest();
        longMessageRequest.setModel("llama2");
        longMessageRequest.setMessage("a".repeat(4001)); // 超过4000字符限制
        
        violations = validator.validate(longMessageRequest);
        assertFalse(violations.isEmpty(), "消息内容过长应该校验失败");
        log.info("消息内容过长校验失败: {}", violations.iterator().next().getMessage());
    }

    /**
     * 测试FileUploadRequest参数校验
     */
    @Test
    public void testFileUploadRequestValidation() {
        // 创建测试文件
        MultipartFile validFile = new MockMultipartFile(
                "test.txt", 
                "test.txt", 
                "text/plain", 
                "Hello World".getBytes()
        );

        // 测试正常情况
        FileUploadRequest validRequest = new FileUploadRequest();
        validRequest.setRagTag("test-tag");
        validRequest.setFiles(List.of(validFile));
        validRequest.setFile(validFile);
        
        Set<ConstraintViolation<FileUploadRequest>> violations = validator.validate(validRequest);
        assertTrue(violations.isEmpty(), "正常文件上传请求应该通过校验");

        // 测试RAG标签为空
        FileUploadRequest invalidTagRequest = new FileUploadRequest();
        invalidTagRequest.setRagTag("");
        invalidTagRequest.setFiles(List.of(validFile));
        invalidTagRequest.setFile(validFile);
        
        violations = validator.validate(invalidTagRequest);
        assertFalse(violations.isEmpty(), "空RAG标签应该校验失败");
        log.info("RAG标签为空校验失败: {}", violations.iterator().next().getMessage());

        // 测试文件列表为空
        FileUploadRequest emptyFilesRequest = new FileUploadRequest();
        emptyFilesRequest.setRagTag("test-tag");
        emptyFilesRequest.setFiles(List.of());
        emptyFilesRequest.setFile(validFile);
        
        violations = validator.validate(emptyFilesRequest);
        assertFalse(violations.isEmpty(), "空文件列表应该校验失败");
        log.info("文件列表为空校验失败: {}", violations.iterator().next().getMessage());
    }

    /**
     * 测试标准校验注解
     */
    @Test
    public void testStandardValidationAnnotations() {
        // 测试@Size注解
        TestSizeClass testString = new TestSizeClass();
        testString.setShortString("a".repeat(5)); // 5个字符，在1-10范围内
        
        Set<ConstraintViolation<TestSizeClass>> violations = validator.validate(testString);
        assertTrue(violations.isEmpty(), "正常字符串长度应该通过校验");

        // 测试字符串过长
        testString.setShortString("a".repeat(15)); // 15个字符，超过10个字符限制
        violations = validator.validate(testString);
        assertFalse(violations.isEmpty(), "字符串过长应该校验失败");
        log.info("字符串过长校验失败: {}", violations.iterator().next().getMessage());

        // 测试字符串过短
        testString.setShortString(""); // 空字符串，少于1个字符限制
        violations = validator.validate(testString);
        assertFalse(violations.isEmpty(), "字符串过短应该校验失败");
        log.info("字符串过短校验失败: {}", violations.iterator().next().getMessage());
    }

    /**
     * 测试类用于测试@Size注解
     */
    private static class TestSizeClass {
        @Size(min = 1, max = 10, message = "字符串长度必须在1-10个字符之间")
        private String shortString;

        public void setShortString(String shortString) {
            this.shortString = shortString;
        }
    }
}
