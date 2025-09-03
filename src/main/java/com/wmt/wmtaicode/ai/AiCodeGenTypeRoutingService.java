package com.wmt.wmtaicode.ai;

import com.wmt.wmtaicode.ai.model.enums.CodeGenTypeEnum;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

/**
 * AI代码生成类型路由
 * 根据用户输入的提示器，判断使用哪种代码生成类型
 */
public interface AiCodeGenTypeRoutingService {
	@SystemMessage(fromResource = "prompt/codegen-routing-system-prompt.txt")
	CodeGenTypeEnum codeGenTypeRouting(@UserMessage String prompt);
}
