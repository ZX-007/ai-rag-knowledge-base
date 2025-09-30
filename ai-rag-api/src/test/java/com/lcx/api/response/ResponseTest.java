package com.lcx.api.response;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Response类测试
 * <p>
 * 测试Response类的各种功能和方法。
 * </p>
 *
 * @author lcx
 * @version 1.0
 * @since 1.0
 */
public class ResponseTest {

    @Test
    public void testSuccessResponse() {
        // 测试成功响应
        Response<String> response = Response.success();
        
        assertNotNull(response);
        assertEquals(Response.SUCCESS_CODE, response.getCode());
        assertEquals("success", response.getInfo());
        assertTrue(response.isSuccess());
        assertFalse(response.isFailure());
    }

    @Test
    public void testSuccessResponseWithData() {
        // 测试带数据的成功响应
        List<String> data = Arrays.asList("tag1", "tag2", "tag3");
        Response<List<String>> response = Response.success("查询成功", data);
        
        assertNotNull(response);
        assertEquals(Response.SUCCESS_CODE, response.getCode());
        assertEquals("查询成功", response.getInfo());
        assertEquals(data, response.getData());
        assertTrue(response.isSuccess());
    }

    @Test
    public void testFailureResponse() {
        // 测试失败响应
        Response<String> response = Response.failure();
        
        assertNotNull(response);
        assertEquals(Response.FAILURE_CODE, response.getCode());
        assertEquals("failure", response.getInfo());
        assertFalse(response.isSuccess());
        assertTrue(response.isFailure());
    }

    @Test
    public void testCustomResponse() {
        // 测试自定义响应
        Response<String> response = Response.of("4000", "参数错误");
        
        assertNotNull(response);
        assertEquals("4000", response.getCode());
        assertEquals("参数错误", response.getInfo());
        assertFalse(response.isSuccess());
    }

    @Test
    public void testResponseUtils() {
        // 测试ResponseUtils工具类
        Response<String> ok = ResponseUtils.ok("操作成功");
        assertEquals(Response.SUCCESS_CODE, ok.getCode());
        
        Response<String> error = ResponseUtils.error("操作失败");
        assertEquals(Response.FAILURE_CODE, error.getCode());
        
        Response<String> badRequest = ResponseUtils.badRequest("参数错误");
        assertEquals(Response.PARAM_ERROR_CODE, badRequest.getCode());
    }

    @Test
    public void testResponseCode() {
        // 测试ResponseCode枚举
        assertEquals("2000", ResponseCode.SUCCESS.getCode());
        assertEquals("操作成功", ResponseCode.SUCCESS.getMessage());
        
        assertEquals("5000", ResponseCode.INTERNAL_ERROR.getCode());
        assertEquals("服务器内部错误", ResponseCode.INTERNAL_ERROR.getMessage());
        
        // 测试状态码判断
        assertTrue(ResponseCode.isSuccess("2000"));
        assertFalse(ResponseCode.isSuccess("5000"));
        assertTrue(ResponseCode.isFailure("5000"));
        assertFalse(ResponseCode.isFailure("2000"));
        
        // 测试根据状态码获取枚举
        ResponseCode successCode = ResponseCode.fromCode("2000");
        assertEquals(ResponseCode.SUCCESS, successCode);
        
        ResponseCode unknownCode = ResponseCode.fromCode("9999");
        assertNull(unknownCode);
    }

    @Test
    public void testPageResult() {
        // 测试分页结果
        List<String> data = Arrays.asList("item1", "item2", "item3");
        PageResult<String> pageResult = PageResult.<String>builder()
                .data(data)
                .total(100)
                .page(1)
                .pageSize(10)
                .totalPages(10)
                .build();
        
        assertNotNull(pageResult);
        assertEquals(data, pageResult.getData());
        assertEquals(100, pageResult.getTotal());
        assertEquals(1, pageResult.getPage());
        assertEquals(10, pageResult.getPageSize());
        assertEquals(10, pageResult.getTotalPages());
        assertTrue(pageResult.hasNext());
        assertFalse(pageResult.hasPrevious());
        assertFalse(pageResult.isEmpty());
        assertEquals(3, pageResult.getSize());
    }

    @Test
    public void testPageResponse() {
        // 测试分页响应
        List<String> data = Arrays.asList("item1", "item2", "item3");
        Response<PageResult<String>> response = ResponseUtils.page(data, 100, 1, 10);
        
        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertNotNull(response.getData());
        
        PageResult<String> pageResult = response.getData();
        assertEquals(data, pageResult.getData());
        assertEquals(100, pageResult.getTotal());
        assertEquals(1, pageResult.getPage());
        assertEquals(10, pageResult.getPageSize());
    }

    @Test
    public void testChainedMethods() {
        // 测试链式调用
        Response<String> response = Response.success("操作成功")
                .traceId("trace-123")
                .data("result data");
        
        assertNotNull(response);
        assertEquals("trace-123", response.getTraceId());
        assertEquals("result data", response.getData());
        assertTrue(response.isSuccess());
    }

    @Test
    public void testTimestamp() {
        // 测试时间戳
        Response<String> response = Response.success("操作成功");
        
        assertNotNull(response);
        assertNotNull(response.getTimestamp());
        // 时间戳应该是最近的时间
        assertTrue(response.getTimestamp().isAfter(java.time.LocalDateTime.now().minusMinutes(1)));
    }
}
