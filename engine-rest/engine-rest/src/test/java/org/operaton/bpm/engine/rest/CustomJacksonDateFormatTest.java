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
package org.operaton.bpm.engine.rest;

import static io.restassured.RestAssured.given;
import static org.operaton.bpm.engine.rest.mapper.JacksonConfigurator.DEFAULT_DATE_FORMAT;
import static org.hamcrest.Matchers.is;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import jakarta.ws.rs.core.Response.Status;

import org.operaton.bpm.engine.impl.RuntimeServiceImpl;
import org.operaton.bpm.engine.rest.helper.variable.EqualsPrimitiveValue;
import org.operaton.bpm.engine.rest.mapper.JacksonConfigurator;
import org.operaton.bpm.engine.rest.util.VariablesBuilder;
import org.operaton.bpm.engine.rest.util.container.TestContainerExtension;
import org.operaton.bpm.engine.variable.Variables;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import io.restassured.http.ContentType;

@Disabled("See PR#52: Causes an issue resolving operaton-core-internal-dependencies")
public class CustomJacksonDateFormatTest extends AbstractRestServiceTest {

  @RegisterExtension
  public static TestContainerExtension rule = new TestContainerExtension();

  protected static final String PROCESS_INSTANCE_URL = TEST_RESOURCE_ROOT_PATH + "/process-instance";
  protected static final String SINGLE_PROCESS_INSTANCE_URL = PROCESS_INSTANCE_URL + "/{id}";
  protected static final String PROCESS_INSTANCE_VARIABLES_URL = SINGLE_PROCESS_INSTANCE_URL + "/variables";
  protected static final String SINGLE_PROCESS_INSTANCE_VARIABLE_URL = PROCESS_INSTANCE_VARIABLES_URL + "/{varId}";

  protected static final SimpleDateFormat TEST_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
  protected static final Date TEST_DATE = new Date(1450282812000L);
  protected static final String TEST_DATE_FORMATTED = TEST_DATE_FORMAT.format(TEST_DATE);

  protected RuntimeServiceImpl runtimeServiceMock;

  @BeforeEach
  void setUpRuntimeData() {
    runtimeServiceMock = mock(RuntimeServiceImpl.class);

    when(runtimeServiceMock.getVariableTyped(EXAMPLE_PROCESS_INSTANCE_ID, EXAMPLE_VARIABLE_KEY, true))
      .thenReturn(Variables.dateValue(TEST_DATE));

    when(processEngine.getRuntimeService()).thenReturn(runtimeServiceMock);
  }

  @AfterAll
  static void reset() {
    JacksonConfigurator.setDateFormatString(DEFAULT_DATE_FORMAT);
  }

  @Test
  void testGetDateVariable() {
    given()
        .pathParam("id", EXAMPLE_PROCESS_INSTANCE_ID)
        .pathParam("varId", EXAMPLE_VARIABLE_KEY)
      .then().expect()
        .statusCode(Status.OK.getStatusCode())
        .body("value", is(TEST_DATE_FORMATTED))
        .body("type", is("Date"))
      .when()
        .get(SINGLE_PROCESS_INSTANCE_VARIABLE_URL);
  }

  @Test
  void testSetDateVariable() {
    String variableValue = TEST_DATE_FORMAT.format(TEST_DATE);

    Map<String, Object> variableJson = VariablesBuilder.getVariableValueMap(variableValue, "Date");

    given()
        .pathParam("id", EXAMPLE_PROCESS_INSTANCE_ID)
        .pathParam("varId", EXAMPLE_VARIABLE_KEY)
        .contentType(ContentType.JSON)
        .body(variableJson)
      .then().expect()
        .statusCode(Status.NO_CONTENT.getStatusCode())
      .when()
        .put(SINGLE_PROCESS_INSTANCE_VARIABLE_URL);

    verify(runtimeServiceMock).setVariable(eq(EXAMPLE_PROCESS_INSTANCE_ID), eq(EXAMPLE_VARIABLE_KEY),
      argThat(EqualsPrimitiveValue.dateValue(TEST_DATE)));
  }

}
