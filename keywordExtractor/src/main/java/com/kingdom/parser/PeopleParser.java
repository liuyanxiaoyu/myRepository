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
 * 解析董、监、高及核心技术人员基本情况
 * @Author YAN.LIU
 * @Date 2018/11/15 11:26
 **/
public class PeopleParser {

	/**存放所有人员基本信息*/
	private Map<String, Map<String, String>> allPeopleMap = new ConcurrentHashMap<>();

	/**
	 * 根据姓名获取成员信息
	 * @param memberNames
	 * @return
	 */
	public Map<String, Map<String, String>> getMemberInfo(Collection<String> memberNames){
		if(memberNames == null || memberNames.size()<1) return null;

		Map<String, Map<String, String>> map = new HashMap<>();
		for(String name : memberNames){
			map.put(name, allPeopleMap.get(name));
		}

		return map;
	}

	/**
	 * 根据姓名获取成员信息
	 * @param memberName
	 * @return
	 */
	public Map<String, String> getMemberInfo(String memberName){
		return allPeopleMap.get(memberName);
	}

	/**
	 * 获取成员名称
	 * @param textContent
	 * @return
	 */
	public Set<String> getMembers(String textContent, JSONObject config){
		Map<String, Map<String, String>> map = getBaseInfo(textContent, config);
		return map == null ? null : map.keySet();
	}

	/**
	 * 从文本中解析出人员信息
	 */
	public Map<String, Map<String, String>> getBaseInfo(String textContent, JSONObject config){
//		System.out.println(textContent);
		if(StringUtils.isBlank(textContent)) return null;

		textContent = textContent.replace("\n", "");
		Map<String, Map<String, String>> map = new ConcurrentHashMap<>();
		Document doc = Jsoup.parse(textContent);

		//判断是否有表格，若有则优先处理表格内容
		tableHandle(doc, config, map);
//		System.out.println(map);
		//处理文本内容
		textHandle(doc, config, map);

		//清除无效数据, 数据清洗工作后续
//		for(String info : map.keySet()){
//			if(map.get(info).size()>0 && StringUtils.isBlank(map.get(info).get("性别"))){
//				map.remove(info);
//			}
//		}

		allPeopleMap.putAll(map);
//		System.out.println(map);
		return map;
	}

	/**
	 * 处理文本内容
	 * @param doc
	 * @param peopleMap
	 */
	private void textHandle(Document doc, JSONObject config, Map<String, Map<String, String>> peopleMap) {
		List<Element> ps = doc.body().select("p");
		JSONObject textConfig = config.getJSONObject(ConfigConstant.CONF_TEXT_KEYWORDS);
		JSONObject tableConfig = config.getJSONObject(ConfigConstant.CONF_TABLE_KEYWORDS);
		String nameRegExp = StringUtils.join(textConfig.getJSONObject("姓名").getJSONArray(ConfigConstant.CONF_EXTRACT_REGEXP), "|");
		if(ps != null && ps.size()>1){
			for(Element p : ps){
				String text = p.text();
//				System.out.println(text);
				if (RegExpUtil.isMatch("国籍|居留权", text)){
//					System.out.println(text);

					String peopleName = RegExpUtil.getStartMatcher(nameRegExp, text);
					if(StringUtils.isBlank(peopleName)) continue;

					Map<String, String> people = peopleMap.get(peopleName) == null ? new HashMap<>() : peopleMap.get(peopleName);
					people.put("姓名", peopleName);

					text = text.replaceAll("\\s*", "");

					for(String key : textConfig.keySet()){
						if("姓名".equals(key)) continue;

						String textRegExp = StringUtils.join(textConfig.getJSONObject(key).getJSONArray(ConfigConstant.CONF_EXTRACT_REGEXP), "|");
						String textVal = RegExpUtil.getStartMatcher(textRegExp, text);
						if(tableConfig.getJSONObject(key).getBoolean(ConfigConstant.CONF_JOIN)){
							String tableVal = people.get(key);
							if(StringUtils.isNotBlank(tableVal) && StringUtils.isBlank(textVal)){

							} else if (StringUtils.isBlank(tableVal) && StringUtils.isNotBlank(textVal)){
								people.put(key, textVal);
							} else if (StringUtils.isNotBlank(tableVal) && StringUtils.isNotBlank(textVal)){
								Set<String> valSet = new HashSet<>();
								valSet.addAll(Arrays.asList(tableVal.split("、")));
								valSet.addAll(Arrays.asList(textVal.split("、")));
								people.put(key, StringUtils.join(valSet, "、"));
							} else {
								people.put(key, "");
							}
						} else {
							if (people.get(key) == null) {
								people.put(key, textVal == null ? "" : textVal);
							}
						}
					}


					if(people.get("性别") != null){
						if(text.contains("男") || text.contains("先生")){
							people.put("性别", "男");
						}else if(text.contains("女") || text.contains("女士")){
							people.put("性别", "女");
						}
					}

					String nationality = people.get("国籍");
					if(StringUtils.isBlank(nationality) && "无".equals(people.get("境外居留权"))){
						people.put("国籍", "中国");
					}
					if(StringUtils.isNotBlank(nationality) && StringUtils.isBlank(people.get("境外居留权"))
							&& !(nationality.equals("中国") || nationality.equals("中华人民共和国"))){
						people.put("境外居留权", "有");
					}

					if(peopleMap.get(people.get("姓名")) == null)
						peopleMap.put(people.get("姓名"), people);

				} else if (RegExpUtil.isMatch("简历同上|.*简历[详参]?见", text)){
					String peopleNameStr = RegExpUtil.getStartMatcher("[^，；：]*(?=(的.*简历[详参]?见))", text);
					if(StringUtils.isNotBlank(peopleNameStr)){
						String[] peopleNames = peopleNameStr.split("人员");
						peopleNameStr = peopleNames.length > 1 ? peopleNames[peopleNames.length-1] : peopleNames[0];
						String[] names = peopleNameStr.replaceAll("先生|女士", "").split("、");
						for(String name : names){
							if(StringUtils.isBlank(name)) continue;
							Map<String, String> peopleInfo = allPeopleMap.get(name);
							peopleMap.put(name, peopleInfo == null ? new HashMap<>() : peopleInfo);
						}
					} else {
						String peopleName = RegExpUtil.getStartMatcher(nameRegExp, text);
						if(StringUtils.isBlank(peopleName)) continue;

						Map<String, String> people = allPeopleMap.get(peopleName);
						peopleMap.put(peopleName, people == null ? new HashMap<>() : people);
					}
				}
			}
		}
	}

	/**
	 * 处理表格内人员信息
	 * @param doc
	 * @param peopleMap
	 */
	private void tableHandle(Document doc, JSONObject config, Map<String, Map<String, String>> peopleMap) {
		Elements tables = doc.body().select("table");
		if(tables == null || tables.size()<1) return;

		Element infoTable = null;
		for(Element table : tables){
			List<Element> tds = table.select("tr td");
			if(tds != null && tds.size()>1){
				for(int i=0; i<tds.size(); i++){
					if("姓名".equals(tds.get(i).text().trim())){
						infoTable = table;
						break;
					}
				}
			}
		}
		if(infoTable == null) return;

		List<Element> trs = infoTable.select("tr");
		if(trs != null && trs.size()>1){
			Map<Integer, String> thMap = new HashMap<>();
			Map<String, Integer> tdMap = new HashMap<>();
			Map<String, String> thRegExpMap = new HashMap<>();
			Map<String, String> tdRegExpMap = new HashMap<>();

			int tdCount = trs.get(0).children().size();
			JSONObject tableConfig = config.getJSONObject(ConfigConstant.CONF_TABLE_KEYWORDS);
			for(String key : tableConfig.keySet()){
				thRegExpMap.put(key, StringUtils.join(tableConfig.getJSONObject(key).getJSONArray(ConfigConstant.CONF_TH_REGEXP), "|"));
				tdRegExpMap.put(key, StringUtils.join(tableConfig.getJSONObject(key).getJSONArray(ConfigConstant.CONF_EXTRACT_REGEXP), "|"));
			}

			for(int i=0; i<trs.size(); i++){
				List<Element> tdBodys = trs.get(i).children();
				if(i==0){
					List<Element> tdHeads = tdBodys;

					for (int j=0; j<tdHeads.size(); j++) {
						String th = tdHeads.get(j).text().trim();
						thMap.put(j, th);
						for(String key : thRegExpMap.keySet()){
							if(key.equalsIgnoreCase(th) || RegExpUtil.isMatch(thRegExpMap.get(key), th)){
								tdMap.put(key, j);
							}
						}
					}
				} else {
					//判断首列序号是否缺失
					int currentTdCount = tdBodys.size();
					if(tdCount - currentTdCount > 1) continue;
					boolean isFirstRowMiss = false;
					if(tdCount - currentTdCount == 1){
						isFirstRowMiss = true;
					}

					String peopleName = "";
					Map<String, String> people = new HashMap<>();
//					System.out.println(tdCount);
//					System.out.println(currentTdCount);
					for (int j=0; j<tdCount; j++) {
						if(! tdMap.containsValue(j)) continue;
						if(isFirstRowMiss && j == 0) break;

//						System.out.println("j"+j);
						String text = isFirstRowMiss ? tdBodys.get(j-1).text().trim() : tdBodys.get(j).text().trim();
						for(String key : tdMap.keySet()){
							if(tdMap.get(key) == j){
								people.put(key, StringUtils.isBlank(tdRegExpMap.get(key)) ? text : RegExpUtil.getStartMatcher(tdRegExpMap.get(key), text));
							}
						}

						if(thMap.get(j).equals("姓名")) peopleName = text;
					}
					if(people.size()>0)
						peopleMap.put(peopleName, people);
				}
			}
		}
	}

}
