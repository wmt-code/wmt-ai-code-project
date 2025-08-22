package com.wmt.wmtaicode.model.dto.chathistory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

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
public class AddChatHistoryReq implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;


	/**
	 * 消息内容
	 */
	private String message;

	/**
	 * 消息类型 user/ai
	 */
	private String messageType;

	/**
	 * 对应的应用ID
	 */
	private Long appId;

}
