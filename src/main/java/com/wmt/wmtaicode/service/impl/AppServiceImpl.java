package com.wmt.wmtaicode.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.wmt.wmtaicode.ai.core.AiCodeGeneratorFacade;
import com.wmt.wmtaicode.ai.model.enums.CodeGenTypeEnum;
import com.wmt.wmtaicode.exception.ErrorCode;
import com.wmt.wmtaicode.exception.ThrowUtils;
import com.wmt.wmtaicode.mapper.AppMapper;
import com.wmt.wmtaicode.model.dto.app.AppQueryReq;
import com.wmt.wmtaicode.model.entity.App;
import com.wmt.wmtaicode.model.entity.User;
import com.wmt.wmtaicode.model.vo.AppVO;
import com.wmt.wmtaicode.model.vo.UserVO;
import com.wmt.wmtaicode.service.AppService;
import com.wmt.wmtaicode.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

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
	public Flux<String> chatToGenCode(Long appId, String chatMessage, HttpServletRequest request) {
		ThrowUtils.throwIf(appId == null, ErrorCode.PARAMS_ERROR, "应用ID不能为空");
		ThrowUtils.throwIf(chatMessage == null || chatMessage.isBlank(), ErrorCode.PARAMS_ERROR, "用户提示词不能为空");
		App app = this.getById(appId);
		ThrowUtils.throwIf(app == null, ErrorCode.NOT_FOUND_ERROR, "应用不存在");
		UserVO loginUser = userService.getLoginUser(request);
		ThrowUtils.throwIf(!app.getUserId().equals(loginUser.getId()), ErrorCode.NO_AUTH_ERROR, "无权限访问该应用");
		String codeGenType = app.getCodeGenType();
		CodeGenTypeEnum codeGenTypeEnum = CodeGenTypeEnum.getByValue(codeGenType);
		ThrowUtils.throwIf(codeGenTypeEnum == null, ErrorCode.PARAMS_ERROR, "不支持的代码生成类型");
		// 调用AI代码生成器生成代码
		return aiCodeGeneratorFacade.generateAndSaveCodeStream(chatMessage, codeGenTypeEnum, appId);
	}
}
