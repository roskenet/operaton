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
package org.operaton.bpm.engine.impl.digest;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * In order to distinguish between the used hashed algorithm
 * for the password encryption, as prefix is persisted with the
 * encrypted to the database.
 * The {@link DatabasePrefixHandler} is used to handle the prefix, especially for building
 * the prefix, retrieving the algorithm name from the prefix and
 * removing the prefix name from the hashed password.
 */
public class DatabasePrefixHandler {

  protected Pattern pattern = Pattern.compile("^\\{(.*?)\\}");

  public String generatePrefix(String algorithmName){
    return "{" + algorithmName + "}";
  }

  public String retrieveAlgorithmName(String encryptedPasswordWithPrefix) {
    Matcher matcher = pattern.matcher(encryptedPasswordWithPrefix);
    if(matcher.find()){
      return matcher.group(1);
    }
    return null;
  }

  public String removePrefix(String encryptedPasswordWithPrefix) {
    int index = encryptedPasswordWithPrefix.indexOf("}");
    if(!encryptedPasswordWithPrefix.startsWith("{") || index < 0){
      return null;
    }
    return encryptedPasswordWithPrefix.substring(index+1);
  }

}
