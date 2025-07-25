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
package org.operaton.bpm.engine.impl.pvm;

import java.io.Serializable;

import org.operaton.bpm.engine.impl.core.model.Properties;


/**
 * @author Tom Baeyens
 */
public interface PvmProcessElement extends Serializable {

  /**
   * The id of the element
   * @return the id
   */
  String getId();

  /**
   * The process definition scope, root of the scope hierarchy.
   * @return
   */
  PvmProcessDefinition getProcessDefinition();

  Object getProperty(String name);

  /**
   * Returns the properties of the element.
   *
   * @return the properties
   */
  Properties getProperties();
}
