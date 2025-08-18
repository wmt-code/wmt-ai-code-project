package com.wmt.wmtaicode.ai.model;

import dev.langchain4j.model.output.structured.Description;
import lombok.Data;

@Description("生成HTML代码的结果")
@Data
public class HTMLCodeResult {
	@Description("html代码")
	private String htmlCode;
	@Description("代码描述")
	private String description;
}
