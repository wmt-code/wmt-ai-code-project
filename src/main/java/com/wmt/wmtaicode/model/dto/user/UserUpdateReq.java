package com.wmt.wmtaicode.model.dto.user;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class UserUpdateReq implements Serializable {
	@Serial
	private static final long serialVersionUID = 1L;
	private Long id;
	private String userName;
	private String userAvatar;
	private String userProfile;
	private String userRole;
}
