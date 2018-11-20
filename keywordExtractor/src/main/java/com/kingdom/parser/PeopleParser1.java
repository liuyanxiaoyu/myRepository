package com.kingdom.parser;


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
public class PeopleParser1 {

	/**人员基本信息*/
	private static Set<String> BASE_INFO = new HashSet<>(Arrays.asList(
			"姓名", "国籍", "境外居留权", "性别", "出生年月",
				"学历", "职称", "现任职务", "起始日期", "终止日期"));

	/**存放所有人员基本信息*/
	private Map<String, Map<String, String>> allPeopleMap = new ConcurrentHashMap<>();

	/**待解析文本信息*/
	private String content;

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
	public Set<String> getMembers(String textContent){
		Map<String, Map<String, String>> map = getBaseInfo(textContent);
		System.out.println(map);
		return map == null ? null : map.keySet();
	}

	/**
	 * 从文本中解析出人员信息
	 */
	public Map<String, Map<String, String>> getBaseInfo(String textContent){
//		System.out.println(textContent);
		if(StringUtils.isBlank(textContent)) return null;

		textContent = textContent.replace("\n", "");
		Map<String, Map<String, String>> map = new ConcurrentHashMap<>();
		Document doc = Jsoup.parse(textContent);

		//判断是否有表格，若有则处理表格内容
		tableHandle(doc, map);
		System.out.println(map);
		//处理文本内容
		textHandle(doc, map);

		//清除无效数据
		for(String info : map.keySet()){
			if(map.get(info).size()>0 && StringUtils.isBlank(map.get(info).get("性别"))){
				map.remove(info);
			}
		}

		allPeopleMap.putAll(map);
//		System.out.println(map);
		return map;
	}

	/**
	 * 处理文本内容
	 * @param doc
	 * @param peopleMap
	 */
	private void textHandle(Document doc, Map<String, Map<String, String>> peopleMap) {
		List<Element> ps = doc.body().select("p");
		if(ps != null && ps.size()>1){
			for(Element p : ps){
				String text = p.text();
				if (text.contains("国籍") || text.contains("居留权")){
					System.out.println(text);
					String peopleName = RegExpUtil.getStartMatcher("(?<=[0-9：、])?[^，；：、]*(?=((先生|女士)?[，：]))", text);
					System.out.println(peopleMap);

					Map<String, String> people = peopleMap.get(peopleName) == null ? new HashMap<>() : peopleMap.get(peopleName);
					people.put("姓名", peopleName);

					if(people.get("性别") == null){
						if(text.contains("男") || text.contains("先生")){
							people.put("性别", "男");
						}else if(text.contains("女") || text.contains("女士")){
							people.put("性别", "女");
						}
					}

					text = text.replaceAll("\\s*", "");

					String post1 = people.get("现任职务");
					String post2 = RegExpUtil.getStartMatcher("(?<=(至今，任[职]?[本]?公司))[^，；。]*", text);
					if(post1 != null && post2 == null){

					} else if (post1 == null && post2 != null){
						people.put("现任职务", post2);
					} else if (post1 != null && post2 != null){
						Set<String> postSet = new HashSet<>();
						postSet.addAll(Arrays.asList(post1.split("、")));
						postSet.addAll(Arrays.asList(post2.split("、")));
						people.put("现任职务", StringUtils.join(postSet, "、"));
					} else {
						people.put("现任职务", "");
					}


					putRegStr2Map("国籍", "(?<=[，；：])[^，；：]*?(?=国籍)" +
							"|(?<=国籍)[^，；：]*?(?<=[，；：])", text, people);
					String nationality = people.get("国籍");
					putRegStr2Map("境外居留权", "(?<=[，；：])[^，；：]*?(?=永久境外居留权)" +
							"|(?<=[，；：])[^，；：]*?(?=境外永久居留权)" +
							"|(?<=[，；：])[^，；：]*?(?=境外居留权)", text, people);

					if(StringUtils.isBlank(nationality) && "无".equals(people.get("境外居留权"))){
						people.put("国籍", "中国");
					}
					if(StringUtils.isNotBlank(nationality) && StringUtils.isBlank(people.get("境外居留权"))
							&& !(nationality.equals("中国") || nationality.equals("中华人民共和国"))){
						people.put("境外居留权", "有");
					}

					putRegStr2Map("学历", "(?<=[，；：])[^，；：。]*?(?=学历)" +
							"|(?<=学历)[^，；：。]*?(?<=[，；：])|(?<=[，；：])(博士(研究生)?|硕士(研究生)?|(大学)?本科|大本|(大学)?专科|大专|中专|高中|初中|小学)(?=[，；：。])", text, people);

					String profTitle = "(?<=[，；：])[^，；：、总]{0,5}?(工程师|研究员|技术员|实验师)";
					putRegStr2Map("职称", "(?<=[，；：])[^，；：]*?(?=职称)" +
							"|(?<=职称)[^，；：]*?(?<=[，；：])" + "|" + profTitle, text, people);

					putRegStr2Map("出生年月", "(?<=[，；：])[^，；：]*?(?=[出]?生)" +
							"|(?<=出生日期)[^，；：]*?(?<=[，；：])|(?<=[，；：])[^，；：]*?岁", text, people);

					putRegStr2Map("起始日期", "(?<=[，；：。])[^，；：。]*?(?=(至今，任[职]?[本]?公司))", text, people);
                    putRegStr2Map("终止日期", "至今", text, people);

					if(peopleMap.get(people.get("姓名")) == null)
						peopleMap.put(people.get("姓名"), people);

				} else if (text.contains("简历同上") || text.contains("简历见")){
					String peopleName = RegExpUtil.getStartMatcher("(?<=[0-9：、])?[^，；：、]*(?=[，：])", text);
					Map<String, String> people = allPeopleMap.get(peopleName);
					peopleMap.put(peopleName, people == null ? new HashMap<>() : people);
				} else if (text.contains("简历详见")){
					String people = RegExpUtil.getStartMatcher("[^，；：]*(?=(的.*简历[详参]?见))", text);
					String[] peopleNames = people.split("人员");
					people = peopleNames.length > 1 ? peopleNames[peopleNames.length-1] : peopleNames[0];
					String[] names = people.replaceAll("先生|女士", "").split("、");
					for(String name : names){
						Map<String, String> peopleInfo = allPeopleMap.get(name);
						peopleMap.put(name, people == null ? new HashMap<>() : peopleInfo);
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
	private void tableHandle(Document doc, Map<String, Map<String, String>> peopleMap) {
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
			Map<Integer, String> tdMap = null;
			int tdCount = trs.get(0).children().size();
			for(int i=0; i<trs.size(); i++){
//                System.out.println("***********");
				if(i==0){
					List<Element> tdHeads = trs.get(i).children();
					tdMap = new HashMap<>();
					for (int j=0; j<tdHeads.size(); j++) {
						String th = tdHeads.get(j).text().trim();
						if (BASE_INFO.contains(th)){
							tdMap.put(j, tdHeads.get(j).text().trim());
						} else if (th.contains("职位") || th.contains("职务")){
							tdMap.put(j, "现任职务");
						} else if (th.contains("任期") || th.contains("任职期间")){
							tdMap.put(j, "任期");
						}
					}
//                    System.out.println(tdMap);
				} else {
					//判断首列序号是否缺失
					int currentTdCount = trs.get(i).children().size();
					if(tdCount - currentTdCount > 1) continue;
					boolean isFirstRowMiss = false;
					if(tdCount - currentTdCount == 1){
						isFirstRowMiss = true;
					}

					String peopleName = "";
					Map<String, String> people = new HashMap<>();
					List<Element> tdBodys = trs.get(i).children();
					for (int j=0; j<tdCount; j++) {
						if(tdMap.get(j) == null) continue;

						String text = isFirstRowMiss ? tdBodys.get(j-1).text().trim() : tdBodys.get(j).text().trim();
						if(tdMap.get(j).equals("任期")){
							people.put("起始日期", RegExpUtil.getStartMatcher("(\\d{2,4}[年.-/]\\d{1,2}[月.-/]\\d{1,2}[日]?)(?=[至|到|-])", text));
							people.put("终止日期", RegExpUtil.getStartMatcher("(?<=[至|到|-])(\\d{2,4}[年.-/]\\d{1,2}[月.-/]\\d{1,2}[日]?)", text));
						} else {
							people.put(tdMap.get(j), text);
							if(tdMap.get(j).equals("姓名")) peopleName = text;
						}
					}
					if(people.size()>0)
						peopleMap.put(peopleName, people);
				}
			}
		}
	}

	/**
	 * 查找当前map中没有的数据项
	 * @param key
	 * @param content
	 * @param regExp
	 * @param people
	 */
	private static void putRegStr2Map(String key, String regExp, String content, Map<String, String> people){
		if (people.get(key) == null){
			String value = RegExpUtil.getStartMatcher(regExp, content);
			people.put(key, value == null ? "" : value);
		}
	}

}
