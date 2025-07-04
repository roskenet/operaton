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
package org.operaton.bpm.container.impl.jboss.service;

import java.io.ByteArrayInputStream;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.operaton.bpm.application.ProcessApplicationInterface;
import org.operaton.bpm.application.ProcessApplicationReference;
import org.operaton.bpm.application.ProcessApplicationRegistration;
import org.operaton.bpm.application.impl.metadata.spi.ProcessArchiveXml;
import org.operaton.bpm.container.impl.jboss.util.Tccl;
import org.operaton.bpm.container.impl.metadata.PropertyHelper;
import org.operaton.bpm.engine.ProcessEngine;
import org.operaton.bpm.engine.RepositoryService;
import org.operaton.bpm.engine.impl.context.Context;
import org.operaton.bpm.engine.repository.ProcessApplicationDeployment;
import org.operaton.bpm.engine.repository.ProcessApplicationDeploymentBuilder;
import org.operaton.bpm.engine.repository.ResumePreviousBy;
import org.jboss.as.ee.component.ComponentView;
import org.jboss.as.naming.ManagedReference;
import org.jboss.modules.Module;
import org.jboss.msc.service.Service;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StartException;
import org.jboss.msc.service.StopContext;

/**
 * <p>Service responsible for performing a deployment to the process engine and managing
 * the resulting {@link ProcessApplicationRegistration} with the process engine.</p>
 *
 * <p>We construct one of these per Process Archive of a Process Application.</p>
 *
 * <p>We need a dependency on the componentView service of the ProcessApplication
 * component and the process engine to which the deployment should be performed.</p>
 *
 * @author Daniel Meyer
 *
 */
public class ProcessApplicationDeploymentService implements Service<ProcessApplicationDeploymentService> {

  private static final Logger LOGGER = Logger.getLogger(ProcessApplicationDeploymentService.class.getName());

  protected final Supplier<ExecutorService> executorSupplier;
  protected final Supplier<ProcessEngine> processEngineSupplier;
  protected final Supplier<ProcessApplicationInterface> noViewProcessApplicationSupplier;
  // for view-exposing ProcessApplicationComponents
  protected final Supplier<ComponentView> paComponentViewSupplier;
  protected final Consumer<ProcessApplicationDeploymentService> provider;

  /** the map of deployment resources obtained  through scanning */
  protected final Map<String,byte[]> deploymentMap;
  /** deployment metadata that is passed in */
  protected final ProcessArchiveXml processArchive;

  /** the deployment we create here */
  protected ProcessApplicationDeployment deployment;

  protected Module module;

  public ProcessApplicationDeploymentService(
      Map<String, byte[]> deploymentMap,
      ProcessArchiveXml processArchive,
      Module module,
      Supplier<ExecutorService> executorSupplier,
      Supplier<ProcessEngine> processEngineInjector,
      Supplier<ProcessApplicationInterface> noViewProcessApplication,
      Supplier<ComponentView> paComponentView, Consumer<ProcessApplicationDeploymentService> provider) {
    this.deploymentMap = deploymentMap;
    this.processArchive = processArchive;
    this.module = module;
    this.executorSupplier = executorSupplier;
    this.processEngineSupplier = processEngineInjector;
    this.noViewProcessApplicationSupplier = noViewProcessApplication;
    this.paComponentViewSupplier = paComponentView;
    this.provider = provider;
  }

  @Override
  public void start(final StartContext context) throws StartException {
    provider.accept(this);
    context.asynchronous();
    executorSupplier.get().submit(new Runnable() {
      @Override
      public void run() {
        try {
          performDeployment();
          context.complete();
        } catch (StartException e) {
          context.failed(e);
        } catch (Throwable e) {
          context.failed(new StartException(e));
        }
      }
    });
  }

  @Override
  public void stop(final StopContext context) {
    provider.accept(null);
    context.asynchronous();
    executorSupplier.get().submit(new Runnable() {
      @Override
      public void run() {
        try {
          performUndeployment();
        } finally {
          context.complete();
        }
      }
    });
  }

  protected void performDeployment() throws StartException {

    ManagedReference reference = null;
    try {

      // get process engine
      ProcessEngine processEngine = processEngineSupplier.get();

      // get the process application component
      ProcessApplicationInterface processApplication = null;
      if(paComponentViewSupplier != null) {
        ComponentView componentView = paComponentViewSupplier.get();
        reference = componentView.createInstance();
        processApplication = (ProcessApplicationInterface) reference.getInstance();
      } else {
        processApplication = noViewProcessApplicationSupplier.get();
      }

      // get the application name
      String processApplicationName = processApplication.getName();

      // build the deployment
      final RepositoryService repositoryService = processEngine.getRepositoryService();

      final ProcessApplicationDeploymentBuilder deploymentBuilder = repositoryService.createDeployment(processApplication.getReference());

      // enable duplicate filtering
      deploymentBuilder.enableDuplicateFiltering(PropertyHelper.getBooleanProperty(processArchive.getProperties(), ProcessArchiveXml.PROP_IS_DEPLOY_CHANGED_ONLY, false));

      // enable resuming of previous versions:
      if(PropertyHelper.getBooleanProperty(processArchive.getProperties(), ProcessArchiveXml.PROP_IS_RESUME_PREVIOUS_VERSIONS, true)) {
        enableResumingOfPreviousVersions(deploymentBuilder);
      }

      // set the name for the deployment
      String deploymentName = processArchive.getName();
      if(deploymentName == null || deploymentName.isEmpty()) {
        deploymentName = processApplicationName;
      }
      deploymentBuilder.name(deploymentName);

      // set the tenant id for the deployment
      String tenantId = processArchive.getTenantId();
      if(tenantId != null && !tenantId.isEmpty()) {
        deploymentBuilder.tenantId(tenantId);
      }

      // add deployment resources
      for (Entry<String, byte[]> resource : deploymentMap.entrySet()) {
        deploymentBuilder.addInputStream(resource.getKey(), new ByteArrayInputStream(resource.getValue()));
      }

      // let the process application component add resources to the deployment.
      processApplication.createDeployment(processArchive.getName(), deploymentBuilder);

      Collection<String> resourceNames = deploymentBuilder.getResourceNames();
      if(!resourceNames.isEmpty()) {
        logDeploymentSummary(resourceNames, deploymentName, processApplicationName);
        ProcessApplicationReference paReference = processApplication.getReference();
        // perform the actual deployment
        deployment = Tccl.runUnderClassloader(() -> {
          try {
            Context.setCurrentProcessApplication(paReference);

            // deploy the application
            return deploymentBuilder.deploy();

          } finally {
            Context.removeCurrentProcessApplication();

          }

        }, module.getClassLoader());

      } else {
        LOGGER.info("Not creating a deployment for process archive '" + processArchive.getName() + "': no resources provided.");

      }

    } catch (Exception e) {
      throw new StartException("Could not register process application with shared process engine ",e);

    } finally {
      if(reference != null) {
        reference.release();
      }
    }
  }

  protected void enableResumingOfPreviousVersions(ProcessApplicationDeploymentBuilder deploymentBuilder) throws IllegalArgumentException {
    deploymentBuilder.resumePreviousVersions();
    String resumePreviousBy = processArchive.getProperties().get(ProcessArchiveXml.PROP_RESUME_PREVIOUS_BY);
    if (resumePreviousBy == null) {
      deploymentBuilder.resumePreviousVersionsBy(ResumePreviousBy.RESUME_BY_PROCESS_DEFINITION_KEY);
    } else if (isValidValueForResumePreviousBy(resumePreviousBy)) {
      deploymentBuilder.resumePreviousVersionsBy(resumePreviousBy);
    } else {
      StringBuilder b = new StringBuilder();
      b.append("Illegal value passed for property ").append(ProcessArchiveXml.PROP_RESUME_PREVIOUS_BY);
      b.append(". Value was ").append(resumePreviousBy);
      b.append(" expected ").append(ResumePreviousBy.RESUME_BY_DEPLOYMENT_NAME);
      b.append(" or ").append(ResumePreviousBy.RESUME_BY_PROCESS_DEFINITION_KEY).append(".");
      throw new IllegalArgumentException(b.toString());
    }
  }

  protected boolean isValidValueForResumePreviousBy(String resumePreviousBy) {
    return resumePreviousBy.equals(ResumePreviousBy.RESUME_BY_DEPLOYMENT_NAME) || resumePreviousBy.equals(ResumePreviousBy.RESUME_BY_PROCESS_DEFINITION_KEY);
  }

  /**
   * @param deploymentMap2
   * @param deploymentName
   */
  protected void logDeploymentSummary(Collection<String> resourceNames, String deploymentName, String processApplicationName) {
    // log a summary of the deployment
    StringBuilder builder = new StringBuilder();
    builder.append("Deployment summary for process archive '"+deploymentName+"' of process application '"+processApplicationName+"': \n");
    builder.append("\n");
    for (String resourceName : resourceNames) {
      builder.append("        "+resourceName);
      builder.append("\n");
    }
    LOGGER.log(Level.INFO, builder.toString());
  }

  protected void performUndeployment() {

    final ProcessEngine processEngine = processEngineSupplier.get();

    try {
      if(deployment != null) {
        // always unregister
        Set<String> deploymentIds = deployment.getProcessApplicationRegistration().getDeploymentIds();
        processEngine.getManagementService().unregisterProcessApplication(deploymentIds, true);
      }
    } catch(Exception e) {
      LOGGER.log(Level.SEVERE, "Exception while unregistering process application with the process engine.");

    }

    // delete the deployment only if requested in metadata
    if(deployment != null && PropertyHelper.getBooleanProperty(processArchive.getProperties(), ProcessArchiveXml.PROP_IS_DELETE_UPON_UNDEPLOY, false)) {
      try {
        LOGGER.info("Deleting cascade deployment with name '"+deployment.getName()+"/"+deployment.getId()+"'.");
        // always cascade & skip custom listeners
        processEngine.getRepositoryService().deleteDeployment(deployment.getId(), true, true);

      } catch (Exception e) {
        LOGGER.log(Level.WARNING, "Exception while deleting process engine deployment", e);

      }

    }
  }

  @Override
  public ProcessApplicationDeploymentService getValue() throws IllegalStateException, IllegalArgumentException {
    return this;
  }

  public ProcessApplicationDeployment getDeployment() {
    return deployment;
  }

  public String getProcessEngineName() {
    return processEngineSupplier.get().getName();
  }

  public Supplier<ProcessEngine> getProcessEngineSupplier() {
    return processEngineSupplier;
  }

}
