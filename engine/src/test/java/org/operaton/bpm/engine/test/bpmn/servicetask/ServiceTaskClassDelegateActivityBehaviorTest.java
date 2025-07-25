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
package org.operaton.bpm.engine.test.bpmn.servicetask;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.util.Collections;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.operaton.bpm.engine.ProcessEngineException;
import org.operaton.bpm.engine.RuntimeService;
import org.operaton.bpm.engine.exception.NullValueException;
import org.operaton.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.operaton.bpm.engine.test.Deployment;
import org.operaton.bpm.engine.test.junit5.ProcessEngineExtension;

/**
 * @author Ronny Bräunlich
 *
 */
@ExtendWith(ProcessEngineExtension.class)
class ServiceTaskClassDelegateActivityBehaviorTest {

  ProcessEngineConfigurationImpl processEngineConfiguration;
  RuntimeService runtimeService;

  @Deployment
  @Test
  void testExceptionThrownBySecondScopeServiceTaskIsNotHandled() {
    Map<Object, Object> beans = processEngineConfiguration.getBeans();
    beans.put("dummyServiceTask", new DummyServiceTask());
    processEngineConfiguration.setBeans(beans);
    var variables = Collections.<String, Object> singletonMap("count", 0);
    try {
      runtimeService.startProcessInstanceByKey("process", variables);
      fail("");
    } // since the NVE extends the ProcessEngineException we have to handle it
      // separately
    catch (NullValueException nve) {
      fail("Shouldn't have received NullValueException");
    } catch (ProcessEngineException e) {
      assertThat(e.getMessage()).contains("Invalid format");
    }
  }

}
