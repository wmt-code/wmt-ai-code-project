package com.wmt.wmtaicode.model.dto.app;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class AppDeployReq implements Serializable {

	/**
	 * 应用 id
	 */
	private Long appId;

	@Serial
	private static final long serialVersionUID = 1L;
}
