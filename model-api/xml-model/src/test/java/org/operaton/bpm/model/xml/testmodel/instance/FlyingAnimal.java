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
package org.operaton.bpm.model.xml.testmodel.instance;

import org.operaton.bpm.model.xml.ModelBuilder;
import org.operaton.bpm.model.xml.impl.instance.ModelTypeInstanceContext;
import org.operaton.bpm.model.xml.type.ModelElementTypeBuilder;
import org.operaton.bpm.model.xml.type.attribute.Attribute;
import org.operaton.bpm.model.xml.type.child.SequenceBuilder;
import org.operaton.bpm.model.xml.type.reference.ElementReference;
import org.operaton.bpm.model.xml.type.reference.ElementReferenceCollection;

import java.util.Collection;

import static org.operaton.bpm.model.xml.testmodel.TestModelConstants.*;

/**
 * @author Daniel Meyer
 *
 */
public abstract class FlyingAnimal extends Animal {

  // only public for testing (normally private)
  public static ElementReference<FlyingAnimal, FlightInstructor> flightInstructorChild;
  public static ElementReferenceCollection<FlyingAnimal, FlightPartnerRef> flightPartnerRefsColl;
  public static Attribute<Double> wingspanAttribute;

  public static void registerType(ModelBuilder modelBuilder) {

    ModelElementTypeBuilder typeBuilder = modelBuilder.defineType(FlyingAnimal.class, TYPE_NAME_FLYING_ANIMAL)
      .namespaceUri(MODEL_NAMESPACE)
      .extendsType(Animal.class)
      .abstractType();

    wingspanAttribute = typeBuilder.doubleAttribute(ATTRIBUTE_NAME_WINGSPAN)
      .build();

    SequenceBuilder sequence = typeBuilder.sequence();

    flightInstructorChild = sequence.element(FlightInstructor.class)
      .idElementReference(FlyingAnimal.class)
      .build();

    flightPartnerRefsColl = sequence.elementCollection(FlightPartnerRef.class)
      .idElementReferenceCollection(FlyingAnimal.class)
      .build();

    typeBuilder.build();

  }

  FlyingAnimal(ModelTypeInstanceContext instanceContext) {
    super(instanceContext);
  }

  public Double getWingspan() {
    return wingspanAttribute.getValue(this);
  }

  public void setWingspan(double wingspan) {
    wingspanAttribute.setValue(this, wingspan);
  }

  public FlyingAnimal getFlightInstructor() {
    return flightInstructorChild.getReferenceTargetElement(this);
  }

  public void setFlightInstructor(FlyingAnimal flightInstructor) {
    flightInstructorChild.setReferenceTargetElement(this, flightInstructor);
  }

  public void removeFlightInstructor() {
    flightInstructorChild.clearReferenceTargetElement(this);
  }

  public Collection<FlyingAnimal> getFlightPartnerRefs() {
    return flightPartnerRefsColl.getReferenceTargetElements(this);
  }

  public Collection<FlightPartnerRef> getFlightPartnerRefElements() {
    return flightPartnerRefsColl.getReferenceSourceCollection().get(this);
  }
}
