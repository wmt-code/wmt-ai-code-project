package com.wmt.wmtaicode.ai.model.message;

import lombok.Getter;

/**
 * 流式消息类型枚举
 */
@Getter
public enum StreamMessageTypeEnum {
	AI_RESPONSE("ai_response", "ai响应消息"),
	TOOL_REQUEST("tool_request", "工具请求消息"),
	TOOL_EXECUTED("tool_executed", "工具执行结果消息");
	private final String value;
	private final String text;

	StreamMessageTypeEnum(String value, String text) {
		this.value = value;
		this.text = text;
	}

	public static StreamMessageTypeEnum getEnumByValue(String value) {
		if (value == null) {
			return null;
		}
		for (StreamMessageTypeEnum typeEnum : StreamMessageTypeEnum.values()) {
			if (typeEnum.getValue().equals(value)) {
				return typeEnum;
			}
		}
		return null;
	}
}
