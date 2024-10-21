package com.mindskip.xzs.controller.student;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mindskip.xzs.base.BaseApiController;
import com.mindskip.xzs.base.RestResponse;
import com.mindskip.xzs.domain.ExamPaperQuestionCustomerAnswer;
import com.mindskip.xzs.domain.Subject;
import com.mindskip.xzs.domain.TextContent;
import com.mindskip.xzs.domain.question.QuestionObject;
import com.mindskip.xzs.service.*;
import com.mindskip.xzs.utility.DateTimeUtil;
import com.mindskip.xzs.utility.HtmlUtil;
import com.mindskip.xzs.utility.JsonUtil;
import com.mindskip.xzs.utility.PageInfoHelper;
import com.mindskip.xzs.viewmodel.admin.question.QuestionEditRequestVM;
import com.mindskip.xzs.viewmodel.student.exam.ExamPaperSubmitItemVM;
import com.mindskip.xzs.viewmodel.student.question.answer.QuestionAnswerVM;
import com.mindskip.xzs.viewmodel.student.question.answer.QuestionPageStudentRequestVM;
import com.mindskip.xzs.viewmodel.student.question.answer.QuestionPageStudentResponseVM;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController("StudentQuestionAnswerController")
@RequestMapping(value = "/api/student/question/answer")
public class QuestionAnswerController extends BaseApiController {

    private final ExamPaperQuestionCustomerAnswerService examPaperQuestionCustomerAnswerService;
    private final QuestionService questionService;
    private final TextContentService textContentService;
    private final SubjectService subjectService;

    @Autowired
    OpenAIService openAIService;

    @Autowired
    public QuestionAnswerController(ExamPaperQuestionCustomerAnswerService examPaperQuestionCustomerAnswerService, QuestionService questionService, TextContentService textContentService, SubjectService subjectService) {
        this.examPaperQuestionCustomerAnswerService = examPaperQuestionCustomerAnswerService;
        this.questionService = questionService;
        this.textContentService = textContentService;
        this.subjectService = subjectService;
    }

    @RequestMapping(value = "/page", method = RequestMethod.POST)
    public RestResponse<PageInfo<QuestionPageStudentResponseVM>> pageList(@RequestBody QuestionPageStudentRequestVM model) {
        model.setCreateUser(getCurrentUser().getId());
        PageInfo<ExamPaperQuestionCustomerAnswer> pageInfo = examPaperQuestionCustomerAnswerService.studentPage(model);
        PageInfo<QuestionPageStudentResponseVM> page = PageInfoHelper.copyMap(pageInfo, q -> {
            Subject subject = subjectService.selectById(q.getSubjectId());
            QuestionPageStudentResponseVM vm = modelMapper.map(q, QuestionPageStudentResponseVM.class);
            vm.setCreateTime(DateTimeUtil.dateFormat(q.getCreateTime()));
            TextContent textContent = textContentService.selectById(q.getQuestionTextContentId());
            QuestionObject questionObject = JsonUtil.toJsonObject(textContent.getContent(), QuestionObject.class);
            String clearHtml = HtmlUtil.clear(questionObject.getTitleContent());
            vm.setShortTitle(clearHtml);
            vm.setSubjectName(subject.getName());
            return vm;
        });
        return RestResponse.ok(page);
    }


//    @RequestMapping(value = "/select/{id}", method = RequestMethod.POST)
//    public RestResponse<QuestionAnswerVM> select(@PathVariable Integer id) {
//        QuestionAnswerVM vm = new QuestionAnswerVM();
//        ExamPaperQuestionCustomerAnswer examPaperQuestionCustomerAnswer = examPaperQuestionCustomerAnswerService.selectById(id);
//        ExamPaperSubmitItemVM questionAnswerVM = examPaperQuestionCustomerAnswerService.examPaperQuestionCustomerAnswerToVM(examPaperQuestionCustomerAnswer);
//        QuestionEditRequestVM questionVM = questionService.getQuestionEditRequestVM(examPaperQuestionCustomerAnswer.getQuestionId());
//        vm.setQuestionVM(questionVM);
//        vm.setQuestionAnswerVM(questionAnswerVM);
//        return RestResponse.ok(vm);
//    }

@RequestMapping(value = "/select/{id}", method = RequestMethod.POST)
public RestResponse<QuestionAnswerVM> select(@PathVariable Integer id) throws IOException {
    QuestionAnswerVM vm = new QuestionAnswerVM();
    ExamPaperQuestionCustomerAnswer examPaperQuestionCustomerAnswer = examPaperQuestionCustomerAnswerService.selectById(id);
    ExamPaperSubmitItemVM questionAnswerVM = examPaperQuestionCustomerAnswerService.examPaperQuestionCustomerAnswerToVM(examPaperQuestionCustomerAnswer);
    QuestionEditRequestVM questionVM = questionService.getQuestionEditRequestVM(examPaperQuestionCustomerAnswer.getQuestionId());
    vm.setQuestionVM(questionVM);
    vm.setQuestionAnswerVM(questionAnswerVM);

    System.out.println(vm.getQuestionVM());
    System.out.println(vm.getQuestionAnswerVM());

    String originalTitle = vm.getQuestionVM().getTitle();
    System.out.println(originalTitle);

//    int start = originalTitle.indexOf("<p>") + "<p>".length();
//    int end = originalTitle.indexOf("</p >");
//    String realTitle = originalTitle.substring(start, end);


    int start = originalTitle.indexOf("<p>");
    int end = originalTitle.indexOf("</p>");

// 检查 <p> 和 </p> 是否都存在，确保索引有效
    String realTitle;
    if (start != -1 && end != -1 && start + "<p>".length() <= end) {
        start += "<p>".length();
        realTitle = originalTitle.substring(start, end);
        System.out.println(realTitle);
    } else {
        System.out.println("Invalid HTML tags in the title.");
        // 处理错误情况，例如返回默认值或抛出异常
        return RestResponse.fail(4, "无效的题目格式");
    }

    System.out.println(realTitle);

    System.out.println("=====================================");
    String prompt = String.format("题目是：%s", realTitle);
    String aiResponse = openAIService.getOpenAIResponse3(prompt);
    System.out.println(aiResponse);
    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode rootNode = objectMapper.readTree(aiResponse);

// 定位到 "content" 字段
    String content = rootNode.path("choices").get(0)
            .path("message").path("content").asText();
    System.out.println(content);
    vm.getQuestionVM().setAnalyze(content);

    return RestResponse.ok(vm);
}

}
