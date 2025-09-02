package com.wmt.wmtaicode.constant;

/**
 * 应用常量接口，定义应用相关的常量。
 * 该接口可以用于集中管理应用中的常量，便于维护和修改。
 */
public interface AppConstant {
	/**
	 * 精选应用优先级
	 */
	Integer GOOD_APP_PRIORITY = 100;
	/**
	 * 默认应用优先级
	 */
	Integer DEFAULT_APP_PRIORITY = 0;


	/**
	 * 应用生成目录
	 */
	String CODE_OUTPUT_ROOT_DIR = System.getProperty("user.dir") + "/tmp/code_output";

	/**
	 * 应用部署目录
	 */
	String CODE_DEPLOY_ROOT_DIR = System.getProperty("user.dir") + "/tmp/code_deploy";

	/**
	 * 应用部署域名
	 */
	String CODE_DEPLOY_HOST = "http://localhost";

}
