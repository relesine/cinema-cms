package com.starlight.cms.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 公共字段自动填充：
 * 插入时自动写入 createTime/updateTime/isDeleted；
 * 更新时自动刷新 updateTime。
 */
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        LocalDateTime now = LocalDateTime.now();
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, now);
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, now);
        // 逻辑删除字段默认 0（字段存在才填）
        Object deleted = this.getFieldValByName("isDeleted", metaObject);
        if (deleted == null) {
            this.strictInsertFill(metaObject, "isDeleted", Integer.class, 0);
        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    }
}
