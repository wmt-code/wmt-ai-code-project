package com.wmt.wmtaicode.langgraph4j.tools;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversation;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationParam;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationResult;
import com.alibaba.dashscope.common.MultiModalMessage;
import com.alibaba.dashscope.common.Role;
import com.wmt.wmtaicode.langgraph4j.model.ImageResource;
import com.wmt.wmtaicode.langgraph4j.model.enums.ImageCategoryEnum;
import com.wmt.wmtaicode.model.enums.FileTypeEnum;
import com.wmt.wmtaicode.service.FileService;
import dev.langchain4j.agent.tool.P;
import dev.langchain4j.agent.tool.Tool;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.*;

@Slf4j
@Component
public class LogoGeneratorTool {
	@Resource
	private FileService fileService;

	@Value("${dashscope.api-key}")
	private String dashScopeApiKey;

	@Value("${dashscope.image-model}")
	private String imageModel;

	@Tool("根据描述生成 Logo 设计图片，用于网站品牌标识")
	public List<ImageResource> generateLogos(@P("Logo 设计描述，如名称、行业、风格等，尽量详细") String description) {
		List<ImageResource> logoList = new ArrayList<>();
		try {
			// 构建 Logo 设计提示词
			String logoPrompt = String.format("生成 Logo，Logo 中禁止包含任何文字！Logo 介绍：%s", description);
			MultiModalConversation conv = new MultiModalConversation();

			MultiModalMessage userMessage = MultiModalMessage.builder().role(Role.USER.getValue())
					.content(Arrays.asList(
							Collections.singletonMap("text", logoPrompt)
					)).build();

			Map<String, Object> parameters = new HashMap<>();
			parameters.put("watermark", false);
			parameters.put("prompt_extend", true);
			parameters.put("negative_prompt", "");
			parameters.put("size", "1328*1328");

			MultiModalConversationParam param = MultiModalConversationParam.builder()
					.apiKey(dashScopeApiKey)
					.model(imageModel)
					.messages(Collections.singletonList(userMessage))
					.parameters(parameters)
					.build();

			MultiModalConversationResult result = conv.call(param);
			String imageUrl = result.getOutput().getChoices().getFirst().getMessage().getContent().getFirst().get(
					"image").toString();
			// 创建临时目录
			String imageDirector = System.getProperty("user.dir") + File.separator + "tmp" + File.separator +
					"logos";
			FileUtil.mkdir(imageDirector);
			String imagePath = imageDirector + File.separator +
					RandomUtil.randomString(6) + ".png";
			HttpUtil.downloadFile(imageUrl, imagePath);
			log.info("Logo 设计图片下载成功, 图片地址: {}", imagePath);
			String uploadUrl = fileService.uploadFile(new File(imagePath), "logos", FileTypeEnum.IMAGE);
			log.info("Logo 设计图片上传成功, 图片地址: {}", uploadUrl);
			FileUtil.del(imagePath);// 删除临时文件
			logoList.add(ImageResource.builder()
					.category(ImageCategoryEnum.LOGO)
					.description(description)
					.url(uploadUrl)
					.build());
			log.info("Logo 设计完成，图片地址: {}", uploadUrl);
		} catch (Exception exception) {
			log.error("生成 Logo 失败: {}", exception.getMessage(), exception);
		}
		return logoList;
	}
}
