package com.kingdom.starter;


import com.kingdom.extractor.KeywordExtractor;
import com.kingdom.utils.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.*;

/**
 * @Author YAN.LIU
 * @Date 2018/11/16 16:29
 **/
public class Starter {

	public static void main(String[] args) throws Exception {
		String fromPathName = args != null && args.length>0 && StringUtils.isNotBlank(args[0]) ? args[0] : "output1.json";
		String toPathName = args != null && args.length>1 && StringUtils.isNotBlank(args[1]) ? args[1] : null;
		String confPathName = args != null && args.length>2 && StringUtils.isNotBlank(args[2]) ? args[2] : null;

		if(fromPathName == null) throw new IllegalArgumentException("The parameter of the input file name is must !");

		String line, config = "";
		if(StringUtils.isBlank(confPathName)){
			try (InputStream ins = Starter.class.getClass().getResourceAsStream("/config.json");
				 BufferedReader br = new BufferedReader(new InputStreamReader(ins, "UTF-8"))
			) {
				while ((line = br.readLine()) != null) {
					config += line;
				}
			} catch (IOException e) {
				e.printStackTrace();
				throw e;
			}
		} else {
			config = FileUtils.readFile(confPathName);
		}

		File fromFile = new File(fromPathName);

		if(fromFile.isDirectory()){
			File[] files = fromFile.listFiles();
			if (files != null) {
				for (int i = 0; i < files.length; i++) {
					String fileName = files[i].getName();
					if (files[i].isFile() && fileName.endsWith(".json")) {
						doTask(files[i].getName(), toPathName, config);
					}
				}
			}
		} else {
			doTask(fromPathName, toPathName, config);
		}
	}

	/**
	 * 执行解析过程并输出结果
	 * @param fileName
	 * @param toPathName
	 * @param config
	 * @throws Exception
	 */
	private static void doTask(String fileName, String toPathName, String config) throws Exception {
		String json = "";
		String input = FileUtils.readFile(fileName);
		if(StringUtils.isNotBlank(input) && StringUtils.isNotBlank(config)){
			KeywordExtractor extractor = new KeywordExtractor();
			extractor.build(input, config);
			json = extractor.exportJson();
		}


		if(StringUtils.isBlank(toPathName)){
			FileUtils.writeFile(fileName.replace(".json", "_result.json"), json);
		} else {
			File toFile = new File(toPathName);
			if(toPathName.matches("^.*[\\\\/]+$") && !toFile.exists()){
				toFile.mkdirs();
			}

			if(toFile.isDirectory()){
				fileName = (new File(fileName)).getName().replace(".json", "_result.json");
				FileUtils.writeFile(toPathName + File.separator + fileName, json);
			} else {
				FileUtils.writeFile(toPathName, json);
			}
		}
	}

}
