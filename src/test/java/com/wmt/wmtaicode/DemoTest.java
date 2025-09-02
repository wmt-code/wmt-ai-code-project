package com.wmt.wmtaicode;


import com.wmt.wmtaicode.utils.WebScreenshotUtils;

public class DemoTest {
	public static void main(String[] args) {
		String imagePth = WebScreenshotUtils.takeScreenshot("https://www.baidu.com");
		System.out.println("screenshot path: " + imagePth);
	}
}
