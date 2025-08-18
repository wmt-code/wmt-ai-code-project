package com.wmt.wmtaicode.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import com.mybatisflex.core.query.QueryWrapper;
import com.mybatisflex.spring.service.impl.ServiceImpl;
import com.wmt.wmtaicode.constant.UserConstant;
import com.wmt.wmtaicode.exception.BusinessException;
import com.wmt.wmtaicode.exception.ErrorCode;
import com.wmt.wmtaicode.exception.ThrowUtils;
import com.wmt.wmtaicode.mapper.UserMapper;
import com.wmt.wmtaicode.model.dto.user.UserLoginReq;
import com.wmt.wmtaicode.model.dto.user.UserQueryReq;
import com.wmt.wmtaicode.model.dto.user.UserRegisterReq;
import com.wmt.wmtaicode.model.entity.User;
import com.wmt.wmtaicode.model.enums.UserRoleEnum;
import com.wmt.wmtaicode.model.vo.UserVO;
import com.wmt.wmtaicode.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * 用户 服务层实现。
 *
 * @author ethereal
 * @since 2025-08-17
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

	@Override
	public long userRegister(UserRegisterReq userRegisterReq) {
		String userAccount = userRegisterReq.getUserAccount();
		String userPassword = userRegisterReq.getUserPassword();
		String confirmPassword = userRegisterReq.getConfirmPassword();
		if (StrUtil.hasBlank(userAccount, userPassword, confirmPassword)) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
		}
		// 账号和密码长度校验
		ThrowUtils.throwIf(userAccount.length() < 4 || userAccount.length() > 20, ErrorCode.PARAMS_ERROR, "用户账号为4~20" +
				"位");
		ThrowUtils.throwIf(userPassword.length() < 6 || userPassword.length() > 32, ErrorCode.PARAMS_ERROR, "用户密码为6" +
				"~32" +
				"位");
		ThrowUtils.throwIf(!userPassword.equals(confirmPassword), ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
		// 检查账号是否已注册
		QueryWrapper qw = QueryWrapper.create()
				.eq(User::getUserAccount, userAccount);
		long count = this.mapper.selectCountByQuery(qw);
		ThrowUtils.throwIf(count > 0, ErrorCode.PARAMS_ERROR, "用户账号已存在");
		String encPassword = getEncPassword(userPassword);
		User user = new User();
		user.setUserAccount(userAccount);
		user.setUserPassword(encPassword);
		user.setUserName(userAccount);
		user.setUserRole(UserRoleEnum.USER.getValue());
		boolean save = this.save(user);
		ThrowUtils.throwIf(!save, ErrorCode.OPERATION_ERROR, "用户注册失败");
		return user.getId();
	}

	@Override
	public UserVO userLogin(UserLoginReq userLoginReq, HttpServletRequest request) {
		String userAccount = userLoginReq.getUserAccount();
		String userPassword = userLoginReq.getUserPassword();
		if (StrUtil.hasBlank(userAccount, userPassword)) {
			throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
		}
		// 账号和密码长度校验
		ThrowUtils.throwIf(userAccount.length() < 4 || userAccount.length() > 20, ErrorCode.PARAMS_ERROR, "用户账号为4~20" +
				"位");
		ThrowUtils.throwIf(userPassword.length() < 6 || userPassword.length() > 32, ErrorCode.PARAMS_ERROR, "用户密码为6" +
				"~32" +
				"位");
		// 检查用户是否存在
		String encPassword = getEncPassword(userPassword);
		QueryWrapper queryWrapper = new QueryWrapper();
		queryWrapper.eq(User::getUserAccount, userAccount)
				.eq(User::getUserPassword, encPassword);
		User user = this.getOne(queryWrapper);
		ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR, "用户不存在或密码错误");
		// 记录登录信息
		request.getSession().setAttribute(UserConstant.USER_LOGIN_STATE, user);
		// 返回用户信息
		return this.getUserVo(user);
	}

	@Override
	public String getEncPassword(String userPassword) {
		final String SALT = "EtherealSummer";
		return SecureUtil.md5(SALT + userPassword);
	}

	@Override
	public UserVO getLoginUser(HttpServletRequest request) {
		User user = (User) request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
		ThrowUtils.throwIf(user == null, ErrorCode.NOT_LOGIN_ERROR, "用户未登录");
		// 返回用户信息
		Long id = user.getId();
		user = this.getById(id);
		ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR, "用户不存在");
		return this.getUserVo(user);
	}

	@Override
	public boolean logout(HttpServletRequest request) {
		User user = (User) request.getSession().getAttribute(UserConstant.USER_LOGIN_STATE);
		ThrowUtils.throwIf(user == null, ErrorCode.NOT_LOGIN_ERROR, "用户未登录");
		request.getSession().removeAttribute(UserConstant.USER_LOGIN_STATE);
		return true;
	}

	@Override
	public QueryWrapper getQueryWrapper(UserQueryReq userQueryReq) {
		ThrowUtils.throwIf(userQueryReq == null, ErrorCode.PARAMS_ERROR, "查询参数不能为空");
		Long id = userQueryReq.getId();
		String userName = userQueryReq.getUserName();
		String userProfile = userQueryReq.getUserProfile();
		String userRole = userQueryReq.getUserRole();
		String userAccount = userQueryReq.getUserAccount();
		String sortField = userQueryReq.getSortField();
		String sortOrder = userQueryReq.getSortOrder();
		return QueryWrapper.create()
				.eq(User::getId, id)
				.like(User::getUserName, userName)
				.like(User::getUserProfile, userProfile)
				.eq(User::getUserRole, userRole)
				.like(User::getUserAccount, userAccount)
				.orderBy(sortField, "ascend".equalsIgnoreCase(sortOrder));
	}

	@Override
	public UserVO getUserVo(User user) {
		if (user == null) {
			return null;
		}
		UserVO userVo = new UserVO();
		BeanUtil.copyProperties(user, userVo);
		return userVo;
	}

	@Override
	public List<UserVO> getUserVoList(List<User> userList) {
		if (CollUtil.isEmpty(userList)) {
			return Collections.emptyList();
		}
		return userList.stream()
				.map(this::getUserVo)
				.toList();
	}
}
