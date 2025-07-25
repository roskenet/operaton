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
package org.operaton.bpm.admin;

/**
 * The admin application. Provides access to the admin core services.
 *
 * @author Daniel Meyer
 *
 */
public class Admin {
  private Admin() {
  }

  /**
   * The {@link AdminRuntimeDelegate} is an delegate that will be
   * initialized by bootstrapping operaton admin with an specific
   * instance
   */
  protected static AdminRuntimeDelegate adminRuntimeDelegate;

  /**
   * Returns an instance of {@link AdminRuntimeDelegate}
   *
   * @return
   */
  public static AdminRuntimeDelegate getRuntimeDelegate() {
    return adminRuntimeDelegate;
  }

  /**
   * A setter to set the {@link AdminRuntimeDelegate}.
   * @param adminRuntimeDelegate
   */
  public static void setAdminRuntimeDelegate(AdminRuntimeDelegate adminRuntimeDelegate) {
    Admin.adminRuntimeDelegate = adminRuntimeDelegate;
  }

}
