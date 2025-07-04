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
package org.operaton.bpm.engine.impl.mock;

import java.util.HashMap;
import java.util.Map;

/** Registry for mock objects.
 *
 * <p>Usage: <code>Mocks.register("myMock", myMock);</code></p>
 *
 * <p>This class lets you register mock objects that will then be used by the
 * {@link MockElResolver}. It binds a map of mock objects to ThreadLocal. This way, the
 * mocks can be set up independent of how the process engine configuration is built.</p>
 *
 * @author Nils Preusker - n.preusker@gmail.com
 */
public class Mocks {

  private static final ThreadLocal<Map<String, Object>> mockContainer = new ThreadLocal<>();

  private Mocks() {
  }

  public static Map<String, Object> getMocks() {
    Map<String, Object> mocks = mockContainer.get();
    if (mocks == null) {
      mocks = new HashMap<>();
      Mocks.mockContainer.set(mocks);
    }
    return mocks;
  }

  /**
   * This method lets you register a mock object. Make sure to register the
   * {@link MockExpressionManager} with your process engine configuration.
   *
   * @param key
   *          the key under which the mock object will be registered
   * @param value
   *          the mock object
   */
  public static void register(String key, Object value) {
    getMocks().put(key, value);
  }

  /**
   * This method returns the mock object registered under the provided key or
   * null if there is no object for the provided key.
   *
   * @param key
   *          the key of the requested object
   * @return the mock object registered under the provided key or null if there
   *         is no object for the provided key
   */
  public static Object get(Object key) {
    return getMocks().get(key);
  }

  /**
   * This method resets the internal map of mock objects.
   */
  public static void reset() {
    if (getMocks() != null) {
      getMocks().clear();
    }
  }
}
