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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.operaton.bpm.engine.identity.User;

public class MockUserBuilder {

  protected String id;
  protected String firstName;
  protected String lastName;
  protected String email;
  protected String password;

  public MockUserBuilder id(String id) {
    this.id = id;
    return this;
  }

  public MockUserBuilder firstName(String firstName) {
    this.firstName = firstName;
    return this;
  }

  public MockUserBuilder lastName(String lastName) {
    this.lastName = lastName;
    return this;
  }

  public MockUserBuilder email(String email) {
    this.email = email;
    return this;
  }

  public MockUserBuilder password(String password) {
    this.password = password;
    return this;
  }

  @SuppressWarnings("unchecked")
  public User build() {
    User user = mock(User.class);
    when(user.getId()).thenReturn(id);
    when(user.getFirstName()).thenReturn(firstName);
    when(user.getLastName()).thenReturn(lastName);
    when(user.getEmail()).thenReturn(email);
    when(user.getPassword()).thenReturn(password);
    return user;
  }

}
