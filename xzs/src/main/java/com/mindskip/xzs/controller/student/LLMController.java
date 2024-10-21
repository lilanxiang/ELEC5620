package com.mindskip.xzs.controller.student;

/**
 * @author LLX
 * @description
 * @since 2024/10/15 9:20
 */

import com.mindskip.xzs.service.OpenAIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api")
public class LLMController {
        // 注入 OpenAIService，用于调用 OpenAI API
        @Autowired
        private OpenAIService openAIService;

        // 定义一个 POST 接口，用于接受来自前端的 prompt 请求
        @PostMapping("/call-llm")
        public ResponseEntity<String> callOpenAI(@RequestBody String prompt) {
            try {
                // 调用 OpenAIService 获取模型响应
                String response = openAIService.getOpenAIResponse(prompt);
                return ResponseEntity.ok(response);
            } catch (Exception e) {
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("调用 OpenAI API 时发生错误");
            }
        }

    @PostMapping("/call-llm2")
    public ResponseEntity<String> callOpenAI2(@RequestBody String prompt) {
        try {
            // 调用 OpenAIService 获取模型响应
            String response = openAIService.getOpenAIResponse2(prompt);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("调用 OpenAI API 时发生错误");
        }
    }
}


