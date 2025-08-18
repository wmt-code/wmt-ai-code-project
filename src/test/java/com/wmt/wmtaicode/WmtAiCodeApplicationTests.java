package com.wmt.wmtaicode;

import com.wmt.wmtaicode.ai.AiCodeGeneratorService;
import com.wmt.wmtaicode.ai.model.HTMLCodeResult;
import com.wmt.wmtaicode.ai.model.MultiFileCodeResult;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class WmtAiCodeApplicationTests {
	@Resource
	private AiCodeGeneratorService aiCodeGeneratorService;

	@Test
	void generatorHTMLCode() {
		HTMLCodeResult htmlCodeResult = aiCodeGeneratorService.generateHTMLCode("做个博客首页");
		Assertions.assertNotNull(htmlCodeResult);
	}
	@Test
	void generatorMultiFileCode() {
		MultiFileCodeResult multiFileCodeResult = aiCodeGeneratorService.generateMutiFileCode("做个博客首页");
		Assertions.assertNotNull(multiFileCodeResult);
	}
}
