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
package org.operaton.bpm.engine.impl.cmmn.handler;

import java.util.Collection;
import java.util.List;

import org.operaton.bpm.engine.impl.cmmn.model.CmmnActivity;
import org.operaton.bpm.engine.impl.cmmn.model.CmmnSentryDeclaration;
import org.operaton.bpm.model.cmmn.instance.CasePlanModel;
import org.operaton.bpm.model.cmmn.instance.CmmnElement;
import org.operaton.bpm.model.cmmn.instance.PlanItemDefinition;
import org.operaton.bpm.model.cmmn.instance.Sentry;

/**
 * @author Roman Smirnov
 *
 */
public class CasePlanModelHandler extends StageItemHandler {

  @Override
  protected PlanItemDefinition getDefinition(CmmnElement element) {
    return (PlanItemDefinition) element;
  }

  @Override
  protected List<String> getStandardEvents(CmmnElement element) {
    return CASE_PLAN_MODEL_EVENTS;
  }

  @SuppressWarnings("unused")
  public void initializeExitCriterias(CasePlanModel casePlanModel, CmmnActivity activity, CmmnHandlerContext context) {
    Collection<Sentry> exitCriterias = casePlanModel.getExitCriteria();
    for (Sentry sentry : exitCriterias) {
      String sentryId = sentry.getId();
      CmmnSentryDeclaration sentryDeclaration = activity.getSentry(sentryId);
      activity.addExitCriteria(sentryDeclaration);
    }
  }

}
