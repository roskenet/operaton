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
package org.operaton.bpm.engine.cdi;

import org.operaton.bpm.engine.delegate.ExecutionListener;
import org.operaton.bpm.engine.delegate.TaskListener;

/**
 * The type of a business process event. Indicates what is happening/has
 * happened, i.e. whether a transition is taken, an activity is entered or left,
 * a task is created, assigned, completed or deleted.
 *
 * @author Daniel Meyer
 */
public interface BusinessProcessEventType {

  /** Signifies that a transition is being taken / was taken **/
  BusinessProcessEventType TAKE = new DefaultBusinessProcessEventType(ExecutionListener.EVENTNAME_TAKE);

  /** Signifies that an activity is being entered / war entered **/
  BusinessProcessEventType START_ACTIVITY = new DefaultBusinessProcessEventType(ExecutionListener.EVENTNAME_START);

  /** Signifies that an activity is being left / was left **/
  BusinessProcessEventType END_ACTIVITY = new DefaultBusinessProcessEventType(ExecutionListener.EVENTNAME_END);

  /** Signifies that a task is created **/
  BusinessProcessEventType CREATE_TASK = new DefaultBusinessProcessEventType(TaskListener.EVENTNAME_CREATE);

  /** Signifies that a task is assigned **/
  BusinessProcessEventType ASSIGN_TASK = new DefaultBusinessProcessEventType(TaskListener.EVENTNAME_ASSIGNMENT);

  /** Signifies that a task is completed **/
  BusinessProcessEventType COMPLETE_TASK = new DefaultBusinessProcessEventType(TaskListener.EVENTNAME_COMPLETE);

  /** Signifies that a task is updated **/
  BusinessProcessEventType UPDATE_TASK = new DefaultBusinessProcessEventType(TaskListener.EVENTNAME_UPDATE);

  /** Signifies that a task is deleted **/
  BusinessProcessEventType DELETE_TASK = new DefaultBusinessProcessEventType(TaskListener.EVENTNAME_DELETE);

  class DefaultBusinessProcessEventType implements BusinessProcessEventType {

    protected final String typeName;

    public DefaultBusinessProcessEventType(String typeName) {
      this.typeName = typeName;
    }

    @Override
    public String getTypeName() {
      return typeName;
    }

    @Override
    public String toString() {
      return typeName;
    }

  }

  String getTypeName();

}
