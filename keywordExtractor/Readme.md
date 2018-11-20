#### 文本提取说明

##### 环境：Java 1.8

##### 把extractor-*.jar加到build path

##### 使用提取工具类

```java
public class ExtractUtil {

    public static String extractToJson(File pdfFile) {}

    public static String extractToJson(File pdfFile, String template) {}

}
```

##### POM依赖

```xml
<dependencies>
        <dependency>
            <groupId>org.apache.pdfbox</groupId>
            <artifactId>pdfbox</artifactId>
            <version>2.0.8</version>
        </dependency>
        <dependency>
            <groupId>org.jsoup</groupId>
            <artifactId>jsoup</artifactId>
            <version>1.11.3</version>
        </dependency>
        <dependency>
            <groupId>com.alibaba</groupId>
            <artifactId>fastjson</artifactId>
            <version>1.2.44</version>
        </dependency>
    </dependencies>
```

