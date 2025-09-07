package com.wmt.wmtaicode.langgraph4j.ai;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;

/**
 * 图片收集AI服务接口
 */
public interface ImageCollectionService {


	@SystemMessage(fromResource = "prompt/image-collection-system-prompt.txt")
	String collectImages(@UserMessage String prompt);
}
