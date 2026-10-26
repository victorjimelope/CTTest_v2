package com.cttest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.cttest.model.entity.Answer;
import com.cttest.model.entity.Center;
import com.cttest.model.entity.Question;
import com.cttest.model.entity.Student;
import com.cttest.model.entity.StudentAnswer;
import com.cttest.model.entity.StudentGroup;
import com.cttest.model.entity.StudentTest;
import com.cttest.model.entity.TestInstance;
import com.cttest.model.entity.User;
import com.cttest.model.enums.AnswersLayoutType;
import com.cttest.model.enums.GenderType;
import com.cttest.model.enums.StudentTestStatus;
import com.cttest.model.enums.TestInstanceStatus;
import com.cttest.model.enums.TestStatus;
import com.cttest.model.enums.TestType;
import com.cttest.repository.CenterRepository;
import com.cttest.repository.QuestionRepository;
import com.cttest.repository.StudentAnswerRepository;
import com.cttest.repository.StudentGroupRepository;
import com.cttest.repository.StudentRepository;
import com.cttest.repository.StudentTestRepository;
import com.cttest.repository.TestInstanceRepository;
import com.cttest.repository.TestRepository;
import com.cttest.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

class TestFlowTest extends BaseTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CenterRepository centerRepository;

    @Autowired
    private StudentGroupRepository studentGroupRepository;

    @Autowired
    private StudentRepository studentRepository;

    @Autowired
    private TestRepository testRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private TestInstanceRepository testInstanceRepository;

    @Autowired
    private StudentTestRepository studentTestRepository;

    @Autowired
    private StudentAnswerRepository studentAnswerRepository;

    private Student student;
    private TestInstance testInstance;
    private StudentTest studentTest;
    private com.cttest.model.entity.Test test;
    private Question question1;
    private Question question2;
    private Answer correctAnswer1;
    private Answer wrongAnswer1;
    private Answer correctAnswer2;
    private Answer wrongAnswer2;
    private int pin;

    @BeforeEach
    void setUp() {
        studentAnswerRepository.deleteAll();
        studentTestRepository.deleteAll();
        testInstanceRepository.deleteAll();
        questionRepository.deleteAll();
        testRepository.deleteAll();
        studentRepository.deleteAll();
        studentGroupRepository.deleteAll();
        userRepository.deleteAll();
        centerRepository.deleteAll();

        Center center = new Center();
        center.setName("Test Center");
        center = centerRepository.save(center);

        StudentGroup group = new StudentGroup();
        group.setCenter(center);
        group.setName("Test Group");
        group = studentGroupRepository.save(group);

        student = new Student();
        student.setStudentGroup(group);
        student.setName("Test Student");
        student.setGender(GenderType.UNKNOWN);
        student = studentRepository.save(student);

        User admin = new User();
        admin.setUsername("flow-admin");
        admin.setPassword("encoded");
        admin.setAdmin(true);
        admin.setCenter(center);
        admin = userRepository.save(admin);

        test = new com.cttest.model.entity.Test();
        test.setCenterId(center.getId());
        test.setName("Flow Test");
        test.setType(TestType.PUBLIC);
        test.setStatus(TestStatus.ACTIVE);
        test = testRepository.save(test);

        question1 = new Question();
        question1.setTest(test);
        question1.setOrder(1);
        question1.setQuestion("Question 1");
        question1.setAnswersLayout(AnswersLayoutType.ROWS);
        question1 = questionRepository.save(question1);

        correctAnswer1 = new Answer();
        correctAnswer1.setQuestion(question1);
        correctAnswer1.setOrder(1);
        correctAnswer1.setCorrect(true);
        question1.getAnswers().add(correctAnswer1);

        wrongAnswer1 = new Answer();
        wrongAnswer1.setQuestion(question1);
        wrongAnswer1.setOrder(2);
        wrongAnswer1.setCorrect(false);
        question1.getAnswers().add(wrongAnswer1);

        question1 = questionRepository.save(question1);
        correctAnswer1 = question1.getAnswers().get(0);
        wrongAnswer1 = question1.getAnswers().get(1);

        question2 = new Question();
        question2.setTest(test);
        question2.setOrder(2);
        question2.setQuestion("Question 2");
        question2.setAnswersLayout(AnswersLayoutType.ROWS);
        question2 = questionRepository.save(question2);

        correctAnswer2 = new Answer();
        correctAnswer2.setQuestion(question2);
        correctAnswer2.setOrder(1);
        correctAnswer2.setCorrect(true);
        question2.getAnswers().add(correctAnswer2);

        wrongAnswer2 = new Answer();
        wrongAnswer2.setQuestion(question2);
        wrongAnswer2.setOrder(2);
        wrongAnswer2.setCorrect(false);
        question2.getAnswers().add(wrongAnswer2);

        question2 = questionRepository.save(question2);
        correctAnswer2 = question2.getAnswers().get(0);
        wrongAnswer2 = question2.getAnswers().get(1);

        pin = 555555;
        testInstance = new TestInstance();
        testInstance.setTestId(test.getId());
        testInstance.setStudentGroup(group);
        testInstance.setStatus(TestInstanceStatus.IN_PROGRESS);
        testInstance.setAssignedBy(admin);
        testInstance.setPin(pin);
        testInstance = testInstanceRepository.save(testInstance);

        studentTest = new StudentTest();
        studentTest.setTestInstanceId(testInstance.getId());
        studentTest.setStudent(student);
        studentTest.setStatus(StudentTestStatus.ASSIGNED);
        studentTest = studentTestRepository.save(studentTest);
    }

    @Test
    void validatePin_WithValidPin_ShouldReturnStudentTestId() throws Exception {
        mockMvc.perform(post("/api/public/validate-pin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"pin\": " + pin + ", \"studentId\": " + student.getId() + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studentTestId").value(studentTest.getId()));
    }

    @Test
    void validatePin_WithInvalidPin_ShouldReturn400() throws Exception {
        mockMvc.perform(post("/api/public/validate-pin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"pin\": 999999, \"studentId\": " + student.getId() + "}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid PIN"));
    }

    @Test
    void validatePin_WithWrongStudent_ShouldReturn400() throws Exception {
        mockMvc.perform(post("/api/public/validate-pin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"pin\": " + pin + ", \"studentId\": 99999}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Student not assigned to this test"));
    }

    @Test
    void getTestData_ShouldReturnQuestionsAndAnswers() throws Exception {
        mockMvc.perform(get("/api/public/student-tests/" + studentTest.getId() + "/test-data"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.studentTestId").value(studentTest.getId()))
                .andExpect(jsonPath("$.testData.questions.length()").value(2))
                .andExpect(jsonPath("$.status").value("ASSIGNED"));
    }

    @Test
    void saveAnswer_ShouldCreateAnswer() throws Exception {
        mockMvc.perform(post("/api/public/student-tests/" + studentTest.getId() + "/answer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"questionId\": " + question1.getId() + ", \"answerId\": " + correctAnswer1.getId() + "}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/public/student-tests/" + studentTest.getId() + "/test-data"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.answers." + question1.getId()).value(correctAnswer1.getId()))
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    void saveAnswer_ShouldUpdateExistingAnswer() throws Exception {
        mockMvc.perform(post("/api/public/student-tests/" + studentTest.getId() + "/answer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"questionId\": " + question1.getId() + ", \"answerId\": " + wrongAnswer1.getId() + "}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/public/student-tests/" + studentTest.getId() + "/answer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"questionId\": " + question1.getId() + ", \"answerId\": " + correctAnswer1.getId() + "}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/public/student-tests/" + studentTest.getId() + "/test-data"))
                .andExpect(jsonPath("$.answers." + question1.getId()).value(correctAnswer1.getId()));
    }

    @Test
    void finishTest_WithAllCorrect_ShouldReturnFullScore() throws Exception {
        mockMvc.perform(post("/api/public/student-tests/" + studentTest.getId() + "/answer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"questionId\": " + question1.getId() + ", \"answerId\": " + correctAnswer1.getId() + "}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/public/student-tests/" + studentTest.getId() + "/answer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"questionId\": " + question2.getId() + ", \"answerId\": " + correctAnswer2.getId() + "}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/public/student-tests/" + studentTest.getId() + "/finish"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correctCount").value(2))
                .andExpect(jsonPath("$.totalQuestions").value(2))
                .andExpect(jsonPath("$.score").value(100));
    }

    @Test
    void finishTest_WithPartialCorrect_ShouldReturnPartialScore() throws Exception {
        mockMvc.perform(post("/api/public/student-tests/" + studentTest.getId() + "/answer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"questionId\": " + question1.getId() + ", \"answerId\": " + correctAnswer1.getId() + "}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/public/student-tests/" + studentTest.getId() + "/answer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"questionId\": " + question2.getId() + ", \"answerId\": " + wrongAnswer2.getId() + "}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/public/student-tests/" + studentTest.getId() + "/finish"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correctCount").value(1))
                .andExpect(jsonPath("$.totalQuestions").value(2))
                .andExpect(jsonPath("$.score").value(50));
    }

    @Test
    void finishTest_WhenAlreadyFinished_ShouldReturn400() throws Exception {
        mockMvc.perform(post("/api/public/student-tests/" + studentTest.getId() + "/finish"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/public/student-tests/" + studentTest.getId() + "/finish"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Test already finished"));
    }

    @Test
    void saveAnswer_WhenTestFinished_ShouldReturn400() throws Exception {
        mockMvc.perform(post("/api/public/student-tests/" + studentTest.getId() + "/finish"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/public/student-tests/" + studentTest.getId() + "/answer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"questionId\": " + question1.getId() + ", \"answerId\": " + correctAnswer1.getId() + "}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getResult_ShouldReturnScore() throws Exception {
        mockMvc.perform(post("/api/public/student-tests/" + studentTest.getId() + "/answer")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"questionId\": " + question1.getId() + ", \"answerId\": " + correctAnswer1.getId() + "}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/public/student-tests/" + studentTest.getId() + "/finish"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/public/student-tests/" + studentTest.getId() + "/result"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correctCount").value(1))
                .andExpect(jsonPath("$.totalQuestions").value(2))
                .andExpect(jsonPath("$.studentTest.status").value("FINISHED"));
    }

    @Test
    void validatePin_WhenClosedInstance_ShouldReturn400() throws Exception {
        testInstance.setStatus(TestInstanceStatus.CLOSED);
        testInstanceRepository.save(testInstance);

        mockMvc.perform(post("/api/public/validate-pin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"pin\": " + pin + ", \"studentId\": " + student.getId() + "}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("This test instance is closed"));
    }
}
