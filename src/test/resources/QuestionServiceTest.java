package cn.jiguang.community.base.data.service;

import cn.jiguang.community.base.data.Application;
import cn.jiguang.community.base.data.model.QuestionEntity;
import cn.jiguang.community.base.data.model.SearchForm;
import lombok.extern.slf4j.Slf4j;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

/**
 * QuestionService Tester.
 *
 * @author <Authors name>
 * @version 1.0
 * @since <pre>Oct 31, 2019</pre>
 */

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
public class QuestionServiceTest {

    @Autowired
    private QuestionService questionService;

    @Before
    public void before() throws Exception {
    }

    @After
    public void after() throws Exception {
    }

    /**
     * Method: createQuestion(QuestionEntity entity)
     */
    @Test
    public void testCreateQuestion() throws Exception {

        QuestionEntity questionEntity = new QuestionEntity();
        questionEntity.setUserId(234L);
        questionEntity.setQuestionTitle("xdx");
        questionEntity.setQuestionContent("");
        questionService.createQuestion(questionEntity);
    }

    /**
     * Method: getQuestion(Long id)
     */
    @Test
    public void testGetQuestion() throws Exception {

    }

    @Test
    public void testQuery() throws Exception {
        SearchForm searchForm = new SearchForm();
        searchForm.setRecommend(true);
        searchForm.setPageIndex(1);
        searchForm.setPageSize(10);

        List<QuestionEntity> result = questionService.query(searchForm);
        log.info("result:{}", result);
    }

    @Test
    public void testCount() {

        SearchForm searchForm = new SearchForm();
        searchForm.setPageIndex(1);
        searchForm.setPageSize(10);
        searchForm.setRecommend(true);
        searchForm.setSearchKeyword("问题");
        int cnt = questionService.count(searchForm);
        List<QuestionEntity> rows = questionService.query(searchForm);
        log.info("{}, {}", cnt, rows);
    }

    @Test
    public void testListRecommendOrAcceptQuestionId() {
        List<Long> result = questionService.listRecommendOrAcceptQuestionId();
        log.info("result:{}", result);
    }
} 
