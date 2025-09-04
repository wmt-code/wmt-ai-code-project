package com.wmt.wmtaicode.ai.tools;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class ToolManager {
	// 工具名称 工具实例
	private final Map<String, BaseTool> toolMap = new HashMap<>();
	/**
	 * 获取所有工具实例
	 */
	@Resource
	private BaseTool[] tools;

	/**
	 * 将所有工具实例放入map
	 */
	@PostConstruct
	private void init() {
		for (BaseTool tool : tools) {
			toolMap.put(tool.getToolName(), tool);
			log.info("注册工具: {}", tool.getToolName());
		}
	}

	public BaseTool getToolByName(String toolName) {
		return toolMap.get(toolName);
	}

	public BaseTool[] getAllTools() {
		return tools;
	}
}
