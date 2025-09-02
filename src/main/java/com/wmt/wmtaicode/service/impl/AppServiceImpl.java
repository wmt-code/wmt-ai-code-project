package com.wmt.wmtaicode.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.wmt.wmtaicode.core.AiCodeGeneratorFacade;
import com.wmt.wmtaicode.core.builder.VueProjectBuilder;
import com.wmt.wmtaicode.core.handler.StreamHandlerExecutor;
import com.wmt.wmtaicode.ai.model.enums.CodeGenTypeEnum;
import com.wmt.wmtaicode.constant.AppConstant;
import com.wmt.wmtaicode.exception.BusinessException;
import com.wmt.wmtaicode.exception.ErrorCode;
import com.wmt.wmtaicode.exception.ThrowUtils;
import com.wmt.wmtaicode.mapper.AppMapper;
import com.wmt.wmtaicode.model.dto.app.AppDeployReq;
import com.wmt.wmtaicode.model.dto.app.AppQueryReq;
import com.wmt.wmtaicode.model.entity.App;
import com.wmt.wmtaicode.model.entity.User;
import com.wmt.wmtaicode.model.vo.AppVO;
import com.wmt.wmtaicode.model.vo.UserVO;
import com.wmt.wmtaicode.service.AppService;
import com.wmt.wmtaicode.service.ChatHistoryService;
import com.wmt.wmtaicode.service.ScreenshotService;
import com.wmt.wmtaicode.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 应用表 服务层实现。
 *
 * @author ethereal
 * @since 2025-08-18
 */
@Service
public class AppServiceImpl extends ServiceImpl<AppMapper, App> implements AppService {
	@Resource
	private UserService userService;
	@Resource
	private AiCodeGeneratorFacade aiCodeGeneratorFacade;
	@Resource
	private StreamHandlerExecutor streamHandlerExecutor;
	@Resource
	@Lazy
	private ChatHistoryService chatHistoryService;
	@Resource
	private VueProjectBuilder vueProjectBuilder;
	@Resource
	private ScreenshotService screenshotService;

	@Override

	public AppVO getAppVO(App app) {
		if (app == null) {
			return null;
		}
		AppVO appVO = new AppVO();
		BeanUtils.copyProperties(app, appVO);
		Long userId = app.getUserId();
		if (userId != null) {
			User user = userService.getById(userId);
			UserVO userVo = userService.getUserVo(user);
			appVO.setUser(userVo);
		}
		return appVO;
	}

	@Override
	public List<AppVO> getAppVOList(List<App> appList) {
		if (CollUtil.isEmpty(appList)) {
			return Collections.emptyList();
		}
		// 查出所有用户
		Set<Long> userIDSet = appList.stream().map(App::getUserId)
				.collect(Collectors.toSet());
		Map<Long, UserVO> userVOMap = userService.listByIds(userIDSet).stream()
				.collect(Collectors.toMap(User::getId, userService::getUserVo));
		return appList.stream()
				.map(app -> {
					AppVO appVO = new AppVO();
					BeanUtil.copyProperties(app, appVO);
					appVO.setUser(userVOMap.get(app.getUserId()));
					return appVO;
				}).collect(Collectors.toList());

	}

	@Override
	public QueryWrapper getQueryWrapper(AppQueryReq appQueryReq) {
		ThrowUtils.throwIf(appQueryReq == null, ErrorCode.PARAMS_ERROR);
		Long id = appQueryReq.getId();
		String appName = appQueryReq.getAppName();
		String cover = appQueryReq.getCover();
		String initPrompt = appQueryReq.getInitPrompt();
		String codeGenType = appQueryReq.getCodeGenType();
		String deployKey = appQueryReq.getDeployKey();
		Integer priority = appQueryReq.getPriority();
		Long userId = appQueryReq.getUserId();
		String sortField = appQueryReq.getSortField();
		String sortOrder = appQueryReq.getSortOrder();
		return QueryWrapper.create()
				.eq(App::getId, id)
				.like(App::getAppName, appName)
				.like(App::getCover, cover)
				.like(App::getInitPrompt, initPrompt)
				.eq(App::getCodeGenType, codeGenType)
				.eq(App::getDeployKey, deployKey)
				.eq(App::getPriority, priority)
				.eq(App::getUserId, userId)
				.orderBy(sortField, "ascend".equalsIgnoreCase(sortOrder));
	}

	@Override
	public Flux<String> chatToGenCode(Long appId, String chatMessage, UserVO loginUser) {
		ThrowUtils.throwIf(appId == null, ErrorCode.PARAMS_ERROR, "应用ID不能为空");
		ThrowUtils.throwIf(chatMessage == null || chatMessage.isBlank(), ErrorCode.PARAMS_ERROR, "用户提示词不能为空");
		App app = this.getById(appId);
		ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
		ThrowUtils.throwIf(!app.getUserId().equals(loginUser.getId()), ErrorCode.NO_AUTH_ERROR, "无权限访问该应用");
		String codeGenType = app.getCodeGenType();
		CodeGenTypeEnum codeGenTypeEnum = CodeGenTypeEnum.getByValue(codeGenType);
		ThrowUtils.throwIf(codeGenTypeEnum == null, ErrorCode.PARAMS_ERROR, "不支持的代码生成类型");
		// 调用AI代码生成器生成代码
		Flux<String> codeStream = aiCodeGeneratorFacade.generateAndSaveCodeStream(chatMessage, codeGenTypeEnum, appId);
		// 根据不同的代码生成类型，使用不同的流处理器
		return streamHandlerExecutor.doExecute(codeStream, chatHistoryService, appId, loginUser, codeGenTypeEnum);
	}

	/**
	 * 应用部署
	 */
	@Override
	public String deployApp(AppDeployReq appDeployReq, HttpServletRequest request) {
		Long appId = appDeployReq.getAppId();
		ThrowUtils.throwIf(appId == null || appId <= 0, ErrorCode.PARAMS_ERROR, "应用ID无效");
		App app = this.getById(appId);
		// 判断应用是否存在
		ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR);
		// 校验权限
		UserVO loginUser = userService.getLoginUser(request);
		ThrowUtils.throwIf(!app.getUserId().equals(loginUser.getId()), ErrorCode.NO_AUTH_ERROR, "无权限访问该应用");
		String deployKey = app.getDeployKey();
		if (StrUtil.isBlank(deployKey)) {
			// 生成16位随机字符串作为部署标识
			deployKey = RandomUtil.randomString(16);
		}
		// 获取应用生成的代码目录
		String codeGenType = app.getCodeGenType();
		String sourceDirName = codeGenType + "_" + appId;
		String sourceDir = AppConstant.CODE_OUTPUT_ROOT_DIR + File.separator + sourceDirName;
		// 检查目录是否存在
		File sourceDirFile = new File(sourceDir);
		ThrowUtils.throwIf(!sourceDirFile.exists() || !sourceDirFile.isDirectory(), ErrorCode.NOT_FOUND_ERROR,
				"应用代码目录不存在");
		// 对vue项目进行特殊处理
		CodeGenTypeEnum codeGenTypeEnum = CodeGenTypeEnum.getByValue(codeGenType);
		if (codeGenTypeEnum == CodeGenTypeEnum.VUE_PROJECT) {
			boolean buildProject = vueProjectBuilder.buildProject(sourceDir);
			ThrowUtils.throwIf(!buildProject, ErrorCode.OPERATION_ERROR, "项目构建失败，无法部署");
			// 构建完成后，更新源代码目录为构建后的目录
			sourceDirFile = new File(sourceDir + File.separator + "dist");
			ThrowUtils.throwIf(!sourceDirFile.exists() || !sourceDirFile.isDirectory(), ErrorCode.NOT_FOUND_ERROR,
					"应用代码目录不存在");
		}
		// 复制代码到部署目录
		String deployDir = AppConstant.CODE_DEPLOY_ROOT_DIR + File.separator + deployKey;
		try {
			FileUtil.copyContent(sourceDirFile, new File(deployDir), true);
		} catch (IORuntimeException e) {
			throw new BusinessException(ErrorCode.OPERATION_ERROR, e.getMessage());
		}
		// 更新应用部署信息
		App updateApp = new App();
		updateApp.setId(appId);
		updateApp.setDeployKey(deployKey);
		updateApp.setDeployTime(LocalDateTime.now());
		boolean res = this.updateById(updateApp);
		ThrowUtils.throwIf(!res, ErrorCode.OPERATION_ERROR, "应用部署失败");
		// 异步更新应用封面
		String deployUrl = String.format("%s/%s", AppConstant.CODE_DEPLOY_HOST, deployKey);
		generateAndUploadScreenshotAsync(appId, deployUrl);
		return deployUrl;
	}

	/**
	 * 异步生成并上传应用截图
	 *
	 * @param appId     应用ID
	 * @param deployUrl 部署访问URL
	 */
	private void generateAndUploadScreenshotAsync(Long appId, String deployUrl) {
		Thread.ofVirtual().name("generateAndUploadScreenshotAsync_" + System.currentTimeMillis()).start(() -> {
			String imageUrl = screenshotService.generateAndUploadScreenshot(deployUrl);
			if (StrUtil.isNotBlank(imageUrl)) {
				App updateApp = new App();
				updateApp.setId(appId);
				updateApp.setCover(imageUrl);
				boolean res = this.updateById(updateApp);
				ThrowUtils.throwIf(!res, ErrorCode.OPERATION_ERROR, "应用封面更新失败");
			}
		});
	}
}
