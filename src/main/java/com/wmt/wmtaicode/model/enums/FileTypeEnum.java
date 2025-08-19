package com.wmt.wmtaicode.model.enums;

import lombok.Getter;

@Getter
public enum FileTypeEnum {
	/**
	 * 目录
	 */
	DIRECTORY("目录", "directory"),

	/**
	 * 图片文件
	 */
	IMAGE("图片文件", "image"),

	/**
	 * 文本文件
	 */
	TEXT("文本文件", "text"),

	/**
	 * 音频文件
	 */
	AUDIO("音频文件", "audio"),

	/**
	 * 视频文件
	 */
	VIDEO("视频文件", "video");

	private final String text;
	private final String value;

	FileTypeEnum(String text, String value) {
		this.text = text;
		this.value = value;
	}

	public static FileTypeEnum getByValue(String value) {
		if (value != null) {
			for (FileTypeEnum fileTypeEnum : FileTypeEnum.values()) {
				if (fileTypeEnum.getValue().equals(value)) {
					return fileTypeEnum;
				}
			}
		}
		return null;
	}
}
