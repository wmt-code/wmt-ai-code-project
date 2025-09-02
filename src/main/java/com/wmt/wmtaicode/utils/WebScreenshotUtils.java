package com.wmt.wmtaicode.utils;

import cn.hutool.core.img.ImgUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.wmt.wmtaicode.exception.BusinessException;
import com.wmt.wmtaicode.exception.ErrorCode;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.time.Duration;
import java.util.Objects;

/**
 * 网页截图工具类
 */
@Slf4j
public class WebScreenshotUtils {
	private static final WebDriver webDriver;

	// 静态代码块初始化WebDriver,只初始化一次
	static {
		// 获取resource目录下的chromedriver文件路径
		String os = System.getProperty("os.name").toLowerCase();
		String driverPath;
		if (os.contains("win")) {
			driverPath = Objects.requireNonNull(WebScreenshotUtils.class.getClassLoader()
					.getResource("chromedriver-win64/chromedriver.exe")).getPath();
			// 修复Windows路径格式问题
			if (driverPath.startsWith("/") && driverPath.length() > 1 && driverPath.charAt(2) == ':') {
				driverPath = driverPath.substring(1);
			}
		} else if (os.contains("linux")) {
			driverPath = Objects.requireNonNull(WebScreenshotUtils.class.getClassLoader()
					.getResource("chromedriver-linux64/chromedriver")).getPath();
		} else {
			throw new BusinessException(ErrorCode.SYSTEM_ERROR, "不支持的操作系统: " + os);
		}
		log.info("使用的ChromeDriver路径: {}", driverPath);
		System.setProperty("webdriver.chrome.driver", driverPath);
		// 初始化WebDriver
		final int DEFAULT_WIDTH = 1600;
		final int DEFAULT_HEIGHT = 900;
		webDriver = initChromeDriver(DEFAULT_WIDTH, DEFAULT_HEIGHT);
	}

	/**
	 * 截取网页截图并保存为本地文件
	 *
	 * @param webUrl 网页URL
	 * @return 保存的图片路径
	 */
	public static String takeScreenshot(String webUrl) {
		if (StrUtil.isBlank(webUrl)) {
			log.error("网页URL不能为空");
			return null;
		}
		// 创建临时目录
		String imageDirector = System.getProperty("user.dir") + File.separator + "tmp" + File.separator +
				"screenshots";
		FileUtil.mkdir(imageDirector);// 创建目录
		String imagePath = imageDirector + File.separator +
				RandomUtil.randomString(6) + ".png";
		try {
			// 访问网页
			webDriver.get(webUrl);
			// 等待页面加载完成
			waitForPageLoad(webDriver);
			// 截图并保存到文件
			byte[] imageBytes = ((TakesScreenshot) webDriver).getScreenshotAs(OutputType.BYTES);
			// 保存图片
			saveImage(imageBytes, imagePath);
			log.info("网页截图已保存到: {}", imagePath);
			return imagePath;
		} catch (Exception e) {
			log.error("网页截图失败: {}", e.getMessage(), e);
		}
		return null;
	}


	/**
	 * 初始化谷歌浏览器驱动
	 *
	 * @param defaultWidth  默认宽度
	 * @param defaultHeight 默认高度
	 * @return WebDriver实例
	 */
	private static WebDriver initChromeDriver(int defaultWidth, int defaultHeight) {
		try {
			// 自动管理ChromeDriver的版本
			// WebDriverManager.chromedriver().setup();
			// WebDriverManager.chromedriver().useMirror().setup();
			// 设置Chrome浏览器的选项
			ChromeDriver driver = getChromeDriver(defaultWidth, defaultHeight);
			driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30)); // 设置页面加载超时时间
			driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(10)); // 设置隐式等待时间
			return driver;
		} catch (Exception e) {
			log.error("初始化ChromeDriver失败: {}", e.getMessage(), e);
			throw new BusinessException(ErrorCode.SYSTEM_ERROR, "初始化ChromeDriver失败");
		}
	}

	private static ChromeDriver getChromeDriver(int defaultWidth, int defaultHeight) {
		// 使用本地Chrome驱动
		var options = new ChromeOptions();
		options.addArguments("--headless"); // 无头模式
		options.addArguments("--disable-gpu"); // 禁用GPU加速
		options.addArguments("--no-sandbox"); // 以root用户运行时需要添加
		options.addArguments("--disable-dev-shm-usage"); // 解决资源受限问题
		options.addArguments("--disable-extensions"); // 禁用扩展
		options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, " +
				"like" +
				" " +
				"Gecko) Chrome/58.0.3029.110 Safari/537.3"); // 设置用户代理
		options.addArguments(String.format("--window-size=%d,%d", defaultWidth, defaultHeight)); // 设置窗口大小
		// 创建WebDriver实例
		return new ChromeDriver(options);
	}

	@PreDestroy
	public void destroy() {
		if (webDriver != null) {
			webDriver.quit();
		}
	}

	/**
	 * 保存网页截图到文件
	 */
	public static void saveImage(byte[] imageBytes, String filePath) {
		try {
			FileUtil.writeBytes(imageBytes, filePath);
		} catch (IORuntimeException e) {
			log.error("保存网页截图失败: {}", e.getMessage(), e);
			throw new BusinessException(ErrorCode.OPERATION_ERROR, "保存网页截图失败");
		}
	}

	/**
	 * 压缩图片
	 */
	public static void compressImage(String inputImagePath, String outputImagePath) {
		final float quality = 0.3f; // 压缩质量，范围0-1，值越小质量越低
		try {
			// 使用hutool的ImageUtil进行图片压缩
			ImgUtil.scale(FileUtil.file(inputImagePath), FileUtil.file(outputImagePath), quality);
		} catch (IORuntimeException e) {
			log.error("压缩图片失败: {}", e.getMessage(), e);
			throw new BusinessException(ErrorCode.OPERATION_ERROR, "压缩图片失败");
		}
	}

	/**
	 * 等待页面加载完成
	 */
	public static void waitForPageLoad(WebDriver webDriver) {
		try {
			// 创建页面等待对象
			WebDriverWait webDriverWait = new WebDriverWait(webDriver, Duration.ofSeconds(10));
			// 等待document.readyState为complete
			webDriverWait.until(driver -> Objects.equals(((ChromeDriver) driver).executeScript("return document" +
					".readyState"), "complete"));
			Thread.sleep(2000); // 额外等待2秒，确保页面完全加载
		} catch (InterruptedException e) {
			log.error("等待页面加载完成失败: {}", e.getMessage(), e);
		}
	}
}
