package com.mindskip.xzs.controller.student;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mindskip.xzs.base.BaseApiController;
import com.mindskip.xzs.base.RestResponse;
import com.mindskip.xzs.domain.*;
import com.mindskip.xzs.domain.enums.ExamPaperAnswerStatusEnum;
import com.mindskip.xzs.event.CalculateExamPaperAnswerCompleteEvent;
import com.mindskip.xzs.event.UserEvent;
import com.mindskip.xzs.service.ExamPaperAnswerService;
import com.mindskip.xzs.service.ExamPaperService;
import com.mindskip.xzs.service.OpenAIService;
import com.mindskip.xzs.service.SubjectService;
import com.mindskip.xzs.utility.DateTimeUtil;
import com.mindskip.xzs.utility.ExamUtil;
import com.mindskip.xzs.utility.PageInfoHelper;
import com.mindskip.xzs.viewmodel.admin.exam.ExamPaperEditRequestVM;
import com.mindskip.xzs.viewmodel.student.exam.ExamPaperReadVM;
import com.mindskip.xzs.viewmodel.student.exam.ExamPaperSubmitVM;
import com.mindskip.xzs.viewmodel.student.exampaper.ExamPaperAnswerPageResponseVM;
import com.mindskip.xzs.viewmodel.student.exampaper.ExamPaperAnswerPageVM;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.IOException;
import java.util.Date;

@RestController("StudentExamPaperAnswerController")
@RequestMapping(value = "/api/student/exampaper/answer")
public class ExamPaperAnswerController extends BaseApiController {

    private final ExamPaperAnswerService examPaperAnswerService;
    private final ExamPaperService examPaperService;
    private final SubjectService subjectService;
    private final ApplicationEventPublisher eventPublisher;
    @Autowired
    OpenAIService openAIService;

    @Autowired
    public ExamPaperAnswerController(ExamPaperAnswerService examPaperAnswerService, ExamPaperService examPaperService, SubjectService subjectService, ApplicationEventPublisher eventPublisher) {
        this.examPaperAnswerService = examPaperAnswerService;
        this.examPaperService = examPaperService;
        this.subjectService = subjectService;
        this.eventPublisher = eventPublisher;
    }


    @RequestMapping(value = "/pageList", method = RequestMethod.POST)
    public RestResponse<PageInfo<ExamPaperAnswerPageResponseVM>> pageList(@RequestBody @Valid ExamPaperAnswerPageVM model) {
        model.setCreateUser(getCurrentUser().getId());
        PageInfo<ExamPaperAnswer> pageInfo = examPaperAnswerService.studentPage(model);
        PageInfo<ExamPaperAnswerPageResponseVM> page = PageInfoHelper.copyMap(pageInfo, e -> {
            ExamPaperAnswerPageResponseVM vm = modelMapper.map(e, ExamPaperAnswerPageResponseVM.class);
            Subject subject = subjectService.selectById(vm.getSubjectId());
            vm.setDoTime(ExamUtil.secondToVM(e.getDoTime()));
            vm.setSystemScore(ExamUtil.scoreToVM(e.getSystemScore()));
            vm.setUserScore(ExamUtil.scoreToVM(e.getUserScore()));
            vm.setPaperScore(ExamUtil.scoreToVM(e.getPaperScore()));
            vm.setSubjectName(subject.getName());
            vm.setCreateTime(DateTimeUtil.dateFormat(e.getCreateTime()));
            return vm;
        });
        return RestResponse.ok(page);
    }


    @RequestMapping(value = "/answerSubmit", method = RequestMethod.POST)
    public RestResponse answerSubmit(@RequestBody @Valid ExamPaperSubmitVM examPaperSubmitVM) {
        User user = getCurrentUser();
        ExamPaperAnswerInfo examPaperAnswerInfo = examPaperAnswerService.calculateExamPaperAnswer(examPaperSubmitVM, user);
        if (null == examPaperAnswerInfo) {
            return RestResponse.fail(2, "试卷不能重复做");
        }
        ExamPaperAnswer examPaperAnswer = examPaperAnswerInfo.getExamPaperAnswer();
        Integer userScore = examPaperAnswer.getUserScore();
        String scoreVm = ExamUtil.scoreToVM(userScore);
        UserEventLog userEventLog = new UserEventLog(user.getId(), user.getUserName(), user.getRealName(), new Date());
        String content = user.getUserName() + " 提交试卷：" + examPaperAnswerInfo.getExamPaper().getName()
                + " 得分：" + scoreVm
                + " 耗时：" + ExamUtil.secondToVM(examPaperAnswer.getDoTime());
        userEventLog.setContent(content);
        eventPublisher.publishEvent(new CalculateExamPaperAnswerCompleteEvent(examPaperAnswerInfo));
        eventPublisher.publishEvent(new UserEvent(userEventLog));
        return RestResponse.ok(scoreVm);
    }


    @RequestMapping(value = "/edit", method = RequestMethod.POST)
    public RestResponse edit(@RequestBody @Valid ExamPaperSubmitVM examPaperSubmitVM) {
        boolean notJudge = examPaperSubmitVM.getAnswerItems().stream().anyMatch(i -> i.getDoRight() == null && i.getScore() == null);
        if (notJudge) {
            return RestResponse.fail(2, "有未批改题目");
        }

        ExamPaperAnswer examPaperAnswer = examPaperAnswerService.selectById(examPaperSubmitVM.getId());
        ExamPaperAnswerStatusEnum examPaperAnswerStatusEnum = ExamPaperAnswerStatusEnum.fromCode(examPaperAnswer.getStatus());
        if (examPaperAnswerStatusEnum == ExamPaperAnswerStatusEnum.Complete) {
            return RestResponse.fail(3, "试卷已完成");
        }
        String score = examPaperAnswerService.judge(examPaperSubmitVM);
        User user = getCurrentUser();
        UserEventLog userEventLog = new UserEventLog(user.getId(), user.getUserName(), user.getRealName(), new Date());
        String content = user.getUserName() + " 批改试卷：" + examPaperAnswer.getPaperName() + " 得分：" + score;
        userEventLog.setContent(content);
        eventPublisher.publishEvent(new UserEvent(userEventLog));
        return RestResponse.ok(score);
    }

    @RequestMapping(value = "/read/{id}", method = RequestMethod.POST)
    public RestResponse<ExamPaperReadVM> read(@PathVariable Integer id) throws IOException {
        ExamPaperAnswer examPaperAnswer = examPaperAnswerService.selectById(id);
        ExamPaperReadVM vm = new ExamPaperReadVM();
        ExamPaperEditRequestVM paper = examPaperService.examPaperToVM(examPaperAnswer.getExamPaperId());
        ExamPaperSubmitVM answer = examPaperAnswerService.examPaperAnswerToVM(examPaperAnswer.getId());
        vm.setPaper(paper);
        vm.setAnswer(answer);
        System.out.println(vm.getAnswer());//不用了
        System.out.println(vm.getPaper());//{id=3, level=1, subjectId=3, paperType=1, name='english test', suggestTime=2,
        // limitDateTime=null, titleItems=[ExamPaperTitleItemVM{name='test',
        // questionItems=[QuestionEditRequestVM{id=3, questionType=5, subjectId=3,
        // title='<p>苹果的英文？</p>', gradeLevel=1, items=[], analyze='apple', correctArray=null, correct='apple<br/>', score='1', difficult=1, itemOrder=1}]}], score='1'}
        String title = vm.getPaper().getTitleItems().get(0).getQuestionItems().get(0).getTitle();
        System.out.println(title);

//        int start = title.indexOf("<p>") + "<p>".length();
//        int end = title.indexOf("</p>");
//        String realTitle = title.substring(start, end);

        int start = title.indexOf("<p>");
        int end = title.indexOf("</p>");

// 检查 <p> 和 </p> 是否都存在，确保索引有效
        String realTitle;
        if (start != -1 && end != -1 && start + "<p>".length() <= end) {
            start += "<p>".length();
            realTitle = title.substring(start, end);
            System.out.println(realTitle);
        } else {
            System.out.println("Invalid HTML tags in the title.");
            // 处理错误情况，例如返回默认值或抛出异常
            return RestResponse.fail(4, "无效的题目格式");
        }
        System.out.println("=====================================");
        System.out.println(realTitle);
        //String realAnswer = vm.getPaper().getTitleItems().get(0).getQuestionItems().get(0).getAnalyze();
        String realAnswer = vm.getAnswer().getAnswerItems().get(0).getContent();
        System.out.println(realAnswer);
        //realTitle和realAnswer就是题目和回答





        System.out.println("=====================================");
        String prompt = String.format("题目是：%s 答案是：%s 请给出这个答案的合理性分析。", realTitle, realAnswer);
        String aiResponse = openAIService.getOpenAIResponse2(prompt);
//        System.out.println(aiResponse);//aiResponse是分数就是一个数字
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(aiResponse);
        String mark = rootNode.path("choices").get(0)
                .path("message").path("content").asText();
        System.out.println(mark);

        System.out.println("===============mark================");

        //获取分析
        String prompt1 = String.format("题目是：%s 答案是：%s 请给出这个答案的合理性分析。", realTitle, realAnswer);
        String aiResponse2 = openAIService.getOpenAIResponse(prompt1);
        ObjectMapper objectMapper2 = new ObjectMapper();
        JsonNode rootNode2 = objectMapper2.readTree(aiResponse2);
        String analyse = rootNode2.path("choices").get(0)
                .path("message").path("content").asText();
        System.out.println(analyse);

        System.out.println("===========analyse=================");
        // 定位到 "mark" 字段

        String fakeName = vm.getPaper().getName() +"?"+ mark+"?"+analyse;
        vm.getPaper().setName(fakeName);
        System.out.println(vm.getPaper().getName());
        return RestResponse.ok(vm);
    }


}
