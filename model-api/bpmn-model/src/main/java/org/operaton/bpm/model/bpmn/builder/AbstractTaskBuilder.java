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
package org.operaton.bpm.model.bpmn.builder;

import org.operaton.bpm.model.bpmn.BpmnModelInstance;
import org.operaton.bpm.model.bpmn.instance.Task;

/**
 * @author Sebastian Menski
 */
public abstract class AbstractTaskBuilder<B extends AbstractTaskBuilder<B, E>, E extends Task> extends AbstractActivityBuilder<B, E> {

  protected AbstractTaskBuilder(BpmnModelInstance modelInstance, E element, Class<?> selfType) {
    super(modelInstance, element, selfType);
  }

  /** operaton extensions */

  /**
   * Sets the operaton async attribute to true.
   *
   * @deprecated Use {@link #operatonAsyncBefore(boolean)} instead.
   * @return the builder object
   */
  @Deprecated(forRemoval = true, since = "1.0")
  public B operatonAsync() {
    element.setOperatonAsyncBefore(true);
    return myself;
  }

  /**
   * Sets the operaton async attribute.
   *
   * @deprecated Use {@link #operatonAsyncBefore(boolean)} instead.
   * @param isOperatonAsync the async state of the task
   * @return the builder object
   */
  @Deprecated(forRemoval = true, since = "1.0")
  public B operatonAsync(boolean isOperatonAsync) {
    element.setOperatonAsyncBefore(isOperatonAsync);
    return myself;
  }

}
