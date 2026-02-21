package org.example;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.*;

@Service
public class DataService {

    // 只存原始 JSON 字符串
    private List<String> rawJsonList = new ArrayList<>();

    // 存扁平化索引字符串
    private List<String> searchIndexList = new ArrayList<>();

    public List<String> getRawJsonList() {
        return rawJsonList;
    }

    public List<String> getSearchIndexList() {
        return searchIndexList;
    }

    @PostConstruct
    public void loadData() {
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonFactory factory = mapper.getFactory();

            InputStream is = new ClassPathResource("alumni.json").getInputStream();
            JsonParser parser = factory.createParser(is);

            if (parser.nextToken() != JsonToken.START_ARRAY) {
                throw new RuntimeException("JSON 必须是数组格式");
            }

            int count = 0;

            // 一条一条读取
            while (parser.nextToken() == JsonToken.START_OBJECT) {

                Map<String, Object> item =
                        mapper.readValue(parser, Map.class);

                // 保存原始 JSON
                String rawJson = mapper.writeValueAsString(item);
                rawJsonList.add(rawJson);

                // 构建扁平索引
                String flat = flatten(item);
                searchIndexList.add(flat);

                count++;

                if (count % 10000 == 0) {
                    System.out.println("已加载: " + count);
                }
            }

            System.out.println("数据加载完成，总条数: " + count);

            parser.close();
            is.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 扁平化
    private String flatten(Object obj) {
        StringBuilder sb = new StringBuilder();
        flattenRecursive(obj, sb);
        return sb.toString();
    }

    private void flattenRecursive(Object obj, StringBuilder sb) {

        if (obj == null) return;

        if (obj instanceof String str) {
            sb.append(str).append(" ");
        }
        else if (obj instanceof Number num) {
            sb.append(num.toString()).append(" ");
        }
        else if (obj instanceof Map<?, ?> map) {
            for (Object value : map.values()) {
                flattenRecursive(value, sb);
            }
        }
        else if (obj instanceof List<?> list) {
            for (Object item : list) {
                flattenRecursive(item, sb);
            }
        }
    }
}
