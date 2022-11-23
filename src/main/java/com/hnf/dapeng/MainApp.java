package com.hnf.dapeng;

import com.hnf.dapeng.view.MainPageView;
import com.hnf.jfx.abs.AbstractJavaFxApplicationSupport;
import com.teamdev.jxbrowser.chromium.BrowserPreferences;
import com.teamdev.jxbrowser.chromium.ba;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigInteger;

/**
 * @Author GJ
 * @Description TODO
 * @Date 2022/11/18 16:08
 * @Version 1.0
 */
public class MainApp extends AbstractJavaFxApplicationSupport {
	public static final String APP_ICON_PATH = "fx/img/dapeng.png";
	public static final String APP_NAME = "大鹏视频下载爬虫工具";

	static {
		try {
			Field e = ba.class.getDeclaredField("e");
			e.setAccessible(true);
			Field f = ba.class.getDeclaredField("f");
			f.setAccessible(true);
			Field modifersField = Field.class.getDeclaredField("modifiers");
			modifersField.setAccessible(true);
			modifersField.setInt(e, e.getModifiers() & ~Modifier.FINAL);
			modifersField.setInt(f, f.getModifiers() & ~Modifier.FINAL);
			e.set(null, new BigInteger("1"));
			f.set(null, new BigInteger("1"));
			modifersField.setAccessible(false);
			BrowserPreferences.setChromiumSwitches(
					"--disable-gpu",
					"--disable-gpu-compositing",
					"--enable-begin-frame-scheduling",
					"--software-rendering-fps=60"
			);

		} catch (IllegalAccessException |
				 IllegalArgumentException |
				 NoSuchFieldException |
				 SecurityException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void start(Stage stage) throws Exception {
		stage.getIcons().addAll(new Image(APP_ICON_PATH));
		stage.setTitle(APP_NAME);
		super.start(stage);
		stage.setOnCloseRequest(event -> System.exit(0));
		stage.show();
	}

	public static void main(String[] args) {
		launchApp(MainApp.class, MainPageView.class, args);
	}
}
