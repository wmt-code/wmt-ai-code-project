package com.wmt.wmtaicode.langgraph4j.tools;

import com.wmt.wmtaicode.langgraph4j.model.ImageResource;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
class LogoGeneratorToolTest {
	@Resource
	private LogoGeneratorTool logoGeneratorTool;

	@Test
	void generateLogos() {
		// 测试生成Logo
		List<ImageResource> logos = logoGeneratorTool.generateLogos("技术公司现代简约风格Logo");
		logos.forEach(logo ->
				System.out.println("Logo: " + logo.getDescription() + " - " + logo.getUrl())
		);
	}
}