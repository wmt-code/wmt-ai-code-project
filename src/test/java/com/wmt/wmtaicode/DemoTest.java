package com.wmt.wmtaicode;

import cn.hutool.core.io.FileUtil;

public class DemoTest {
	public static void main(String[] args) {
		String filePath = "/src/App.vue";
		String suffix = FileUtil.getSuffix(filePath);
		System.out.println(suffix);
	}
}
