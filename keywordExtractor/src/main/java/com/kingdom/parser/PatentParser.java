package com.kingdom.parser;


import com.alibaba.fastjson.JSONObject;
import com.kingdom.utils.RegExpUtil;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 专利
 */
public class PatentParser extends Parser{

	/**
	 * 解析专利
	 */
	public List<Map<String, String>> getBaseInfo(String textContent, JSONObject config){
		System.out.println(textContent);
		if(StringUtils.isBlank(textContent)) return null;

		textContent = textContent.replace("\n", "");
		List<Map<String, String>> list = new ArrayList<>();
		Document doc = Jsoup.parse(textContent);

		//处理文本内容
		tableHandle(doc, config, list);

		return list;
	}

	/**
	 * 处理表格内信息
	 * @param doc
	 * @param list
	 */
	private void tableHandle(Document doc, JSONObject config, List<Map<String, String>> list) {
		Elements tables = doc.body().select("table");
		if(tables == null || tables.size()<1) return;

		Map<String, String> thRegExpMap = getThRegExpMap(config);
		Map<String, String> tdRegExpMap = getTdRegExpMap(config);
		Map<Integer, String> thColMap = new HashMap<>();
		int thCount = 0;

		for(Element table : tables){
			if(! table.text().contains("专利号")) continue;

			Elements trs = table.select("tr");

			for(int i=0; i<trs.size(); i++) {
				List<Element> tds = trs.get(i).children();

				//匹配关键字和每一列名
				if(thColMap.size() == 0){
					for(String key : thRegExpMap.keySet()){
						for(int j=0; j<tds.size(); j++){
							if(RegExpUtil.isMatch(thRegExpMap.get(key), tds.get(j).text())){
								thColMap.put(j, key);
								break;
							}
						}
					}

					thCount = tds.size();
					continue;
				}

				if(tds.size() < thCount) continue;


				Map<String, String> tdMap = new HashMap<>();
				for(int j=0; j<tds.size(); j++){
					if(! thColMap.containsKey(j)) continue;

					String value = RegExpUtil.getStartMatcher(tdRegExpMap.get(thColMap.get(j)), tds.get(j).text());
					tdMap.put(thColMap.get(j), value == null ? "" : value);
				}

				if(tdMap.size()>0 && !"专利号".equals(tdMap.get("专利号"))){
					list.add(tdMap);
				}
			}
		}
	}

}
