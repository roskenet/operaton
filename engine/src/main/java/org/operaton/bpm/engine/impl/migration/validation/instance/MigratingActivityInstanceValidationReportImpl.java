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
package org.operaton.bpm.engine.impl.migration.validation.instance;

import java.util.ArrayList;
import java.util.List;

import org.operaton.bpm.engine.impl.migration.instance.MigratingActivityInstance;
import org.operaton.bpm.engine.migration.MigratingActivityInstanceValidationReport;
import org.operaton.bpm.engine.migration.MigrationInstruction;

public class MigratingActivityInstanceValidationReportImpl implements MigratingActivityInstanceValidationReport {

  protected String activityInstanceId;
  protected String sourceScopeId;
  protected MigrationInstruction migrationInstruction;
  protected List<String> failures = new ArrayList<>();

  public MigratingActivityInstanceValidationReportImpl(MigratingActivityInstance migratingActivityInstance) {
    this.activityInstanceId = migratingActivityInstance.getActivityInstance().getId();
    this.sourceScopeId = migratingActivityInstance.getSourceScope().getId();
    this.migrationInstruction = migratingActivityInstance.getMigrationInstruction();
  }

  @Override
  public String getSourceScopeId() {
    return sourceScopeId;
  }

  @Override
  public String getActivityInstanceId() {
    return activityInstanceId;
  }

  @Override
  public MigrationInstruction getMigrationInstruction() {
    return migrationInstruction;
  }

  public void addFailure(String failure) {
    failures.add(failure);
  }

  @Override
  public boolean hasFailures() {
    return !failures.isEmpty();
  }

  @Override
  public List<String> getFailures() {
    return failures;
  }

}
