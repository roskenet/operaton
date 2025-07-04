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
package org.operaton.bpm.engine.impl.migration.instance;

import org.operaton.bpm.engine.impl.ProcessEngineLogger;
import org.operaton.bpm.engine.impl.bpmn.parser.EventSubscriptionDeclaration;
import org.operaton.bpm.engine.impl.migration.MigrationLogger;
import org.operaton.bpm.engine.impl.persistence.entity.EventSubscriptionEntity;
import org.operaton.bpm.engine.impl.persistence.entity.ExecutionEntity;
import org.operaton.bpm.engine.impl.pvm.process.ActivityImpl;
import org.operaton.bpm.engine.impl.pvm.process.ScopeImpl;

public class MigratingEventSubscriptionInstance implements MigratingInstance, RemovingInstance, EmergingInstance {

  public static final MigrationLogger MIGRATION_LOGGER = ProcessEngineLogger.MIGRATION_LOGGER;

  protected EventSubscriptionEntity eventSubscriptionEntity;
  protected ScopeImpl targetScope;
  protected boolean updateEvent;
  protected EventSubscriptionDeclaration targetDeclaration;

  protected EventSubscriptionDeclaration eventSubscriptionDeclaration;

  public MigratingEventSubscriptionInstance(EventSubscriptionEntity eventSubscriptionEntity,
      ScopeImpl targetScope,
      boolean updateEvent,
      EventSubscriptionDeclaration targetDeclaration) {
    this.eventSubscriptionEntity = eventSubscriptionEntity;
    this.targetScope = targetScope;
    this.updateEvent = updateEvent;
    this.targetDeclaration = targetDeclaration;
  }

  public MigratingEventSubscriptionInstance(EventSubscriptionEntity eventSubscriptionEntity) {
    this(eventSubscriptionEntity, null, false, null);
  }

  public MigratingEventSubscriptionInstance(EventSubscriptionDeclaration eventSubscriptionDeclaration) {
    this.eventSubscriptionDeclaration = eventSubscriptionDeclaration;
  }

  @Override
  public boolean isDetached() {
    return eventSubscriptionEntity.getExecutionId() == null;
  }

  @Override
  public void detachState() {
    eventSubscriptionEntity.setExecution(null);
  }

  @Override
  public void attachState(MigratingScopeInstance newOwningInstance) {
    eventSubscriptionEntity.setExecution(newOwningInstance.resolveRepresentativeExecution());
  }

  @Override
  public void attachState(MigratingTransitionInstance targetTransitionInstance) {
    throw MIGRATION_LOGGER.cannotAttachToTransitionInstance(this);
  }

  @Override
  public void migrateState() {
    if (updateEvent) {
      targetDeclaration.updateSubscription(eventSubscriptionEntity);
    }
    eventSubscriptionEntity.setActivity((ActivityImpl) targetScope);
  }

  @Override
  public void migrateDependentEntities() {
    // do nothing
  }

  @Override
  public void create(ExecutionEntity scopeExecution) {
    eventSubscriptionDeclaration.createSubscriptionForExecution(scopeExecution);
  }

  @Override
  public void remove() {
    eventSubscriptionEntity.delete();
  }
}
