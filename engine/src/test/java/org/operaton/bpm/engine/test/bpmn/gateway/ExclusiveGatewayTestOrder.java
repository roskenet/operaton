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
package org.operaton.bpm.engine.test.bpmn.gateway;

import java.io.Serializable;


/**
 * @author Joram Barrez
 */
public class ExclusiveGatewayTestOrder implements Serializable {

  private static final long serialVersionUID = 1L;

  private int price;

  public ExclusiveGatewayTestOrder(int price) {
    this.price = price;
  }

  public void setPrice(int price) {
    this.price = price;
  }

  public int getPrice() {
    return price;
  }

  public boolean isBasic() {
    return price <= 100;
  }

  public boolean isStandard() {
    return price > 100 && price < 250;
  }

  public boolean isGold() {
    return price >= 250;
  }

}
