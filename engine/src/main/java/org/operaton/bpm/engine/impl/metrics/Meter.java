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
package org.operaton.bpm.engine.impl.metrics;

import java.util.concurrent.atomic.AtomicLong;

/**
 * A Meter implementation based on AtomicLong
 *
 * @author Daniel Meyer
 *
 */
public class Meter {

  protected AtomicLong counter = new AtomicLong(0);

  protected String name;

  public Meter(String name) {
    this.name = name;
  }

  public void mark() {
    counter.incrementAndGet();
  }

  public void markTimes(long times) {
    counter.addAndGet(times);
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public long getAndClear() {
    return counter.getAndSet(0);
  }

  public long get() {
    return counter.get();
  }

}
