package com.wmt.wmtaicode.controller;

import com.wmt.wmtaicode.common.BaseResponse;
import com.wmt.wmtaicode.common.ResultUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/health")
public class HealthController {
	@GetMapping("/check")
	public BaseResponse<String> health() {
		return ResultUtils.success("ok");
	}
}
