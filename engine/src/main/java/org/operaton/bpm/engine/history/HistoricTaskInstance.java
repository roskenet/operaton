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
package org.operaton.bpm.engine.history;

import java.util.Date;


/**
 * Represents a historic task instance (waiting, finished or deleted) that is stored permanent for
 * statistics, audit and other business intelligence purposes.
 *
 * @author Tom Baeyens
 */
public interface HistoricTaskInstance {

  /**
   * The unique identifier of this historic task instance. This is the same identifier as the
   * runtime Task instance.
   */
  String getId();

  /** Process definition key reference. */
  String getProcessDefinitionKey();

  /** Process definition reference. */
  String getProcessDefinitionId();

  /** Root process instance reference. */
  String getRootProcessInstanceId();

  /** Process instance reference. */
  String getProcessInstanceId();

  /** Execution reference. */
  String getExecutionId();

  /** Case definition key reference. */
  String getCaseDefinitionKey();

  /** Case definition reference. */
  String getCaseDefinitionId();

  /** Case instance reference. */
  String getCaseInstanceId();

  /** Case execution reference. */
  String getCaseExecutionId();

  /** Activity instance reference. */
  String getActivityInstanceId();

  /** The latest name given to this task. */
  String getName();

  /** The latest description given to this task. */
  String getDescription();

  /** The reason why this task was deleted {'completed' | 'deleted' | any other user defined string }. */
  String getDeleteReason();

  /** Task owner */
  String getOwner();

  /** The latest assignee given to this task. */
  String getAssignee();

  /** Time when the task started. */
  Date getStartTime();

  /** Time when the task was deleted or completed. */
  Date getEndTime();

  /** Difference between {@link #getEndTime()} and {@link #getStartTime()} in milliseconds.  */
  Long getDurationInMillis();

  /** Task definition key. */
  String getTaskDefinitionKey();

  /** Task priority **/
  int getPriority();

  /** Task due date **/
  Date getDueDate();

  /** The parent task of this task, in case this task was a subtask */
  String getParentTaskId();

  /** Task follow-up date */
  Date getFollowUpDate();

  /**
   * The id of the tenant this historic task instance belongs to. Can be <code>null</code>
   * if the historic task instance belongs to no single tenant.
   */
  String getTenantId();

  /** The time the historic task instance will be removed. */
  Date getRemovalTime();

  /**
   * Task State also referred as lifeCycleState
   */
  String getTaskState();
}
