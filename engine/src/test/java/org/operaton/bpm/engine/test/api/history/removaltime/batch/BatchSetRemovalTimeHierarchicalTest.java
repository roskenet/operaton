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
package org.operaton.bpm.engine.test.api.history.removaltime.batch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.operaton.bpm.engine.ProcessEngineConfiguration.HISTORY_FULL;
import static org.operaton.bpm.engine.test.api.history.removaltime.batch.helper.BatchSetRemovalTimeRule.addDays;

import java.io.ByteArrayInputStream;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.operaton.bpm.engine.AuthorizationService;
import org.operaton.bpm.engine.DecisionService;
import org.operaton.bpm.engine.ExternalTaskService;
import org.operaton.bpm.engine.HistoryService;
import org.operaton.bpm.engine.IdentityService;
import org.operaton.bpm.engine.ManagementService;
import org.operaton.bpm.engine.RuntimeService;
import org.operaton.bpm.engine.TaskService;
import org.operaton.bpm.engine.authorization.Authorization;
import org.operaton.bpm.engine.authorization.AuthorizationQuery;
import org.operaton.bpm.engine.authorization.Resources;
import org.operaton.bpm.engine.history.HistoricActivityInstance;
import org.operaton.bpm.engine.history.HistoricDecisionInputInstance;
import org.operaton.bpm.engine.history.HistoricDecisionInstance;
import org.operaton.bpm.engine.history.HistoricDecisionInstanceQuery;
import org.operaton.bpm.engine.history.HistoricDecisionOutputInstance;
import org.operaton.bpm.engine.history.HistoricDetail;
import org.operaton.bpm.engine.history.HistoricExternalTaskLog;
import org.operaton.bpm.engine.history.HistoricIdentityLinkLog;
import org.operaton.bpm.engine.history.HistoricIncident;
import org.operaton.bpm.engine.history.HistoricJobLog;
import org.operaton.bpm.engine.history.HistoricProcessInstance;
import org.operaton.bpm.engine.history.HistoricProcessInstanceQuery;
import org.operaton.bpm.engine.history.HistoricTaskInstance;
import org.operaton.bpm.engine.history.HistoricVariableInstance;
import org.operaton.bpm.engine.history.UserOperationLogEntry;
import org.operaton.bpm.engine.impl.history.event.HistoricDecisionInputInstanceEntity;
import org.operaton.bpm.engine.impl.history.event.HistoricDecisionOutputInstanceEntity;
import org.operaton.bpm.engine.impl.history.event.HistoricExternalTaskLogEntity;
import org.operaton.bpm.engine.impl.persistence.entity.AttachmentEntity;
import org.operaton.bpm.engine.impl.persistence.entity.ByteArrayEntity;
import org.operaton.bpm.engine.impl.persistence.entity.HistoricJobLogEventEntity;
import org.operaton.bpm.engine.impl.persistence.entity.HistoricVariableInstanceEntity;
import org.operaton.bpm.engine.task.Attachment;
import org.operaton.bpm.engine.task.Comment;
import org.operaton.bpm.engine.test.Deployment;
import org.operaton.bpm.engine.test.RequiredHistoryLevel;
import org.operaton.bpm.engine.test.api.history.removaltime.batch.helper.BatchSetRemovalTimeExtension;
import org.operaton.bpm.engine.test.dmn.businessruletask.TestPojo;
import org.operaton.bpm.engine.test.junit5.ProcessEngineExtension;
import org.operaton.bpm.engine.test.junit5.ProcessEngineTestExtension;
import org.operaton.bpm.engine.variable.Variables;

/**
 * @author Tassilo Weidner
 */
@RequiredHistoryLevel(HISTORY_FULL)
class BatchSetRemovalTimeHierarchicalTest {

  @RegisterExtension
  static ProcessEngineExtension engineRule = ProcessEngineExtension.builder().build();
  @RegisterExtension
  ProcessEngineTestExtension engineTestRule = new ProcessEngineTestExtension(engineRule);
  @RegisterExtension
  BatchSetRemovalTimeExtension testRule = new BatchSetRemovalTimeExtension(engineRule, engineTestRule);

  protected final Date currentDate = testRule.CURRENT_DATE;

  protected RuntimeService runtimeService;
  protected HistoryService historyService;
  protected ManagementService managementService;
  protected TaskService taskService;
  protected IdentityService identityService;
  protected ExternalTaskService externalTaskService;
  protected DecisionService decisionService;
  protected AuthorizationService authorizationService;

  @Test
  @Deployment(resources = {
      "org/operaton/bpm/engine/test/dmn/deployment/drdDish.dmn11.xml"
  })
  void shouldSetRemovalTime_DecisionInstance() {
    // given
    testRule.process()
      .call()
        .passVars("temperature", "dayType")
      .ruleTask("dish-decision")
      .userTask()
      .deploy()
      .startWithVariables(
        Variables.createVariables()
          .putValue("temperature", 32)
          .putValue("dayType", "Weekend")
      );

    List<HistoricDecisionInstance> historicDecisionInstances = historyService.createHistoricDecisionInstanceQuery().list();

    // assume
    assertThat(historicDecisionInstances.get(0).getRemovalTime()).isNull();
    assertThat(historicDecisionInstances.get(1).getRemovalTime()).isNull();
    assertThat(historicDecisionInstances.get(2).getRemovalTime()).isNull();

    testRule.updateHistoryTimeToLive("rootProcess", 5);

    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery().rootProcessInstances();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricProcessInstances()
        .calculatedRemovalTime()
        .byQuery(query)
        .hierarchical()
        .executeAsync()
    );

    historicDecisionInstances = historyService.createHistoricDecisionInstanceQuery().list();

    // then
    assertThat(historicDecisionInstances.get(0).getRemovalTime()).isEqualTo(addDays(currentDate, 5));
    assertThat(historicDecisionInstances.get(1).getRemovalTime()).isEqualTo(addDays(currentDate, 5));
    assertThat(historicDecisionInstances.get(2).getRemovalTime()).isEqualTo(addDays(currentDate, 5));
  }

  @Test
  @Deployment(resources = {
      "org/operaton/bpm/engine/test/dmn/deployment/drdDish.dmn11.xml"
  })
  void shouldSetRemovalTimeToStandaloneDecision_RootDecisionInstance() {
    // given
    decisionService.evaluateDecisionByKey("dish-decision")
      .variables(
        Variables.createVariables()
          .putValue("temperature", 32)
          .putValue("dayType", "Weekend")
      ).evaluate();

    List<HistoricDecisionInstance> historicDecisionInstances = historyService.createHistoricDecisionInstanceQuery()
      .decisionDefinitionKey("dish-decision")
      .list();

    // assume
    assertThat(historicDecisionInstances.get(0).getRemovalTime()).isNull();

    testRule.updateHistoryTimeToLiveDmn("dish-decision", 5);

    HistoricDecisionInstanceQuery query = historyService.createHistoricDecisionInstanceQuery().rootDecisionInstancesOnly();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricDecisionInstances()
        .calculatedRemovalTime()
        .byQuery(query)
        .hierarchical()
        .executeAsync()
    );

    historicDecisionInstances = historyService.createHistoricDecisionInstanceQuery()
      .decisionDefinitionKey("dish-decision")
      .list();

    // then
    assertThat(historicDecisionInstances.get(0).getRemovalTime()).isEqualTo(addDays(currentDate, 5));
  }

  @Test
  @Deployment(resources = {
      "org/operaton/bpm/engine/test/dmn/deployment/drdDish.dmn11.xml"
  })
  void shouldSetRemovalTimeToStandaloneDecision_ChildDecisionInstance() {
    // given
    decisionService.evaluateDecisionByKey("dish-decision")
      .variables(
        Variables.createVariables()
          .putValue("temperature", 32)
          .putValue("dayType", "Weekend")
      ).evaluate();

    List<HistoricDecisionInstance> historicDecisionInstances = historyService.createHistoricDecisionInstanceQuery()
      .decisionDefinitionKey("season")
      .list();

    // assume
    assertThat(historicDecisionInstances.get(0).getRemovalTime()).isNull();

    testRule.updateHistoryTimeToLiveDmn("dish-decision", 5);

    HistoricDecisionInstanceQuery query = historyService.createHistoricDecisionInstanceQuery().rootDecisionInstancesOnly();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricDecisionInstances()
        .calculatedRemovalTime()
        .byQuery(query)
        .hierarchical()
        .executeAsync()
    );

    historicDecisionInstances = historyService.createHistoricDecisionInstanceQuery()
      .decisionDefinitionKey("season")
      .list();

    // then
    assertThat(historicDecisionInstances.get(0).getRemovalTime()).isEqualTo(addDays(currentDate, 5));
  }

  @Test
  @Deployment(resources = {
      "org/operaton/bpm/engine/test/dmn/deployment/drdDish.dmn11.xml"
  })
  void shouldSetRemovalTime_DecisionInputInstance() {
    // given
    testRule.process()
      .call()
        .passVars("temperature", "dayType")
      .ruleTask("dish-decision")
      .userTask()
      .deploy()
      .startWithVariables(
        Variables.createVariables()
        .putValue("temperature", 32)
        .putValue("dayType", "Weekend")
      );

    HistoricDecisionInstance historicDecisionInstance = historyService.createHistoricDecisionInstanceQuery()
      .rootDecisionInstancesOnly()
      .includeInputs()
      .singleResult();

    List<HistoricDecisionInputInstance> historicDecisionInputInstances = historicDecisionInstance.getInputs();

    // assume
    assertThat(historicDecisionInputInstances.get(0).getRemovalTime()).isNull();
    assertThat(historicDecisionInputInstances.get(1).getRemovalTime()).isNull();

    testRule.updateHistoryTimeToLive("rootProcess", 5);

    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery().rootProcessInstances();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricProcessInstances()
        .calculatedRemovalTime()
        .byQuery(query)
        .hierarchical()
        .executeAsync()
    );

    historicDecisionInstance = historyService.createHistoricDecisionInstanceQuery()
      .rootDecisionInstancesOnly()
      .includeInputs()
      .singleResult();

    historicDecisionInputInstances = historicDecisionInstance.getInputs();

    // then
    assertThat(historicDecisionInputInstances.get(0).getRemovalTime()).isEqualTo(addDays(currentDate, 5));
    assertThat(historicDecisionInputInstances.get(1).getRemovalTime()).isEqualTo(addDays(currentDate, 5));
  }

  @Test
  @Deployment(resources = {
      "org/operaton/bpm/engine/test/dmn/deployment/drdDish.dmn11.xml"
  })
  void shouldSetRemovalTimeForStandaloneDecision_RootDecisionInputInstance() {
    // given
    decisionService.evaluateDecisionByKey("dish-decision")
      .variables(
        Variables.createVariables()
          .putValue("temperature", 32)
          .putValue("dayType", "Weekend")
      ).evaluate();

    HistoricDecisionInstance historicDecisionInstance = historyService.createHistoricDecisionInstanceQuery()
      .includeInputs()
      .decisionDefinitionKey("dish-decision")
      .singleResult();

    List<HistoricDecisionInputInstance> historicDecisionInputInstances = historicDecisionInstance.getInputs();

    // assume
    assertThat(historicDecisionInputInstances.get(0).getRemovalTime()).isNull();

    testRule.updateHistoryTimeToLiveDmn("dish-decision", 5);

    HistoricDecisionInstanceQuery query = historyService.createHistoricDecisionInstanceQuery().rootDecisionInstancesOnly();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricDecisionInstances()
        .calculatedRemovalTime()
        .byQuery(query)
        .hierarchical()
        .executeAsync()
    );

    historicDecisionInstance = historyService.createHistoricDecisionInstanceQuery()
      .includeInputs()
      .decisionDefinitionKey("dish-decision")
      .singleResult();

    historicDecisionInputInstances = historicDecisionInstance.getInputs();

    // then
    assertThat(historicDecisionInputInstances.get(0).getRemovalTime()).isEqualTo(addDays(currentDate, 5));
  }

  @Test
  @Deployment(resources = {
      "org/operaton/bpm/engine/test/dmn/deployment/drdDish.dmn11.xml"
  })
  void shouldSetRemovalTimeForStandaloneDecision_ChildDecisionInputInstance() {
    // given
    decisionService.evaluateDecisionByKey("dish-decision")
      .variables(
        Variables.createVariables()
          .putValue("temperature", 32)
          .putValue("dayType", "Weekend")
      ).evaluate();

    HistoricDecisionInstance historicDecisionInstance = historyService.createHistoricDecisionInstanceQuery()
      .includeInputs()
      .decisionDefinitionKey("season")
      .singleResult();

    List<HistoricDecisionInputInstance> historicDecisionInputInstances = historicDecisionInstance.getInputs();

    // assume
    assertThat(historicDecisionInputInstances.get(0).getRemovalTime()).isNull();

    testRule.updateHistoryTimeToLiveDmn("dish-decision", 5);

    HistoricDecisionInstanceQuery query = historyService.createHistoricDecisionInstanceQuery().rootDecisionInstancesOnly();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricDecisionInstances()
        .calculatedRemovalTime()
        .byQuery(query)
        .hierarchical()
        .executeAsync()
    );

    historicDecisionInstance = historyService.createHistoricDecisionInstanceQuery()
      .includeInputs()
      .decisionDefinitionKey("season")
      .singleResult();

    historicDecisionInputInstances = historicDecisionInstance.getInputs();

    // then
    assertThat(historicDecisionInputInstances.get(0).getRemovalTime()).isEqualTo(addDays(currentDate, 5));
  }

  @Test
  @Deployment(resources = {
      "org/operaton/bpm/engine/test/dmn/deployment/drdDish.dmn11.xml"
  })
  void shouldSetRemovalTime_DecisionOutputInstance() {
    // given
    testRule.process()
      .call()
        .passVars("temperature", "dayType")
      .ruleTask("dish-decision")
      .userTask()
      .deploy()
      .startWithVariables(
        Variables.createVariables()
        .putValue("temperature", 32)
        .putValue("dayType", "Weekend")
      );

    HistoricDecisionInstance historicDecisionInstance = historyService.createHistoricDecisionInstanceQuery()
      .rootDecisionInstancesOnly()
      .includeOutputs()
      .singleResult();

    List<HistoricDecisionOutputInstance> historicDecisionOutputInstances = historicDecisionInstance.getOutputs();

    // assume
    assertThat(historicDecisionOutputInstances.get(0).getRemovalTime()).isNull();

    testRule.updateHistoryTimeToLive("rootProcess", 5);

    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery().rootProcessInstances();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricProcessInstances()
        .calculatedRemovalTime()
        .byQuery(query)
        .hierarchical()
        .executeAsync()
    );

    historicDecisionInstance = historyService.createHistoricDecisionInstanceQuery()
      .rootDecisionInstancesOnly()
      .includeOutputs()
      .singleResult();

    historicDecisionOutputInstances = historicDecisionInstance.getOutputs();

    // then
    assertThat(historicDecisionOutputInstances.get(0).getRemovalTime()).isEqualTo(addDays(currentDate, 5));
  }

  @Test
  @Deployment(resources = {
      "org/operaton/bpm/engine/test/dmn/deployment/drdDish.dmn11.xml"
  })
  void shouldSetRemovalTimeForStandaloneDecision_RootDecisionOutputInstance() {
    // given
    decisionService.evaluateDecisionByKey("dish-decision")
      .variables(
        Variables.createVariables()
          .putValue("temperature", 32)
          .putValue("dayType", "Weekend")
      ).evaluate();

    HistoricDecisionInstance historicDecisionInstance = historyService.createHistoricDecisionInstanceQuery()
      .includeOutputs()
      .decisionDefinitionKey("dish-decision")
      .singleResult();

    List<HistoricDecisionOutputInstance> historicDecisionOutputInstances = historicDecisionInstance.getOutputs();

    // assume
    assertThat(historicDecisionOutputInstances.get(0).getRemovalTime()).isNull();

    testRule.updateHistoryTimeToLiveDmn("dish-decision", 5);

    HistoricDecisionInstanceQuery query = historyService.createHistoricDecisionInstanceQuery().rootDecisionInstancesOnly();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricDecisionInstances()
        .calculatedRemovalTime()
        .byQuery(query)
        .hierarchical()
        .executeAsync()
    );

    historicDecisionInstance = historyService.createHistoricDecisionInstanceQuery()
      .includeOutputs()
      .decisionDefinitionKey("dish-decision")
      .singleResult();

    historicDecisionOutputInstances = historicDecisionInstance.getOutputs();

    // then
    assertThat(historicDecisionOutputInstances.get(0).getRemovalTime()).isEqualTo(addDays(currentDate, 5));
  }

  @Test
  @Deployment(resources = {
      "org/operaton/bpm/engine/test/dmn/deployment/drdDish.dmn11.xml"
  })
  void shouldSetRemovalTimeForStandaloneDecision_ChildDecisionOutputInstance() {
    // given
    decisionService.evaluateDecisionByKey("dish-decision")
      .variables(
        Variables.createVariables()
          .putValue("temperature", 32)
          .putValue("dayType", "Weekend")
      ).evaluate();

    HistoricDecisionInstance historicDecisionInstance = historyService.createHistoricDecisionInstanceQuery()
      .includeOutputs()
      .decisionDefinitionKey("season")
      .singleResult();

    List<HistoricDecisionOutputInstance> historicDecisionOutputInstances = historicDecisionInstance.getOutputs();

    // assume
    assertThat(historicDecisionOutputInstances.get(0).getRemovalTime()).isNull();

    testRule.updateHistoryTimeToLiveDmn("dish-decision", 5);

    HistoricDecisionInstanceQuery query = historyService.createHistoricDecisionInstanceQuery().rootDecisionInstancesOnly();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricDecisionInstances()
        .calculatedRemovalTime()
        .byQuery(query)
        .hierarchical()
        .executeAsync()
    );

    historicDecisionInstance = historyService.createHistoricDecisionInstanceQuery()
      .includeOutputs()
      .decisionDefinitionKey("season")
      .singleResult();

    historicDecisionOutputInstances = historicDecisionInstance.getOutputs();

    // then
    assertThat(historicDecisionOutputInstances.get(0).getRemovalTime()).isEqualTo(addDays(currentDate, 5));
  }

  @Test
  void shouldSetRemovalTime_ProcessInstance() {
    // given
    testRule.process().call().userTask().deploy().start();

    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery().rootProcessInstances();

    List<HistoricProcessInstance> historicProcessInstances = historyService.createHistoricProcessInstanceQuery().rootProcessInstances().list();

    // assume
    assertThat(historicProcessInstances.get(0).getRemovalTime()).isNull();

    testRule.updateHistoryTimeToLive("rootProcess", 5);

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricProcessInstances()
        .calculatedRemovalTime()
        .byQuery(query)
        .hierarchical()
        .executeAsync()
    );

    historicProcessInstances = historyService.createHistoricProcessInstanceQuery().list();

    // then
    assertThat(historicProcessInstances.get(0).getRemovalTime()).isEqualTo(addDays(currentDate, 5));
    assertThat(historicProcessInstances.get(1).getRemovalTime()).isEqualTo(addDays(currentDate, 5));
  }

  @Test
  void shouldSetRemovalTime_ActivityInstance() {
    // given
    testRule.process().call().userTask().deploy().start();

    HistoricActivityInstance historicActivityInstance = historyService.createHistoricActivityInstanceQuery()
      .activityName("userTask")
      .singleResult();

    // assume
    assertThat(historicActivityInstance.getRemovalTime()).isNull();

    testRule.updateHistoryTimeToLive("rootProcess", 5);

    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery().rootProcessInstances();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricProcessInstances()
        .calculatedRemovalTime()
        .byQuery(query)
        .hierarchical()
        .executeAsync()
    );

    historicActivityInstance = historyService.createHistoricActivityInstanceQuery()
      .activityName("userTask")
      .singleResult();

    // then
    assertThat(historicActivityInstance.getRemovalTime()).isEqualTo(addDays(currentDate, 5));
  }

  @Test
  void shouldSetRemovalTime_TaskInstance() {
    // given
    testRule.process().call().userTask().deploy().start();

    HistoricTaskInstance historicTaskInstance = historyService.createHistoricTaskInstanceQuery().singleResult();

    // assume
    assertThat(historicTaskInstance.getRemovalTime()).isNull();

    testRule.updateHistoryTimeToLive("rootProcess", 5);

    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery().rootProcessInstances();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricProcessInstances()
        .calculatedRemovalTime()
        .byQuery(query)
        .hierarchical()
        .executeAsync()
    );

    historicTaskInstance = historyService.createHistoricTaskInstanceQuery().singleResult();

    // then
    assertThat(historicTaskInstance.getRemovalTime()).isEqualTo(addDays(currentDate, 5));
  }

  @Test
  void shouldSetRemovalTime_HistoricTaskInstanceAuthorization() {
    // given
    testRule.getProcessEngineConfiguration()
        .setEnableHistoricInstancePermissions(true);

    testRule.enableAuth();
    testRule.process().call().userTask().deploy().start();
    testRule.disableAuth();

    HistoricTaskInstance historicTaskInstance =
        historyService.createHistoricTaskInstanceQuery().singleResult();

    // assume
    assertThat(historicTaskInstance.getRemovalTime()).isNull();

    testRule.updateHistoryTimeToLive("rootProcess", 5);

    HistoricProcessInstanceQuery query =
        historyService.createHistoricProcessInstanceQuery().rootProcessInstances();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricProcessInstances()
        .calculatedRemovalTime()
        .byQuery(query)
        .hierarchical()
        .executeAsync()
    );

    Authorization authorization =
        authorizationService.createAuthorizationQuery()
            .resourceType(Resources.HISTORIC_TASK)
            .singleResult();

    // then
    assertThat(authorization.getRemovalTime()).isEqualTo(addDays(currentDate, 5));
  }

  @Test
  void shouldNotSetRemovalTime_HistoricTaskInstancePermissionsDisabled() {
    // given
    testRule.getProcessEngineConfiguration()
        .setEnableHistoricInstancePermissions(true);

    testRule.enableAuth();
    testRule.process().call().userTask().deploy().start();
    testRule.disableAuth();

    testRule.getProcessEngineConfiguration()
        .setEnableHistoricInstancePermissions(false);

    testRule.updateHistoryTimeToLive("rootProcess", 5);

    HistoricProcessInstanceQuery query =
        historyService.createHistoricProcessInstanceQuery().rootProcessInstances();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricProcessInstances()
        .calculatedRemovalTime()
        .byQuery(query)
        .hierarchical()
        .executeAsync()
    );

    Authorization authorization =
        authorizationService.createAuthorizationQuery()
            .resourceType(Resources.HISTORIC_TASK)
            .singleResult();

    // then
    assertThat(authorization.getRemovalTime()).isNull();
  }

  @Test
  void shouldSetRemovalTime_HistoricProcessInstanceAuthorization() {
    // given
    testRule.getProcessEngineConfiguration()
        .setEnableHistoricInstancePermissions(true);

    String rootProcessInstanceId = testRule.process().call().userTask().deploy().start();

    Authorization authorization =
        authorizationService.createNewAuthorization(Authorization.AUTH_TYPE_GRANT);
    authorization.setResource(Resources.HISTORIC_PROCESS_INSTANCE);

    String processInstanceId = historyService.createHistoricProcessInstanceQuery()
        .activeActivityIdIn("userTask")
        .singleResult()
        .getId();

    authorization.setResourceId(processInstanceId);
    authorization.setUserId("foo");

    authorizationService.saveAuthorization(authorization);

    // assume
    AuthorizationQuery authQuery = authorizationService.createAuthorizationQuery()
        .resourceType(Resources.HISTORIC_PROCESS_INSTANCE);

    assertThat(authQuery.list())
        .extracting("removalTime", "resourceId", "rootProcessInstanceId")
        .containsExactly(tuple(null, processInstanceId, rootProcessInstanceId));

    // when
    testRule.updateHistoryTimeToLive("rootProcess", 5);

    HistoricProcessInstanceQuery query =
        historyService.createHistoricProcessInstanceQuery().rootProcessInstances();

    testRule.syncExec(
        historyService.setRemovalTimeToHistoricProcessInstances()
            .calculatedRemovalTime()
            .byQuery(query)
            .hierarchical()
            .executeAsync()
    );

    // then
    authQuery = authorizationService.createAuthorizationQuery()
        .resourceType(Resources.HISTORIC_PROCESS_INSTANCE);

    Date removalTime = addDays(currentDate, 5);
    assertThat(authQuery.list())
        .extracting("removalTime", "resourceId", "rootProcessInstanceId")
        .containsExactly(tuple(removalTime, processInstanceId, rootProcessInstanceId));
  }

  @Test
  void shouldNotSetRemovalTime_HistoricProcessInstancePermissionsDisabled() {
    // given
    testRule.getProcessEngineConfiguration()
        .setEnableHistoricInstancePermissions(false);

    String rootProcessInstanceId = testRule.process().call().userTask().deploy().start();

    Authorization authorization =
        authorizationService.createNewAuthorization(Authorization.AUTH_TYPE_GRANT);
    authorization.setResource(Resources.HISTORIC_PROCESS_INSTANCE);

    String processInstanceId = historyService.createHistoricProcessInstanceQuery()
        .activeActivityIdIn("userTask")
        .singleResult()
        .getId();

    authorization.setResourceId(processInstanceId);
    authorization.setUserId("foo");

    authorizationService.saveAuthorization(authorization);

    // assume
    AuthorizationQuery authQuery = authorizationService.createAuthorizationQuery()
        .resourceType(Resources.HISTORIC_PROCESS_INSTANCE);

    assertThat(authQuery.list())
        .extracting("removalTime", "resourceId", "rootProcessInstanceId")
        .containsExactly(tuple(null, processInstanceId, rootProcessInstanceId));

    testRule.updateHistoryTimeToLive("rootProcess", 5);

    // when
    HistoricProcessInstanceQuery query =
        historyService.createHistoricProcessInstanceQuery().rootProcessInstances();

    testRule.syncExec(
        historyService.setRemovalTimeToHistoricProcessInstances()
            .calculatedRemovalTime()
            .byQuery(query)
            .hierarchical()
            .executeAsync()
    );

    // then
    authQuery = authorizationService.createAuthorizationQuery()
        .resourceType(Resources.HISTORIC_PROCESS_INSTANCE);

    assertThat(authQuery.list())
        .extracting("removalTime", "resourceId", "rootProcessInstanceId")
        .containsExactly(tuple(null, processInstanceId, rootProcessInstanceId));
  }

  @Test
  void shouldSetRemovalTime_VariableInstance() {
    // given
    testRule.process().call().userTask().deploy()
      .startWithVariables(
        Variables.createVariables()
          .putValue("aVariableName", "aVariableValue"));

    HistoricVariableInstance historicVariableInstance = historyService.createHistoricVariableInstanceQuery().singleResult();

    // assume
    assertThat(historicVariableInstance.getRemovalTime()).isNull();

    testRule.updateHistoryTimeToLive("rootProcess", 5);

    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery().rootProcessInstances();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricProcessInstances()
        .calculatedRemovalTime()
        .byQuery(query)
        .hierarchical()
        .executeAsync()
    );

    historicVariableInstance = historyService.createHistoricVariableInstanceQuery().singleResult();

    // then
    assertThat(historicVariableInstance.getRemovalTime()).isEqualTo(addDays(currentDate, 5));
  }

  @Test
  void shouldSetRemovalTime_Detail() {
    // given
    testRule.process().call().userTask().deploy()
      .startWithVariables(
        Variables.createVariables()
          .putValue("aVariableName", "aVariableValue"));

    HistoricDetail historicDetail = historyService.createHistoricDetailQuery().singleResult();

    // assume
    assertThat(historicDetail.getRemovalTime()).isNull();

    testRule.updateHistoryTimeToLive("rootProcess", 5);

    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery().rootProcessInstances();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricProcessInstances()
        .calculatedRemovalTime()
        .byQuery(query)
        .hierarchical()
        .executeAsync()
    );

    historicDetail = historyService.createHistoricDetailQuery().singleResult();

    // then
    assertThat(historicDetail.getRemovalTime()).isEqualTo(addDays(currentDate, 5));
  }

  @Test
  void shouldSetRemovalTime_ExternalTaskLog() {
    // given
    testRule.process().call().externalTask().deploy().start();

    HistoricExternalTaskLog historicExternalTaskLog = historyService.createHistoricExternalTaskLogQuery().singleResult();

    // assume
    assertThat(historicExternalTaskLog.getRemovalTime()).isNull();

    testRule.updateHistoryTimeToLive("rootProcess", 5);

    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery().rootProcessInstances();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricProcessInstances()
        .calculatedRemovalTime()
        .byQuery(query)
        .hierarchical()
        .executeAsync()
    );

    historicExternalTaskLog = historyService.createHistoricExternalTaskLogQuery().singleResult();

    // then
    assertThat(historicExternalTaskLog.getRemovalTime()).isEqualTo(addDays(currentDate, 5));
  }

  @Test
  void shouldSetRemovalTime_JobLog() {
    // given
    testRule.process().call().async().userTask().deploy().start();

    HistoricJobLog job = historyService.createHistoricJobLogQuery()
      .processDefinitionKey("process")
      .singleResult();

    // assume
    assertThat(job.getRemovalTime()).isNull();

    testRule.updateHistoryTimeToLive("rootProcess", 5);

    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery().rootProcessInstances();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricProcessInstances()
        .calculatedRemovalTime()
        .byQuery(query)
        .hierarchical()
        .executeAsync()
    );

    job = historyService.createHistoricJobLogQuery()
      .processDefinitionKey("process")
      .singleResult();

    // then
    assertThat(job.getRemovalTime()).isEqualTo(addDays(currentDate, 5));
  }

  @Test
  void shouldSetRemovalTime_Incident() {
    // given
    String rootProcessInstanceId = testRule.process().call().async().userTask().deploy().start();

    String jobId = managementService.createJobQuery().singleResult().getId();

    managementService.setJobRetries(jobId, 0);

    String leafProcessInstanceId = historyService.createHistoricProcessInstanceQuery()
      .superProcessInstanceId(rootProcessInstanceId)
      .singleResult()
      .getId();

    HistoricIncident historicIncident = historyService.createHistoricIncidentQuery()
      .processInstanceId(leafProcessInstanceId)
      .singleResult();

    // assume
    assertThat(historicIncident.getRemovalTime()).isNull();

    testRule.updateHistoryTimeToLive("rootProcess", 5);

    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery().rootProcessInstances();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricProcessInstances()
        .calculatedRemovalTime()
        .byQuery(query)
        .hierarchical()
        .executeAsync()
    );

    historicIncident = historyService.createHistoricIncidentQuery()
      .processInstanceId(leafProcessInstanceId)
      .singleResult();

    // then
    assertThat(historicIncident.getRemovalTime()).isEqualTo(addDays(currentDate, 5));
  }

  @Test
  void shouldSetRemovalTime_OperationLog() {
    // given
    String processInstanceId = testRule.process().call().async().userTask().deploy().start();

    identityService.setAuthenticatedUserId("aUserId");
    runtimeService.suspendProcessInstanceById(processInstanceId);
    identityService.clearAuthentication();

    UserOperationLogEntry userOperationLog = historyService.createUserOperationLogQuery().singleResult();

    // assume
    assertThat(userOperationLog.getRemovalTime()).isNull();

    testRule.updateHistoryTimeToLive("rootProcess", 5);

    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery().rootProcessInstances();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricProcessInstances()
        .calculatedRemovalTime()
        .byQuery(query)
        .hierarchical()
        .executeAsync()
    );

    userOperationLog = historyService.createUserOperationLogQuery().singleResult();

    // then
    assertThat(userOperationLog.getRemovalTime()).isEqualTo(addDays(currentDate, 5));
  }

  @Test
  void shouldSetRemovalTime_IdentityLinkLog() {
    // given
    testRule.process().call().userTask().deploy().start();

    HistoricIdentityLinkLog identityLinkLog = historyService.createHistoricIdentityLinkLogQuery().singleResult();

    // assume
    assertThat(identityLinkLog.getRemovalTime()).isNull();

    testRule.updateHistoryTimeToLive("rootProcess", 5);

    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery().rootProcessInstances();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricProcessInstances()
        .calculatedRemovalTime()
        .byQuery(query)
        .hierarchical()
        .executeAsync()
    );

    identityLinkLog = historyService.createHistoricIdentityLinkLogQuery().singleResult();

    // then
    assertThat(identityLinkLog.getRemovalTime()).isEqualTo(addDays(currentDate, 5));
  }

  @Test
  void shouldSetRemovalTime_CommentByTaskId() {
    // given
    testRule.process().call().userTask().deploy().start();

    String taskId = historyService.createHistoricTaskInstanceQuery()
      .taskName("userTask")
      .singleResult()
      .getId();

    taskService.createComment(taskId, null, "aComment");

    Comment comment = taskService.getTaskComments(taskId).get(0);

    testRule.updateHistoryTimeToLive("rootProcess", 5);

    // assume
    assertThat(comment.getRemovalTime()).isNull();

    testRule.updateHistoryTimeToLive("rootProcess", 5);

    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery().rootProcessInstances();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricProcessInstances()
        .calculatedRemovalTime()
        .byQuery(query)
        .hierarchical()
        .executeAsync()
    );

    comment = taskService.getTaskComments(taskId).get(0);

    // then
    assertThat(comment.getRemovalTime()).isEqualTo(addDays(currentDate, 5));
  }

  @Test
  void shouldSetRemovalTime_CommentByProcessInstanceId() {
    // given
    String processInstanceId = testRule.process().call().userTask().deploy().start();

    taskService.createComment(null, processInstanceId, "aComment");

    Comment comment = taskService.getProcessInstanceComments(processInstanceId).get(0);

    testRule.updateHistoryTimeToLive("rootProcess", 5);

    // assume
    assertThat(comment.getRemovalTime()).isNull();

    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery().rootProcessInstances();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricProcessInstances()
        .calculatedRemovalTime()
        .byQuery(query)
        .hierarchical()
        .executeAsync()
    );

    comment = taskService.getProcessInstanceComments(processInstanceId).get(0);

    // then
    assertThat(comment.getRemovalTime()).isEqualTo(addDays(currentDate, 5));
  }

  @Test
  void shouldSetRemovalTime_AttachmentByTaskId() {
    // given
    testRule.process().call().userTask().deploy().start();

    String taskId = historyService.createHistoricTaskInstanceQuery()
      .taskName("userTask")
      .singleResult()
      .getId();

    Attachment attachment = taskService.createAttachment(null, taskId,
      null, null, null, "http://operaton.com");

    // assume
    assertThat(attachment.getRemovalTime()).isNull();

    testRule.updateHistoryTimeToLive("rootProcess", 5);

    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery().rootProcessInstances();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricProcessInstances()
        .calculatedRemovalTime()
        .byQuery(query)
        .hierarchical()
        .executeAsync()
    );

    attachment = taskService.getTaskAttachments(taskId).get(0);

    // then
    assertThat(attachment.getRemovalTime()).isEqualTo(addDays(currentDate, 5));
  }

  @Test
  void shouldSetRemovalTime_AttachmentByProcessInstanceId() {
    // given
    String processInstanceId = testRule.process().call().userTask().deploy().start();

    Attachment attachment = taskService.createAttachment(null, null,
      processInstanceId, null, null, "http://operaton.com");

    // assume
    assertThat(attachment.getRemovalTime()).isNull();

    testRule.updateHistoryTimeToLive("rootProcess", 5);

    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery().rootProcessInstances();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricProcessInstances()
        .calculatedRemovalTime()
        .byQuery(query)
        .hierarchical()
        .executeAsync()
    );

    attachment = taskService.getProcessInstanceAttachments(processInstanceId).get(0);

    // then
    assertThat(attachment.getRemovalTime()).isEqualTo(addDays(currentDate, 5));
  }

  @Test
  void shouldSetRemovalTime_ByteArray_AttachmentByTaskId() {
    // given
    testRule.process().call().userTask().deploy().start();

    String taskId = historyService.createHistoricTaskInstanceQuery()
      .taskName("userTask")
      .singleResult()
      .getId();

    AttachmentEntity attachment = (AttachmentEntity) taskService.createAttachment(null, taskId,
      null, null, null, new ByteArrayInputStream("".getBytes()));

    ByteArrayEntity byteArrayEntity = testRule.findByteArrayById(attachment.getContentId());

    testRule.updateHistoryTimeToLive("rootProcess", 5);

    // assume
    assertThat(byteArrayEntity.getRemovalTime()).isNull();

    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery().rootProcessInstances();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricProcessInstances()
        .calculatedRemovalTime()
        .byQuery(query)
        .hierarchical()
        .executeAsync()
    );

    byteArrayEntity = testRule.findByteArrayById(attachment.getContentId());

    // then
    assertThat(byteArrayEntity.getRemovalTime()).isEqualTo(addDays(currentDate, 5));
  }

  @Test
  void shouldSetRemovalTime_ByteArray_AttachmentByProcessInstanceId() {
    // given
    String processInstanceId = testRule.process().call().userTask().deploy().start();

    AttachmentEntity attachment = (AttachmentEntity) taskService.createAttachment(null, null,
      processInstanceId, null, null, new ByteArrayInputStream("".getBytes()));

    String byteArrayId = attachment.getContentId();

    ByteArrayEntity byteArrayEntity = testRule.findByteArrayById(byteArrayId);

    testRule.updateHistoryTimeToLive("rootProcess", 5);

    // assume
    assertThat(byteArrayEntity.getRemovalTime()).isNull();

    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery().rootProcessInstances();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricProcessInstances()
        .calculatedRemovalTime()
        .byQuery(query)
        .hierarchical()
        .executeAsync()
    );

    byteArrayEntity = testRule.findByteArrayById(byteArrayId);

    // then
    assertThat(byteArrayEntity.getRemovalTime()).isEqualTo(addDays(currentDate, 5));
  }

  @Test
  void shouldSetRemovalTime_ByteArray_Variable() {
    // given
    testRule.process().call().userTask().deploy()
      .startWithVariables(
        Variables.createVariables()
          .putValue("aVariableName",
            Variables.fileValue("file.xml")
              .file("<root />".getBytes())));

    HistoricVariableInstance historicVariableInstance = historyService.createHistoricVariableInstanceQuery().singleResult();

    String byteArrayId = ((HistoricVariableInstanceEntity) historicVariableInstance).getByteArrayId();

    ByteArrayEntity byteArrayEntity = testRule.findByteArrayById(byteArrayId);

    // assume
    assertThat(byteArrayEntity.getRemovalTime()).isNull();

    testRule.updateHistoryTimeToLive("rootProcess", 5);

    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery().rootProcessInstances();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricProcessInstances()
        .calculatedRemovalTime()
        .byQuery(query)
        .hierarchical()
        .executeAsync()
    );

    byteArrayEntity = testRule.findByteArrayById(byteArrayId);

    // then
    assertThat(byteArrayEntity.getRemovalTime()).isEqualTo(addDays(currentDate, 5));
  }

  @Test
  void shouldSetRemovalTime_ByteArray_JobLog() {
    // given
    testRule.process().call().async().scriptTask().deploy().start();

    String jobId = managementService.createJobQuery().singleResult().getId();

    try {
      managementService.executeJob(jobId);
    } catch (Exception ignored) {
      // ignored
    }

    HistoricJobLog historicJobLog = historyService.createHistoricJobLogQuery()
      .failureLog()
      .singleResult();

    String byteArrayId = ((HistoricJobLogEventEntity) historicJobLog).getExceptionByteArrayId();

    ByteArrayEntity byteArrayEntity = testRule.findByteArrayById(byteArrayId);

    // assume
    assertThat(byteArrayEntity.getRemovalTime()).isNull();

    testRule.updateHistoryTimeToLive("rootProcess", 5);

    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery().rootProcessInstances();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricProcessInstances()
        .calculatedRemovalTime()
        .byQuery(query)
        .hierarchical()
        .executeAsync()
    );

    byteArrayEntity = testRule.findByteArrayById(byteArrayId);

    // then
    assertThat(byteArrayEntity.getRemovalTime()).isEqualTo(addDays(currentDate, 5));
  }

  @Test
  void shouldSetRemovalTime_ByteArray_ExternalTaskLog() {
    // given
    testRule.process().call().externalTask().deploy().start();

    String externalTaskId = externalTaskService.fetchAndLock(1, "aWorkerId")
      .topic("aTopicName", Integer.MAX_VALUE)
      .execute()
      .get(0)
      .getId();

    externalTaskService.handleFailure(externalTaskId, "aWorkerId",
      null, "errorDetails", 5, 3000L);

    HistoricExternalTaskLog externalTaskLog = historyService.createHistoricExternalTaskLogQuery()
      .failureLog()
      .singleResult();

    String byteArrayId = ((HistoricExternalTaskLogEntity) externalTaskLog).getErrorDetailsByteArrayId();

    ByteArrayEntity byteArrayEntity = testRule.findByteArrayById(byteArrayId);

    testRule.updateHistoryTimeToLive("rootProcess", 5);

    // assume
    assertThat(byteArrayEntity.getRemovalTime()).isNull();

    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery().rootProcessInstances();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricProcessInstances()
        .calculatedRemovalTime()
        .byQuery(query)
        .hierarchical()
        .executeAsync()
    );

    byteArrayEntity = testRule.findByteArrayById(byteArrayId);

    // then
    assertThat(byteArrayEntity.getRemovalTime()).isEqualTo(addDays(currentDate, 5));
  }

  @Test
  @Deployment(resources = {
      "org/operaton/bpm/engine/test/api/history/testDmnWithPojo.dmn11.xml"
  })
  void shouldSetRemovalTime_ByteArray_DecisionInputInstance() {
    // given
    testRule.process()
      .call()
        .passVars("pojo")
      .ruleTask("testDecision")
      .userTask()
      .deploy()
      .startWithVariables(
        Variables.createVariables()
          .putValue("pojo", new TestPojo("okay", 13.37))
      );

    HistoricDecisionInstance historicDecisionInstance = historyService.createHistoricDecisionInstanceQuery()
      .rootDecisionInstancesOnly()
      .includeInputs()
      .singleResult();

    String byteArrayId = ((HistoricDecisionInputInstanceEntity) historicDecisionInstance.getInputs().get(0))
      .getByteArrayValueId();

    ByteArrayEntity byteArrayEntity = testRule.findByteArrayById(byteArrayId);

    testRule.updateHistoryTimeToLive("rootProcess", 5);

    // assume
    assertThat(byteArrayEntity.getRemovalTime()).isNull();

    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery().rootProcessInstances();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricProcessInstances()
        .calculatedRemovalTime()
        .byQuery(query)
        .hierarchical()
        .executeAsync()
    );

    byteArrayEntity = testRule.findByteArrayById(byteArrayId);

    // then
    assertThat(byteArrayEntity.getRemovalTime()).isEqualTo(addDays(currentDate, 5));
  }

  @Test
  @Deployment(resources = {
      "org/operaton/bpm/engine/test/api/history/removaltime/drd.dmn11.xml"
  })
  void shouldSetRemovalTimeForStandaloneDecision_ByteArray_RootDecisionInputInstance() {
    // given
    decisionService.evaluateDecisionByKey("root")
      .variables(
        Variables.createVariables()
          .putValue("pojo", new TestPojo("okay", 13.37))
      ).evaluate();

    HistoricDecisionInstance historicDecisionInstance = historyService.createHistoricDecisionInstanceQuery()
      .includeInputs()
      .decisionDefinitionKey("root")
      .singleResult();

    String byteArrayId = ((HistoricDecisionInputInstanceEntity) historicDecisionInstance.getInputs().get(0))
      .getByteArrayValueId();

    testRule.updateHistoryTimeToLiveDmn("root", 5);

    ByteArrayEntity byteArrayEntity = testRule.findByteArrayById(byteArrayId);

    // assume
    assertThat(byteArrayEntity.getRemovalTime()).isNull();

    HistoricDecisionInstanceQuery query = historyService.createHistoricDecisionInstanceQuery().rootDecisionInstancesOnly();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricDecisionInstances()
        .calculatedRemovalTime()
        .byQuery(query)
        .hierarchical()
        .executeAsync()
    );

    byteArrayEntity = testRule.findByteArrayById(byteArrayId);

    // then
    assertThat(byteArrayEntity.getRemovalTime()).isEqualTo(addDays(currentDate, 5));
  }

  @Test
  @Deployment(resources = {
      "org/operaton/bpm/engine/test/api/history/removaltime/drd.dmn11.xml"
  })
  void shouldSetRemovalTimeForStandaloneDecision_ByteArray_ChildDecisionInputInstance() {
    // given
    decisionService.evaluateDecisionByKey("root")
      .variables(
        Variables.createVariables()
          .putValue("pojo", new TestPojo("okay", 13.37))
      ).evaluate();

    HistoricDecisionInstance historicDecisionInstance = historyService.createHistoricDecisionInstanceQuery()
      .includeInputs()
      .decisionDefinitionKey("child")
      .singleResult();

    String byteArrayId = ((HistoricDecisionInputInstanceEntity) historicDecisionInstance.getInputs().get(0))
      .getByteArrayValueId();

    testRule.updateHistoryTimeToLiveDmn("root", 5);

    ByteArrayEntity byteArrayEntity = testRule.findByteArrayById(byteArrayId);

    // assume
    assertThat(byteArrayEntity.getRemovalTime()).isNull();

    HistoricDecisionInstanceQuery query = historyService.createHistoricDecisionInstanceQuery().rootDecisionInstancesOnly();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricDecisionInstances()
        .calculatedRemovalTime()
        .byQuery(query)
        .hierarchical()
        .executeAsync()
    );

    byteArrayEntity = testRule.findByteArrayById(byteArrayId);

    // then
    assertThat(byteArrayEntity.getRemovalTime()).isEqualTo(addDays(currentDate, 5));
  }

  @Test
  @Deployment(resources = {
      "org/operaton/bpm/engine/test/api/history/testDmnWithPojo.dmn11.xml"
  })
  void shouldSetRemovalTime_ByteArray_DecisionOutputInstance() {
    // given
    testRule.process()
      .call()
        .passVars("pojo")
      .ruleTask("testDecision")
      .userTask()
      .deploy()
      .startWithVariables(
        Variables.createVariables()
          .putValue("pojo", new TestPojo("okay", 13.37))
      );

    HistoricDecisionInstance historicDecisionInstance = historyService.createHistoricDecisionInstanceQuery()
      .rootDecisionInstancesOnly()
      .includeOutputs()
      .singleResult();

    String byteArrayId = ((HistoricDecisionOutputInstanceEntity) historicDecisionInstance.getOutputs().get(0))
      .getByteArrayValueId();

    ByteArrayEntity byteArrayEntity = testRule.findByteArrayById(byteArrayId);

    // assume
    assertThat(byteArrayEntity.getRemovalTime()).isNull();

    testRule.updateHistoryTimeToLive("rootProcess", 5);

    HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery().rootProcessInstances();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricProcessInstances()
        .calculatedRemovalTime()
        .byQuery(query)
        .hierarchical()
        .executeAsync()
    );

    byteArrayEntity = testRule.findByteArrayById(byteArrayId);

    // then
    assertThat(byteArrayEntity.getRemovalTime()).isEqualTo(addDays(currentDate, 5));
  }

  @Test
  @Deployment(resources = {
      "org/operaton/bpm/engine/test/api/history/removaltime/drd.dmn11.xml"
  })
  void shouldSetRemovalTimeForStandaloneDecision_ByteArray_RootDecisionOutputInstance() {
    // given
    decisionService.evaluateDecisionByKey("root")
      .variables(
        Variables.createVariables()
          .putValue("pojo", new TestPojo("okay", 13.37))
      ).evaluate();

    HistoricDecisionInstance historicDecisionInstance = historyService.createHistoricDecisionInstanceQuery()
      .includeOutputs()
      .decisionDefinitionKey("root")
      .singleResult();

    String byteArrayId = ((HistoricDecisionOutputInstanceEntity) historicDecisionInstance.getOutputs().get(0))
      .getByteArrayValueId();

    testRule.updateHistoryTimeToLiveDmn("root", 5);

    ByteArrayEntity byteArrayEntity = testRule.findByteArrayById(byteArrayId);

    // assume
    assertThat(byteArrayEntity.getRemovalTime()).isNull();

    HistoricDecisionInstanceQuery query = historyService.createHistoricDecisionInstanceQuery().rootDecisionInstancesOnly();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricDecisionInstances()
        .calculatedRemovalTime()
        .byQuery(query)
        .hierarchical()
        .executeAsync()
    );

    byteArrayEntity = testRule.findByteArrayById(byteArrayId);

    // then
    assertThat(byteArrayEntity.getRemovalTime()).isEqualTo(addDays(currentDate, 5));
  }

  @Test
  @Deployment(resources = {
      "org/operaton/bpm/engine/test/api/history/removaltime/drd.dmn11.xml"
  })
  void shouldSetRemovalTimeForStandaloneDecision_ByteArray_ChildDecisionOutputInstance() {
    // given
    decisionService.evaluateDecisionByKey("root")
      .variables(
        Variables.createVariables()
          .putValue("pojo", new TestPojo("okay", 13.37))
      ).evaluate();

    HistoricDecisionInstance historicDecisionInstance = historyService.createHistoricDecisionInstanceQuery()
      .includeOutputs()
      .decisionDefinitionKey("child")
      .singleResult();

    String byteArrayId = ((HistoricDecisionOutputInstanceEntity) historicDecisionInstance.getOutputs().get(0))
      .getByteArrayValueId();

    testRule.updateHistoryTimeToLiveDmn("root", 5);

    ByteArrayEntity byteArrayEntity = testRule.findByteArrayById(byteArrayId);

    // assume
    assertThat(byteArrayEntity.getRemovalTime()).isNull();

    HistoricDecisionInstanceQuery query = historyService.createHistoricDecisionInstanceQuery().rootDecisionInstancesOnly();

    // when
    testRule.syncExec(
      historyService.setRemovalTimeToHistoricDecisionInstances()
        .calculatedRemovalTime()
        .byQuery(query)
        .hierarchical()
        .executeAsync()
    );

    byteArrayEntity = testRule.findByteArrayById(byteArrayId);

    // then
    assertThat(byteArrayEntity.getRemovalTime()).isEqualTo(addDays(currentDate, 5));
  }

}
