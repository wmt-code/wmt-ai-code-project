package com.wmt.wmtaicode.model.dto.app;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class AppUpdateReq implements Serializable {
	private Long Id;
	private String appName;
	@Serial
	private static final long serialVersionUID = 1L;
}
