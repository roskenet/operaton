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
package org.operaton.bpm.engine.test.jobexecutor;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.operaton.bpm.engine.history.HistoricJobLog;
import org.operaton.bpm.engine.impl.cmd.AcquireJobsCmd;
import org.operaton.bpm.engine.impl.interceptor.CommandExecutor;
import org.operaton.bpm.engine.impl.jobexecutor.AcquiredJobs;
import org.operaton.bpm.engine.impl.jobexecutor.ExecuteJobHelper;
import org.operaton.bpm.engine.impl.jobexecutor.JobExecutor;
import org.operaton.bpm.engine.impl.persistence.entity.MessageEntity;
import org.operaton.bpm.engine.impl.persistence.entity.TimerEntity;
import org.operaton.bpm.engine.impl.util.ClockUtil;

/**
 * @author Tom Baeyens
 */
class JobExecutorCmdHappyTest extends JobExecutorTestCase {

  @Test
  void testJobCommandsWithMessage() {
    CommandExecutor commandExecutor = processEngineConfiguration.getCommandExecutorTxRequired();
    JobExecutor jobExecutor = processEngineConfiguration.getJobExecutor();
    String jobId = commandExecutor.execute(commandContext -> {
      MessageEntity message = createTweetMessage("i'm coding a test");
      commandContext.getJobManager().send(message);
      return message.getId();
    });

    AcquiredJobs acquiredJobs = commandExecutor.execute(new AcquireJobsCmd(jobExecutor));
    List<List<String>> jobIdsList = acquiredJobs.getJobIdBatches();
    assertThat(jobIdsList).hasSize(1);

    List<String> jobIds = jobIdsList.get(0);

    List<String> expectedJobIds = new ArrayList<>();
    expectedJobIds.add(jobId);

    assertThat(new ArrayList<String>(jobIds)).isEqualTo(expectedJobIds);
    assertThat(tweetHandler.getMessages()).isEmpty();

    ExecuteJobHelper.executeJob(jobId, commandExecutor);

    assertThat(tweetHandler.getMessages().get(0)).isEqualTo("i'm coding a test");
    assertThat(tweetHandler.getMessages()).hasSize(1);

    clearDatabase();
  }

  static final long SOME_TIME = 928374923546L;
  static final long SECOND = 1000;

  @Test
  void testJobCommandsWithTimer() {
    // clock gets automatically reset in LogTestCase.runTest
    ClockUtil.setCurrentTime(new Date(SOME_TIME));

    CommandExecutor commandExecutor = processEngineConfiguration.getCommandExecutorTxRequired();
    JobExecutor jobExecutor = processEngineConfiguration.getJobExecutor();

    String jobId = commandExecutor.execute(commandContext -> {
      TimerEntity timer = createTweetTimer("i'm coding a test", new Date(SOME_TIME + (10 * SECOND)));
      commandContext.getJobManager().schedule(timer);
      return timer.getId();
    });

    AcquiredJobs acquiredJobs = commandExecutor.execute(new AcquireJobsCmd(jobExecutor));
    List<List<String>> jobIdsList = acquiredJobs.getJobIdBatches();
    assertThat(jobIdsList).isEmpty();

    List<String> expectedJobIds = new ArrayList<>();

    ClockUtil.setCurrentTime(new Date(SOME_TIME + (20 * SECOND)));

    acquiredJobs = commandExecutor.execute(new AcquireJobsCmd(jobExecutor, jobExecutor.getMaxJobsPerAcquisition()));
    jobIdsList = acquiredJobs.getJobIdBatches();
    assertThat(jobIdsList).hasSize(1);

    List<String> jobIds = jobIdsList.get(0);

    expectedJobIds.add(jobId);
    assertThat(new ArrayList<String>(jobIds)).isEqualTo(expectedJobIds);

    assertThat(tweetHandler.getMessages()).isEmpty();

    ExecuteJobHelper.executeJob(jobId, commandExecutor);

    assertThat(tweetHandler.getMessages().get(0)).isEqualTo("i'm coding a test");
    assertThat(tweetHandler.getMessages()).hasSize(1);

    clearDatabase();
  }

  protected void clearDatabase() {
    processEngineConfiguration.getCommandExecutorTxRequired().execute(commandContext -> {

      List<HistoricJobLog> historicJobLogs = processEngineConfiguration
          .getHistoryService()
          .createHistoricJobLogQuery()
          .list();

      for (HistoricJobLog historicJobLog : historicJobLogs) {
        commandContext
            .getHistoricJobLogManager()
            .deleteHistoricJobLogById(historicJobLog.getId());
      }

      return null;
    });
  }

}
