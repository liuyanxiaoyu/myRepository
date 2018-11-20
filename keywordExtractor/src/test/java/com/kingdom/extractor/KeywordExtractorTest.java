package com.kingdom.extractor;

import com.kingdom.utils.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.junit.Assert.assertFalse;

public class KeywordExtractorTest {

    private KeywordExtractor extractor;
    private String config;


    @Before
    public void build() throws Exception {
        URI uri = KeywordExtractorTest.class.getClassLoader().getResource("output1.json").toURI();
        URI confUri = KeywordExtractorTest.class.getClassLoader().getResource("config.json").toURI();
        config = new String(Files.readAllBytes(Paths.get(confUri)));
        extractor = new KeywordExtractor();
        extractor.build(new String(Files.readAllBytes(Paths.get(uri))), config);
    }

    /**
     * 董监高及核心技术人员
     * @throws Exception
     */
    @Test
    public void getAllPeopleInfo() throws Exception {
        String json = extractor.exportJson();
        System.out.println(json);
        FileUtils.writeFile("resultJson.json", json);
    }

    @Test
    public void exportJson() throws Exception {
//        String rootPath = "C:\\Users\\123\\Desktop\\关键字提取\\数据提取\\关键字提取\\";
        String filename = "龙岩卓越新能源.json";
//        String filename = "北京锋尚世纪文化传媒.json";
//        String filename = "西安瑞联新材料.json";
//        String filename = "铁将军汽车电子.json";
//        String filename = "_万兴科技.json";
//        String filename = "_浙江捷昌线性驱动.json";
//        String filename = "_深圳市安健科技.json";
//        String filename = "_苏州工业园区凌志软件.json";
//        String filename = "output1.json";
        URI uri = KeywordExtractorTest.class.getClassLoader().getResource(filename).toURI();
        extractor = new KeywordExtractor();
        extractor.build(new String(Files.readAllBytes(Paths.get(uri))), config);

        String json = extractor.exportJson();

        FileUtils.writeFile("output\\result_" + filename, json);
    }

    @Test
    public void batchExpostJson() throws Exception {
//        File dir = new File("D:\\IEDAWorkspaces\\kdpdfextractor\\target\\test-classes");
        File dir = new File("input_json");
        System.out.println(dir.getAbsolutePath());
        File[] files = dir.listFiles();
        if (files != null) {
            for (int i = 0; i < files.length; i++) {
                String fileName = files[i].getName();
                if (files[i].isFile() && fileName.endsWith("_clean.json")) {
                    System.out.println("---" + files[i].getName());

                    String input = FileUtils.readFile(files[i]);
                    String json = "";
                    if(StringUtils.isNotBlank(input) && StringUtils.isNotBlank(config)){
                        extractor.build(input, config);
                        json = extractor.exportJson();
                    }


                    FileUtils.writeFile("output_json" + File.separator + files[i].getName().replace("_clean", "_result"), json);
                }
            }

        }
    }

}