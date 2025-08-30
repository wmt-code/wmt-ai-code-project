package com.wmt.wmtaicode.model.enums;

import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import lombok.Getter;

/**
 * 消息类型枚举，区分用户消息和AI消息
 */
@Getter
public enum MessageTypeEnum {

	USER("用户消息", "user"),
	AI("AI消息", "ai");

	private final String text;

	private final String value;

	MessageTypeEnum(String text, String value) {
		this.text = text;
		this.value = value;
	}

	public static MessageTypeEnum getEnumByValue(String value) {
		if (ObjUtil.isEmpty(value)) {
			return null;
		}
		for (MessageTypeEnum anEnum : MessageTypeEnum.values()) {
			if (anEnum.value.equals(value)) {
				return anEnum;
			}
		}
		return null;
	}

	/**
	 * 消息类型校验
	 *
	 * @param messageType 消息类型
	 * @return true: 有效的消息类型; false: 无效的消息类型
	 */
	public static boolean isValid(String messageType) {
		if (StrUtil.isEmpty(messageType)) {
			return false;
		}
		for (MessageTypeEnum anEnum : MessageTypeEnum.values()) {
			if (anEnum.value.equals(messageType)) {
				return true;
			}
		}
		return false;
	}
}