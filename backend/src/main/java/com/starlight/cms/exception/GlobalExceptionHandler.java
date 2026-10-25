package com.starlight.cms.exception;

import com.starlight.cms.common.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

/**
 * 全局异常处理器：把各种异常统一转成标准 Result，避免堆栈直接抛给前端
 * 细化常见异常的语义提示
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 业务异常（预期内，warn 级别） */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusiness(BusinessException e) {
        log.warn("业务异常：code={} {}", e.getCode(), e.getMessage());
        return Result.error(e.getCode(), e.getMessage());
    }

    /** @RequestBody 参数校验失败 */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValid(MethodArgumentNotValidException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String msg = fieldError == null ? "参数校验失败" : fieldError.getDefaultMessage();
        return Result.error(400, msg);
    }

    /** 表单参数校验失败 */
    @ExceptionHandler(BindException.class)
    public Result<Void> handleBind(BindException e) {
        FieldError fieldError = e.getBindingResult().getFieldError();
        String msg = fieldError == null ? "参数校验失败" : fieldError.getDefaultMessage();
        return Result.error(400, msg);
    }

    /** 请求体 JSON 格式错误（如引号缺失） */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Result<Void> handleNotReadable(HttpMessageNotReadableException e) {
        log.warn("请求体格式错误：{}", e.getMessage());
        return Result.error(400, "请求参数格式错误，请检查请求体 JSON");
    }

    /** 缺少必填请求参数 */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public Result<Void> handleMissingParam(MissingServletRequestParameterException e) {
        return Result.error(400, "缺少参数: " + e.getParameterName());
    }

    /** 参数类型不匹配（如页码传成了字母） */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public Result<Void> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        return Result.error(400, "参数类型错误: " + e.getName());
    }

    /** 上传文件超出 multipart 配置大小 */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public Result<Void> handleMaxUpload(MaxUploadSizeExceededException e) {
        return Result.error(400, "上传文件大小超出限制（海报最大10MB）");
    }

    /** 数据库约束冲突（唯一键/外键）给用户友好提示 */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public Result<Void> handleDataIntegrity(DataIntegrityViolationException e) {
        log.warn("数据完整性冲突：{}", e.getMessage());
        return Result.error(500, "操作失败，数据存在关联或重复");
    }

    /** 兜底：其它未预期异常（error 级别，打印堆栈便于排查） */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常：", e);
        return Result.error(500, "系统繁忙：" + e.getMessage());
    }
}
