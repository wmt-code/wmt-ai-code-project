package com.wmt.wmtaicode.generator;

import cn.hutool.core.lang.Dict;
import cn.hutool.setting.yaml.YamlUtil;
import com.mybatisflex.codegen.Generator;
import com.mybatisflex.codegen.config.GlobalConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.util.Map;

public class Codegen {
	// 定义表名
	private static final String[] TABLES = {
			"chat_history",
	};

	public static void main(String[] args) {
		Dict yaml = YamlUtil.loadByPath("application.yml");
		Map<String, Object> dataSourceConfig = yaml.getByPath("spring.datasource");
		// 配置数据源
		HikariDataSource dataSource = new HikariDataSource();
		dataSource.setJdbcUrl(String.valueOf(dataSourceConfig.get("url")));
		dataSource.setUsername(String.valueOf(dataSourceConfig.get("username")));
		dataSource.setPassword(String.valueOf(dataSourceConfig.get("password")));
		dataSource.setDriverClassName(String.valueOf(dataSourceConfig.get("driver-class-name")));

		// 创建配置内容，两种风格都可以。
		GlobalConfig globalConfig = createGlobalConfigUseStyle1();
		// GlobalConfig globalConfig = createGlobalConfigUseStyle2();

		// 通过 datasource 和 globalConfig 创建代码生成器
		Generator generator = new Generator(dataSource, globalConfig);

		// 生成代码
		generator.generate();
	}

	public static GlobalConfig createGlobalConfigUseStyle1() {
		// 创建配置内容
		GlobalConfig globalConfig = new GlobalConfig();

		// 设置根包
		globalConfig.setBasePackage("com.generated");

		// 设置表前缀和只生成哪些表
		globalConfig.getStrategyConfig()
				.setGenerateTable(TABLES)
				.setLogicDeleteColumn("isDelete");
		// 设置生成 entity 并启用 Lombok
		globalConfig.setEntityGenerateEnable(true);
		globalConfig.setEntityWithLombok(true);
		// 设置项目的JDK版本，项目的JDK为14及以上时建议设置该项，小于14则可以不设置
		globalConfig.setEntityJdkVersion(21);
		// 设置生成 mapper
		globalConfig.setMapperGenerateEnable(true);
		globalConfig.setMapperXmlGenerateEnable(true);
		// 设置生成service
		globalConfig.setServiceGenerateEnable(true);
		globalConfig.setServiceImplGenerateEnable(true);
		// 设置生成controller
		globalConfig.setControllerGenerateEnable(true);

		return globalConfig;
	}

}