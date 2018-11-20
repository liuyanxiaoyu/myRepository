package com.kingdom.extractor;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.kingdom.common.ConfigConstant;
import com.kingdom.parser.*;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * @Author YAN.LIU
 * @Date 2018/11/14 13:37
 **/
public class KeywordExtractor {

	private JSONObject jsonObject;

	private JSONObject config;

	public void build(String obj, String config) {
		if (StringUtils.isBlank(obj)) {
			throw new IllegalArgumentException();
		}
		this.jsonObject = JSON.parseObject(obj);

		if (StringUtils.isBlank(config)) {
			throw new IllegalArgumentException("The parameter of the input file name is must !");
		}
		this.config = JSON.parseObject(config);
	}

	/**
	 * 导出json格式文本
	 * @return
	 */
	public String exportJson() {
		Map<String, Object> map = new HashMap<>();
		PeopleParser peopleParser = null;
		Map<String, Set<String>> peopleMap = new HashMap<>();
		Map<String, String> locationMap = new HashMap<>();

		for (String key : config.keySet()) {
//			System.out.println(key);
			if(config.get(key) != null && config.get(key) instanceof Map){
				String location = config.getJSONObject(key).getString(ConfigConstant.LOCATION);
				String model = config.getJSONObject(key).getString(ConfigConstant.CONF_MODEL);
				String content_from = config.getJSONObject(key).getString(ConfigConstant.CONF_CONTENT_FROM);

				if(jsonObject.containsKey(content_from)){
					//获取待解析内容
					String content = jsonObject.getJSONObject(content_from).getString(ConfigConstant.FROM_CONTENT);

					//董监高和核心技术人员基本信息解析
					if("peopleParper".equalsIgnoreCase(model)){
						if(peopleParser == null){
							peopleParser = new PeopleParser();
						}

						Set<String> members = peopleParser.getMembers(content, config.getJSONObject(key));
						peopleMap.put(key, members);
						locationMap.put(key, location);

					//发行人基本信息解析
					} else if("issuerParper".equalsIgnoreCase(model)){
						IssuerParser issuerParser = new IssuerParser();
						Map<String, String> issuerMap = issuerParser.getBaseInfo(content, config.getJSONObject(key));
						Map<String, Object> map1 = new HashMap<>();
						map1.put(ConfigConstant.LOCATION, location);
						map1.put(ConfigConstant.TO_VALUE, issuerMap);
						map.put(key, map1);
						System.out.println(map);

					//释义解析
					} else if("paraphraseParper".equalsIgnoreCase(model)){
						System.out.println("start paraphrase....");
						ParaphraseParser paraphraseParser = new ParaphraseParser();
						Map<String, String> paraphraseMap = paraphraseParser.getBaseInfo(content, config.getJSONObject(key));
						Map<String, Object> map1 = new HashMap<>();
						map1.put(ConfigConstant.LOCATION, location);
						map1.put(ConfigConstant.TO_VALUE, paraphraseMap);
						map.put(key, map1);
						System.out.println(map);

					//发行人所处行业
					} else if("industryParper".equalsIgnoreCase(model)){
						System.out.println("start industryParper....");
						IndustryParser industryParser = new IndustryParser();
						List<Map<String, String>> industryMap = industryParser.getBaseInfo(content, config.getJSONObject(key));
						Map<String, Object> map1 = new HashMap<>();
						map1.put(ConfigConstant.LOCATION, location);
						map1.put(ConfigConstant.TO_VALUE, industryMap);
						map.put(key, map1);
						System.out.println(map);

					//募集资金用途
					} else if("fundraisingParper".equalsIgnoreCase(model)){
						System.out.println("start fundraisingParper....");
						FundraisingParser fundraisingParser = new FundraisingParser();
						List<Map<String, String>> industryMap = fundraisingParser.getBaseInfo(content, config.getJSONObject(key));
						Map<String, Object> map1 = new HashMap<>();
						map1.put(ConfigConstant.LOCATION, location);
						map1.put(ConfigConstant.TO_VALUE, industryMap);
						map.put(key, map1);
						System.out.println(map);

					//专利
					} else if("patentParper".equalsIgnoreCase(model)){
						System.out.println("start patentParper....");
						System.out.println("from_location"+jsonObject.getJSONObject(content_from).getString(ConfigConstant.LOCATION));

						PatentParser patentParser = new PatentParser();
						List<Map<String, String>> patentMap = patentParser.getBaseInfo(content, config.getJSONObject(key));
						Map<String, Object> map1 = new HashMap<>();
						map1.put(ConfigConstant.LOCATION, location);
						map1.put(ConfigConstant.TO_VALUE, patentMap);
						map.put(key, map1);
						System.out.println(map);

					//主要客户
					} else if("customerParper".equalsIgnoreCase(model)){
						System.out.println("start patentParper....");
						System.out.println("from_location"+jsonObject.getJSONObject(content_from).getString(ConfigConstant.LOCATION));

						PatentParser patentParser = new PatentParser();
						List<Map<String, String>> patentMap = patentParser.getBaseInfo(content, config.getJSONObject(key));
						Map<String, Object> map1 = new HashMap<>();
						map1.put(ConfigConstant.LOCATION, location);
						map1.put(ConfigConstant.TO_VALUE, patentMap);
						map.put(key, map1);
						System.out.println(map);
					}

				}

			}
		}

		if(peopleParser != null && peopleMap.size()>0){
			for(String key : peopleMap.keySet()){
				if(locationMap.get(key) != null && peopleParser.getMemberInfo(peopleMap.get(key)) != null){
					map.put(key, outputFormat(locationMap.get(key), peopleParser.getMemberInfo(peopleMap.get(key)).values()));
				} else {
					System.out.println(key + ": " + locationMap.get(key));
					System.out.println(key + ": " + peopleParser.getMemberInfo(peopleMap.get(key)));
				}
			}
		}

		return JSONObject.toJSONString(map, SerializerFeature.DisableCircularReferenceDetect);
	}

	/**
	 * 输出要素封装
	 * @param location
	 * @param values
	 * @return
	 */
	private static Map<String, Object> outputFormat(String location, Collection<Map<String, String>> values){
		Map<String, Object> map = new HashMap<>();
		map.put(ConfigConstant.LOCATION, location);
		map.put(ConfigConstant.TO_VALUE, values);

		return map;
	}
}
