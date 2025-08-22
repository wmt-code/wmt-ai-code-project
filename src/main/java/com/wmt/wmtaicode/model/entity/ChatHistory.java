package com.wmt.wmtaicode.model.entity;

import com.mybatisflex.annotation.Column;
import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import com.mybatisflex.core.keygen.KeyGenerators;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 对话历史记录表 实体类。
 *
 * @author ethereal
 * @since 2025-08-23
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("chat_history")
public class ChatHistory implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 消息ID
	 */
	@Id(keyType = KeyType.Generator, value = KeyGenerators.snowFlakeId)
	private Long id;

	/**
	 * 消息内容
	 */
	@Column("message")
	private String message;

	/**
	 * 消息类型 user/ai
	 */
	@Column("messageType")
	private String messageType;

	/**
	 * 对应的应用ID
	 */
	@Column("appId")
	private Long appId;

	/**
	 * 用户ID
	 */
	@Column("userId")
	private Long userId;

	/**
	 * 创建时间
	 */
	@Column("createTime")
	private LocalDateTime createTime;

	/**
	 * 编辑时间
	 */
	@Column("editTime")
	private LocalDateTime editTime;

	/**
	 * 更新时间
	 */
	@Column("updateTime")
	private LocalDateTime updateTime;

	/**
	 * 逻辑删除
	 */
	@Column(value = "isDelete", isLogicDelete = true)
	private Integer isDelete;

}
