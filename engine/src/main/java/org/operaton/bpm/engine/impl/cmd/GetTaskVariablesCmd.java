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
package org.operaton.bpm.engine.impl.cmd;

import static org.operaton.bpm.engine.impl.util.EnsureUtil.ensureNotNull;

import java.io.Serializable;
import java.util.Collection;

import org.operaton.bpm.engine.impl.cfg.CommandChecker;
import org.operaton.bpm.engine.impl.context.Context;
import org.operaton.bpm.engine.impl.interceptor.Command;
import org.operaton.bpm.engine.impl.interceptor.CommandContext;
import org.operaton.bpm.engine.impl.persistence.entity.TaskEntity;
import org.operaton.bpm.engine.variable.VariableMap;
import org.operaton.bpm.engine.variable.impl.VariableMapImpl;


/**
 * @author Tom Baeyens
 */
public class GetTaskVariablesCmd implements Command<VariableMap>, Serializable {

  private static final long serialVersionUID = 1L;
  protected String taskId;
  protected Collection<String> variableNames;
  protected boolean isLocal;
  protected boolean deserializeValues;

  public GetTaskVariablesCmd(String taskId, Collection<String> variableNames, boolean isLocal, boolean deserializeValues) {
    this.taskId = taskId;
    this.variableNames = variableNames;
    this.isLocal = isLocal;
    this.deserializeValues = deserializeValues;
  }

  @Override
  public VariableMap execute(CommandContext commandContext) {
    ensureNotNull("taskId", taskId);

    TaskEntity task = Context
      .getCommandContext()
      .getTaskManager()
      .findTaskById(taskId);

    ensureNotNull("task " + taskId + " doesn't exist", "task", task);

    checkGetTaskVariables(task, commandContext);

    VariableMapImpl variables = new VariableMapImpl();

    // collect variables from task
    task.collectVariables(variables, variableNames, isLocal, deserializeValues);

    return variables;

  }

  protected void checkGetTaskVariables(TaskEntity task, CommandContext commandContext) {
    for(CommandChecker checker : commandContext.getProcessEngineConfiguration().getCommandCheckers()) {
      checker.checkReadTaskVariable(task);
    }
  }
}
