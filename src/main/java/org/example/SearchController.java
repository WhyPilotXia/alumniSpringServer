package org.example;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
public class SearchController {
    private static final Logger log = LoggerFactory.getLogger(SearchController.class);
    private final DataService dataService;

    public SearchController(DataService dataService) {
        this.dataService = dataService;
    }

    @PostMapping("/api/search")
    public Object search(@RequestBody String body, HttpServletRequest rawRequest){
        // 获取IP
        String ip = rawRequest.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = rawRequest.getRemoteAddr();
        }

        // User-Agent
        String ua = rawRequest.getHeader("User-Agent");

        // URI 和方法
        String uri = rawRequest.getRequestURI();
        String method = rawRequest.getMethod();

        log.info("IP={} {} {} Body={} UA={}", ip, method, uri, body, ua);
        try {
            Map<String, Object> request = new ObjectMapper().readValue(body, Map.class);

            String keyword = request.get("keyword").toString();
            int page = Integer.parseInt(request.getOrDefault("page", 1).toString());
            int size = Integer.parseInt(request.getOrDefault("size", 20).toString());

            if (size > 20) size = 20;

            List<String> rawList = dataService.getRawJsonList();
            List<String> indexList = dataService.getSearchIndexList();

            int total = 0;
            List<String> pageData = new ArrayList<>();

            int fromIndex = (page - 1) * size;
            int toIndex = fromIndex + size;

            for (int i = 0; i < indexList.size(); i++) {

                if (indexList.get(i).contains(keyword)) {

                    if (total >= fromIndex && total < toIndex) {
                        pageData.add(rawList.get(i));
                    }

                    total++;
                }
            }

            Map<String, Object> result = new HashMap<>();
            result.put("total", total);
            result.put("data", pageData);

            return result;
        }catch (JsonProcessingException|NumberFormatException e){
            Map<String, Object> result = new HashMap<>();
            result.put("total", -1);
            Map<String, Object> errorData = new HashMap<>();
            errorData.put("errorType", "JsonProcessingException"); // 异常类型
            errorData.put("errorMessage", e.getMessage()); // 异常提示信息
            errorData.put("stackTrace", Arrays.toString(e.getStackTrace()));
            result.put("data", errorData);

            return result;
        }
    }
}
