package com.mindskip.xzs.service;

/**
 * @author LLX
 * @description
 * @since 2024/10/15 10:14
 */

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OpenAIService {
    private static final String API_URL = "https://api.openai.com/v1/chat/completions";
    private final RestTemplate restTemplate = new RestTemplate();




    public String getOpenAIResponse(String prompt) {
//       prompt = prompt +"我可以得多少分，假如满分是100,只给我分数就好了";
       // 构造请求头
       HttpHeaders headers = new HttpHeaders();
       headers.setContentType(MediaType.APPLICATION_JSON);
       headers.setBearerAuth(API_KEY);

       // 构造请求体，使用消息历史而不是简单的 prompt
       Map<String, Object> requestBody = new HashMap<>();
       requestBody.put("model", "gpt-4");  // 使用新的模型

       // 构建消息数组
       List<Map<String, String>> messages = new ArrayList<>();

       // 系统消息，定义助手的行为
       Map<String, String> systemMessage = new HashMap<>();
       systemMessage.put("role", "system");
       systemMessage.put("content", "You are a helpful assistant.");
       messages.add(systemMessage);

       // 用户输入的消息
       Map<String, String> userMessage = new HashMap<>();
       userMessage.put("role", "user");
       userMessage.put("content", prompt);
       messages.add(userMessage);

       // 将消息历史添加到请求体
       requestBody.put("messages", messages);

       // 添加其他参数
       requestBody.put("max_tokens", 500);
       requestBody.put("temperature", 0.7);

       try {
           // 使用 ObjectMapper 将请求体转换为 JSON 字符串
           ObjectMapper objectMapper = new ObjectMapper();
           String jsonRequestBody = objectMapper.writeValueAsString(requestBody);

           // 将请求头和请求体封装到 HttpEntity
           HttpEntity<String> entity = new HttpEntity<>(jsonRequestBody, headers);

           // 发送 POST 请求并获取响应
           ResponseEntity<String> response = restTemplate.exchange(API_URL, HttpMethod.POST, entity, String.class);
           System.out.println(response.getBody());

           if (response.getStatusCode() == HttpStatus.OK) {
               return response.getBody();  // 返回响应内容
           } else {
               return "请求失败，状态码: " + response.getStatusCode();
           }
       } catch (Exception e) {
           e.printStackTrace();
           return "请求失败，发生异常: " + e.getMessage();
       }
   }

   // AI 自动评分
    public String getOpenAIResponse2(String prompt) {
       prompt = prompt +"我可以得多少分，假如满分是100，只给我一个数字";
        // 构造请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(API_KEY);

        // 构造请求体，使用消息历史而不是简单的 prompt
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-4");  // 使用新的模型

        // 构建消息数组
        List<Map<String, String>> messages = new ArrayList<>();

        // 系统消息，定义助手的行为
        Map<String, String> systemMessage = new HashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", "You are a helpful assistant.");
        messages.add(systemMessage);

        // 用户输入的消息
        Map<String, String> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", prompt);
        messages.add(userMessage);

        // 将消息历史添加到请求体
        requestBody.put("messages", messages);

        // 添加其他参数
        requestBody.put("max_tokens", 500);
        requestBody.put("temperature", 0.7);

        try {
            // 使用 ObjectMapper 将请求体转换为 JSON 字符串
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonRequestBody = objectMapper.writeValueAsString(requestBody);

            // 将请求头和请求体封装到 HttpEntity
            HttpEntity<String> entity = new HttpEntity<>(jsonRequestBody, headers);

            // 发送 POST 请求并获取响应
            ResponseEntity<String> response = restTemplate.exchange(API_URL, HttpMethod.POST, entity, String.class);
            System.out.println(response.getBody());

            if (response.getStatusCode() == HttpStatus.OK) {
                return response.getBody();  // 返回响应内容
            } else {
                return "请求失败，状态码: " + response.getStatusCode();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "请求失败，发生异常: " + e.getMessage();
        }
    }

    // 生成类似错题
    public String getOpenAIResponse3(String prompt) {
        prompt = prompt +"我只需要你给出这个问题的三个类似问题。请分点罗列出来。";
        // 构造请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(API_KEY);

        // 构造请求体，使用消息历史而不是简单的 prompt
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "gpt-4");  // 使用新的模型

        // 构建消息数组
        List<Map<String, String>> messages = new ArrayList<>();

        // 系统消息，定义助手的行为
        Map<String, String> systemMessage = new HashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", "You are a helpful assistant.");
        messages.add(systemMessage);

        // 用户输入的消息
        Map<String, String> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", prompt);
        messages.add(userMessage);

        // 将消息历史添加到请求体
        requestBody.put("messages", messages);

        // 添加其他参数
        requestBody.put("max_tokens", 500);
        requestBody.put("temperature", 0.7);

        try {
            // 使用 ObjectMapper 将请求体转换为 JSON 字符串
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonRequestBody = objectMapper.writeValueAsString(requestBody);

            // 将请求头和请求体封装到 HttpEntity
            HttpEntity<String> entity = new HttpEntity<>(jsonRequestBody, headers);

            // 发送 POST 请求并获取响应
            ResponseEntity<String> response = restTemplate.exchange(API_URL, HttpMethod.POST, entity, String.class);
            System.out.println(response.getBody());

            if (response.getStatusCode() == HttpStatus.OK) {
                return response.getBody();  // 返回响应内容
            } else {
                return "请求失败，状态码: " + response.getStatusCode();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "请求失败，发生异常: " + e.getMessage();
        }
    }
}

