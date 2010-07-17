/*
 * Copyright (c) 2005-2010 Grameen Foundation USA
 *  All rights reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 *  implied. See the License for the specific language governing
 *  permissions and limitations under the License.
 *
 *  See also http://www.apache.org/licenses/LICENSE-2.0.html for an
 *  explanation of the license and how it is applied.
 */

package org.mifos.platform.questionnaire.validators;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mifos.framework.exceptions.SystemException;
import org.mifos.platform.questionnaire.persistence.EventSourceDao;
import org.mifos.platform.questionnaire.service.*;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mifos.platform.questionnaire.QuestionnaireConstants.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;   //NOPMD

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("PMD")
public class QuestionValidatorTest {

    private QuestionnaireValidator questionnaireValidator;

    @Mock
    private EventSourceDao eventSourceDao;

    @Before
    public void setUp() {
        questionnaireValidator = new QuestionnaireValidatorImpl(eventSourceDao);
    }

    @Test
    public void shouldNotThrowExceptionWhenQuestionTitleIsProvided() {
        try {
            questionnaireValidator.validateForDefineQuestion(new QuestionDetail("Title", QuestionType.FREETEXT));
        } catch (SystemException e) {
            fail("Should not have thrown the exception");
        }
    }

    @Test
    public void shouldThrowExceptionWhenQuestionTitleIsProvided() {
        try {
            questionnaireValidator.validateForDefineQuestion(new QuestionDetail(null, QuestionType.FREETEXT));
            fail("Should have thrown the application exception");
        } catch (SystemException e) {
            assertEquals(QUESTION_TITLE_NOT_PROVIDED, e.getKey());
        }
    }

    @Test
    public void shouldThrowExceptionWhenQuestionTypeNotProvided() {
        try {
            questionnaireValidator.validateForDefineQuestion(new QuestionDetail("Title 123", QuestionType.INVALID));
            fail("Should have thrown the application exception");
        } catch (SystemException e) {
            assertEquals(QUESTION_TYPE_NOT_PROVIDED, e.getKey());
        }
    }

    @Test
    public void shouldNotThrowExceptionWhenQuestionGroupTitleIsProvided() {
        when(eventSourceDao.retrieveCountByEventAndSource(anyString(), anyString())).thenReturn(asList((long) 1));
        try {
            questionnaireValidator.validateForDefineQuestionGroup(getQuestionGroupDetail(0, "Title", "Create", "Client"));
        } catch (SystemException e) {
            fail("shouldNotThrowExceptionWhenQuestionGroupTitleIsProvided:Should not have thrown the exception");
        }
        verify(eventSourceDao, times(1)).retrieveCountByEventAndSource(anyString(), anyString());
    }

    @Test
    public void shouldThrowExceptionWhenQuestionGroupTitleIsProvided() {
        try {
            questionnaireValidator.validateForDefineQuestionGroup(getQuestionGroupDetail(0, null, "Create", "Client"));
            fail("Should have thrown the application exception");
        } catch (SystemException e) {
            assertEquals(QUESTION_GROUP_TITLE_NOT_PROVIDED, e.getKey());
        }
    }

    @Test
    public void shouldNotThrowExceptionWhenQuestionGroupHasAtLeastOneSection() {
        when(eventSourceDao.retrieveCountByEventAndSource(anyString(), anyString())).thenReturn(asList((long) 1));
        try {
            questionnaireValidator.validateForDefineQuestionGroup(getQuestionGroupDetail(0, "Title", "Create", "Client"));
        } catch (SystemException e) {
            fail("shouldNotThrowExceptionWhenQuestionGroupHasAtLeastOneSection:Should not have thrown the exception");
        }
        verify(eventSourceDao, times(1)).retrieveCountByEventAndSource(anyString(), anyString());
    }

    @Test
    public void shouldThrowExceptionWhenQuestionGroupHasNoSections() {
        try {
            questionnaireValidator.validateForDefineQuestionGroup(getQuestionGroupDetail(0, "Title", "Create", "Client", new ArrayList<SectionDetail>()));
            fail("Should have thrown the application exception");
        } catch (SystemException e) {
            assertEquals(QUESTION_GROUP_SECTION_NOT_PROVIDED, e.getKey());
        }
    }

    @Test
    public void shouldThrowExceptionWhenEventIsNotProvided() {
        try {
            questionnaireValidator.validateForDefineQuestionGroup(getQuestionGroupDetail(0, "Title", null, "Client"));
            fail("Should have thrown the application exception");
        } catch (SystemException e) {
            assertEquals(INVALID_EVENT_SOURCE, e.getKey());
        }
        verify(eventSourceDao, never()).retrieveCountByEventAndSource(anyString(), anyString());
    }

    @Test
    public void shouldThrowExceptionWhenSourceIsNotProvided() {
        try {
            questionnaireValidator.validateForDefineQuestionGroup(getQuestionGroupDetail(0, "Title", "Create", null));
            fail("Should have thrown the application exception");
        } catch (SystemException e) {
            assertEquals(INVALID_EVENT_SOURCE, e.getKey());
        }
        verify(eventSourceDao, never()).retrieveCountByEventAndSource(anyString(), anyString());
    }

    @Test
    public void shouldThrowExceptionWhenEventSourceIsNotProvided() {
        try {
            questionnaireValidator.validateForDefineQuestionGroup(new QuestionGroupDetail(0, "Title", null, asList(getSection("S1"))));
            fail("Should have thrown the application exception");
        } catch (SystemException e) {
            assertEquals(INVALID_EVENT_SOURCE, e.getKey());
        }
        verify(eventSourceDao, never()).retrieveCountByEventAndSource(anyString(), anyString());
    }

    @Test
    public void shouldThrowExceptionWhenAGivenSectionHasNoQuestions() {
        try {
            questionnaireValidator.validateForDefineQuestionGroup(getQuestionGroupDetail(0, "Title", "Create", "Client", asList(getSectionWithQuestions("S1"))));
            fail("Should have thrown the application exception");
        } catch (SystemException e) {
            assertEquals(NO_QUESTIONS_FOUND_IN_SECTION, e.getKey());
        }
        verify(eventSourceDao, never()).retrieveCountByEventAndSource(anyString(), anyString());
    }

    @Test
    public void shouldThrowExceptionWhenAGivenQuestionAppearsInMoreThanOneSection() {
        try {
            SectionDetail sectionDefinition1 = getSectionWithQuestions("S1", 1, 3);
            SectionDetail sectionDefinition2 = getSectionWithQuestions("S2", 3, 2);
            List<SectionDetail> sectionDetails = asList(sectionDefinition1, sectionDefinition2);
            questionnaireValidator.validateForDefineQuestionGroup(getQuestionGroupDetail(0, "Title", "Create", "Client", sectionDetails));
            fail("Should have thrown the application exception");
        } catch (SystemException e) {
            assertEquals(DUPLICATE_QUESTION_FOUND_IN_SECTION, e.getKey());
        }
        verify(eventSourceDao, never()).retrieveCountByEventAndSource(anyString(), anyString());
    }

    private QuestionGroupDetail getQuestionGroupDetail(int id, String title, String event, String source) {
        return getQuestionGroupDetail(id, title, event, source, asList(getSection("S1")));
    }

    private QuestionGroupDetail getQuestionGroupDetail(int id, String title, String event, String source, List<SectionDetail> sectionDetails) {
        return new QuestionGroupDetail(id, title, getEventSource(event, source), sectionDetails);
    }

    private SectionDetail getSection(String name) {
        SectionDetail section = new SectionDetail();
        section.setName(name);
        section.addQuestion(new SectionQuestionDetail(new QuestionDetail(12, null, null, QuestionType.INVALID), true));
        return section;
    }

    private SectionDetail getSectionWithQuestions(String name, int... questionIds) {
        SectionDetail section = new SectionDetail();
        section.setName(name);
        if (questionIds != null) {
            for (int questionId : questionIds) {
                section.addQuestion(new SectionQuestionDetail(new QuestionDetail(questionId, null, null, QuestionType.INVALID), true));
            }
        }
        return section;
    }

    private EventSource getEventSource(String event, String source) {
        return new EventSource(event, source, null);
    }

}