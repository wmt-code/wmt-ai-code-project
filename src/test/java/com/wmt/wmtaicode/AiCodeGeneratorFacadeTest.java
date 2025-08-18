package com.wmt.wmtaicode;

import com.wmt.wmtaicode.ai.core.AiCodeGeneratorFacade;
import com.wmt.wmtaicode.ai.model.enums.CodeGenTypeEnum;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;

import java.io.File;
import java.util.List;

@SpringBootTest
class AiCodeGeneratorFacadeTest {

	@Resource
	private AiCodeGeneratorFacade aiCodeGeneratorFacade;

	@Test
	void generateAndSaveCode() {
		File file = aiCodeGeneratorFacade.generateAndSaveCode("任务记录网站", CodeGenTypeEnum.MULTI_FILE,1L);
	}

	@Test
	void generateAndSaveCodeStream() {
		Flux<String> flux = aiCodeGeneratorFacade.generateAndSaveCodeStream("任务记录网站", CodeGenTypeEnum.MULTI_FILE,1L);
		List<String> result = flux.collectList().block();
		Assertions.assertNotNull(result);
		String join = String.join("", result);
		Assertions.assertNotNull(join);
	}
}
