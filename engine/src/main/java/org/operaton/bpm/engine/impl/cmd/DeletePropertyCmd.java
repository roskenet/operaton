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

import java.util.Collections;

import org.operaton.bpm.engine.history.UserOperationLogEntry;
import org.operaton.bpm.engine.impl.cfg.CommandChecker;
import org.operaton.bpm.engine.impl.interceptor.Command;
import org.operaton.bpm.engine.impl.interceptor.CommandContext;
import org.operaton.bpm.engine.impl.persistence.entity.PropertyChange;
import org.operaton.bpm.engine.impl.persistence.entity.PropertyEntity;
import org.operaton.bpm.engine.impl.persistence.entity.PropertyManager;

/**
 * @author Daniel Meyer
 *
 */
public class DeletePropertyCmd implements Command<Object> {

  protected String name;

  /**
   * @param name
   */
  public DeletePropertyCmd(String name) {
    this.name = name;
  }

  @Override
  public Object execute(CommandContext commandContext) {
    commandContext.getAuthorizationManager().checkOperatonAdminOrPermission(CommandChecker::checkDeleteProperty);

    final PropertyManager propertyManager = commandContext.getPropertyManager();

    PropertyEntity propertyEntity = propertyManager.findPropertyById(name);

    if(propertyEntity != null) {
      propertyManager.delete(propertyEntity);

      commandContext.getOperationLogManager().logPropertyOperation(UserOperationLogEntry.OPERATION_TYPE_DELETE,
          Collections.singletonList(new PropertyChange("name", null, name)));
    }

    return null;
  }

}
