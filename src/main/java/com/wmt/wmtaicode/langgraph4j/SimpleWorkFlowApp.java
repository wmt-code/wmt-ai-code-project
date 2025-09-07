package com.wmt.wmtaicode.langgraph4j;

import lombok.extern.slf4j.Slf4j;
import org.bsc.langgraph4j.CompiledGraph;
import org.bsc.langgraph4j.GraphRepresentation;
import org.bsc.langgraph4j.GraphStateException;
import org.bsc.langgraph4j.action.AsyncNodeAction;
import org.bsc.langgraph4j.prebuilt.MessagesState;
import org.bsc.langgraph4j.prebuilt.MessagesStateGraph;

import java.util.Map;

import static org.bsc.langgraph4j.StateGraph.END;
import static org.bsc.langgraph4j.StateGraph.START;

/**
 * 简化版工作流应用-使用MessageState
 */
@Slf4j
public class SimpleWorkFlowApp {
	static AsyncNodeAction<MessagesState<String>> makeNode(String message) {
		return AsyncNodeAction.node_async(state -> {
			log.info("执行节点: {}", message);
			return Map.of("messages", message);
		});
	}

	public static void main(String[] args) throws GraphStateException {
		CompiledGraph<MessagesState<String>> compiledGraph = new MessagesStateGraph<String>()
				// 添加节点
				.addNode("image_collector", makeNode("获取图片素材"))
				.addNode("prompt_enhancer", makeNode("提示词增强"))
				.addNode("router", makeNode("智能路由选择"))
				.addNode("code_generator", makeNode("生成代码"))
				.addNode("project_builder", makeNode("构建项目"))
				// 添加边
				.addEdge(START, "image_collector") // 开始->图片素材收集
				.addEdge("image_collector", "prompt_enhancer")  // 图片素材收集->优化提示词
				.addEdge("prompt_enhancer", "router") // 优化提示词->智能路由选择
				.addEdge("router", "code_generator") // 智能路由选择->代码生成
				.addEdge("code_generator", "project_builder") // 代码生成->构建项目
				.addEdge("project_builder", END) // 构建项目->结束
				.compile();

		log.info("工作流开始执行...");
		GraphRepresentation graph = compiledGraph.getGraph(GraphRepresentation.Type.MERMAID);
		log.info("工作流图: \n{}", graph.content());
		// 开始执行
		for (var item : compiledGraph.stream(Map.of())) {
			log.info("当前状态: {}", item);
		}
		log.info("工作流执行结束.");
	}
}
