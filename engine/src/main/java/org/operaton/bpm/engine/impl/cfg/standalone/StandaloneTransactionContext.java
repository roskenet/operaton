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
package org.operaton.bpm.engine.impl.cfg.standalone;

import java.util.*;

import org.operaton.bpm.engine.impl.ProcessEngineLogger;
import org.operaton.bpm.engine.impl.cfg.TransactionContext;
import org.operaton.bpm.engine.impl.cfg.TransactionListener;
import org.operaton.bpm.engine.impl.cfg.TransactionLogger;
import org.operaton.bpm.engine.impl.cfg.TransactionState;
import org.operaton.bpm.engine.impl.context.Context;
import org.operaton.bpm.engine.impl.db.PersistenceSession;
import org.operaton.bpm.engine.impl.interceptor.CommandContext;

/**
 * @author Sebastian Menski
 */
public class StandaloneTransactionContext implements TransactionContext {

  private static final TransactionLogger LOG = ProcessEngineLogger.TX_LOGGER;

  protected CommandContext commandContext;
  protected Map<TransactionState, List<TransactionListener>> stateTransactionListeners = null;
  private TransactionState lastTransactionState;

  public StandaloneTransactionContext(CommandContext commandContext) {
    this.commandContext = commandContext;
  }

  @Override
  public void addTransactionListener(TransactionState transactionState, TransactionListener transactionListener) {
    if (stateTransactionListeners == null) {
      stateTransactionListeners = new EnumMap<>(TransactionState.class);
    }
    stateTransactionListeners
      .computeIfAbsent(transactionState, k -> new ArrayList<>())
      .add(transactionListener);
  }

  @Override
  public void commit() {
    LOG.debugTransactionOperation("firing event committing...");

    fireTransactionEvent(TransactionState.COMMITTING);

    LOG.debugTransactionOperation("committing the persistence session...");

    getPersistenceProvider().commit();

    LOG.debugTransactionOperation("firing event committed...");

    fireTransactionEvent(TransactionState.COMMITTED);
  }

  protected void fireTransactionEvent(TransactionState transactionState) {
    this.setLastTransactionState(transactionState);
    if (stateTransactionListeners==null) {
      return;
    }
    List<TransactionListener> transactionListeners = stateTransactionListeners.get(transactionState);
    if (transactionListeners==null) {
      return;
    }
    for (TransactionListener transactionListener: transactionListeners) {
      transactionListener.execute(commandContext);
    }
  }

  protected void setLastTransactionState(TransactionState transactionState) {
    this.lastTransactionState = transactionState;
  }

  private PersistenceSession getPersistenceProvider() {
    return commandContext.getSession(PersistenceSession.class);
  }

  @Override
  public void rollback() {
    try {
      try {
        LOG.debugTransactionOperation("firing event rollback...");
        fireTransactionEvent(TransactionState.ROLLINGBACK);

      }
      catch (Throwable exception) {
        LOG.exceptionWhileFiringEvent(TransactionState.ROLLINGBACK, exception);
        Context.getCommandInvocationContext().trySetThrowable(exception);
      }
      finally {
        LOG.debugTransactionOperation("rolling back the persistence session...");
        getPersistenceProvider().rollback();
      }

    }
    catch (Throwable exception) {
      LOG.exceptionWhileFiringEvent(TransactionState.ROLLINGBACK, exception);
      Context.getCommandInvocationContext().trySetThrowable(exception);
    }
    finally {
      LOG.debugFiringEventRolledBack();
      fireTransactionEvent(TransactionState.ROLLED_BACK);
    }
  }

  @Override
  public boolean isTransactionActive() {
    return !TransactionState.ROLLINGBACK.equals(lastTransactionState) && !TransactionState.ROLLED_BACK.equals(lastTransactionState);
  }
}
