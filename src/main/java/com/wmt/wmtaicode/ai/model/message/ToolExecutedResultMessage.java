package com.wmt.wmtaicode.ai.model.message;

import dev.langchain4j.service.tool.ToolExecution;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 工具执行结果消息
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class ToolExecutedResultMessage extends StreamMessage {
	private String id;
	private String name;
	private String arguments;
	private String result;

	public ToolExecutedResultMessage(ToolExecution toolExecution) {
		super(StreamMessageTypeEnum.TOOL_EXECUTED.getValue());
		this.id = toolExecution.request().id();
		this.name = toolExecution.request().name();
		this.arguments = toolExecution.request().arguments();
		this.result = toolExecution.result();
	}
}
