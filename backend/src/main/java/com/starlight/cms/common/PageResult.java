package com.starlight.cms.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * 分页结果统一封装
 * total：总条数；list：当前页数据
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> implements Serializable {
    private Long total;
    private List<T> list;

    /** 基于 MyBatis Plus 的 IPage 快速构造 */
    public static <T> PageResult<T> of(long total, List<T> records) {
        return new PageResult<>(total, records);
    }
}
