package com.wmt.wmtaicode.model.dto.user;

import com.wmt.wmtaicode.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
public class UserQueryReq extends PageRequest implements Serializable {
	@Serial
	private static final long serialVersionUID = 1L;
	private Long id;
	private String userName;
	private String userAccount;
	private String userProfile;
	private String userRole;
}
