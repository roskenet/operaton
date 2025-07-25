/*
 * Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
 * under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright
 * ownership. Camunda licenses this file to you under the Apache License,
 * Version 2.0; you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.operaton.bpm.engine.test.assertions.cmmn;

import static org.operaton.bpm.engine.test.assertions.bpmn.BpmnAwareTests.complete;
import static org.operaton.bpm.engine.test.assertions.bpmn.BpmnAwareTests.processInstanceQuery;
import static org.operaton.bpm.engine.test.assertions.bpmn.BpmnAwareTests.task;
import static org.operaton.bpm.engine.test.assertions.cmmn.CmmnAwareTests.assertThat;
import static org.operaton.bpm.engine.test.assertions.cmmn.CmmnAwareTests.caseExecution;
import static org.operaton.bpm.engine.test.assertions.cmmn.CmmnAwareTests.caseInstanceQuery;
import static org.operaton.bpm.engine.test.assertions.cmmn.CmmnAwareTests.caseService;
import static org.operaton.bpm.engine.test.assertions.cmmn.CmmnAwareTests.complete;
import static org.operaton.bpm.engine.test.assertions.cmmn.CmmnAwareTests.disable;
import static org.operaton.bpm.engine.test.assertions.cmmn.CmmnAwareTests.manuallyStart;
import org.junit.jupiter.api.Test;
import org.operaton.bpm.engine.runtime.CaseExecution;
import org.operaton.bpm.engine.runtime.CaseInstance;
import org.operaton.bpm.engine.runtime.ProcessInstance;
import org.operaton.bpm.engine.test.Deployment;
import org.operaton.bpm.engine.test.assertions.helpers.ProcessAssertTestCase;

public class StageTest extends ProcessAssertTestCase {

  public static final String TASK_A    = "PI_HT_A";
  public static final String TASK_B    = "PI_CT_B";
  public static final String TASK_C    = "PI_PT_C";
  public static final String STAGE_S   = "PI_S_S";
  public static final String STAGE_S2  = "PI_S_S2";
  public static final String STAGE_S3  = "PI_S_S3";
  public static final String USER_TASK = "UserTask_1";

  @Test
  @Deployment(resources = {"cmmn/StageTest.cmmn"})
  void caseIsActiveAndStageIsEnabled() {
    // Given
    // case model is deployed
    // When
    CaseInstance caseInstance = givenCaseIsCreated();
    // Then
    assertThat(caseInstance).isActive().stage(STAGE_S).isEnabled();
  }

  @Test
  @Deployment(resources = {"cmmn/StageTest.cmmn"})
  void stageIsActiveAndTaskIsEnabled() {
    // Given
    CaseInstance caseInstance = givenCaseIsCreated();
    // When
    manuallyStart(caseExecution(STAGE_S, caseInstance));
    // Then
    assertThat(caseInstance).isActive().stage(STAGE_S).isActive().humanTask(TASK_A).isEnabled();
  }

  @Test
  @Deployment(resources = {"cmmn/StageTest.cmmn"})
  void stageAndTaskAreActive() {
    // Given
    CaseInstance caseInstance = givenCaseIsCreatedAndStageSActive();
    // When
    manuallyStart(caseExecution(TASK_A, caseInstance));
    // Then
    assertThat(caseInstance).isActive().stage(STAGE_S).isActive().humanTask(TASK_A).isActive();
  }

  @Test
  @Deployment(resources = {"cmmn/StageTest.cmmn", "cmmn/ProcessTaskAssert-calledProcess.bpmn"})
  void caseIsCompletedWhenTasksAreCompleted() {
    // Given
    CaseInstance caseInstance = givenCaseIsCreatedAndStageSActiveAndTaskAActive();
    StageAssert stage = assertThat(caseInstance).stage(STAGE_S);
    // When
    complete(caseExecution(TASK_A, caseInstance));
    stage.caseTask(TASK_B).isNotNull().isEnabled();
    manuallyStart(caseExecution(TASK_B, caseInstance));// case task is non-blocking, completes after activation
    // Then
    assertThat(caseInstance).isCompleted();
    stage.isCompleted();
    // And
    CaseInstance caseInstanceCalled = caseInstanceQuery().active().singleResult();
    StageAssert stageCalled = assertThat(caseInstanceCalled).isNotNull().isActive()
        .stage(STAGE_S2).isActive().stage(STAGE_S3).isActive();
    manuallyStart(caseExecution(TASK_C, caseInstanceCalled));
    ProcessTaskAssert processTask = stageCalled.isActive().processTask(TASK_C).isActive();
    complete(task(USER_TASK, calledProcessInstance(caseInstanceCalled)));
    processTask.isCompleted();
    stageCalled.isCompleted();
    assertThat(caseInstanceCalled).isCompleted();
  }

  @Test
  @Deployment(resources = {"cmmn/StageTest.cmmn"})
  void stageAndTaskAreDisabled() {
    // Given
    CaseInstance caseInstance = givenCaseIsCreated();
    StageAssert stage = assertThat(caseInstance).stage(STAGE_S);
    // When
    CaseExecution stageExecution = caseExecution(STAGE_S, caseInstance);
    disable(stageExecution);
    // Then
    assertThat(caseInstance).isCompleted();
    assertThat(stageExecution).isDisabled();
    stage.isDisabled();
  }

  @Test
  @Deployment(resources = {"cmmn/StageTest.cmmn"})
  void stageIsTerminated() {
    // Given
    CaseInstance caseInstance = givenCaseIsCreatedAndStageSActive();
    StageAssert stage = assertThat(caseInstance).stage(STAGE_S);
    // When
    CaseExecution stageExecution = caseExecution(STAGE_S, caseInstance);
    caseService().terminateCaseExecution(caseExecution(STAGE_S, caseInstance).getId());
    // Then
    assertThat(stageExecution).isTerminated();
    stage.isTerminated();
  }

  private CaseInstance givenCaseIsCreated() {
    return caseService().createCaseInstanceByKey("Case_StageTests");
  }

  private CaseInstance givenCaseIsCreatedAndStageSActive() {
    CaseInstance caseInstance = givenCaseIsCreated();
    manuallyStart(caseExecution(STAGE_S, caseInstance));
    return caseInstance;
  }

  private CaseInstance givenCaseIsCreatedAndStageSActiveAndTaskAActive() {
    CaseInstance caseInstance = givenCaseIsCreatedAndStageSActive();
    manuallyStart(caseExecution(TASK_A, caseInstance));
    return caseInstance;
  }

  private ProcessInstance calledProcessInstance(CaseInstance caseInstance) {
    return processInstanceQuery().superCaseInstanceId(caseInstance.getId()).singleResult();
  }

}
