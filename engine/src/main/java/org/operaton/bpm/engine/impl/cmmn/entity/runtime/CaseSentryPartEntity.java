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
package org.operaton.bpm.engine.impl.cmmn.entity.runtime;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.operaton.bpm.engine.impl.cmmn.execution.CmmnExecution;
import org.operaton.bpm.engine.impl.cmmn.execution.CmmnSentryPart;
import org.operaton.bpm.engine.impl.context.Context;
import org.operaton.bpm.engine.impl.db.DbEntity;
import org.operaton.bpm.engine.impl.db.HasDbReferences;
import org.operaton.bpm.engine.impl.db.HasDbRevision;

/**
 * @author Roman Smirnov
 *
 */
public class CaseSentryPartEntity extends CmmnSentryPart implements DbEntity, HasDbRevision, HasDbReferences {

  private static final long serialVersionUID = 1L;

  // references

  protected CaseExecutionEntity caseInstance;
  protected CaseExecutionEntity caseExecution;
  protected CaseExecutionEntity sourceCaseExecution;

  // persistence

  protected String id;
  protected int revision = 1;
  protected String caseInstanceId;
  protected String caseExecutionId;
  protected String sourceCaseExecutionId;
  protected String tenantId;
  private boolean forcedUpdate;

  // id ///////////////////////////////////////////////////////////////////

  @Override
  public String getId() {
    return id;
  }

  @Override
  public void setId(String id) {
    this.id = id;
  }

  // case instance id /////////////////////////////////////////////////////

  public String getCaseInstanceId() {
    return caseInstanceId;
  }

  @Override
  public CaseExecutionEntity getCaseInstance() {
    ensureCaseInstanceInitialized();
    return caseInstance;
  }

  protected void ensureCaseInstanceInitialized() {
    if ((caseInstance == null) && (caseInstanceId != null)) {
      caseInstance = findCaseExecutionById(caseInstanceId);
    }
  }

  @Override
  public void setCaseInstance(CmmnExecution caseInstance) {
    this.caseInstance = (CaseExecutionEntity) caseInstance;

    if (caseInstance != null) {
      caseInstanceId = caseInstance.getId();
    } else {
      caseInstanceId = null;
    }
  }

  // case execution id //////////////////////////////////////////////////////

  public String getCaseExecutionId() {
    return caseExecutionId;
  }

  @Override
  public CaseExecutionEntity getCaseExecution() {
    ensureCaseExecutionInitialized();
    return caseExecution;
  }

  protected void ensureCaseExecutionInitialized() {
    if ((caseExecution == null) && (caseExecutionId != null)) {
      caseExecution = findCaseExecutionById(caseExecutionId);
    }
  }

  @Override
  public void setCaseExecution(CmmnExecution caseExecution) {
    this.caseExecution = (CaseExecutionEntity) caseExecution;

    if (caseExecution != null) {
      caseExecutionId = caseExecution.getId();
    } else {
      caseExecutionId = null;
    }
  }

  // source case execution id //////////////////////////////////////////////////

  @Override
  public String getSourceCaseExecutionId() {
    return sourceCaseExecutionId;
  }

  @Override
  public CmmnExecution getSourceCaseExecution() {
    ensureSourceCaseExecutionInitialized();
    return sourceCaseExecution;
  }

  protected void ensureSourceCaseExecutionInitialized() {
    if ((sourceCaseExecution == null) && (sourceCaseExecutionId != null)) {
      sourceCaseExecution = findCaseExecutionById(sourceCaseExecutionId);
    }
  }

  @Override
  public void setSourceCaseExecution(CmmnExecution sourceCaseExecution) {
    this.sourceCaseExecution = (CaseExecutionEntity) sourceCaseExecution;

    if (sourceCaseExecution != null) {
      sourceCaseExecutionId = sourceCaseExecution.getId();
    } else {
      sourceCaseExecutionId = null;
    }
  }

  // persistence /////////////////////////////////////////////////////////

  @Override
  public int getRevision() {
    return revision;
  }

  @Override
  public void setRevision(int revision) {
    this.revision = revision;
  }

  @Override
  public int getRevisionNext() {
    return revision + 1;
  }

  public String getTenantId() {
    return tenantId;
  }

  public void setTenantId(String tenantId) {
    this.tenantId = tenantId;
  }

  public void forceUpdate() {
    this.forcedUpdate = true;
  }

  @Override
  public Object getPersistentState() {
    Map<String, Object> persistentState = new HashMap<>();
    persistentState.put("satisfied", isSatisfied());

    if (forcedUpdate) {
      persistentState.put("forcedUpdate", Boolean.TRUE);
    }

    return persistentState;
  }

  // helper ////////////////////////////////////////////////////////////////////

  protected CaseExecutionEntity findCaseExecutionById(String caseExecutionId) {
    return Context
        .getCommandContext()
        .getCaseExecutionManager()
        .findCaseExecutionById(caseExecutionId);
  }

  @Override
  public Set<String> getReferencedEntityIds() {
    return new HashSet<>();
  }

  @Override
  public Map<String, Class> getReferencedEntitiesIdAndClass() {
    Map<String, Class> referenceIdAndClass = new HashMap<>();

    if (caseExecutionId != null) {
      referenceIdAndClass.put(caseExecutionId, CaseExecutionEntity.class);
    }
    if (caseInstanceId != null) {
      referenceIdAndClass.put(caseInstanceId, CaseExecutionEntity.class);
    }

    return referenceIdAndClass;
  }
}
