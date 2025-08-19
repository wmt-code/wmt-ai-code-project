package com.wmt.wmtaicode.service;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.wmt.wmtaicode.model.dto.app.AppDeployReq;
import com.wmt.wmtaicode.model.dto.app.AppQueryReq;
import com.wmt.wmtaicode.model.entity.App;
import com.wmt.wmtaicode.model.vo.AppVO;
import jakarta.servlet.http.HttpServletRequest;
import reactor.core.publisher.Flux;

import java.util.List;

/**
 * 应用表 服务层。
 *
 * @author ethereal
 * @since 2025-08-18
 */
public interface AppService extends IService<App> {

	AppVO getAppVO(App app);

	List<AppVO> getAppVOList(List<App> appList);

	QueryWrapper getQueryWrapper(AppQueryReq appQueryReq);

	Flux<String> chatToGenCode(Long appId, String chatMessage, HttpServletRequest request);

	String deployApp(AppDeployReq appDeployReq, HttpServletRequest request);
}
