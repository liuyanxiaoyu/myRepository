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
 * 募集资金与用途解析器
 */
public class FundraisingParser extends Parser{

	/**
	 * 解析募集资金与用途
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
		Elements trs = doc.body().select("table tr");
		if(trs == null || trs.size()<1) return;

		Map<String, String> thRegExpMap = getThRegExpMap(config);
		Map<String, String> tdRegExpMap = getTdRegExpMap(config);
		Map<Integer, String> thColMap = new HashMap<>();

		String unit = null;
		int thCount = 0;
		for(int i=0; i<trs.size(); i++) {
			List<Element> tdBodys = trs.get(i).children();

			//查找表格中是否有统一的货币单位
			if(i == 0){
				for(Element e : tdBodys){
					if(RegExpUtil.isMatch(thRegExpMap.get("货币单位"), e.text())){
						unit = RegExpUtil.getStartMatcher(tdRegExpMap.get("货币单位"), e.text());
						break;
					}
				}
			}

			//匹配关键字和每一列
			if((i==0 && unit == null) || (i==1 && unit != null)){
				thCount = tdBodys.size();
				for(String key : thRegExpMap.keySet()){
					for(int j=0; j<tdBodys.size(); j++){
						if(RegExpUtil.isMatch(thRegExpMap.get(key), tdBodys.get(j).text())){
							thColMap.put(j, key);
							break;
						}
					}
				}
				continue;
			}

			if(tdBodys.size() < thCount || thColMap.size() == 0) continue;

			Map<String, String> tdMap = new HashMap<>();
			for(int j=0; j<tdBodys.size(); j++){
				if(! thColMap.containsKey(j)) continue;

				String value = RegExpUtil.getStartMatcher(tdRegExpMap.get(thColMap.get(j)), tdBodys.get(j).text());
				tdMap.put(thColMap.get(j), value == null ? "" : value);
			}

			if(tdMap.size()>0){
				tdMap.put("货币单位", unit);
				list.add(tdMap);
			}
		}
	}

}
