package com.wmt.wmtaicode.service;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.wmt.wmtaicode.model.dto.chathistory.AddChatHistoryReq;
import com.wmt.wmtaicode.model.dto.chathistory.ChatHistoryQueryReq;
import com.wmt.wmtaicode.model.entity.ChatHistory;
import com.wmt.wmtaicode.model.vo.UserVO;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;

import java.time.LocalDateTime;

/**
 * 对话历史记录表 服务层。
 *
 * @author ethereal
 * @since 2025-08-23
 */
public interface ChatHistoryService extends IService<ChatHistory> {

	boolean addChatHistory(AddChatHistoryReq addChatHistoryReq, UserVO loginUser);

	boolean deleteByAppId(long appId);

	QueryWrapper getQueryWrapper(ChatHistoryQueryReq chatHistoryQueryReq);

	Page<ChatHistory> listAppChatHistoryByPage(long appId, int pageSize, LocalDateTime lastCreateTime,
											   UserVO userVO);

	int loadChatHistoryToMemory(long appId, MessageWindowChatMemory chatMemory, int maxCount);
}
