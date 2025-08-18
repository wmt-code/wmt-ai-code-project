package com.wmt.wmtaicode.service;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.wmt.wmtaicode.model.dto.app.AppQueryReq;
import com.wmt.wmtaicode.model.entity.App;
import com.wmt.wmtaicode.model.vo.AppVO;

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
}
