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
package org.operaton.bpm.engine.impl.pvm.runtime.operation;

import org.operaton.bpm.engine.delegate.ExecutionListener;
import org.operaton.bpm.engine.impl.pvm.process.ScopeImpl;
import org.operaton.bpm.engine.impl.pvm.runtime.LegacyBehavior;
import org.operaton.bpm.engine.impl.pvm.runtime.PvmExecutionImpl;


/**
 * @author Tom Baeyens
 * @author Daniel Meyer
 */
public class PvmAtomicOperationProcessStart extends AbstractPvmEventAtomicOperation {

  @Override
  public boolean isAsync(PvmExecutionImpl execution) {
    return execution.getActivity().isAsyncBefore();
  }

  @Override
  public boolean isAsyncCapable() {
    return true;
  }

  @Override
  protected ScopeImpl getScope(PvmExecutionImpl execution) {
    return execution.getProcessDefinition();
  }

  protected String getEventName() {
    return ExecutionListener.EVENTNAME_START;
  }

  @Override
  protected PvmExecutionImpl eventNotificationsStarted(PvmExecutionImpl execution) {

    // restoring the starting flag in case this operation is executed
    // asynchronously
    execution.setProcessInstanceStarting(true);

    if (execution.getActivity() != null && execution.getActivity().isAsyncBefore()) {
      LegacyBehavior.createMissingHistoricVariables(execution);
    }

    return execution;
  }

  protected void eventNotificationsCompleted(PvmExecutionImpl execution) {

    execution.continueIfExecutionDoesNotAffectNextOperation(execution1 -> {
      execution1.dispatchEvent(null);
      return null;
    }, execution2 -> {

      execution2.setIgnoreAsync(true);
      execution2.performOperation(ACTIVITY_START_CREATE_SCOPE);

      return null;
    }, execution);

  }

  @Override
  public String getCanonicalName() {
    return "process-start";
  }

}
