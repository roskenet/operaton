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
package org.operaton.bpm.qa.upgrade.scenarios720.compensation;

import static org.operaton.bpm.qa.upgrade.util.ActivityInstanceAssert.assertThat;
import static org.operaton.bpm.qa.upgrade.util.ActivityInstanceAssert.describeActivityInstanceTree;

import org.operaton.bpm.engine.runtime.ActivityInstance;
import org.operaton.bpm.engine.runtime.ProcessInstance;
import org.operaton.bpm.engine.task.Task;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Thorben Lindhauer
 *
 */
@ScenarioUnderTest("SubprocessParallelThrowCompensationScenario")
@Origin("7.2.0")
public class SubprocessParallelThrowCompensationScenarioTest {

  @Rule
  public UpgradeTestRule rule = new UpgradeTestRule();

  @Test
  @ScenarioUnderTest("init.1")
  public void testInitCompletion() {
    // given
    Task beforeCompensationTask = rule.taskQuery().taskDefinitionKey("beforeCompensate").singleResult();
    Task concurrentTask = rule.taskQuery().taskDefinitionKey("concurrentTask").singleResult();

    // when compensation is thrown
    rule.getTaskService().complete(beforeCompensationTask.getId());

    // then there is an active compensation handler task
    Task compensationHandlerTask = rule.taskQuery().taskDefinitionKey("undoTask").singleResult();
    Assert.assertNotNull(compensationHandlerTask);

    // and it can be completed such that the process instance ends successfully
    rule.getTaskService().complete(compensationHandlerTask.getId());

    Task afterCompensateTask = rule.taskQuery().taskDefinitionKey("afterCompensate").singleResult();
    Assert.assertNotNull(afterCompensateTask);

    rule.getTaskService().complete(afterCompensateTask.getId());
    rule.getTaskService().complete(concurrentTask.getId());

    rule.assertScenarioEnded();
  }

  @Test
  @ScenarioUnderTest("init.2")
  public void testInitDeletion() {
    // when compensation is thrown
    ProcessInstance instance = rule.processInstance();

    Task beforeCompensationTask = rule.taskQuery().taskDefinitionKey("beforeCompensate").singleResult();
    rule.getTaskService().complete(beforeCompensationTask.getId());

    // then the process instance can be deleted
    rule.getRuntimeService().deleteProcessInstance(instance.getId(), "");

    // and the process is ended
    rule.assertScenarioEnded();
  }

  @Test
  @ScenarioUnderTest("init.3")
  public void testInitActivityInstanceTree() {
    // given
    ProcessInstance instance = rule.processInstance();

    // when compensation is thrown
    Task beforeCompensationTask = rule.taskQuery().taskDefinitionKey("beforeCompensate").singleResult();
    rule.getTaskService().complete(beforeCompensationTask.getId());

    // then the activity instance tree is meaningful
    ActivityInstance activityInstance = rule.getRuntimeService().getActivityInstance(instance.getId());
    Assert.assertNotNull(activityInstance);
    assertThat(activityInstance).hasStructure(
      describeActivityInstanceTree(instance.getProcessDefinitionId())
        .activity("concurrentTask")
        .activity("throwCompensate")
        .beginScope("subProcess")
          .activity("undoTask")
      .done());
  }

  @Test
  @ScenarioUnderTest("init.triggerCompensation.1")
  public void testInitTriggerCompensationCompletion() {
    // given active compensation
    Task compensationHandlerTask = rule.taskQuery().taskDefinitionKey("undoTask").singleResult();
    Task concurrentTask = rule.taskQuery().taskDefinitionKey("concurrentTask").singleResult();

    // then it is possible to complete compensation and the follow-up task, as well as the concurrent task
    rule.getTaskService().complete(compensationHandlerTask.getId());

    Task afterCompensateTask = rule.taskQuery().taskDefinitionKey("afterCompensate").singleResult();
    Assert.assertNotNull(afterCompensateTask);

    rule.getTaskService().complete(afterCompensateTask.getId());
    rule.getTaskService().complete(concurrentTask.getId());

    rule.assertScenarioEnded();
  }

  @Test
  @ScenarioUnderTest("init.triggerCompensation.2")
  public void testInitTriggerCompensationDeletion() {
    // given active compensation
    ProcessInstance instance = rule.processInstance();

    // then the process instance can be deleted
    rule.getRuntimeService().deleteProcessInstance(instance.getId(), "");

    // and the process is ended
    rule.assertScenarioEnded();
  }

  @Test
  @ScenarioUnderTest("init.triggerCompensation.3")
  public void testInitTriggerCompensationActivityInstanceTree() {
    // given active compensation
    ProcessInstance instance = rule.processInstance();

    // then the activity instance tree is meaningful
    ActivityInstance activityInstance = rule.getRuntimeService().getActivityInstance(instance.getId());
    Assert.assertNotNull(activityInstance);
    assertThat(activityInstance).hasStructure(
      describeActivityInstanceTree(instance.getProcessDefinitionId())
        .activity("concurrentTask")
        .beginScope("throwCompensate")
        // due to different activity instance id assingment in >= 7.4,
        // the subProcess instance is not represented and undoTask is a child of throwCompensate
//        .beginScope("subProcess")
          .activity("undoTask")
      .done());
  }
}

