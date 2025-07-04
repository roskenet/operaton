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
package org.operaton.bpm.engine;

import java.io.InputStream;
import java.util.Collection;
import java.util.Map;

import org.operaton.bpm.engine.authorization.Permissions;
import org.operaton.bpm.engine.authorization.Resources;
import org.operaton.bpm.engine.form.StartFormData;
import org.operaton.bpm.engine.form.TaskFormData;
import org.operaton.bpm.engine.runtime.ProcessInstance;
import org.operaton.bpm.engine.runtime.ProcessInstanceQuery;
import org.operaton.bpm.engine.task.Task;
import org.operaton.bpm.engine.task.TaskQuery;
import org.operaton.bpm.engine.variable.VariableMap;
import org.operaton.bpm.engine.variable.value.SerializableValue;


/** Access to form data and rendered forms for starting new process instances and completing tasks.
 *
 * @author Tom Baeyens
 * @author Falko Menge (operaton)
 */
public interface FormService {

  /**
   * Retrieves all data necessary for rendering a form to start a new process instance.
   * This can be used to perform rendering of the forms outside of the process engine.
   *
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#READ} permission on {@link Resources#PROCESS_DEFINITION}.
   */
  StartFormData getStartFormData(String processDefinitionId);

  /**
   * Rendered form generated by the default build-in form engine for starting a new process instance.
   *
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#READ} permission on {@link Resources#PROCESS_DEFINITION}.
   */
  Object getRenderedStartForm(String processDefinitionId);

  /**
   * Rendered form generated by the given build-in form engine for starting a new process instance.
   *
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#READ} permission on {@link Resources#PROCESS_DEFINITION}.
   */
  Object getRenderedStartForm(String processDefinitionId, String formEngineName);

  /**
   * @deprecated Use {@link #submitStartForm(String, Map)} instead.
   * */
  @Deprecated(forRemoval = true, since = "1.0")
  default ProcessInstance submitStartFormData(String processDefinitionId, Map<String, String> properties) {
    return submitStartForm(processDefinitionId, (Map) properties);
  }

  /**
   * Start a new process instance with the user data that was entered as properties in a start form.
   *
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#CREATE} permission on {@link Resources#PROCESS_INSTANCE}
   *          and no {@link Permissions#CREATE_INSTANCE} permission on {@link Resources#PROCESS_DEFINITION}.
   */
  ProcessInstance submitStartForm(String processDefinitionId, Map<String, Object> properties);

  /**
   * @deprecated Use {@link #submitStartForm(String, String, Map)} instead.
   */
  @Deprecated(forRemoval = true, since = "1.0")
  default ProcessInstance submitStartFormData(String processDefinitionId, String businessKey,
                                         Map<String, String> properties) {
    return submitStartForm(processDefinitionId, businessKey, (Map) properties);
  }

  /**
   * Start a new process instance with the user data that was entered as properties in a start form.
   *
   * A business key can be provided to associate the process instance with a
   * certain identifier that has a clear business meaning. For example in an
   * order process, the business key could be an order id. This business key can
   * then be used to easily look up that process instance , see
   * {@link ProcessInstanceQuery#processInstanceBusinessKey(String)}. Providing such a business
   * key is definitely a best practice.
   *
   * Note that a business key MUST be unique for the given process definition.
   * Process instance from different process definition are allowed to have the
   * same business key.
   *
   * @param processDefinitionId the id of the process definition, cannot be null.
   * @param businessKey a key that uniquely identifies the process instance in the context or the
   *                    given process definition.
   * @param properties the properties to pass, can be null.
   *
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#CREATE} permission on {@link Resources#PROCESS_INSTANCE}
   *          and no {@link Permissions#CREATE_INSTANCE} permission on {@link Resources#PROCESS_DEFINITION}.
   */
  ProcessInstance submitStartForm(String processDefinitionId, String businessKey, Map<String, Object> properties);

  /**
   * Retrieves all data necessary for rendering a form to complete a task.
   * This can be used to perform rendering of the forms outside of the process engine.
   *
   * @throws AuthorizationException
   *          <p>In case of standalone tasks:
   *          <li>if the user has no {@link Permissions#READ} permission on {@link Resources#TASK} or</li>
   *          <li>if the user has no {@link TaskPermissions#READ_VARIABLE} permission on {@link Resources#TASK}</li></p>
   *          <p>In case the task is part of a running process instance:</li>
   *          <li>if the user has no {@link Permissions#READ} permission on {@link Resources#TASK} or
   *           no {@link Permissions#READ_TASK} permission on {@link Resources#PROCESS_DEFINITION} </li>
   *          <li>if the user has {@link TaskPermissions#READ_VARIABLE} permission on {@link Resources#TASK} or
   *          no {@link ProcessDefinitionPermissions#READ_TASK_VARIABLE} permission on {@link Resources#PROCESS_DEFINITION}
   *          when {@link ProcessEngineConfiguration#enforceSpecificVariablePermission this} config is enabled</li>
   *          </ul></p>
   */
  TaskFormData getTaskFormData(String taskId);

  /**
   * Rendered form generated by the default build-in form engine for completing a task.
   *
   * @throws AuthorizationException
   *          <p>In case of standalone tasks:
   *          <li>if the user has no {@link Permissions#READ} permission on {@link Resources#TASK} or</li>
   *          <li>if the user has no {@link TaskPermissions#READ_VARIABLE} permission on {@link Resources#TASK}
   *          when {@link ProcessEngineConfiguration#enforceSpecificVariablePermission this} config is enabled</li></p>
   *          <p>In case the task is part of a running process instance:</li>
   *          <li>if the user has no {@link Permissions#READ} permission on {@link Resources#TASK} or
   *           no {@link Permissions#READ_TASK} permission on {@link Resources#PROCESS_DEFINITION} </li>
   *          <li>if the user has {@link TaskPermissions#READ_VARIABLE} permission on {@link Resources#TASK} or
   *          no {@link ProcessDefinitionPermissions#READ_TASK_VARIABLE} permission on {@link Resources#PROCESS_DEFINITION}
   *          when {@link ProcessEngineConfiguration#enforceSpecificVariablePermission this} config is enabled</li></p>
   */
  Object getRenderedTaskForm(String taskId);

  /**
   * Rendered form generated by the given build-in form engine for completing a task.
   *
   * @throws AuthorizationException
   *          <p>In case of standalone tasks:
   *          <li>if the user has no {@link Permissions#READ} permission on {@link Resources#TASK} or</li>
   *          <li>if the user has no {@link TaskPermissions#READ_VARIABLE} permission on {@link Resources#TASK}
   *          when {@link ProcessEngineConfiguration#enforceSpecificVariablePermission this} config is enabled</li></p>
   *          <p>In case the task is part of a running process instance:</li>
   *          <li>if the user has no {@link Permissions#READ} permission on {@link Resources#TASK} or
   *           no {@link Permissions#READ_TASK} permission on {@link Resources#PROCESS_DEFINITION} </li>
   *          <li>if the user has {@link TaskPermissions#READ_VARIABLE} permission on {@link Resources#TASK} or
   *          no {@link ProcessDefinitionPermissions#READ_TASK_VARIABLE} permission on {@link Resources#PROCESS_DEFINITION}
   *          when {@link ProcessEngineConfiguration#enforceSpecificVariablePermission this} config is enabled</li></p>
   */
  Object getRenderedTaskForm(String taskId, String formEngineName);

  /**
   * @deprecated Use {@link #submitTaskForm(String, Map)} instead.
   */
  @Deprecated(forRemoval = true, since = "1.0")
  default void submitTaskFormData(String taskId, Map<String, String> properties) {
    submitTaskForm(taskId, (Map) properties);
  }

  /**
   * Completes a task with the user data that was entered as properties in a task form.
   *
   * @param taskId
   * @param properties
   *
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#UPDATE} permission on {@link Resources#TASK}
   *          or no {@link Permissions#UPDATE_TASK} permission on {@link Resources#PROCESS_DEFINITION}.
   */
  void submitTaskForm(String taskId, Map<String, Object> properties);

  /**
   * Completes a task with the user data that was entered as properties in a task form.
   *
   * @param taskId
   * @param properties
   * @param deserializeValues if false, returned {@link SerializableValue}s
   *   will not be deserialized (unless they are passed into this method as a
   *   deserialized value or if the BPMN process triggers deserialization)
   * @return a map of process variables
   *
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#UPDATE} permission on {@link Resources#TASK}
   *          or no {@link Permissions#UPDATE_TASK} permission on {@link Resources#PROCESS_DEFINITION}.
   */
  VariableMap submitTaskFormWithVariablesInReturn(String taskId, Map<String, Object> properties, boolean deserializeValues);

  /**
   * Retrieves a list of all variables for rendering a start from. The method takes into account
   * FormData specified for the start event. This allows defining default values for form fields.
   *
   * @param processDefinitionId the id of the process definition for which the start form should be retrieved.
   * @return a map of VariableInstances.
   *
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#READ} permission on {@link Resources#PROCESS_DEFINITION}.
   */
  VariableMap getStartFormVariables(String processDefinitionId);

  /**
   * Retrieves a list of requested variables for rendering a start from. The method takes into account
   * FormData specified for the start event. This allows defining default values for form fields.
   *
   * @param processDefinitionId the id of the process definition for which the start form should be retrieved.
   * @param formVariables a Collection of the names of the variables to retrieve. Allows restricting the set of retrieved variables.
   * @param deserializeObjectValues if false object values are not deserialized
   * @return a map of VariableInstances.
   *
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#READ} permission on {@link Resources#PROCESS_DEFINITION}.
   */
  VariableMap getStartFormVariables(String processDefinitionId, Collection<String> formVariables, boolean deserializeObjectValues);

  /**
   * <p>Retrieves a list of all variables for rendering a task form. In addition to the task variables and process variables,
   * the method takes into account FormData specified for the task. This allows defining default values for form fields.</p>
   *
   * <p>A variable is resolved in the following order:
   * <ul>
   *   <li>First, the method collects all form fields and creates variable instances for the form fields.</li>
   *   <li>Next, the task variables are collected.</li>
   *   <li>Next, process variables from the parent scopes of the task are collected, until the process instance scope is reached.</li>
   * </ul>
   * </p>
   *
   * @param taskId the id of the task for which the variables should be retrieved.
   * @return a map of VariableInstances.
   *
   * @throws AuthorizationException
   *          <p>In case of standalone tasks:
   *          <li>if the user has no {@link Permissions#READ} permission on {@link Resources#TASK} or</li>
   *          <li>if the user has no {@link TaskPermissions#READ_VARIABLE} permission on {@link Resources#TASK}
   *          when {@link ProcessEngineConfiguration#enforceSpecificVariablePermission this} config is enabled</li></p>
   *          <p>In case the task is part of a running process instance:</li>
   *          <li>if the user has no {@link Permissions#READ} permission on {@link Resources#TASK} or
   *           no {@link Permissions#READ_TASK} permission on {@link Resources#PROCESS_DEFINITION} </li>
   *          <li>if the user has {@link TaskPermissions#READ_VARIABLE} permission on {@link Resources#TASK} or
   *          no {@link ProcessDefinitionPermissions#READ_TASK_VARIABLE} permission on {@link Resources#PROCESS_DEFINITION}
   *          when {@link ProcessEngineConfiguration#enforceSpecificVariablePermission this} config is enabled</li></p>
   */
  VariableMap getTaskFormVariables(String taskId);

  /**
   * <p>Retrieves a list of requested variables for rendering a task form. In addition to the task variables and process variables,
   * the method takes into account FormData specified for the task. This allows defining default values for form fields.</p>
   *
   * <p>A variable is resolved in the following order:
   * <ul>
   *   <li>First, the method collects all form fields and creates variable instances for the form fields.</li>
   *   <li>Next, the task variables are collected.</li>
   *   <li>Next, process variables from the parent scopes of the task are collected, until the process instance scope is reached.</li>
   * </ul>
   * </p>
   *
   * @param taskId the id of the task for which the variables should be retrieved.
   * @param formVariables a Collection of the names of the variables to retrieve. Allows restricting the set of retrieved variables.
   * @param deserializeObjectValues if false object values are not deserialized
   * @return a map of VariableInstances.
   *
   * @throws AuthorizationException
   *          <p>In case of standalone tasks:
   *          <li>if the user has no {@link Permissions#READ} permission on {@link Resources#TASK} or</li>
   *          <li>if the user has no {@link TaskPermissions#READ_VARIABLE} permission on {@link Resources#TASK}
   *          when {@link ProcessEngineConfiguration#enforceSpecificVariablePermission this} config is enabled</li></p>
   *          <p>In case the task is part of a running process instance:</li>
   *          <li>if the user has no {@link Permissions#READ} permission on {@link Resources#TASK} or
   *           no {@link Permissions#READ_TASK} permission on {@link Resources#PROCESS_DEFINITION} </li>
   *          <li>if the user has {@link TaskPermissions#READ_VARIABLE} permission on {@link Resources#TASK} or
   *          no {@link ProcessDefinitionPermissions#READ_TASK_VARIABLE} permission on {@link Resources#PROCESS_DEFINITION}
   *          when {@link ProcessEngineConfiguration#enforceSpecificVariablePermission this} config is enabled</li></p>
   */
  VariableMap getTaskFormVariables(String taskId, Collection<String> formVariables, boolean deserializeObjectValues);

  /**
   * Retrieves a user defined reference to a start form.
   *
   * In the Explorer app, it is assumed that the form key specifies a resource
   * in the deployment, which is the template for the form.  But users are free
   * to use this property differently.
   *
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#READ} permission on {@link Resources#PROCESS_DEFINITION}.
   */
  String getStartFormKey(String processDefinitionId);

  /**
   * Retrieves a user defined reference to a task form.
   *
   * In the Explorer app, it is assumed that the form key specifies a resource
   * in the deployment, which is the template for the form.  But users are free
   * to use this property differently.
   *
   * Both arguments can be obtained from {@link Task} instances returned by any
   * {@link TaskQuery}.
   *
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#READ} permission on {@link Resources#PROCESS_DEFINITION}.
   */
  String getTaskFormKey(String processDefinitionId, String taskDefinitionKey);

  /**
   * Retrieves a deployed start form for a process definition with a given id.
   *
   *
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#READ} permission on {@link Resources#PROCESS_DEFINITION}.
   * @throws NotFoundException
   *          If the start form cannot be found.
   * @throws BadUserRequestException
   *          If the start form key has wrong format ("embedded:deployment:<path>" or "deployment:<path>" required).
   */
  InputStream getDeployedStartForm(String processDefinitionId);

  /**
   * Retrieves a deployed task form for a task with a given id.
   *
   *
   * @throws AuthorizationException
   *          If the user has no {@link Permissions#READ} permission on {@link Resources#TASK}.
   * @throws NotFoundException
   *          If the task form cannot be found.
   * @throws BadUserRequestException
   *          If the task form key has wrong format ("embedded:deployment:<path>" or "deployment:<path>" required).
   */
  InputStream getDeployedTaskForm(String taskId);

}
