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
package org.operaton.bpm.model.bpmn.impl.instance;

import org.operaton.bpm.model.bpmn.instance.Activity;
import org.operaton.bpm.model.bpmn.instance.CompensateEventDefinition;
import org.operaton.bpm.model.bpmn.instance.EventDefinition;
import org.operaton.bpm.model.xml.ModelBuilder;
import org.operaton.bpm.model.xml.impl.instance.ModelTypeInstanceContext;
import org.operaton.bpm.model.xml.type.ModelElementTypeBuilder;
import org.operaton.bpm.model.xml.type.attribute.Attribute;
import org.operaton.bpm.model.xml.type.reference.AttributeReference;

import static org.operaton.bpm.model.bpmn.impl.BpmnModelConstants.*;

/**
 * The BPMN compensateEventDefinition element
 *
 * @author Sebastian Menski
 */
public class CompensateEventDefinitionImpl extends EventDefinitionImpl implements CompensateEventDefinition {

  protected static Attribute<Boolean> waitForCompletionAttribute;
  protected static AttributeReference<Activity> activityRefAttribute;

  public static void registerType(ModelBuilder modelBuilder) {
    ModelElementTypeBuilder typeBuilder = modelBuilder.defineType(CompensateEventDefinition.class, BPMN_ELEMENT_COMPENSATE_EVENT_DEFINITION)
      .namespaceUri(BPMN20_NS)
      .extendsType(EventDefinition.class)
      .instanceProvider(CompensateEventDefinitionImpl::new);

    waitForCompletionAttribute = typeBuilder.booleanAttribute(BPMN_ATTRIBUTE_WAIT_FOR_COMPLETION)
      .build();

    activityRefAttribute = typeBuilder.stringAttribute(BPMN_ATTRIBUTE_ACTIVITY_REF)
      .qNameAttributeReference(Activity.class)
      .build();

    typeBuilder.build();
  }

  public CompensateEventDefinitionImpl(ModelTypeInstanceContext context) {
    super(context);
  }

  @Override
  public boolean isWaitForCompletion() {
    return waitForCompletionAttribute.getValue(this);
  }

  @Override
  public void setWaitForCompletion(boolean isWaitForCompletion) {
    waitForCompletionAttribute.setValue(this, isWaitForCompletion);
  }

  @Override
  public Activity getActivity() {
    return activityRefAttribute.getReferenceTargetElement(this);
  }

  @Override
  public void setActivity(Activity activity) {
    activityRefAttribute.setReferenceTargetElement(this, activity);
  }
}
