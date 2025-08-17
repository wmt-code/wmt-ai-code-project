package com.wmt.wmtaicode;

import com.wmt.wmtaicode.annotation.AuthCheck;
import com.wmt.wmtaicode.exception.BusinessException;
import com.wmt.wmtaicode.exception.ErrorCode;
import com.wmt.wmtaicode.exception.ThrowUtils;
import com.wmt.wmtaicode.model.enums.UserRoleEnum;
import com.wmt.wmtaicode.model.vo.UserVo;
import com.wmt.wmtaicode.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
public class AuthInterceptor {
	@Resource
	private UserService userService;

	@Around(value = "@annotation(authCheck)")
	public Object doInterceptor(ProceedingJoinPoint pjp, AuthCheck authCheck) throws Throwable {
		String mustRole = authCheck.mustRole();
		UserRoleEnum enumByValue = UserRoleEnum.getEnumByValue(mustRole);
		// 如果没有指定角色，则直接放行
		if (enumByValue == null) {
			return pjp.proceed();
		}
		// 获取登录用户
		ServletRequestAttributes requestAttributes =
				(ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		HttpServletRequest request = requestAttributes.getRequest();
		UserVo loginUser = userService.getLoginUser(request);
		// 如果没有登录用户，则直接抛出异常
		ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);
		UserRoleEnum loginUserRole = UserRoleEnum.getEnumByValue(loginUser.getUserRole());
		ThrowUtils.throwIf(loginUserRole == null, ErrorCode.NO_AUTH_ERROR);
		// 如果登录用户的角色不符合要求，则直接抛出异常
		if (UserRoleEnum.ADMIN.equals(enumByValue) && !UserRoleEnum.ADMIN.equals(loginUserRole)) {
			throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
		}
		return pjp.proceed();
	}
}
