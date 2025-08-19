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
 * 应用表 实体类。
 *
 * @author ethereal
 * @since 2025-08-18
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("app")
public class App implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	/**
	 * 应用ID
	 */
	@Id(keyType = KeyType.Generator, value = KeyGenerators.snowFlakeId)
	private Long id;

	/**
	 * 应用名称
	 */
	@Column("appName")
	private String appName;

	/**
	 * 应用封面
	 */
	@Column("cover")
	private String cover;

	/**
	 * 初始prompt
	 */
	@Column("initPrompt")
	private String initPrompt;

	/**
	 * 代码生成的类型
	 */
	@Column("codeGenType")
	private String codeGenType;

	/**
	 * 应用部署的唯一标识
	 */
	@Column("deployKey")
	private String deployKey;
	/**
	 * 应用部署日期
	 */
	@Column("deployTime")
	private LocalDateTime deployTime;

	/**
	 * 创建用户ID
	 */
	@Column("userId")
	private Long userId;

	/**
	 * 应用优先级
	 */
	@Column("priority")
	private Integer priority;

	/**
	 * 应用编辑时间
	 */
	@Column("editTime")
	private LocalDateTime editTime;

	/**
	 * 应用创建时间
	 */
	@Column("createTime")
	private LocalDateTime createTime;

	/**
	 * 应用更新时间
	 */
	@Column("updateTime")
	private LocalDateTime updateTime;

	/**
	 * 逻辑删除 1 已删除 0 未删除
	 */
	@Column(value = "isDelete", isLogicDelete = true)
	private Integer isDelete;

}
