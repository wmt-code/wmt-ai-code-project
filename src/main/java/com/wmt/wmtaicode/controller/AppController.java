package com.wmt.wmtaicode.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.mybatisflex.core.paginate.Page;
import com.mybatisflex.core.query.QueryWrapper;
import com.wmt.wmtaicode.ai.model.enums.CodeGenTypeEnum;
import com.wmt.wmtaicode.annotation.AuthCheck;
import com.wmt.wmtaicode.common.BaseResponse;
import com.wmt.wmtaicode.common.DeleteRequest;
import com.wmt.wmtaicode.common.ResultUtils;
import com.wmt.wmtaicode.constant.AppConstant;
import com.wmt.wmtaicode.constant.UserConstant;
import com.wmt.wmtaicode.exception.BusinessException;
import com.wmt.wmtaicode.exception.ErrorCode;
import com.wmt.wmtaicode.exception.ThrowUtils;
import com.wmt.wmtaicode.model.dto.app.*;
import com.wmt.wmtaicode.model.dto.chathistory.AddChatHistoryReq;
import com.wmt.wmtaicode.model.entity.App;
import com.wmt.wmtaicode.model.enums.FileTypeEnum;
import com.wmt.wmtaicode.model.enums.MessageTypeEnum;
import com.wmt.wmtaicode.model.vo.AppVO;
import com.wmt.wmtaicode.model.vo.UserVO;
import com.wmt.wmtaicode.service.AppService;
import com.wmt.wmtaicode.service.ChatHistoryService;
import com.wmt.wmtaicode.service.FileService;
import com.wmt.wmtaicode.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.View;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 应用表 控制层。
 *
 * @author ethereal
 * @since 2025-08-18
 */
@Slf4j
@RestController
@RequestMapping("/app")
public class AppController {
	@Resource
	private UserService userService;
	@Resource
	private AppService appService;
	@Resource
	private FileService fileService;
	@Resource
	private ChatHistoryService chatHistoryService;
	@Autowired
	private View error;

	/**
	 * 添加应用
	 *
	 * @param addRequest 应用添加请求
	 * @param request    HttpServletRequest
	 * @return 应用ID
	 */
	@PostMapping("/add")
	public BaseResponse<Long> addApp(@RequestBody AppAddReq addRequest, HttpServletRequest request) {
		ThrowUtils.throwIf(addRequest == null, ErrorCode.PARAMS_ERROR);
		String initPrompt = addRequest.getInitPrompt();
		if (StrUtil.isBlank(initPrompt)) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "初始化提示词不能为空");
		}
		UserVO loginUser = userService.getLoginUser(request);
		App app = new App();
		BeanUtil.copyProperties(addRequest, app);
		app.setUserId(loginUser.getId());
		// 截取前12个字符作为应用名称
		app.setAppName(initPrompt.substring(0, Math.min(initPrompt.length(), 12)));
		app.setCodeGenType(CodeGenTypeEnum.MULTI_FILE.getValue());
		boolean save = appService.save(app);
		ThrowUtils.throwIf(!save, ErrorCode.OPERATION_ERROR, "添加应用失败");
		return ResultUtils.success(app.getId());
	}

	/**
	 * 更新应用
	 *
	 * @param updateRequest 更新应用请求
	 * @param request       获取登录用户
	 * @return 操作是否成功
	 */
	@PostMapping("/update")
	public BaseResponse<Boolean> updateApp(@RequestBody AppUpdateReq updateRequest, HttpServletRequest request) {
		if (updateRequest == null || updateRequest.getId() == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		if (StrUtil.isBlank(updateRequest.getAppName())) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "应用名称不能为空");
		}
		App oldApp = appService.getById(updateRequest.getId());
		ThrowUtils.throwIf(oldApp == null, ErrorCode.NOT_FOUND_ERROR);
		UserVO loginUser = userService.getLoginUser(request);
		if (!oldApp.getUserId().equals(loginUser.getId())) {
			throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
		}
		App app = new App();
		BeanUtil.copyProperties(updateRequest, app);
		app.setEditTime(LocalDateTime.now());
		boolean res = appService.updateById(app);
		ThrowUtils.throwIf(!res, ErrorCode.OPERATION_ERROR, "更新应用失败");
		return ResultUtils.success(true);
	}

	/**
	 * 删除应用
	 *
	 * @param deleteRequest 删除应用请求
	 * @param request       获取登录用户
	 * @return 操作是否成功
	 */
	@PostMapping("/delete")
	public BaseResponse<Boolean> deleteApp(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
		if (deleteRequest.getId() == null || deleteRequest.getId() <= 0) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		App oldApp = appService.getById(deleteRequest.getId());
		ThrowUtils.throwIf(oldApp == null, ErrorCode.NOT_FOUND_ERROR);
		UserVO loginUser = userService.getLoginUser(request);
		if (!oldApp.getUserId().equals(loginUser.getId())) {
			throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
		}
		boolean res = appService.removeById(deleteRequest.getId());
		ThrowUtils.throwIf(!res, ErrorCode.OPERATION_ERROR, "删除应用失败");
		// 删除应用对应的对话历史记录
		try {
			chatHistoryService.deleteByAppId(deleteRequest.getId());
		} catch (Exception e) {
			log.error("删除应用 {} 的对话历史记录失败: {}", deleteRequest.getId(), e.getMessage());
		}
		return ResultUtils.success(true);
	}

	/**
	 * 获取应用信息
	 *
	 * @param id 应用ID
	 * @return 应用信息
	 */
	@GetMapping("/get/vo")
	public BaseResponse<AppVO> getAppVoById(Long id) {
		if (id == null || id <= 0) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		App app = appService.getById(id);
		if (app == null) {
			throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
		}
		AppVO appVO = appService.getAppVO(app);
		return ResultUtils.success(appVO);
	}

	/**
	 * 获取当前用户的应用列表分页
	 *
	 * @param appQueryReq 应用查询请求
	 * @param request     HttpServletRequest
	 * @return 应用列表分页
	 */
	@PostMapping("/my/list/page/vo")
	public BaseResponse<Page<AppVO>> getMyAppListPage(@RequestBody AppQueryReq appQueryReq,
													  HttpServletRequest request) {

		ThrowUtils.throwIf(appQueryReq == null, ErrorCode.PARAMS_ERROR);
		int pageSize = appQueryReq.getPageSize();
		int current = appQueryReq.getCurrent();
		ThrowUtils.throwIf(pageSize > 20, ErrorCode.PARAMS_ERROR, "每页最多20条数据");
		UserVO loginUser = userService.getLoginUser(request);
		// 限制只能查询自己的应用
		appQueryReq.setUserId(loginUser.getId());
		QueryWrapper queryWrapper = appService.getQueryWrapper(appQueryReq);
		Page<App> appPage = appService.page(Page.of(current, pageSize), queryWrapper);
		Page<AppVO> appVOPage = new Page<>(appPage.getPageNumber(), appPage.getPageSize(), appPage.getTotalRow());
		appVOPage.setRecords(appService.getAppVOList(appPage.getRecords()));
		return ResultUtils.success(appVOPage);
	}

	/**
	 * 获取当前用户的精选应用列表分页
	 *
	 * @param appQueryReq 应用查询请求
	 * @return 应用列表分页
	 */
	@PostMapping("/good/list/page/vo")
	public BaseResponse<Page<AppVO>> getGoodAppListPage(@RequestBody AppQueryReq appQueryReq) {

		ThrowUtils.throwIf(appQueryReq == null, ErrorCode.PARAMS_ERROR);
		int pageSize = appQueryReq.getPageSize();
		int current = appQueryReq.getCurrent();
		ThrowUtils.throwIf(pageSize > 20, ErrorCode.PARAMS_ERROR, "每页最多20条数据");
		appQueryReq.setPriority(AppConstant.GOOD_APP_PRIORITY);
		QueryWrapper queryWrapper = appService.getQueryWrapper(appQueryReq);
		Page<App> appPage = appService.page(Page.of(current, pageSize), queryWrapper);
		Page<AppVO> appVOPage = new Page<>(appPage.getPageNumber(), appPage.getPageSize(), appPage.getTotalRow());
		appVOPage.setRecords(appService.getAppVOList(appPage.getRecords()));
		return ResultUtils.success(appVOPage);
	}

	/**
	 * 管理员删除应用
	 *
	 * @param deleteRequest 删除请求
	 * @return 删除结果
	 */
	@PostMapping("/admin/delete")
	@AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
	public BaseResponse<Boolean> deleteAppByAdmin(@RequestBody DeleteRequest deleteRequest) {
		if (deleteRequest == null || deleteRequest.getId() <= 0) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		long id = deleteRequest.getId();
		// 判断是否存在
		App oldApp = appService.getById(id);
		ThrowUtils.throwIf(oldApp == null, ErrorCode.NOT_FOUND_ERROR);
		boolean result = appService.removeById(id);
		return ResultUtils.success(result);
	}

	/**
	 * 管理员更新应用
	 *
	 * @param appAdminUpdateReq 更新请求
	 * @return 更新结果
	 */
	@PostMapping("/admin/update")
	@AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
	public BaseResponse<Boolean> updateAppByAdmin(@RequestBody AppAdminUpdateReq appAdminUpdateReq) {
		if (appAdminUpdateReq == null || appAdminUpdateReq.getId() == null) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR);
		}
		long id = appAdminUpdateReq.getId();
		// 判断是否存在
		App oldApp = appService.getById(id);
		ThrowUtils.throwIf(oldApp == null, ErrorCode.NOT_FOUND_ERROR);
		App app = new App();
		BeanUtil.copyProperties(appAdminUpdateReq, app);
		// 设置编辑时间
		app.setEditTime(LocalDateTime.now());
		boolean result = appService.updateById(app);
		ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
		return ResultUtils.success(true);
	}

	/**
	 * 管理员分页获取应用列表
	 *
	 * @param appQueryReq 查询请求
	 * @return 应用列表
	 */
	@PostMapping("/admin/list/page/vo")
	@AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
	public BaseResponse<Page<AppVO>> listAppVOByPageByAdmin(@RequestBody AppQueryReq appQueryReq) {
		ThrowUtils.throwIf(appQueryReq == null, ErrorCode.PARAMS_ERROR);
		long pageNum = appQueryReq.getCurrent();
		long pageSize = appQueryReq.getPageSize();
		QueryWrapper queryWrapper = appService.getQueryWrapper(appQueryReq);
		Page<App> appPage = appService.page(Page.of(pageNum, pageSize), queryWrapper);
		// 数据封装
		Page<AppVO> appVOPage = new Page<>(pageNum, pageSize, appPage.getTotalRow());
		List<AppVO> appVOList = appService.getAppVOList(appPage.getRecords());
		appVOPage.setRecords(appVOList);
		return ResultUtils.success(appVOPage);
	}

	/**
	 * 管理员根据 id 获取应用详情
	 *
	 * @param id 应用 id
	 * @return 应用详情
	 */
	@GetMapping("/admin/get/vo")
	@AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
	public BaseResponse<AppVO> getAppVOByIdByAdmin(long id) {
		ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
		// 查询数据库
		App app = appService.getById(id);
		ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR);
		// 获取封装类
		return ResultUtils.success(appService.getAppVO(app));
	}

	/**
	 * 与AI对话生成代码
	 *
	 * @param appId       应用ID
	 * @param chatMessage 用户提示词
	 * @param request     HttpServletRequest
	 * @return 代码生成结果的Flux流
	 */
	@GetMapping(value = "/chat/gen/code", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
	public Flux<ServerSentEvent<String>> chatToGenCode(@RequestParam("appId") Long appId,
													   @RequestParam("chatMessage") String chatMessage,
													   HttpServletRequest request) {
		ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID无效");
		ThrowUtils.throwIf(StrUtil.isBlank(chatMessage), ErrorCode.PARAMS_ERROR, "用户提示词不能为空");
		UserVO loginUser = userService.getLoginUser(request);// 确保用户已登录
		//保存用户的提示词到聊天记录
		AddChatHistoryReq addChatHistoryReq = new AddChatHistoryReq();
		addChatHistoryReq.setAppId(appId);
		addChatHistoryReq.setMessage(chatMessage);
		addChatHistoryReq.setMessageType(MessageTypeEnum.USER.getValue());
		chatHistoryService.addChatHistory(addChatHistoryReq, loginUser);
		Flux<String> contentFlux = appService.chatToGenCode(appId, chatMessage, loginUser);
		StringBuilder aiResponseBuilder = new StringBuilder();
		// 额外封装成ServerSent Events,将原始数据放入json的d字段，解决空格问题
		return contentFlux
				.map(chunk -> {
					aiResponseBuilder.append(chunk);
					Map<String, String> map = Map.of("d", chunk);
					String jsonData = JSONUtil.toJsonStr(map);
					return ServerSentEvent.<String>builder()
							.data(jsonData)
							.build();
				}).concatWith(Mono.just(
								// 发送完成事件
								ServerSentEvent.<String>builder()
										.event("done")
										.data("")
										.build()
						)
				)
				.doOnComplete(() -> {
					// 完成后保存AI生成的代码到聊天记录
					String aiContent = aiResponseBuilder.toString();
					if (StrUtil.isNotBlank(aiContent)) {
						addChatHistoryReq.setAppId(appId);
						addChatHistoryReq.setMessage(aiContent);
						addChatHistoryReq.setMessageType(MessageTypeEnum.AI.getValue());
						chatHistoryService.addChatHistory(addChatHistoryReq, loginUser);
					}
				})
				.doOnError(error -> {
					// 记录错误的消息
					addChatHistoryReq.setAppId(appId);
					addChatHistoryReq.setMessage("AI生成代码失败: " + error.getMessage());
					addChatHistoryReq.setMessageType(MessageTypeEnum.ERROR.getValue());
					chatHistoryService.addChatHistory(addChatHistoryReq, loginUser);
				})
				;
	}

	/**
	 * 应用部署
	 *
	 * @param appDeployReq 应用部署请求
	 * @param request      HttpServletRequest
	 * @return 部署结果的URL
	 */
	@PostMapping("/deploy")
	public BaseResponse<String> deployApp(@RequestBody AppDeployReq appDeployReq, HttpServletRequest request) {
		Long appId = appDeployReq.getAppId();
		ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR);
		String url = appService.deployApp(appDeployReq, request);
		return ResultUtils.success(url);
	}

	/**
	 * 上传应用封面图片
	 *
	 * @param file
	 * @param request
	 * @return
	 */
	@PostMapping("/uploadAppCover")
	public BaseResponse<String> uploadAppCover(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
		ThrowUtils.throwIf(file == null || file.isEmpty(), ErrorCode.PARAMS_ERROR, "上传文件不能为空");
		UserVO loginUser = userService.getLoginUser(request);
		ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR, "用户未登录");
		String coverUrl = fileService.uploadFile(file, "appCover", FileTypeEnum.IMAGE);
		return ResultUtils.success(coverUrl);
	}
}
