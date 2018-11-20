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
public class ParaphraseParser {

	/**
	 * 解析发行人基本信息
	 */
	public Map<String, String> getBaseInfo(String textContent, JSONObject config){
		System.out.println(textContent);
		if(StringUtils.isBlank(textContent)) return null;

		textContent = textContent.replace("\n", "");
		Map<String, String> map = new ConcurrentHashMap<>();
		Document doc = Jsoup.parse(textContent);

		//判断是否有表格，若有则优先处理表格内容
		tableHandle(doc, config, map);
//		System.out.println(map);
		//处理文本内容
		textHandle(doc, config, map);

//		System.out.println(map);
		return map;
	}

	/**
	 * 处理文本内容
	 * @param doc
	 * @param issuerMap
	 */
	private void textHandle(Document doc, JSONObject config, Map<String, String> issuerMap) {
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

			for(String key : textConfig.keySet()){
//				System.out.println("regexp: "+regExpMap.get(key));
//				System.out.println("text: "+text);
				String issuerInfo = RegExpUtil.getStartMatcher(regExpMap.get(key), text);
				if(StringUtils.isBlank(issuerInfo) ){
					continue;
				} else if(tableConfig.getJSONObject(key).getBoolean(ConfigConstant.CONF_JOIN)
						&& StringUtils.isNotBlank(issuerMap.get(key))){
					Set<String> valSet = new HashSet<>();
					valSet.addAll(Arrays.asList(issuerInfo.split("、")));
					valSet.addAll(Arrays.asList(issuerMap.get(key).split("、")));
					issuerMap.put(key, StringUtils.join(valSet, "、"));
				} else if(StringUtils.isNotBlank(issuerMap.get(key))){
					issuerMap.put(key, issuerInfo);
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
