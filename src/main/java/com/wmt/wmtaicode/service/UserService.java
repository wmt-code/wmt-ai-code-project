package com.wmt.wmtaicode.service;

import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.core.service.IService;
import com.wmt.wmtaicode.model.dto.user.UserLoginReq;
import com.wmt.wmtaicode.model.dto.user.UserQueryReq;
import com.wmt.wmtaicode.model.dto.user.UserRegisterReq;
import com.wmt.wmtaicode.model.entity.User;
import com.wmt.wmtaicode.model.vo.UserVo;
import jakarta.servlet.http.HttpServletRequest;

import java.util.List;

/**
 * 用户 服务层。
 *
 * @author ethereal
 * @since 2025-08-17
 */
public interface UserService extends IService<User> {

	/**
	 * 用户注册
	 *
	 * @param userRegisterReq 用户注册请求
	 * @return 用户id
	 */
	long userRegister(UserRegisterReq userRegisterReq);

	/**
	 * 用户登录
	 *
	 * @param userLoginReq 用户登录请求
	 * @return 用户信息
	 */
	UserVo userLogin(UserLoginReq userLoginReq, HttpServletRequest request);

	String getEncPassword(String userPassword);

	QueryWrapper getQueryWrapper(UserQueryReq userQueryReq);

	UserVo getUserVo(User user);

	UserVo getLoginUser(HttpServletRequest request);

	/**
	 * 用户注销
	 *
	 * @param request
	 * @return
	 */
	boolean logout(HttpServletRequest request);

	List<UserVo> getUserVoList(List<User> userList);
}
