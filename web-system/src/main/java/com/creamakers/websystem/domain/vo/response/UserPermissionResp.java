package com.creamakers.websystem.domain.vo.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserPermissionResp {
    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码，经过加密存储
     */
    private String password;

    /**
     * 管理员权限: 0-普通用户, 1-运营组，2-开发组
     */
    private Integer isAdmin;

    /**
     * 是否删除: 0-未删除，1-已删除
     */
    private Boolean isDeleted;

    /**
     * 是否封禁: 0-未封禁，1-已封禁
     */
    private Boolean isBanned;

    /**
     * 记录创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private LocalDateTime createTime;

    /**
     * 记录更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'", timezone = "UTC")
    private LocalDateTime updateTime;

    /**
     * 用户描述
     */
    private String description;
}
