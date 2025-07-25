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
package org.operaton.bpm.engine.impl.cmd;

import java.io.Serializable;
import java.util.Map;

import org.operaton.bpm.engine.impl.interceptor.Command;
import org.operaton.bpm.engine.impl.interceptor.CommandContext;
import org.operaton.bpm.engine.impl.persistence.entity.IdentityInfoEntity;


/**
 * @author Tom Baeyens
 */
public class SetUserInfoCmd implements Command<Object>, Serializable {

  private static final long serialVersionUID = 1L;
  protected String userId;
  protected String userPassword;
  protected String type;
  protected String key;
  protected String value;
  protected String accountPassword;
  protected Map<String, String> accountDetails;

  public SetUserInfoCmd(String userId, String key, String value) {
    this.userId = userId;
    this.type = IdentityInfoEntity.TYPE_USERINFO;
    this.key = key;
    this.value = value;
  }

  public SetUserInfoCmd(String userId, String userPassword, String accountName, String accountUsername, String accountPassword, Map<String, String> accountDetails) {
    this.userId = userId;
    this.userPassword = userPassword;
    this.type = IdentityInfoEntity.TYPE_USERACCOUNT;
    this.key = accountName;
    this.value = accountUsername;
    this.accountPassword = accountPassword;
    this.accountDetails = accountDetails;
  }

  @Override
  public Object execute(CommandContext commandContext) {
    commandContext
      .getIdentityInfoManager()
      .setUserInfo(userId, userPassword, type, key, value, accountPassword, accountDetails);
    return null;
  }
}
