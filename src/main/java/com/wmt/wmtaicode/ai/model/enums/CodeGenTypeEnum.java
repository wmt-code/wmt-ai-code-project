package com.wmt.wmtaicode.ai.model.enums;

import lombok.Getter;

/**
 * 代码生成类型枚举
 */
@Getter
public enum CodeGenTypeEnum {
	HTML("原生HTML模式", "html"),
	MULTI_FILE("原生多文件模式", "multi_file"),
	VUE_PROJECT("vue项目模式", "vue_project");

	private final String text;
	private final String value;

	CodeGenTypeEnum(String text, String value) {
		this.text = text;
		this.value = value;
	}

	public static CodeGenTypeEnum getByValue(String value) {
		if (value != null) {
			for (CodeGenTypeEnum codeGenTypeEnum : CodeGenTypeEnum.values()) {
				if (codeGenTypeEnum.getValue().equals(value)) {
					return codeGenTypeEnum;
				}
			}
		}
		return null;
	}
}
