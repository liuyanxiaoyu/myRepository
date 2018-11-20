package com.kingdom.utils;

import java.io.*;

/**
 * @Author YAN.LIU
 * @Date 2018/11/13 14:53
 **/
public class FileUtils {
	private static final String CHARSET = "UTF-8";

	/**
	 * 读取文件内容返回文本字符串
	 */
	public static String readFile(File file) throws IOException {
		String line, str="";
		try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), CHARSET))
		) {
			while ((line = br.readLine()) != null) {
				str += line;
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		}

		return str;
	}

	/**
	 * 读取文件内容返回文本字符串
	 */
	public static String readFile(String pathname) throws IOException {
		String line, str="";
		try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(pathname), CHARSET))
		) {
			while ((line = br.readLine()) != null) {
				str += line;
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		}

		return str;
	}

	/**
	 * 将字符串写入文本
	 */
	public static void writeFile(String pathname, String str) throws IOException {
		try {
			File writeName = new File(pathname);
			writeName.createNewFile();
			try (PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(pathname), CHARSET)))
			) {
				out.write(str);
				out.flush();
			}
		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		}
	}

}
