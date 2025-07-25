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
package org.operaton.bpm.model.bpmn.impl.instance.di;

import org.operaton.bpm.model.bpmn.instance.di.DiagramElement;
import org.operaton.bpm.model.bpmn.instance.di.Edge;
import org.operaton.bpm.model.bpmn.instance.di.Waypoint;
import org.operaton.bpm.model.xml.ModelBuilder;
import org.operaton.bpm.model.xml.impl.instance.ModelTypeInstanceContext;
import org.operaton.bpm.model.xml.type.ModelElementTypeBuilder;
import org.operaton.bpm.model.xml.type.child.ChildElementCollection;
import org.operaton.bpm.model.xml.type.child.SequenceBuilder;

import java.util.Collection;

import static org.operaton.bpm.model.bpmn.impl.BpmnModelConstants.DI_ELEMENT_EDGE;
import static org.operaton.bpm.model.bpmn.impl.BpmnModelConstants.DI_NS;

/**
 * @author Sebastian Menski
 */
public abstract class EdgeImpl extends DiagramElementImpl implements Edge {

  protected static ChildElementCollection<Waypoint> waypointCollection;

  public static void registerType(ModelBuilder modelBuilder) {
    ModelElementTypeBuilder typeBuilder = modelBuilder.defineType(Edge.class, DI_ELEMENT_EDGE)
      .namespaceUri(DI_NS)
      .extendsType(DiagramElement.class)
      .abstractType();

    SequenceBuilder sequenceBuilder = typeBuilder.sequence();

    waypointCollection = sequenceBuilder.elementCollection(Waypoint.class)
      .minOccurs(2)
      .build();

    typeBuilder.build();
  }

  protected EdgeImpl(ModelTypeInstanceContext instanceContext) {
    super(instanceContext);
  }

  @Override
  public Collection<Waypoint> getWaypoints() {
    return waypointCollection.get(this);
  }
}
