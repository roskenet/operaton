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
package org.operaton.bpm.model.xml.impl.type.attribute;

import org.operaton.bpm.model.xml.type.ModelElementType;

/**
 * <p>An attribute exposing an Enum value</p>
 *
 * @author Daniel Meyer
 *
 */
public class EnumAttribute<T extends Enum<T>> extends AttributeImpl<T> {

  private final Class<T> type;

  public EnumAttribute(ModelElementType owningElementType, Class<T> type) {
    super(owningElementType);
    this.type = type;
  }

  @Override
  protected T convertXmlValueToModelValue(String rawValue) {
    if (rawValue != null) {
      return Enum.valueOf(type, rawValue);
    }
    else {
      return null;
    }
  }

  @Override
  protected String convertModelValueToXmlValue(T modelValue) {
    return modelValue.name();
  }

}
