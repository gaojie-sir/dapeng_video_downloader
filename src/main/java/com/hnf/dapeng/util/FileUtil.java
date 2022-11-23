package com.hnf.dapeng.util;

import com.hnf.dapeng.eneity.DownLoadTask;
import com.hnf.jfx.gui.GUIState;
import javafx.stage.DirectoryChooser;

import java.awt.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * @Author GJ
 * @Description TODO
 * @Date 2022/11/21 9:30
 * @Version 1.0
 */
public class FileUtil {
	private static final File file = new File("config/store.txt");
	private static final String OPEN_FILE_BACKUP_FILE = "./config/backup.tmp";
	private static final String TASK_CACHE_FILE = "./config/task.tmp";
	private static String oldSelectFilePath = null;

	public static String getStorePath() {
		String path = null;
		try {
			path = cn.hutool.core.io.FileUtil.readString(file, StandardCharsets.UTF_8);
		} catch (Exception e) {
			return null;
		}
		if (path == null || path.isEmpty()) {
			return null;
		}
		if (!new File(path).exists()) {
			return null;
		}
		return path;
	}

	public static boolean writeStorePath(String path) {
		File saveDir = new File(path);
		try {
			String canonicalPath = saveDir.getCanonicalPath();
			cn.hutool.core.io.FileUtil.writeString(canonicalPath, file, StandardCharsets.UTF_8);
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	/**
	 * 执行cmd命令
	 *
	 * @param cmd
	 * @param call
	 */
	public static void cmdExec(String cmd, Consumer<String> call) {
		Runtime runtime = Runtime.getRuntime();
		try {
			Process exec = runtime.exec(cmd);
			InputStream fis = exec.getInputStream();
			//用一个读输出流类去读
			InputStreamReader isr = new InputStreamReader(fis);
			//用缓冲器读行
			BufferedReader br = new BufferedReader(isr);
			String line;
			//读取命令执行返回信息
			while ((line = br.readLine()) != null) {
				call.accept(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	public static void command(String cmd, Consumer<String> call) {
		ProcessBuilder processBuilder = new ProcessBuilder();
		processBuilder.command(cmd);
		processBuilder.redirectErrorStream(true);
		try {
			Process start = processBuilder.start();
			InputStream inputStream = start.getInputStream();
			InputStreamReader reader = new InputStreamReader(inputStream, "GBK");
			char[] chars = new char[1024];
			int len = -1;
			while ((len = reader.read(chars)) != -1) {
				String s = new String(chars, 0, len);
				call.accept(s);
			}
			reader.close();
			inputStream.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static void command(List<String> commands, Consumer<String> call, AtomicBoolean destroy) {
		ProcessBuilder processBuilder = new ProcessBuilder();
		processBuilder.command(commands);
		processBuilder.redirectErrorStream(true);
		try {
			Process start = processBuilder.start();
			InputStream inputStream = start.getInputStream();
			InputStreamReader reader = new InputStreamReader(inputStream, "GBK");
			char[] chars = new char[1024];
			int len = -1;
			while ((len = reader.read(chars)) != -1) {
				String s = new String(chars, 0, len);
				call.accept(s);
				if (destroy.get()) {
					start.destroy();
				}
			}
			reader.close();
			inputStream.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}


	/**
	 * 调用文件管理器选择一个文件
	 *
	 * @param title 弹窗title
	 * @return 选择的文件对象
	 */
	public static File chooseDirectory(String title) {
		DirectoryChooser directoryChooser = new DirectoryChooser();
		if (oldSelectFilePath == null) {
			String openFileBackupPath = getOpenFileBackupPath();
			if (openFileBackupPath == null) {
				openFileBackupPath = System.getProperty("user.home");
			}
			directoryChooser.setInitialDirectory(new File(openFileBackupPath));
		}
		directoryChooser.setTitle(title);
		File file = directoryChooser.showDialog(GUIState.getStage());
		if (file == null) {
			return null;
		}
		if (file.isFile()) {
			try {
				writeOpenFileBackUpFile(file.getParentFile().getPath());
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			try {
				writeOpenFileBackUpFile(file.getPath());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return file;
	}

	/**
	 * 获取之前打开的文件路径
	 *
	 * @return
	 */
	public static String getOpenFileBackupPath() {
		File file = new File(OPEN_FILE_BACKUP_FILE);
		if (!file.exists()) {
			return null;
		}
		try (BufferedReader bufferedWriter = new BufferedReader(new FileReader(file))) {
			String content = bufferedWriter.readLine();
			if (StringUtils.isNullChar(content).isEmpty()) {
				return null;
			}
			File backupFile = new File(content);
			if (backupFile.exists()) {
				return content;
			}
			return null;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 重写记录上次文件打开位置记录文件
	 *
	 * @param path
	 * @throws IOException
	 */
	public static void writeOpenFileBackUpFile(String path) throws IOException {
		File file = new File(OPEN_FILE_BACKUP_FILE);
		if (file.exists()) {
			file.delete();
		}
		File parentFile = file.getParentFile();
		if (!parentFile.exists()) {
			parentFile.mkdirs();
		}
		file.createNewFile();
		try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file))) {
			bufferedWriter.write(path);
			bufferedWriter.flush();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void openFileManager(String path) {
		File file1 = new File(path);
		if (file1.isFile()) {
			file1 = file1.getParentFile();
		}
		try {
			Desktop.getDesktop().open(file1);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static void storeDownloadTask(List<DownLoadTask> object) {
		try (ObjectOutputStream oos = new ObjectOutputStream(Files.newOutputStream(Paths.get(TASK_CACHE_FILE)))) {
			oos.writeObject(object);
			oos.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static List<DownLoadTask> getCacheDownloadTask() {
		try (ObjectInputStream ois = new ObjectInputStream(Files.newInputStream(Paths.get(TASK_CACHE_FILE)))) {
			Object o = ois.readObject();
			return (List<DownLoadTask>) o;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
