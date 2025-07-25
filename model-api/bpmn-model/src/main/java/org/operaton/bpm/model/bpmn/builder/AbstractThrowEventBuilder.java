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
import org.operaton.bpm.model.bpmn.instance.CompensateEventDefinition;
import org.operaton.bpm.model.bpmn.instance.EscalationEventDefinition;
import org.operaton.bpm.model.bpmn.instance.MessageEventDefinition;
import org.operaton.bpm.model.bpmn.instance.SignalEventDefinition;
import org.operaton.bpm.model.bpmn.instance.ThrowEvent;

/**
 * @author Sebastian Menski
 */
public abstract class AbstractThrowEventBuilder<B extends AbstractThrowEventBuilder<B, E>, E extends ThrowEvent> extends AbstractEventBuilder<B, E> {

  protected AbstractThrowEventBuilder(BpmnModelInstance modelInstance, E element, Class<?> selfType) {
    super(modelInstance, element, selfType);
  }

  /**
   * Sets an event definition for the given message name. If already a message
   * with this name exists it will be used, otherwise a new message is created.
   *
   * @param messageName the name of the message
   * @return the builder object
   */
  public B message(String messageName) {
    MessageEventDefinition messageEventDefinition = createMessageEventDefinition(messageName);
    element.getEventDefinitions().add(messageEventDefinition);

    return myself;
  }

  /**
   * Creates an empty message event definition with an unique id
   * and returns a builder for the message event definition.
   *
   * @return the message event definition builder object
   */
  public MessageEventDefinitionBuilder messageEventDefinition() {
    return messageEventDefinition(null);
  }

  /**
   * Creates an empty message event definition with the given id
   * and returns a builder for the message event definition.
   *
   * @param id the id of the message event definition
   * @return the message event definition builder object
   */
  public MessageEventDefinitionBuilder messageEventDefinition(String id) {
    MessageEventDefinition messageEventDefinition = createEmptyMessageEventDefinition();
    if (id != null) {
      messageEventDefinition.setId(id);
    }

    element.getEventDefinitions().add(messageEventDefinition);
    return new MessageEventDefinitionBuilder(modelInstance, messageEventDefinition);
  }

  /**
   * Sets an event definition for the given signal name. If already a signal
   * with this name exists it will be used, otherwise a new signal is created.
   *
   * @param signalName the name of the signal
   * @return the builder object
   */
  public B signal(String signalName) {
    SignalEventDefinition signalEventDefinition = createSignalEventDefinition(signalName);
    element.getEventDefinitions().add(signalEventDefinition);

    return myself;
  }

  /**
   * Sets an event definition for the given Signal name. If a signal with this
   * name already exists it will be used, otherwise a new signal is created.
   * It returns a builder for the Signal Event Definition.
   *
   * @param signalName the name of the signal
   * @return the signal event definition builder object
   */
  public SignalEventDefinitionBuilder signalEventDefinition(String signalName) {
    SignalEventDefinition signalEventDefinition = createSignalEventDefinition(signalName);
    element.getEventDefinitions().add(signalEventDefinition);

    return new SignalEventDefinitionBuilder(modelInstance, signalEventDefinition);
  }

  /**
   * Sets an escalation definition for the given escalation code. If already an
   * escalation with this code exists it will be used, otherwise a new
   * escalation is created.
   *
   * @param escalationCode the code of the escalation
   * @return the builder object
   */
  public B escalation(String escalationCode) {
    EscalationEventDefinition escalationEventDefinition = createEscalationEventDefinition(escalationCode);
    element.getEventDefinitions().add(escalationEventDefinition);

    return myself;
  }

  public CompensateEventDefinitionBuilder compensateEventDefinition() {
    return compensateEventDefinition(null);
  }

  public CompensateEventDefinitionBuilder compensateEventDefinition(String id) {
    CompensateEventDefinition eventDefinition = createInstance(CompensateEventDefinition.class);
    if (id != null) {
      eventDefinition.setId(id);
    }

    element.getEventDefinitions().add(eventDefinition);
    return new CompensateEventDefinitionBuilder(modelInstance, eventDefinition);
  }
}
