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
package org.operaton.bpm.engine.test.api.runtime;

import org.operaton.bpm.engine.ProcessEngineException;
import org.operaton.bpm.engine.delegate.DelegateExecution;
import org.operaton.bpm.engine.delegate.JavaDelegate;

public class FailingDelegate implements JavaDelegate {

  public static final String EXCEPTION_MESSAGE = "Expected_exception.";

  @Override
  public void execute(DelegateExecution execution) throws Exception {

    Boolean fail = (Boolean) execution.getVariable("fail");
    String message = execution.hasVariable("message") ?
        (String) execution.getVariable("message") : EXCEPTION_MESSAGE;

    if (fail == null || fail == true) {
      throw new ProcessEngineException(message);
    }

  }

}
