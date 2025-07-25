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
package org.operaton.bpm.model.cmmn.instance;

import java.util.Collection;

/**
 * @author Roman Smirnov
 *
 */
public interface Definitions extends CmmnModelElementInstance {

  String getId();

  void setId(String id);

  String getName();

  void setName(String name);

  String getTargetNamespace();

  void setTargetNamespace(String namespace);

  String getExpressionLanguage();

  void setExpressionLanguage(String expressionLanguage);

  String getExporter();

  void setExporter(String exporter);

  String getExporterVersion();

  void setExporterVersion(String exporterVersion);

  String getAuthor();

  void setAuthor(String author);

  Collection<Import> getImports();

  Collection<CaseFileItemDefinition> getCaseFileItemDefinitions();

  Collection<Case> getCases();

  Collection<Process> getProcesses();

  Collection<Decision> getDecisions();

  ExtensionElements getExtensionElements();

  void setExtensionElements(ExtensionElements extensionElements);

  Collection<Relationship> getRelationships();

  Collection<Artifact> getArtifacts();

}
