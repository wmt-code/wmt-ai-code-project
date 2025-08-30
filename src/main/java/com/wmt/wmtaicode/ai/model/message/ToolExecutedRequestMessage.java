package com.wmt.wmtaicode.ai.model.message;

import dev.langchain4j.agent.tool.ToolExecutionRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 工具请求消息类
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class ToolExecutedRequestMessage extends StreamMessage {
	private String id;
	private String name;
	private String arguments;

	public ToolExecutedRequestMessage(ToolExecutionRequest toolExecutionRequest) {
		super(StreamMessageTypeEnum.TOOL_REQUEST.getValue());
		this.id = toolExecutionRequest.id();
		this.name = toolExecutionRequest.name();
		this.arguments = toolExecutionRequest.arguments();
	}
}
