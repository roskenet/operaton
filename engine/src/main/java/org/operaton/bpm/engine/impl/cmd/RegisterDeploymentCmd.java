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

import org.operaton.bpm.engine.impl.cfg.CommandChecker;
import org.operaton.bpm.engine.impl.context.Context;
import org.operaton.bpm.engine.impl.interceptor.Command;
import org.operaton.bpm.engine.impl.interceptor.CommandContext;
import org.operaton.bpm.engine.repository.Deployment;

/**
 * @author Thorben Lindhauer
 */
public class RegisterDeploymentCmd implements Command<Void> {

  protected String deploymentId;

  public RegisterDeploymentCmd(String deploymentId) {
    this.deploymentId = deploymentId;
  }

  @Override
  public Void execute(CommandContext commandContext) {
    Deployment deployment = commandContext.getDeploymentManager().findDeploymentById(deploymentId);

    ensureNotNull("Deployment " + deploymentId + " does not exist", "deployment", deployment);

    commandContext.getAuthorizationManager().checkOperatonAdminOrPermission(CommandChecker::checkRegisterDeployment);

    Context.getProcessEngineConfiguration().getRegisteredDeployments().add(deploymentId);
    return null;
  }

}
