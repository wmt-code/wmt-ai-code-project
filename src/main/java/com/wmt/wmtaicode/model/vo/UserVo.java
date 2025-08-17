package com.wmt.wmtaicode.model.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户 实体类。
 *
 * @author ethereal
 * @since 2025-08-17
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserVo implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * id
	 */
	private Long id;

	/**
	 * 账号
	 */
	private String userAccount;


	/**
	 * 用户昵称
	 */
	private String userName;

	/**
	 * 用户头像
	 */
	private String userAvatar;

	/**
	 * 用户简介
	 */
	private String userProfile;

	/**
	 * 用户角色：user/admin
	 */
	private String userRole;


	/**
	 * 创建时间
	 */
	@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
	private LocalDateTime createTime;

}
