package com.wmt.wmtaicode.controller;

import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.wmt.wmtaicode.annotation.AuthCheck;
import com.wmt.wmtaicode.common.BaseResponse;
import com.wmt.wmtaicode.common.ResultUtils;
import com.wmt.wmtaicode.constant.UserConstant;
import com.wmt.wmtaicode.exception.ErrorCode;
import com.wmt.wmtaicode.exception.ThrowUtils;
import com.wmt.wmtaicode.model.dto.chathistory.ChatHistoryQueryReq;
import com.wmt.wmtaicode.model.entity.ChatHistory;
import com.wmt.wmtaicode.model.vo.UserVO;
import com.wmt.wmtaicode.service.ChatHistoryService;
import com.wmt.wmtaicode.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

/**
 * 对话历史记录表 控制层。
 *
 * @author ethereal
 * @since 2025-08-23
 */
@RestController
@RequestMapping("/chatHistory")
public class ChatHistoryController {
	@Resource
	private UserService userService;
	@Resource
	private ChatHistoryService chatHistoryService;

	/**
	 * 分页查询应用的对话历史记录。（游标查询）
	 *
	 * @param appId          应用ID
	 * @param pageSize       每页大小
	 * @param lastCreateTime 上次查询的最后创建时间（游标）
	 * @param request        HTTP请求对象，用于获取登录用户信息
	 * @return 分页查询结果
	 */
	@GetMapping("/get/{appId}")
	public BaseResponse<Page<ChatHistory>> listAppChatHistoryByPage(@PathVariable long appId,
																	@RequestParam(defaultValue = "10") int pageSize,
																	@RequestParam(required = false) LocalDateTime lastCreateTime,
																	HttpServletRequest request) {
		// 获取登录用户信息
		UserVO loginUser = userService.getLoginUser(request);
		return ResultUtils.success(chatHistoryService.listAppChatHistoryByPage(appId, pageSize, lastCreateTime,
				loginUser));
	}

	/**
	 * 管理员分页查询所有对话历史
	 *
	 * @param chatHistoryQueryReq 查询请求
	 * @return 对话历史分页
	 */
	@PostMapping("/admin/list/page/vo")
	@AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
	public BaseResponse<Page<ChatHistory>> listAllChatHistoryByPageForAdmin(@RequestBody ChatHistoryQueryReq chatHistoryQueryReq) {
		ThrowUtils.throwIf(chatHistoryQueryReq == null, ErrorCode.PARAMS_ERROR);
		long current = chatHistoryQueryReq.getCurrent();
		long pageSize = chatHistoryQueryReq.getPageSize();
		// 查询数据
		QueryWrapper queryWrapper = chatHistoryService.getQueryWrapper(chatHistoryQueryReq);
		Page<ChatHistory> result = chatHistoryService.page(Page.of(current, pageSize), queryWrapper);
		return ResultUtils.success(result);
	}


}
