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
package org.operaton.bpm.client.task;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.operaton.bpm.client.ExternalTaskClient;
import org.operaton.bpm.client.dto.ProcessDefinitionDto;
import org.operaton.bpm.client.dto.ProcessInstanceDto;
import org.operaton.bpm.client.rule.ClientRule;
import org.operaton.bpm.client.rule.EngineRule;
import org.operaton.bpm.client.util.ProcessModels;
import org.operaton.bpm.client.util.RecordingExternalTaskHandler;
import org.operaton.bpm.model.bpmn.Bpmn;
import org.operaton.bpm.model.bpmn.BpmnModelInstance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

public class ExternalTaskCustomExtensionPropertiesIT {

  protected BpmnModelInstance externalTaskProcess;

  @RegisterExtension
  static ClientRule clientRule = new ClientRule();
  @RegisterExtension
  static EngineRule engineRule = new EngineRule();

  protected ExternalTaskClient client;

  protected ProcessDefinitionDto processDefinition;
  protected ProcessInstanceDto processInstance;

  protected RecordingExternalTaskHandler handler = new RecordingExternalTaskHandler();

  @BeforeEach
  public void setup() {
    client = clientRule.client();
    handler.clear();
  }

  @Test
  public void shouldReceiveCustomExtensionProperties() {
    // given
    externalTaskProcess = Bpmn.readModelFromStream(
        getClass().getClassLoader().getResourceAsStream("model/ExternalTaskCustomExtensionPropertiesIT.oneExternalTaskWithCustomProperties.bpmn20.xml"));
    processDefinition = engineRule.deploy(externalTaskProcess).get(0);
    engineRule.startProcessInstance(processDefinition.getId());

    // when
    client.subscribe(ProcessModels.EXTERNAL_TASK_TOPIC_FOO).includeExtensionProperties(true).handler(handler).open();

    // then
    clientRule.waitForFetchAndLockUntil(() -> !handler.getHandledTasks().isEmpty());
    ExternalTask task = handler.getHandledTasks().get(0);

    assertThat(task.getExtensionProperties()).containsOnly(entry("property1", "value1"), entry("property2", "value2"), entry("property3", "value3"));
    assertThat(task.getExtensionProperty("property1")).isEqualTo("value1");
    assertThat(task.getExtensionProperty("property2")).isEqualTo("value2");
    assertThat(task.getExtensionProperty("property3")).isEqualTo("value3");
    assertThat(task.getExtensionProperty("property4")).isNull();
  }

  @Test
  public void shouldReceiveEmptyCustomExtensionProperties() {
    // given
    externalTaskProcess = Bpmn.readModelFromStream(
        getClass().getClassLoader().getResourceAsStream("model/ExternalTaskCustomExtensionPropertiesIT.oneExternalTaskWithoutCustomProperties.bpmn20.xml"));
    processDefinition = engineRule.deploy(externalTaskProcess).get(0);
    engineRule.startProcessInstance(processDefinition.getId());

    // when
    client.subscribe(ProcessModels.EXTERNAL_TASK_TOPIC_FOO).includeExtensionProperties(true).handler(handler).open();

    // then
    clientRule.waitForFetchAndLockUntil(() -> !handler.getHandledTasks().isEmpty());
    ExternalTask task = handler.getHandledTasks().get(0);

    assertThat(task.getExtensionProperties()).isEmpty();
  }

  @Test
  public void shouldNotReceiveCustomExtensionProperties() {
    // given
    externalTaskProcess = Bpmn.readModelFromStream(
        getClass().getClassLoader().getResourceAsStream("model/ExternalTaskCustomExtensionPropertiesIT.oneExternalTaskWithCustomProperties.bpmn20.xml"));
    processDefinition = engineRule.deploy(externalTaskProcess).get(0);
    engineRule.startProcessInstance(processDefinition.getId());

    // when
    client.subscribe(ProcessModels.EXTERNAL_TASK_TOPIC_FOO).handler(handler).open();

    // then
    clientRule.waitForFetchAndLockUntil(() -> !handler.getHandledTasks().isEmpty());
    ExternalTask task = handler.getHandledTasks().get(0);

    assertThat(task.getExtensionProperties()).isEmpty();
  }
}
