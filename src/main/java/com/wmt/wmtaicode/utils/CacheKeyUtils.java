package com.wmt.wmtaicode.utils;

import cn.hutool.json.JSONUtil;
import com.qcloud.cos.utils.Md5Utils;

/**
 * 缓存键生成工具类
 */
public class CacheKeyUtils {
	public static String buildCacheKey(Object object) {
		if (object == null){
			return Md5Utils.md5Hex("null");
		}
		String jsonStr = JSONUtil.toJsonStr(object);
		return Md5Utils.md5Hex(jsonStr);
	}
}
