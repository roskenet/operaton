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
package org.operaton.bpm.model.cmmn.impl;

import org.operaton.bpm.model.cmmn.CmmnModelInstance;
import org.operaton.bpm.model.cmmn.instance.Definitions;
import org.operaton.bpm.model.xml.ModelBuilder;
import org.operaton.bpm.model.xml.impl.ModelImpl;
import org.operaton.bpm.model.xml.impl.ModelInstanceImpl;
import org.operaton.bpm.model.xml.instance.DomDocument;

/**
 * @author Roman Smirnov
 *
 */
public class CmmnModelInstanceImpl extends ModelInstanceImpl implements CmmnModelInstance {

  public CmmnModelInstanceImpl(ModelImpl model, ModelBuilder modelBuilder, DomDocument document) {
    super(model, modelBuilder, document);
  }

  @Override
  public Definitions getDefinitions() {
    return (Definitions) getDocumentElement();
  }

  @Override
  public void setDefinitions(Definitions definitions) {
    setDocumentElement(definitions);
  }

  @Override
  public CmmnModelInstance clone() {
    return new CmmnModelInstanceImpl(model, modelBuilder, document.clone());
  }

}
