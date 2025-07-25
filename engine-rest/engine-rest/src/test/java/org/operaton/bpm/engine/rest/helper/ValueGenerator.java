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
package org.operaton.bpm.engine.rest.helper;

import java.util.HashMap;
import java.util.Map;

/**
 * Generate distinct values based on a format string
 *
 * @author Thorben Lindhauer
 *
 */
public class ValueGenerator {

  protected int counter = 0;
  protected Map<String, String> values = new HashMap<>();
  protected String valueFormat;

  public ValueGenerator(String valueFormat) {
    this.valueFormat = valueFormat;
  }

  public String getValue(String key) {
    if (!values.containsKey(key)) {
      String nextValue = String.format(valueFormat, counter++);
      values.put(key, nextValue);
    }

    return values.get(key);
  }
}
