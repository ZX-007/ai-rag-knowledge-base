package com.lcx.api.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 分页结果封装类
 * <p>
 * 用于封装分页查询的结果，包含数据列表、分页信息等。
 * </p>
 *
 * @param <T> 数据类型
 * @author lcx
 * @version 1.0
 * @since 1.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 数据列表
     */
    private List<T> data;

    /**
     * 总记录数
     */
    private long total;

    /**
     * 当前页码（从1开始）
     */
    private int page;

    /**
     * 每页大小
     */
    private int pageSize;

    /**
     * 总页数
     */
    private int totalPages;

    /**
     * 是否有下一页
     */
    public boolean hasNext() {
        return page < totalPages;
    }

    /**
     * 是否有上一页
     */
    public boolean hasPrevious() {
        return page > 1;
    }

    /**
     * 是否为空
     */
    public boolean isEmpty() {
        return data == null || data.isEmpty();
    }

    /**
     * 获取数据数量
     */
    public int getSize() {
        return data == null ? 0 : data.size();
    }

    /**
     * 获取起始索引（从0开始）
     */
    public long getStartIndex() {
        return (long) (page - 1) * pageSize;
    }

    /**
     * 获取结束索引（从0开始，不包含）
     */
    public long getEndIndex() {
        return Math.min(getStartIndex() + pageSize, total);
    }

    @Override
    public String toString() {
        return "PageResult{" +
                "data=" + (data != null ? data.size() + " items" : "null") +
                ", total=" + total +
                ", page=" + page +
                ", pageSize=" + pageSize +
                ", totalPages=" + totalPages +
                '}';
    }
}
