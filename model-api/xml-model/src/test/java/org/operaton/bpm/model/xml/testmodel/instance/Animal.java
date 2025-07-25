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

import static org.operaton.bpm.model.xml.testmodel.TestModelConstants.ATTRIBUTE_NAME_AGE;
import static org.operaton.bpm.model.xml.testmodel.TestModelConstants.ATTRIBUTE_NAME_BEST_FRIEND_REFS;
import static org.operaton.bpm.model.xml.testmodel.TestModelConstants.ATTRIBUTE_NAME_FATHER;
import static org.operaton.bpm.model.xml.testmodel.TestModelConstants.ATTRIBUTE_NAME_GENDER;
import static org.operaton.bpm.model.xml.testmodel.TestModelConstants.ATTRIBUTE_NAME_ID;
import static org.operaton.bpm.model.xml.testmodel.TestModelConstants.ATTRIBUTE_NAME_IS_ENDANGERED;
import static org.operaton.bpm.model.xml.testmodel.TestModelConstants.ATTRIBUTE_NAME_MOTHER;
import static org.operaton.bpm.model.xml.testmodel.TestModelConstants.ATTRIBUTE_NAME_NAME;
import static org.operaton.bpm.model.xml.testmodel.TestModelConstants.MODEL_NAMESPACE;
import static org.operaton.bpm.model.xml.testmodel.TestModelConstants.TYPE_NAME_ANIMAL;

import java.util.Collection;

import org.operaton.bpm.model.xml.ModelBuilder;
import org.operaton.bpm.model.xml.impl.instance.ModelElementInstanceImpl;
import org.operaton.bpm.model.xml.impl.instance.ModelTypeInstanceContext;
import org.operaton.bpm.model.xml.testmodel.Gender;
import org.operaton.bpm.model.xml.type.ModelElementTypeBuilder;
import org.operaton.bpm.model.xml.type.attribute.Attribute;
import org.operaton.bpm.model.xml.type.child.ChildElementCollection;
import org.operaton.bpm.model.xml.type.child.SequenceBuilder;
import org.operaton.bpm.model.xml.type.reference.AttributeReference;
import org.operaton.bpm.model.xml.type.reference.AttributeReferenceCollection;
import org.operaton.bpm.model.xml.type.reference.ElementReferenceCollection;

/**
 * @author Daniel Meyer
 *
 */
public abstract class Animal extends ModelElementInstanceImpl {

  protected static Attribute<String> idAttr;
  protected static Attribute<String> nameAttr;
  protected static AttributeReference<Animal> fatherRef;
  protected static AttributeReference<Animal> motherRef;
  protected static Attribute<Boolean> isEndangeredAttr;
  protected static Attribute<Gender> genderAttr;
  protected static Attribute<Integer> ageAttr;
  protected static AttributeReferenceCollection<Animal> bestFriendsRefCollection;
  protected static ChildElementCollection<RelationshipDefinition> relationshipDefinitionsColl;
  protected static ElementReferenceCollection<RelationshipDefinition, RelationshipDefinitionRef> relationshipDefinitionRefsColl;

  public static void registerType(ModelBuilder modelBuilder) {

    ModelElementTypeBuilder typeBuilder = modelBuilder.defineType(Animal.class, TYPE_NAME_ANIMAL)
      .namespaceUri(MODEL_NAMESPACE)
      .abstractType();

    idAttr = typeBuilder.stringAttribute(ATTRIBUTE_NAME_ID)
      .idAttribute()
      .build();

    nameAttr = typeBuilder.stringAttribute(ATTRIBUTE_NAME_NAME)
      .build();

    fatherRef = typeBuilder.stringAttribute(ATTRIBUTE_NAME_FATHER)
      .qNameAttributeReference(Animal.class)
      .build();

    motherRef = typeBuilder.stringAttribute(ATTRIBUTE_NAME_MOTHER)
      .idAttributeReference(Animal.class)
      .build();

    isEndangeredAttr = typeBuilder.booleanAttribute(ATTRIBUTE_NAME_IS_ENDANGERED)
      .defaultValue(false)
      .build();

    genderAttr = typeBuilder.enumAttribute(ATTRIBUTE_NAME_GENDER, Gender.class)
      .required()
      .build();

    ageAttr = typeBuilder.integerAttribute(ATTRIBUTE_NAME_AGE)
      .build();

    bestFriendsRefCollection = typeBuilder.stringAttribute(ATTRIBUTE_NAME_BEST_FRIEND_REFS)
        .idAttributeReferenceCollection(Animal.class, AnimalAttributeReferenceCollection.class)
        .build();

    SequenceBuilder sequence = typeBuilder.sequence();

    relationshipDefinitionsColl = sequence.elementCollection(RelationshipDefinition.class)
      .build();

    relationshipDefinitionRefsColl = sequence.elementCollection(RelationshipDefinitionRef.class)
      .qNameElementReferenceCollection(RelationshipDefinition.class)
      .build();

    typeBuilder.build();
  }

  protected Animal(ModelTypeInstanceContext instanceContext) {
    super(instanceContext);
  }

  public String getId() {
    return idAttr.getValue(this);
  }

  public void setId(String id) {
    idAttr.setValue(this, id);
  }

  public String getName() {
    return nameAttr.getValue(this);
  }

  public void setName(String name) {
    nameAttr.setValue(this, name);
  }

  public Animal getFather() {
    return fatherRef.getReferenceTargetElement(this);
  }

  public void setFather(Animal father) {
    fatherRef.setReferenceTargetElement(this, father);
  }

  public Animal getMother() {
    return motherRef.getReferenceTargetElement(this);
  }

  public void setMother(Animal mother) {
    motherRef.setReferenceTargetElement(this, mother);
  }

  public Boolean isEndangered() {
    return isEndangeredAttr.getValue(this);
  }

  public void setIsEndangered(boolean isEndangered) {
    isEndangeredAttr.setValue(this, isEndangered);
  }

  public Gender getGender() {
    return genderAttr.getValue(this);
  }

  public void setGender(Gender gender) {
    genderAttr.setValue(this, gender);
  }

  public Integer getAge() {
    return ageAttr.getValue(this);
  }

  public void setAge(int age) {
    ageAttr.setValue(this, age);
  }

  public Collection<RelationshipDefinition> getRelationshipDefinitions() {
    return relationshipDefinitionsColl.get(this);
  }

  public Collection<RelationshipDefinition> getRelationshipDefinitionRefs() {
    return relationshipDefinitionRefsColl.getReferenceTargetElements(this);
  }

  public Collection<RelationshipDefinitionRef> getRelationshipDefinitionRefElements() {
    return relationshipDefinitionRefsColl.getReferenceSourceCollection().get(this);
  }

  public Collection<Animal> getBestFriends() {
    return bestFriendsRefCollection.getReferenceTargetElements(this);
  }

}
