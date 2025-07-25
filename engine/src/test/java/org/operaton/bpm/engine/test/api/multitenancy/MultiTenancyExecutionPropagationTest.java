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
package org.operaton.bpm.engine.test.api.multitenancy;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.operaton.bpm.engine.CaseService;
import org.operaton.bpm.engine.ExternalTaskService;
import org.operaton.bpm.engine.ManagementService;
import org.operaton.bpm.engine.RepositoryService;
import org.operaton.bpm.engine.RuntimeService;
import org.operaton.bpm.engine.TaskService;
import org.operaton.bpm.engine.delegate.DelegateExecution;
import org.operaton.bpm.engine.delegate.JavaDelegate;
import org.operaton.bpm.engine.externaltask.ExternalTask;
import org.operaton.bpm.engine.externaltask.LockedExternalTask;
import org.operaton.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.operaton.bpm.engine.management.JobDefinition;
import org.operaton.bpm.engine.repository.CaseDefinition;
import org.operaton.bpm.engine.repository.ProcessDefinition;
import org.operaton.bpm.engine.runtime.CaseExecution;
import org.operaton.bpm.engine.runtime.CaseInstance;
import org.operaton.bpm.engine.runtime.EventSubscription;
import org.operaton.bpm.engine.runtime.Execution;
import org.operaton.bpm.engine.runtime.Incident;
import org.operaton.bpm.engine.runtime.Job;
import org.operaton.bpm.engine.runtime.ProcessInstance;
import org.operaton.bpm.engine.runtime.VariableInstance;
import org.operaton.bpm.engine.task.Task;
import org.operaton.bpm.engine.test.junit5.ProcessEngineExtension;
import org.operaton.bpm.engine.test.junit5.ProcessEngineTestExtension;
import org.operaton.bpm.engine.variable.VariableMap;
import org.operaton.bpm.engine.variable.Variables;
import org.operaton.bpm.model.bpmn.Bpmn;

class MultiTenancyExecutionPropagationTest {

  protected static final String CMMN_FILE = "org/operaton/bpm/engine/test/api/cmmn/oneTaskCase.cmmn";
  protected static final String SET_VARIABLE_CMMN_FILE = "org/operaton/bpm/engine/test/api/multitenancy/HumanTaskSetVariableExecutionListener.cmmn";

  protected static final String PROCESS_DEFINITION_KEY = "testProcess";
  protected static final String TENANT_ID = "tenant1";

  @RegisterExtension
  static ProcessEngineExtension engineRule = ProcessEngineExtension.builder().build();
  @RegisterExtension
  ProcessEngineTestExtension testRule = new ProcessEngineTestExtension(engineRule);

  protected ProcessEngineConfigurationImpl processEngineConfiguration;
  protected RuntimeService runtimeService;
  protected RepositoryService repositoryService;
  protected ManagementService managementService;
  protected TaskService taskService;
  protected ExternalTaskService externalTaskService;
  protected CaseService caseService;

  @Test
  void testPropagateTenantIdToProcessDefinition() {

    testRule.deployForTenant(TENANT_ID,  Bpmn.createExecutableProcess(PROCESS_DEFINITION_KEY).startEvent().done());

    ProcessDefinition processDefinition = repositoryService
        .createProcessDefinitionQuery()
        .singleResult();

    assertThat(processDefinition).isNotNull();
    // inherit the tenant id from deployment
    assertThat(processDefinition.getTenantId()).isEqualTo(TENANT_ID);
  }

  @Test
  void testPropagateTenantIdToProcessInstance() {
    testRule.deployForTenant(TENANT_ID,  Bpmn.createExecutableProcess(PROCESS_DEFINITION_KEY)
        .startEvent()
        .userTask()
        .endEvent()
       .done());

    startProcessInstance(PROCESS_DEFINITION_KEY);

    ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().singleResult();
    assertThat(processInstance).isNotNull();
    // inherit the tenant id from process definition
    assertThat(processInstance.getTenantId()).isEqualTo(TENANT_ID);
  }

  @Test
  void testPropagateTenantIdToConcurrentExecution() {

    testRule.deployForTenant(TENANT_ID, Bpmn.createExecutableProcess(PROCESS_DEFINITION_KEY)
        .startEvent()
        .parallelGateway("fork")
          .userTask()
          .parallelGateway("join")
          .endEvent()
          .moveToNode("fork")
            .userTask()
            .connectTo("join")
            .done());

    startProcessInstance(PROCESS_DEFINITION_KEY);

    List<Execution> executions = runtimeService.createExecutionQuery().list();
    assertThat(executions).hasSize(3);
    assertThat(executions.get(0).getTenantId()).isEqualTo(TENANT_ID);
    // inherit the tenant id from process instance
    assertThat(executions.get(1).getTenantId()).isEqualTo(TENANT_ID);
    assertThat(executions.get(2).getTenantId()).isEqualTo(TENANT_ID);
  }

  @Test
  void testPropagateTenantIdToEmbeddedSubprocess() {

    testRule.deployForTenant(TENANT_ID, Bpmn.createExecutableProcess(PROCESS_DEFINITION_KEY)
      .startEvent()
      .subProcess()
        .embeddedSubProcess()
          .startEvent()
          .userTask()
          .endEvent()
      .subProcessDone()
      .endEvent()
    .done());

    startProcessInstance(PROCESS_DEFINITION_KEY);

    List<Execution> executions = runtimeService.createExecutionQuery().list();
    assertThat(executions).hasSize(2);
    assertThat(executions.get(0).getTenantId()).isEqualTo(TENANT_ID);
    // inherit the tenant id from parent execution (e.g. process instance)
    assertThat(executions.get(1).getTenantId()).isEqualTo(TENANT_ID);
  }

  @Test
  void testPropagateTenantIdToTask() {

    testRule.deployForTenant(TENANT_ID, Bpmn.createExecutableProcess(PROCESS_DEFINITION_KEY)
        .startEvent()
        .userTask()
        .endEvent()
      .done());

    startProcessInstance(PROCESS_DEFINITION_KEY);

    Task task = taskService.createTaskQuery().singleResult();
    assertThat(task).isNotNull();
    // inherit the tenant id from execution
    assertThat(task.getTenantId()).isEqualTo(TENANT_ID);
  }

  @Test
  void testPropagateTenantIdToVariableInstanceOnStartProcessInstance() {

    testRule.deployForTenant(TENANT_ID, Bpmn.createExecutableProcess(PROCESS_DEFINITION_KEY)
        .startEvent()
        .userTask()
        .endEvent()
      .done());

    VariableMap variables = Variables.putValue("var", "test");

    ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().singleResult();
    runtimeService.startProcessInstanceById(processDefinition.getId(), variables);

    VariableInstance variableInstance = runtimeService.createVariableInstanceQuery().singleResult();
    assertThat(variableInstance).isNotNull();
    // inherit the tenant id from process instance
    assertThat(variableInstance.getTenantId()).isEqualTo(TENANT_ID);
  }

  @Test
  void testPropagateTenantIdToVariableInstanceFromExecution() {

    testRule.deployForTenant(TENANT_ID, Bpmn.createExecutableProcess(PROCESS_DEFINITION_KEY)
        .startEvent()
        .serviceTask()
          .operatonClass(SetVariableTask.class.getName())
          .operatonAsyncAfter()
        .endEvent()
      .done());

    startProcessInstance(PROCESS_DEFINITION_KEY);

    VariableInstance variableInstance = runtimeService.createVariableInstanceQuery().singleResult();
    assertThat(variableInstance).isNotNull();
    // inherit the tenant id from execution
    assertThat(variableInstance.getTenantId()).isEqualTo(TENANT_ID);
  }

  @Test
  void testPropagateTenantIdToVariableInstanceFromTask() {

    testRule.deployForTenant(TENANT_ID, Bpmn.createExecutableProcess(PROCESS_DEFINITION_KEY)
        .startEvent()
        .userTask()
          .operatonAsyncAfter()
        .endEvent()
      .done());

    startProcessInstance(PROCESS_DEFINITION_KEY);

    VariableMap variables = Variables.createVariables().putValue("var", "test");
    Task task = taskService.createTaskQuery().singleResult();
    taskService.setVariablesLocal(task.getId(), variables);

    VariableInstance variableInstance = runtimeService.createVariableInstanceQuery().singleResult();
    assertThat(variableInstance).isNotNull();
    // inherit the tenant id from task
    assertThat(variableInstance.getTenantId()).isEqualTo(TENANT_ID);
  }

  @Test
  void testPropagateTenantIdToStartMessageEventSubscription() {

    testRule.deployForTenant(TENANT_ID, Bpmn.createExecutableProcess(PROCESS_DEFINITION_KEY)
        .startEvent()
          .message("start")
        .endEvent()
        .done());

    // the event subscription of the message start is created on deployment
    EventSubscription eventSubscription = runtimeService.createEventSubscriptionQuery().singleResult();
    assertThat(eventSubscription).isNotNull();
    // inherit the tenant id from process definition
    assertThat(eventSubscription.getTenantId()).isEqualTo(TENANT_ID);
  }

  @Test
  void testPropagateTenantIdToStartSignalEventSubscription() {

    testRule.deployForTenant(TENANT_ID, Bpmn.createExecutableProcess(PROCESS_DEFINITION_KEY)
      .startEvent()
        .signal("start")
      .endEvent()
      .done());

    // the event subscription of the signal start event is created on deployment
    EventSubscription eventSubscription = runtimeService.createEventSubscriptionQuery().singleResult();
    assertThat(eventSubscription).isNotNull();
    // inherit the tenant id from process definition
    assertThat(eventSubscription.getTenantId()).isEqualTo(TENANT_ID);
  }

  @Test
  void testPropagateTenantIdToIntermediateMessageEventSubscription() {

    testRule.deployForTenant(TENANT_ID, Bpmn.createExecutableProcess(PROCESS_DEFINITION_KEY)
      .startEvent()
      .intermediateCatchEvent()
        .message("start")
      .endEvent()
      .done());

    startProcessInstance(PROCESS_DEFINITION_KEY);

    EventSubscription eventSubscription = runtimeService.createEventSubscriptionQuery().singleResult();
    assertThat(eventSubscription).isNotNull();
    // inherit the tenant id from process instance
    assertThat(eventSubscription.getTenantId()).isEqualTo(TENANT_ID);
  }

  @Test
  void testPropagateTenantIdToIntermediateSignalEventSubscription() {

    testRule.deployForTenant(TENANT_ID, Bpmn.createExecutableProcess(PROCESS_DEFINITION_KEY)
        .startEvent()
        .intermediateCatchEvent()
          .signal("start")
        .endEvent()
        .done());

    startProcessInstance(PROCESS_DEFINITION_KEY);

    EventSubscription eventSubscription = runtimeService.createEventSubscriptionQuery().singleResult();
    assertThat(eventSubscription).isNotNull();
    // inherit the tenant id from process instance
    assertThat(eventSubscription.getTenantId()).isEqualTo(TENANT_ID);
  }

  @Test
  void testPropagateTenantIdToCompensationEventSubscription() {

    testRule.deployForTenant(TENANT_ID, "org/operaton/bpm/engine/test/api/multitenancy/compensationBoundaryEvent.bpmn");

    startProcessInstance(PROCESS_DEFINITION_KEY);

    // the event subscription is created after execute the activity with the attached compensation boundary event
    EventSubscription eventSubscription = runtimeService.createEventSubscriptionQuery().singleResult();
    assertThat(eventSubscription).isNotNull();
    // inherit the tenant id from process instance
    assertThat(eventSubscription.getTenantId()).isEqualTo(TENANT_ID);
  }

  @Test
  void testPropagateTenantIdToStartTimerJobDefinition() {

    testRule.deployForTenant(TENANT_ID, Bpmn.createExecutableProcess(PROCESS_DEFINITION_KEY)
        .startEvent()
          .timerWithDuration("PT1M")
        .endEvent()
        .done());

    // the job definition is created on deployment
    JobDefinition jobDefinition = managementService.createJobDefinitionQuery().singleResult();
    assertThat(jobDefinition).isNotNull();
    // inherit the tenant id from process definition
    assertThat(jobDefinition.getTenantId()).isEqualTo(TENANT_ID);
  }

  @Test
  void testPropagateTenantIdToIntermediateTimerJob() {

    testRule.deployForTenant(TENANT_ID, Bpmn.createExecutableProcess(PROCESS_DEFINITION_KEY)
        .startEvent()
        .intermediateCatchEvent()
          .timerWithDuration("PT1M")
        .endEvent()
        .done());

    startProcessInstance(PROCESS_DEFINITION_KEY);

    // the job is created when the timer event is reached
    Job job = managementService.createJobQuery().singleResult();
    assertThat(job).isNotNull();
    // inherit the tenant id from job definition
    assertThat(job.getTenantId()).isEqualTo(TENANT_ID);
  }

  @Test
  void testPropagateTenantIdToAsyncJob() {

    testRule.deployForTenant(TENANT_ID, Bpmn.createExecutableProcess(PROCESS_DEFINITION_KEY)
        .startEvent()
        .userTask()
          .operatonAsyncBefore()
        .endEvent()
      .done());

    startProcessInstance(PROCESS_DEFINITION_KEY);

    // the job is created when the asynchronous activity is reached
    Job job = managementService.createJobQuery().singleResult();
    assertThat(job).isNotNull();
    // inherit the tenant id from job definition
    assertThat(job.getTenantId()).isEqualTo(TENANT_ID);
  }

  @Test
  void testPropagateTenantIdToFailedJobIncident() {

    testRule.deployForTenant(TENANT_ID, Bpmn.createExecutableProcess(PROCESS_DEFINITION_KEY)
        .startEvent()
        .serviceTask()
          .operatonExpression("${failing}")
          .operatonAsyncBefore()
        .endEvent()
        .done());

    startProcessInstance(PROCESS_DEFINITION_KEY);

    testRule.executeAvailableJobs();

    Incident incident = runtimeService.createIncidentQuery().singleResult();
    assertThat(incident).isNotNull();
    // inherit the tenant id from execution
    assertThat(incident.getTenantId()).isEqualTo(TENANT_ID);
  }

  @Test
  void testPropagateTenantIdToFailedStartTimerIncident() {

    testRule.deployForTenant(TENANT_ID, Bpmn.createExecutableProcess(PROCESS_DEFINITION_KEY)
        .startEvent()
          .timerWithDuration("PT1M")
         .serviceTask()
           .operatonExpression("${failing}")
         .endEvent()
         .done());

    testRule.executeAvailableJobs();

    Incident incident = runtimeService.createIncidentQuery().singleResult();
    assertThat(incident).isNotNull();
    // inherit the tenant id from job
    assertThat(incident.getTenantId()).isEqualTo(TENANT_ID);
  }

  @Test
  void testPropagateTenantIdToFailedExternalTaskIncident() {

    testRule.deployForTenant(TENANT_ID, Bpmn.createExecutableProcess(PROCESS_DEFINITION_KEY)
        .startEvent()
        .serviceTask()
          .operatonType("external")
          .operatonTopic("test")
        .endEvent()
      .done());

    startProcessInstance(PROCESS_DEFINITION_KEY);

    // fetch the external task and mark it as failed which create an incident
    List<LockedExternalTask> tasks = externalTaskService.fetchAndLock(1, "test-worker").topic("test", 1000).execute();
    externalTaskService.handleFailure(tasks.get(0).getId(), "test-worker", "expected", 0, 0);

    Incident incident = runtimeService.createIncidentQuery().singleResult();
    assertThat(incident).isNotNull();
    // inherit the tenant id from execution
    assertThat(incident.getTenantId()).isEqualTo(TENANT_ID);
  }

  @Test
  void testPropagateTenantIdToExternalTask() {

    testRule.deployForTenant(TENANT_ID, Bpmn.createExecutableProcess(PROCESS_DEFINITION_KEY)
        .startEvent()
        .serviceTask()
          .operatonType("external")
          .operatonTopic("test")
        .endEvent()
      .done());

    startProcessInstance(PROCESS_DEFINITION_KEY);

    ExternalTask externalTask = externalTaskService.createExternalTaskQuery().singleResult();
    assertThat(externalTask).isNotNull();
    // inherit the tenant id from execution
    assertThat(externalTask.getTenantId()).isEqualTo(TENANT_ID);

    List<LockedExternalTask> externalTasks = externalTaskService.fetchAndLock(1, "test").topic("test", 1000).execute();
    assertThat(externalTasks).hasSize(1);
    assertThat(externalTasks.get(0).getTenantId()).isEqualTo(TENANT_ID);
  }

  @Test
  void testPropagateTenantIdToVariableInstanceOnCreateCaseInstance() {

    testRule.deployForTenant(TENANT_ID, CMMN_FILE);

    VariableMap variables = Variables.putValue("var", "test");

    CaseDefinition caseDefinition = repositoryService.createCaseDefinitionQuery().singleResult();
    caseService.createCaseInstanceById(caseDefinition.getId(), variables);

    VariableInstance variableInstance = runtimeService.createVariableInstanceQuery().singleResult();
    assertThat(variableInstance).isNotNull();
    // inherit the tenant id from case instance
    assertThat(variableInstance.getTenantId()).isEqualTo(TENANT_ID);
  }

  @Test
  void testPropagateTenantIdToVariableInstanceFromCaseExecution() {

    testRule.deployForTenant(TENANT_ID, SET_VARIABLE_CMMN_FILE);

    createCaseInstance();

    VariableInstance variableInstance = runtimeService.createVariableInstanceQuery().singleResult();
    assertThat(variableInstance).isNotNull();
    // inherit the tenant id from case execution
    assertThat(variableInstance.getTenantId()).isEqualTo(TENANT_ID);
  }

  @Test
  void testPropagateTenantIdToVariableInstanceFromHumanTask() {

    testRule.deployForTenant(TENANT_ID, CMMN_FILE);

    createCaseInstance();

    VariableMap variables = Variables.createVariables().putValue("var", "test");
    CaseExecution caseExecution = caseService.createCaseExecutionQuery().activityId("PI_HumanTask_1").singleResult();
    caseService.setVariables(caseExecution.getId(), variables);

    VariableInstance variableInstance = runtimeService.createVariableInstanceQuery().singleResult();
    assertThat(variableInstance).isNotNull();
    // inherit the tenant id from human task
    assertThat(variableInstance.getTenantId()).isEqualTo(TENANT_ID);
  }

  @Test
  void testPropagateTenantIdToTaskOnCreateCaseInstance() {
    testRule.deployForTenant(TENANT_ID, CMMN_FILE);

    CaseDefinition caseDefinition = repositoryService.createCaseDefinitionQuery().singleResult();
    caseService.createCaseInstanceById(caseDefinition.getId());

    Task task = taskService.createTaskQuery().taskName("A HumanTask").singleResult();
    assertThat(task).isNotNull();
    // inherit the tenant id from case instance
    assertThat(task.getTenantId()).isEqualTo(TENANT_ID);
  }

  public static class SetVariableTask implements JavaDelegate {
    @Override
    public void execute(DelegateExecution execution) throws Exception {
      execution.setVariable("var", "test");
    }
  }

  protected void startProcessInstance(String processDefinitionKey) {
    ProcessDefinition processDefinition = repositoryService
        .createProcessDefinitionQuery()
        .processDefinitionKey(processDefinitionKey)
        .latestVersion()
        .singleResult();

    runtimeService.startProcessInstanceById(processDefinition.getId());
  }

  protected CaseInstance createCaseInstance() {
    CaseDefinition caseDefinition = repositoryService.createCaseDefinitionQuery().singleResult();
    return caseService.createCaseInstanceById(caseDefinition.getId());
  }

}
