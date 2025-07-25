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
package org.operaton.bpm.engine.impl.migration.instance.parser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import org.operaton.bpm.engine.impl.jobexecutor.JobHandlerConfiguration;
import org.operaton.bpm.engine.impl.jobexecutor.TimerDeclarationImpl;
import org.operaton.bpm.engine.impl.jobexecutor.TimerEventJobHandler.TimerJobConfiguration;
import org.operaton.bpm.engine.impl.jobexecutor.TimerExecuteNestedActivityJobHandler;
import org.operaton.bpm.engine.impl.jobexecutor.TimerTaskListenerJobHandler;
import org.operaton.bpm.engine.impl.migration.instance.EmergingJobInstance;
import org.operaton.bpm.engine.impl.migration.instance.MigratingActivityInstance;
import org.operaton.bpm.engine.impl.migration.instance.MigratingJobInstance;
import org.operaton.bpm.engine.impl.migration.instance.MigratingTimerJobInstance;
import org.operaton.bpm.engine.impl.persistence.entity.JobDefinitionEntity;
import org.operaton.bpm.engine.impl.persistence.entity.JobEntity;
import org.operaton.bpm.engine.impl.persistence.entity.TimerEntity;
import org.operaton.bpm.engine.impl.pvm.process.ActivityImpl;
import org.operaton.bpm.engine.migration.MigrationInstruction;

/**
 * @author Thorben Lindhauer
 *
 */
public class ActivityInstanceJobHandler implements MigratingDependentInstanceParseHandler<MigratingActivityInstance, List<JobEntity>> {

  @Override
  public void handle(MigratingInstanceParseContext parseContext, MigratingActivityInstance activityInstance, List<JobEntity> elements) {

    Map<String, TimerDeclarationImpl> sourceTimerDeclarationsInEventScope = TimerDeclarationImpl.getDeclarationsForScope(activityInstance.getSourceScope());
    Map<String, TimerDeclarationImpl> targetTimerDeclarationsInEventScope = new HashMap<>(TimerDeclarationImpl.getDeclarationsForScope(activityInstance.getTargetScope()));

    Map<String, Map<String, TimerDeclarationImpl>> sourceTimeoutListenerDeclarationsInEventScope = TimerDeclarationImpl.getTimeoutListenerDeclarationsForScope(activityInstance.getSourceScope());
    Map<String, Map<String, TimerDeclarationImpl>> targetTimeoutListenerDeclarationsInEventScope = new HashMap<>(TimerDeclarationImpl.getTimeoutListenerDeclarationsForScope(activityInstance.getTargetScope()));

    for (JobEntity job : elements) {
      if (!isTimerJob(job)) {
        // skip non timer jobs
        continue;
      }

      MigrationInstruction migrationInstruction = parseContext.findSingleMigrationInstruction(job.getActivityId());
      ActivityImpl targetActivity = parseContext.getTargetActivity(migrationInstruction);
      JobHandlerConfiguration jobHandlerConfiguration = job.getJobHandlerConfiguration();

      if (targetActivity != null && activityInstance.migratesTo(targetActivity.getEventScope()) &&
          isNoTimeoutListenerOrMigrates(job, jobHandlerConfiguration, targetActivity.getActivityId(), targetTimeoutListenerDeclarationsInEventScope)) {
        // the timer job is migrated
        JobDefinitionEntity targetJobDefinitionEntity = parseContext.getTargetJobDefinition(targetActivity.getActivityId(), job.getJobHandlerType());

        TimerDeclarationImpl targetTimerDeclaration = getTargetTimerDeclaration(job, jobHandlerConfiguration, targetActivity.getActivityId(),
            targetTimeoutListenerDeclarationsInEventScope, targetTimerDeclarationsInEventScope);

        MigratingJobInstance migratingTimerJobInstance =
            new MigratingTimerJobInstance(
                job,
                targetJobDefinitionEntity,
                targetActivity,
                migrationInstruction.isUpdateEventTrigger(),
                targetTimerDeclaration);
        activityInstance.addMigratingDependentInstance(migratingTimerJobInstance);
        parseContext.submit(migratingTimerJobInstance);

      } else {
        // the timer job is removed
        MigratingJobInstance removingJobInstance = new MigratingTimerJobInstance(job);
        activityInstance.addRemovingDependentInstance(removingJobInstance);
        parseContext.submit(removingJobInstance);

      }

      parseContext.consume(job);
    }

    if (activityInstance.migrates()) {
      addEmergingTimerJobs(parseContext, activityInstance, sourceTimerDeclarationsInEventScope, targetTimerDeclarationsInEventScope);
      addEmergingTimeoutListenerJobs(parseContext, activityInstance, sourceTimeoutListenerDeclarationsInEventScope, targetTimeoutListenerDeclarationsInEventScope);
    }
  }

  @SuppressWarnings("unused")
  protected TimerDeclarationImpl getTargetTimerDeclaration(JobEntity job, JobHandlerConfiguration jobHandlerConfiguration,
      String targetActivity, Map<String, Map<String, TimerDeclarationImpl>> targetTimeoutListenerDeclarationsInEventScope,
      Map<String, TimerDeclarationImpl> targetTimerDeclarationsInEventScope) {
    if (isTimeoutListenerJobInTargetScope(jobHandlerConfiguration, targetActivity, targetTimeoutListenerDeclarationsInEventScope)) {
      return removeTimeoutListenerJobFromTargetScope(jobHandlerConfiguration, targetActivity, targetTimeoutListenerDeclarationsInEventScope);
    }
    return targetTimerDeclarationsInEventScope.remove(targetActivity);
  }

  protected static boolean isTimerJob(JobEntity job) {
    return job != null && job.getType().equals(TimerEntity.TYPE);
  }

  protected static boolean isNoTimeoutListenerOrMigrates(JobEntity job, JobHandlerConfiguration jobHandlerConfiguration,
      String targetActivity, Map<String, Map<String, TimerDeclarationImpl>> targetTimeoutListenerDeclarationsInEventScope) {
    return !TimerTaskListenerJobHandler.TYPE.equals(job.getJobHandlerType()) ||
        isTimeoutListenerJobInTargetScope(jobHandlerConfiguration, targetActivity, targetTimeoutListenerDeclarationsInEventScope);
  }

  protected static boolean isTimeoutListenerJobInTargetScope(JobHandlerConfiguration jobHandlerConfiguration,
      String targetActivity, Map<String, Map<String, TimerDeclarationImpl>> targetTimeoutListenerDeclarationsInEventScope) {
    return jobHandlerConfiguration instanceof TimerJobConfiguration timerJobConfiguration &&
        targetTimeoutListenerDeclarationsInEventScope.containsKey(targetActivity) &&
        targetTimeoutListenerDeclarationsInEventScope.get(targetActivity).containsKey(
        		timerJobConfiguration.getTimerElementSecondaryKey());
  }

  protected static TimerDeclarationImpl removeTimeoutListenerJobFromTargetScope(JobHandlerConfiguration jobHandlerConfiguration,
      String targetActivity, Map<String, Map<String, TimerDeclarationImpl>> targetTimeoutListenerDeclarationsInEventScope) {
    if (isTimeoutListenerJobInTargetScope(jobHandlerConfiguration, targetActivity, targetTimeoutListenerDeclarationsInEventScope)) {
      Map<String, TimerDeclarationImpl> activityDeclarations = targetTimeoutListenerDeclarationsInEventScope.get(targetActivity);
      TimerDeclarationImpl declaration = activityDeclarations.remove(((TimerJobConfiguration) jobHandlerConfiguration).getTimerElementSecondaryKey());
      if (activityDeclarations.isEmpty()) {
        targetTimeoutListenerDeclarationsInEventScope.remove(targetActivity);
      }
      return declaration;
    }
    return  null;
  }

  protected void addEmergingTimerJobs(MigratingInstanceParseContext parseContext, MigratingActivityInstance activityInstance,
      Map<String, TimerDeclarationImpl> sourceTimerDeclarationsInEventScope, Map<String, TimerDeclarationImpl> targetTimerDeclarationsInEventScope) {
    for (TimerDeclarationImpl targetTimerDeclaration : targetTimerDeclarationsInEventScope.values()) {
      if(!isNonInterruptingTimerTriggeredAlready(parseContext, sourceTimerDeclarationsInEventScope, targetTimerDeclaration)) {
        activityInstance.addEmergingDependentInstance(new EmergingJobInstance(targetTimerDeclaration));
      }
    }
  }

  protected void addEmergingTimeoutListenerJobs(MigratingInstanceParseContext parseContext, MigratingActivityInstance activityInstance,
      Map<String, Map<String, TimerDeclarationImpl>> sourceTimeoutListenerDeclarationsInEventScope,
      Map<String, Map<String, TimerDeclarationImpl>> targetTimeoutListenerDeclarationsInEventScope) {
    for (Map<String, TimerDeclarationImpl> targetTimerDeclarations : targetTimeoutListenerDeclarationsInEventScope.values()) {
      for (Entry<String, TimerDeclarationImpl> targetTimerDeclaration : targetTimerDeclarations.entrySet()) {
        if(!isNonInterruptingTimeoutListenerTriggeredAlready(parseContext, sourceTimeoutListenerDeclarationsInEventScope, targetTimerDeclaration)) {
          activityInstance.addEmergingDependentInstance(new EmergingJobInstance(targetTimerDeclaration.getValue()));
        }
      }
    }
  }

  protected boolean isNonInterruptingTimerTriggeredAlready(MigratingInstanceParseContext parseContext,
      Map<String, TimerDeclarationImpl> sourceTimerDeclarationsInEventScope, TimerDeclarationImpl targetTimerDeclaration) {
    if (targetTimerDeclaration.isInterruptingTimer() || !Objects.equals(targetTimerDeclaration.getJobHandlerType(), TimerExecuteNestedActivityJobHandler.TYPE) || sourceTimerDeclarationsInEventScope.values().isEmpty()) {
      return false;
    }
    for (TimerDeclarationImpl sourceTimerDeclaration : sourceTimerDeclarationsInEventScope.values()) {
      MigrationInstruction migrationInstruction = parseContext.findSingleMigrationInstruction(sourceTimerDeclaration.getActivityId());
      ActivityImpl targetActivity = parseContext.getTargetActivity(migrationInstruction);

      if (targetActivity != null && targetTimerDeclaration.getActivityId().equals(targetActivity.getActivityId())) {
        return true;
      }
    }
    return false;
  }

  protected boolean isNonInterruptingTimeoutListenerTriggeredAlready(MigratingInstanceParseContext parseContext,
      Map<String, Map<String, TimerDeclarationImpl>> sourceTimeoutListenerDeclarationsInEventScope,
      Entry<String, TimerDeclarationImpl> targetTimerDeclarationEntry) {
    TimerDeclarationImpl targetTimerDeclaration = targetTimerDeclarationEntry.getValue();
    if (targetTimerDeclaration.isInterruptingTimer() || !Objects.equals(targetTimerDeclaration.getJobHandlerType(), TimerTaskListenerJobHandler.TYPE) || sourceTimeoutListenerDeclarationsInEventScope.values().isEmpty()) {
      return false;
    }
    for (Entry<String, Map<String, TimerDeclarationImpl>> sourceTimerDeclarationsEntry : sourceTimeoutListenerDeclarationsInEventScope.entrySet()) {
      MigrationInstruction migrationInstruction = parseContext.findSingleMigrationInstruction(sourceTimerDeclarationsEntry.getKey());
      ActivityImpl targetActivity = parseContext.getTargetActivity(migrationInstruction);

      if (targetActivity != null && targetTimerDeclaration.getActivityId().equals(targetActivity.getActivityId())) {
        for (Entry<String, TimerDeclarationImpl> sourceTimerDeclarationEntry : sourceTimerDeclarationsEntry.getValue().entrySet()) {
          if (sourceTimerDeclarationEntry.getKey().equals(targetTimerDeclarationEntry.getKey())) {
            return true;
          }
        }
      }
    }
    return false;
  }

}
