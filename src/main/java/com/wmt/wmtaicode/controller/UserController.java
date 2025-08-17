package com.wmt.wmtaicode.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.mybatisflex.core.paginate.Page;
import com.wmt.wmtaicode.annotation.AuthCheck;
import com.wmt.wmtaicode.common.BaseResponse;
import com.wmt.wmtaicode.common.DeleteRequest;
import com.wmt.wmtaicode.common.ResultUtils;
import com.wmt.wmtaicode.constant.UserConstant;
import com.wmt.wmtaicode.exception.BusinessException;
import com.wmt.wmtaicode.exception.ErrorCode;
import com.wmt.wmtaicode.exception.ThrowUtils;
import com.wmt.wmtaicode.model.dto.user.*;
import com.wmt.wmtaicode.model.entity.User;
import com.wmt.wmtaicode.model.vo.UserVo;
import com.wmt.wmtaicode.service.FileService;
import com.wmt.wmtaicode.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 用户 控制层。
 *
 * @author ethereal
 * @since 2025-08-17
 */
@RestController
@RequestMapping("/user")
public class UserController {
	@Resource
	private UserService userService;
	@Resource
	private FileService fileService;

	@PostMapping("/register")
	public BaseResponse<Long> userRegister(@RequestBody UserRegisterReq userRegisterReq) {
		ThrowUtils.throwIf(userRegisterReq == null, ErrorCode.PARAMS_ERROR);
		return ResultUtils.success(userService.userRegister(userRegisterReq));
	}

	@PostMapping("/login")
	public BaseResponse<UserVo> userLogin(@RequestBody UserLoginReq userLoginReq, HttpServletRequest request) {
		ThrowUtils.throwIf(userLoginReq == null, ErrorCode.PARAMS_ERROR);
		return ResultUtils.success(userService.userLogin(userLoginReq, request));
	}

	@GetMapping("/get/login")
	public BaseResponse<UserVo> getLoginUser(HttpServletRequest request) {
		return ResultUtils.success(userService.getLoginUser(request));
	}

	@GetMapping("/logout")
	public BaseResponse<Boolean> logout(HttpServletRequest request) {
		return ResultUtils.success(userService.logout(request));
	}

	@PostMapping("/add")
	@AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
	public BaseResponse<Long> addUser(@RequestBody UserAddReq userAddReq) {
		ThrowUtils.throwIf(userAddReq == null, ErrorCode.PARAMS_ERROR);
		User user = new User();
		BeanUtil.copyProperties(userAddReq, user);
		// 默认密码123456
		final String defaultPassword = "123456";
		String encPassword = userService.getEncPassword(defaultPassword);
		user.setUserPassword(encPassword);
		boolean save = userService.save(user);
		ThrowUtils.throwIf(!save, ErrorCode.OPERATION_ERROR, "用户添加失败");
		return ResultUtils.success(user.getId());
	}

	@GetMapping("/get")
	@AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
	public BaseResponse<User> getUserById(Long id) {
		ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR, "用户id不能为空");
		User user = userService.getById(id);
		ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR, "用户不存在");
		return ResultUtils.success(user);
	}

	@GetMapping("/get/vo")
	public BaseResponse<UserVo> getUserVoById(Long id) {
		ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR, "用户id不能为空");
		User user = userService.getById(id);
		ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR, "用户不存在");
		return ResultUtils.success(userService.getUserVo(user));
	}

	@PostMapping("/delete")
	@AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
	public BaseResponse<Boolean> deleteUserById(@RequestBody DeleteRequest deleteRequest) {
		ThrowUtils.throwIf(deleteRequest == null || deleteRequest.getId() <= 0, ErrorCode.PARAMS_ERROR, "用户id不能为空");
		boolean remove = userService.removeById(deleteRequest.getId());
		ThrowUtils.throwIf(!remove, ErrorCode.OPERATION_ERROR, "用户删除失败");
		return ResultUtils.success(true);
	}

	@PostMapping("/update")
	@AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
	public BaseResponse<Boolean> updateUserById(@RequestBody UserUpdateReq userUpdateReq) {
		ThrowUtils.throwIf(userUpdateReq == null || userUpdateReq.getId() <= 0, ErrorCode.PARAMS_ERROR, "用户id不能为空");
		User user = new User();
		BeanUtil.copyProperties(userUpdateReq, user);
		boolean update = userService.updateById(user);
		ThrowUtils.throwIf(!update, ErrorCode.OPERATION_ERROR, "用户更新失败");
		return ResultUtils.success(true);
	}

	@PostMapping("/list/page/vo")
	@AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
	public BaseResponse<Page<UserVo>> listUserVoByPage(@RequestBody UserQueryReq userQueryReq) {
		ThrowUtils.throwIf(userQueryReq == null, ErrorCode.PARAMS_ERROR, "查询参数不能为空");
		int pageSize = userQueryReq.getPageSize();
		int current = userQueryReq.getCurrent();
		Page<User> userPage = userService.page(Page.of(current, pageSize), userService.getQueryWrapper(userQueryReq));
		Page<UserVo> userVoPage = new Page<>(userPage.getPageNumber(), userPage.getPageSize(), userPage.getTotalRow());
		userVoPage.setRecords(userService.getUserVoList(userPage.getRecords()));
		return ResultUtils.success(userVoPage);
	}

	@PostMapping("/update/my")
	public BaseResponse<Boolean> updateSelf(@RequestBody UserUpdateSelfReq userUpdateSelfReq,
											HttpServletRequest request) {
		ThrowUtils.throwIf(userUpdateSelfReq == null, ErrorCode.PARAMS_ERROR);
		UserVo loginUser = userService.getLoginUser(request);
		ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
		User user = new User();
		user.setId(loginUser.getId());
		BeanUtil.copyProperties(userUpdateSelfReq, user);
		// 更新用户密码
		String oldPassword = userUpdateSelfReq.getOldPassword();
		String newPassword = userUpdateSelfReq.getNewPassword();
		String confirmPassword = userUpdateSelfReq.getConfirmPassword();
		if (StrUtil.isNotBlank(oldPassword)) {
			if (StrUtil.hasBlank(newPassword, confirmPassword)) {
				throw new BusinessException(ErrorCode.PARAMS_ERROR, "新密码和确认密码不能为空");
			}
			if (newPassword.length() < 6 || newPassword.length() > 32) {
				throw new BusinessException(ErrorCode.PARAMS_ERROR, "新密码长度应为6~32位");
			}
			User dbuser = userService.getById(loginUser.getId());
			if (!dbuser.getUserPassword().equals(userService.getEncPassword(oldPassword))) {
				throw new BusinessException(ErrorCode.PARAMS_ERROR, "原密码错误");
			}
			if (!newPassword.equals(confirmPassword)) {
				throw new BusinessException(ErrorCode.PARAMS_ERROR, "新密码和确认密码不一致");
			}
			user.setUserPassword(userService.getEncPassword(newPassword));
		}
		boolean update = userService.updateById(user);
		ThrowUtils.throwIf(!update, ErrorCode.OPERATION_ERROR, "用户更新失败");
		return ResultUtils.success(true);
	}

	@PostMapping("/uploadAvatar")
	public BaseResponse<String> uploadAvatar(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
		ThrowUtils.throwIf(file == null || file.isEmpty(), ErrorCode.PARAMS_ERROR, "上传文件不能为空");
		UserVo loginUser = userService.getLoginUser(request);
		ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR, "用户未登录");
		String avatarUrl = fileService.uploadFile(file, "avatar");
		return ResultUtils.success(avatarUrl);
	}
}
