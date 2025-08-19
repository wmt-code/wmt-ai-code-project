package com.wmt.wmtaicode;

import com.wmt.wmtaicode.model.enums.FileTypeEnum;
import com.wmt.wmtaicode.service.FileService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;

@SpringBootTest
public class DemoTest {
	@Resource
	private FileService fileService;

	@Test
	void test() {
		File file = new File("H:/JavaProject/wmt-ai-code/wmt-ai-code-backend/tmp/code_deploy/gallery-025");
		// 上传gallery-025的所有文件
		File[] files = file.listFiles();
		if (files != null) {
			for (File f : files) {
				if (f.isFile()) {
					String fileUrl = fileService.uploadFile(f, "gallery-025", FileTypeEnum.DIRECTORY);
					System.out.println("上传文件: " + f.getName() + " 成功，访问URL: " + fileUrl);
				} else {
					System.out.println("跳过非文件: " + f.getName());
				}
			}
		} else {
			System.out.println("目录为空或不存在");
		}
	}
}
