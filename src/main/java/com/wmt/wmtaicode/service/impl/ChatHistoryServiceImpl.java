package com.wmt.wmtaicode.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.wmt.wmtaicode.exception.BusinessException;
import com.wmt.wmtaicode.exception.ErrorCode;
import com.wmt.wmtaicode.exception.ThrowUtils;
import com.wmt.wmtaicode.model.dto.chathistory.AddChatHistoryReq;
import com.wmt.wmtaicode.model.dto.chathistory.ChatHistoryQueryReq;
import com.wmt.wmtaicode.model.entity.App;
import com.wmt.wmtaicode.model.entity.ChatHistory;
import com.wmt.wmtaicode.mapper.ChatHistoryMapper;
import com.wmt.wmtaicode.model.enums.MessageTypeEnum;
import com.wmt.wmtaicode.model.enums.UserRoleEnum;
import com.wmt.wmtaicode.model.vo.UserVO;
import com.wmt.wmtaicode.service.AppService;
import com.wmt.wmtaicode.service.ChatHistoryService;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 对话历史记录表 服务层实现。
 *
 * @author ethereal
 * @since 2025-08-23
 */
@Slf4j
@Service
public class ChatHistoryServiceImpl extends ServiceImpl<ChatHistoryMapper, ChatHistory> implements ChatHistoryService {
	@Resource
	private AppService appService;

	/**
	 * 创建对话历史记录。
	 *
	 * @param addChatHistoryReq 对话历史记录请求参数
	 * @param loginUser         登录用户信息
	 * @return 是否成功
	 */
	@Override
	public boolean addChatHistory(AddChatHistoryReq addChatHistoryReq, UserVO loginUser) {
		// 参数校验
		String message = addChatHistoryReq.getMessage();
		String messageType = addChatHistoryReq.getMessageType();
		Long appId = addChatHistoryReq.getAppId();
		ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID无效");
		if (StrUtil.hasBlank(message, messageType)) {

			throw new BusinessException(ErrorCode.PARAMS_ERROR, "消息内容和消息类型不能为空");
		}
		// 消息类型校验
		if (!MessageTypeEnum.isValid(messageType)) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "无效的消息类型");
		}
		// 创建对话历史记录
		ChatHistory chatHistory = new ChatHistory();
		BeanUtil.copyProperties(addChatHistoryReq, chatHistory);
		chatHistory.setUserId(loginUser.getId());
		boolean save = this.save(chatHistory);
		ThrowUtils.throwIf(!save, ErrorCode.OPERATION_ERROR, "保存对话历史记录失败");
		return true;
	}

	/**
	 * 删除指定应用的所有对话历史记录。
	 *
	 * @param appId 应用ID
	 * @return 是否成功
	 */
	@Override
	public boolean deleteByAppId(long appId) {
		QueryWrapper queryWrapper = QueryWrapper.create()
				.eq(ChatHistory::getAppId, appId);
		return this.remove(queryWrapper);
	}

	/**
	 * 获取查询条件包装器。
	 *
	 * @param chatHistoryQueryReq 查询请求参数
	 * @return QueryWrapper 查询条件包装器
	 */
	@Override
	public QueryWrapper getQueryWrapper(ChatHistoryQueryReq chatHistoryQueryReq) {
		Long id = chatHistoryQueryReq.getId();
		String message = chatHistoryQueryReq.getMessage();
		String messageType = chatHistoryQueryReq.getMessageType();
		Long appId = chatHistoryQueryReq.getAppId();
		Long userId = chatHistoryQueryReq.getUserId();
		LocalDateTime lastCreateTime = chatHistoryQueryReq.getLastCreateTime();
		String sortField = chatHistoryQueryReq.getSortField();
		String sortOrder = chatHistoryQueryReq.getSortOrder();
		QueryWrapper queryWrapper = QueryWrapper.create()
				.eq(ChatHistory::getId, id)
				.like(ChatHistory::getMessage, message)
				.eq(ChatHistory::getMessageType, messageType)
				.eq(ChatHistory::getAppId, appId)
				.eq(ChatHistory::getUserId, userId);
		if (lastCreateTime != null) {
			queryWrapper.lt(ChatHistory::getCreateTime, lastCreateTime);
		}
		if (StrUtil.isNotBlank(sortField) && StrUtil.isNotBlank(sortOrder)) {
			queryWrapper.orderBy(sortField, "ascend".equalsIgnoreCase(sortOrder));
		} else {
			queryWrapper.orderBy(ChatHistory::getCreateTime, false);
		}
		return queryWrapper;
	}

	/**
	 * 分页查询指定应用的对话历史记录。
	 *
	 * @param appId          应用ID
	 * @param pageSize       页大小
	 * @param lastCreateTime 游标查询 - 最后一条记录的创建时间
	 * @param userVO         当前用户信息
	 * @return 分页结果
	 */
	@Override
	public Page<ChatHistory> listAppChatHistoryByPage(long appId, int pageSize, LocalDateTime lastCreateTime,
													  UserVO userVO) {
		// 参数校验
		ThrowUtils.throwIf(appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID无效");
		ThrowUtils.throwIf(pageSize <= 0, ErrorCode.PARAMS_ERROR, "页大小无效");
		// 校验权限
		App app = appService.getById(appId);
		ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
		boolean isCreator = app.getUserId().equals(userVO.getId());
		boolean isAdmin = UserRoleEnum.ADMIN.getValue().equals(userVO.getUserRole());
		if (!isCreator && !isAdmin) {
			throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "无权限访问该应用的对话历史记录");
		}
		// 构建查询条件
		ChatHistoryQueryReq chatHistoryQueryReq = new ChatHistoryQueryReq();
		chatHistoryQueryReq.setAppId(appId);
		chatHistoryQueryReq.setLastCreateTime(lastCreateTime);
		QueryWrapper queryWrapper = this.getQueryWrapper(chatHistoryQueryReq);
		return this.page(Page.of(1, pageSize), queryWrapper);
	}

	/**
	 * 从数据库中加载对话历史记录到内存中。
	 *
	 * @param appId      应用ID
	 * @param chatMemory 内存中的对话记录
	 * @param maxCount   最大加载数量
	 * @return 实际加载数量
	 */
	@Override
	public int loadChatHistoryToMemory(long appId, MessageWindowChatMemory chatMemory, int maxCount) {
		try {
			QueryWrapper queryWrapper = QueryWrapper.create()
					.eq(ChatHistory::getAppId, appId)
					.orderBy(ChatHistory::getCreateTime, false)
					.limit(1, maxCount);
			List<ChatHistory> historyList = this.list(queryWrapper);
			if (CollUtil.isEmpty(historyList)) {
				return 0;
			}
			// 倒序插入内存
			CollUtil.reverse(historyList);
			AtomicInteger loadedCount = new AtomicInteger();
			// 清除内存
			chatMemory.clear();
			historyList.forEach(history -> {
				if (MessageTypeEnum.USER.getValue().equals(history.getMessageType())) {
					chatMemory.add(UserMessage.from(history.getMessage()));
					loadedCount.getAndIncrement();
				} else if (MessageTypeEnum.AI.getValue().equals(history.getMessageType())) {
					chatMemory.add(AiMessage.from(history.getMessage()));
					loadedCount.getAndIncrement();
				}
			});
			log.info("加载对话历史记录到内存，应用ID: {}, 加载数量: {}", appId, loadedCount.get());
			return loadedCount.get();
		} catch (Exception e) {
			log.error("加载对话历史记录到内存失败，应用ID: {}, 错误信息: {}", appId, e.getMessage());
			return 0;
		}
	}
}
