package com.hnf.dapeng.controller;

import com.hnf.dapeng.eneity.DownLoadTask;
import com.hnf.dapeng.eneity.DownloadBox;
import com.hnf.dapeng.eneity.Video;
import com.hnf.dapeng.util.FileUtil;
import com.teamdev.jxbrowser.chromium.*;
import com.teamdev.jxbrowser.chromium.javafx.BrowserView;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * @Author GJ
 * @Description TODO
 * @Date 2022/11/18 16:06
 * @Version 1.0
 */
public class MainPageController implements Initializable {
	public Label videoSavePathLabel;
	public StackPane downloadListPane;
	public TableView<DownloadBox> downloadTableView;
	public TableColumn<DownloadBox, String> idColumn;
	public TableColumn<DownloadBox, String> videoNameColumn;
	public TableColumn<DownloadBox, ProgressBar> progressColumn;
	public TableColumn<DownloadBox, Label> speedColumn;
	public TableColumn<DownloadBox, Button> operationColumn;
	public Button button;
	public CheckBox batchButton;
	public StackPane aboutCoverPane;
	public StackPane aboutPane;
	@FXML
	private StackPane webView;
	@FXML
	private VBox videoListBox;

	private Browser browser;

	private boolean notShowAboutPane = false;
	public static final ExecutorService executorService = Executors.newFixedThreadPool(2);

	public void setNotShowAboutPane(boolean notShowAboutPane) {
		this.notShowAboutPane = notShowAboutPane;
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		BrowserPreferences.setChromiumSwitches("--remote-debugging-port=9222");
		browser = new Browser(BrowserType.LIGHTWEIGHT, new BrowserContext(new BrowserContextParams(new File(
				"cache/main").getAbsolutePath())));
		BrowserView browserView = new BrowserView(browser);
		webView.getChildren().add(browserView);
		browser.loadURL("https://www.dapengjiaoyu.cn/");
		initBrowserDebugListener(webView, browser);
		listenerVideo();
		refreshStorePath();
		initDownloadListTableView();
		addStoreDownloadTask();
		initCacheDownloadTask();
		initBatchButton();
	}


	public void initBatchButton() {
		batchButton.selectedProperty().addListener(observable -> {
			for (Node child : videoListBox.getChildren()) {
				Video child1 = (Video) child;
				child1.getCheckBox().setSelected(batchButton.isSelected());
			}
		});
	}

	public void batchDownload(MouseEvent mouseEvent) {
		for (Node child : videoListBox.getChildren()) {
			Video video = (Video) child;
			if (video.isSelected()) {
				addDownloadTaskByVideo(video);
			}
		}
	}

	/**
	 * 增加缓存未完成的采集任务
	 */
	public void addStoreDownloadTask() {
		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			ObservableList<DownloadBox> items = downloadTableView.getItems();
			//停止所有任务
			for (DownloadBox item : items) {
				item.cancel();
			}
			List<DownLoadTask> collect = items.stream().filter(task -> !task.isDone()).map(task ->
					new DownLoadTask(task.getVideoName(), task.getCommand())).collect(Collectors.toList());
			//缓存未完成的任务
			FileUtil.storeDownloadTask(collect);
		}));
	}

	/**
	 * 初始化缓存任务
	 */
	public void initCacheDownloadTask() {
		List<DownLoadTask> cacheDownloadTask = FileUtil.getCacheDownloadTask();
		if (cacheDownloadTask == null || cacheDownloadTask.isEmpty()) {
			return;
		}
		List<DownloadBox> collect = cacheDownloadTask.stream().map(task ->
				new DownloadBox(task.getVideoName(), task.getCommand(), this)).collect(Collectors.toList());
		for (DownloadBox downloadBox : collect) {
			setOnMouseCheckDownLoadBoxCancelButton(downloadBox);
			addDownloadBox(downloadBox);
		}

	}


	private void initDownloadListTableView() {
		Label placeholder = new Label("无下载任务");
		placeholder.setId("label_5");
		downloadTableView.setPlaceholder(placeholder);
		downloadTableView.widthProperty().addListener((observable, oldValue, newValue) -> {
			double doubleValue = newValue.doubleValue();
			idColumn.setPrefWidth(doubleValue * 0.1);
			videoNameColumn.setPrefWidth(doubleValue * 0.3);
			progressColumn.setPrefWidth(doubleValue * 0.2);
			speedColumn.setPrefWidth(doubleValue * 0.2);
			operationColumn.setPrefWidth(doubleValue * 0.2);
		});
		idColumn.setCellValueFactory(cell ->
				new SimpleStringProperty(String.valueOf(downloadTableView.getItems().indexOf(cell.getValue()) + 1)));
		videoNameColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getVideoName()));
		progressColumn.setCellValueFactory(cell -> {
			cell.getValue().doneProperty().addListener(observable -> {
				if (cell.getValue().doneProperty().get()) {
					updateDownLoadTaskCompleteNum();
				}
			});
			return new SimpleObjectProperty<>(cell.getValue().getProgressBar());
		});
		speedColumn.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getSpeed()));
		operationColumn.setCellValueFactory(cell -> new SimpleObjectProperty<>(cell.getValue().getButton()));
		downloadTableView.getItems().addListener((ListChangeListener<? super DownloadBox>) observable -> {
			updateDownLoadTaskCompleteNum();
		});
	}


	public void updateDownLoadTaskCompleteNum() {
		Platform.runLater(() -> {
			ObservableList<DownloadBox> items = downloadTableView.getItems();
			if (items == null || items.isEmpty()) {
				button.setText("下载列表");
			} else {
				long count = items.stream().filter(DownloadBox::isDone).count();
				button.setText("下载列表(" + count + "/" + items.size() + ")");
			}
		});
	}

	public void refreshStorePath() {
		String storePath = FileUtil.getStorePath();
		if (storePath == null || storePath.isEmpty()) {
			//默认选择桌面
			String desktopPath = System.getProperty("user.home") + "/Desktop";
			try {
				FileUtil.writeStorePath(new File(desktopPath).getCanonicalPath());
				refreshStorePath();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		} else {
			videoSavePathLabel.setText("视频存储路径: " + storePath);
		}
	}


	/**
	 * 修改视频下载存储路径
	 */
	public void modifyStorePath() {
		File file = FileUtil.chooseDirectory("选择视频下载存储文件夹");
		if (file != null) {
			try {
				FileUtil.writeStorePath(file.getCanonicalPath());
				refreshStorePath();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}


	public void refreshWebPage() {
		browser.getSessionWebStorage().clear();
		browser.getCookieStorage().deleteAll();
		browser.getCacheStorage().clearCache();
		browser.reload();
	}


	private void listenerVideo() {
		browser.getContext().getNetworkService().setNetworkDelegate(new NetworkDelegate() {
			@Override
			public void onBeforeURLRequest(BeforeURLRequestParams beforeURLRequestParams) {

			}

			@Override
			public void onBeforeRedirect(BeforeRedirectParams beforeRedirectParams) {
			}

			@Override
			public void onBeforeSendHeaders(BeforeSendHeadersParams beforeSendHeadersParams) {
			}

			@Override
			public void onSendHeaders(SendHeadersParams sendHeadersParams) {
			}

			@Override
			public void onHeadersReceived(HeadersReceivedParams headersReceivedParams) {
			}

			@Override
			public void onResponseStarted(ResponseStartedParams responseStartedParams) {
			}

			@Override
			public void onDataReceived(DataReceivedParams dataReceivedParams) {
				try {
					String charset = dataReceivedParams.getCharset();
					if ("utf-8".equals(charset)) {
						String url = dataReceivedParams.getURL();
						if (url.contains("api/old/courses/stages")) {
							String data = new String(dataReceivedParams.getData(), charset);
							refreshLiveVideo(data);
							return;
						}
						if (url.contains("api/old/courses")) {
							String data = new String(dataReceivedParams.getData(), charset);
							refreshVideo(data);
						}
					}
				} catch (UnsupportedEncodingException e) {
				}
			}

			@Override
			public void onCompleted(RequestCompletedParams requestCompletedParams) {
			}

			@Override
			public void onDestroyed(RequestParams requestParams) {
			}

			@Override
			public boolean onAuthRequired(AuthRequiredParams authRequiredParams) {
				return true;
			}

			@Override
			public boolean onCanSetCookies(String s, List<Cookie> list) {
				return true;
			}

			@Override
			public boolean onCanGetCookies(String s, List<Cookie> list) {
				return true;
			}

			@Override
			public void onBeforeSendProxyHeaders(BeforeSendProxyHeadersParams beforeSendProxyHeadersParams) {
			}

			@Override
			public void onPACScriptError(PACScriptErrorParams pacScriptErrorParams) {
			}
		});
	}


	public void refreshLiveVideo(String jsonStr) {
		try {
			JSONArray jsonArray = JSONArray.fromObject(jsonStr);
			Platform.runLater(() -> {
				videoListBox.getChildren().clear();
			});
			for (Object chapterObj : jsonArray) {
				JSONObject chapterJson = JSONObject.fromObject(chapterObj);
				String title = chapterJson.getString("title");
				JSONObject videoContent = chapterJson.getJSONObject("videoContent");
				String vid = videoContent.getString("vid");
				String duration = videoContent.getString("duration");
				addVideoToView(title, vid, duration, Video.VideoDefinition.HD1);
			}
		} catch (Exception e) {
		}
	}


	private void addVideoToView(String title, String vid, String duration, Video.VideoDefinition videoDefinition) {
		Platform.runLater(() -> {
			Video video = new Video(title, vid, duration);
			if (videoDefinition != null) {
				video.setDefinition(videoDefinition.getDefinition());
			}
			video.getCheckBox().setSelected(batchButton.isSelected());
			videoListBox.getChildren().add(video);
			video.getButton().setOnMouseClicked(event -> {
				addDownloadTaskByVideo(video);
			});
		});
	}

	public void refreshVideo(String jsonStr) {
		try {
			JSONObject jsonObject = JSONObject.fromObject(jsonStr);
			JSONArray courseVodContents = jsonObject.getJSONArray("courseVodContents");
			Platform.runLater(() -> {
				videoListBox.getChildren().clear();
			});
			for (Object courseVodContent : courseVodContents) {
				JSONObject chapterJson = JSONObject.fromObject(courseVodContent);
				JSONArray lectures = chapterJson.getJSONArray("lectures");
				for (Object lecture : lectures) {
					JSONObject lectureJson = JSONObject.fromObject(lecture);
					JSONObject videoContent = lectureJson.getJSONObject("videoContent");
					String vid = videoContent.getString("vid");
					String title = videoContent.getString("title");
					String duration = videoContent.getString("duration");
					addVideoToView(title, vid, duration, Video.VideoDefinition.HD3);
				}
			}
		} catch (Exception e) {
		}
	}


	private void addDownloadTaskByVideo(Video video) {
		List<String> command = video.getCommand();
		if (command == null) {
			System.out.println("任务创建失败");
		} else {
			DownloadBox downloadBox = new DownloadBox(video.getVideoName(), command, this);
			setOnMouseCheckDownLoadBoxCancelButton(downloadBox);
			addDownloadBox(downloadBox);
		}
	}


	public void setOnMouseCheckDownLoadBoxCancelButton(DownloadBox downloadBox) {
		downloadBox.getButton().setOnMouseClicked(event -> {
			if (downloadBox.isRun()) {
				downloadBox.cancel();
			}
			removeDownloadBox(downloadBox);
		});
	}

	private void removeDownloadBox(DownloadBox downloadBox) {
		downloadTableView.getItems().remove(downloadBox);
	}


	private void addDownloadBox(DownloadBox downloadBox) {
		if (downloadTableView.getItems().contains(downloadBox)) {
			return;
		}
		downloadTableView.getItems().add(downloadBox);
		executorService.execute(downloadBox);
	}


	/**
	 * 浏览器调试监听
	 */
	private void initBrowserDebugListener(StackPane stackPane, Browser browser) {
		Stage browserDebugStage = new Stage();
		Browser f12Browser = new Browser();
		BrowserView browserView = new BrowserView(f12Browser);
		Scene scene = new Scene(browserView, 720, 480);
		browserDebugStage.setScene(scene);
		stackPane.setOnKeyPressed(event -> {
			if (event.getCode() == KeyCode.F12) {
				System.out.println("F12");
				if (browserDebugStage.isShowing()) {
					browserDebugStage.hide();
				} else {
					browserDebugStage.show();
					String remoteDebuggingURL = browser.getRemoteDebuggingURL();
					browserDebugStage.setTitle(remoteDebuggingURL);
					f12Browser.loadURL(remoteDebuggingURL);
				}
			}
		});
	}

	public void showDownloadList() {
		boolean showDownloadPane = !downloadListPane.isVisible();
		downloadListPane.setVisible(showDownloadPane);
		if (!notShowAboutPane && showDownloadPane) {
			showAboutPane(true);
		}
	}


	public void showAboutPane(boolean show) {
		aboutCoverPane.setVisible(show);
		aboutPane.setVisible(show);
	}

	public void closeAboutPane(MouseEvent mouseEvent) {
		showAboutPane(false);
	}

	public void notShowAboutPane(MouseEvent mouseEvent) {
		setNotShowAboutPane(true);
		showAboutPane(false);
	}
}
