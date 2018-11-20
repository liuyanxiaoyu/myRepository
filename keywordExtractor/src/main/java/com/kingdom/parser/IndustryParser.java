package com.kingdom.parser;


import com.alibaba.fastjson.JSONObject;
import com.kingdom.common.ConfigConstant;
import com.kingdom.utils.RegExpUtil;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 发行人基本信息解析
 */
public class IndustryParser {

	/**
	 * 解析发行人基本信息
	 */
	public List<Map<String, String>> getBaseInfo(String textContent, JSONObject config){
		System.out.println(textContent);
		if(StringUtils.isBlank(textContent)) return null;

		textContent = textContent.replace("\n", "");
		List<Map<String, String>> list = new ArrayList<>();
		Document doc = Jsoup.parse(textContent);

		//处理文本内容
		textHandle(doc, config, list);

		return list;
	}

	/**
	 * 处理文本内容
	 * @param doc
	 * @param list
	 */
	private void textHandle(Document doc, JSONObject config, List<Map<String, String>> list) {
		List<Element> ps = doc.body().select("p");
		if(ps == null || ps.size()<1) return;

		JSONObject textConfig = config.getJSONObject(ConfigConstant.CONF_TEXT_KEYWORDS);
		JSONObject tableConfig = config.getJSONObject(ConfigConstant.CONF_TABLE_KEYWORDS);
		Map<String, String> regExpMap = new HashMap<>();
		for(String key : textConfig.keySet()){
			String regExp = StringUtils.join(textConfig.getJSONObject(key).getJSONArray(ConfigConstant.CONF_EXTRACT_REGEXP), "|");
			regExpMap.put(key, regExp == null ? "" : regExp);
		}

		for(Element p : ps){
			String text = p.text().replaceAll("\\s*", "");

			if(RegExpUtil.isMatch("所属行业|.*行业.*分类", text)){
				System.out.println("text: " + text);
				String[] classifys = text.split("；|。");
				for(String classifyText : classifys){
					System.out.println(classifyText);
					Map<String, String> infoMap = null;
					for(String key : textConfig.keySet()){
						String info = RegExpUtil.getStartMatcher(regExpMap.get(key), classifyText);
						if(StringUtils.isNotBlank(info)){
							if(infoMap == null) infoMap = new HashMap<>();
							infoMap.put(key, info);
						}
					}

					if(infoMap != null) list.add(infoMap);
				}
			}

		}
	}

	/**
	 * 处理表格内信息
	 * @param doc
	 * @param paraphraseMap
	 */
	private void tableHandle(Document doc, JSONObject config, Map<String, String> paraphraseMap) {
		Elements trs = doc.body().select("table tr");
		if(trs == null || trs.size()<1) return;

		Map<String, String> thRegExpMap = new HashMap<>();
		Map<String, String> tdRegExpMap = new HashMap<>();

		JSONObject tableConfig = config.getJSONObject(ConfigConstant.CONF_TABLE_KEYWORDS);
		for(String key : tableConfig.keySet()){
			thRegExpMap.put(key, StringUtils.join(tableConfig.getJSONObject(key).getJSONArray(ConfigConstant.CONF_TH_REGEXP), "|"));
			tdRegExpMap.put(key, StringUtils.join(tableConfig.getJSONObject(key).getJSONArray(ConfigConstant.CONF_EXTRACT_REGEXP), "|"));
		}

		for(int i=0; i<trs.size(); i++) {
			List<Element> tdBodys = trs.get(i).children();
			if(tdBodys.size() != 3) continue;

			String keyword = tdBodys.get(0).text().trim();
			String value = tdBodys.get(2).text().trim();
			if(StringUtils.isBlank(keyword) || StringUtils.isBlank(value)) continue;

			if(RegExpUtil.isMatch(thRegExpMap.get("简称"), keyword) && RegExpUtil.isMatch(tdRegExpMap.get("全称"), value))
				paraphraseMap.put(keyword, value);
		}
	}

}
