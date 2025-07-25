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

import static org.operaton.bpm.engine.rest.util.DateTimeUtils.DATE_FORMAT_WITH_TIMEZONE;
import static org.operaton.bpm.engine.rest.util.DateTimeUtils.withTimezone;
import static org.hamcrest.Matchers.equalTo;
import static io.restassured.RestAssured.given;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Date;
import jakarta.ws.rs.core.Response.Status;

import org.operaton.bpm.engine.AuthorizationException;
import org.operaton.bpm.engine.ManagementService;
import org.operaton.bpm.engine.management.Metrics;
import org.operaton.bpm.engine.management.MetricsQuery;
import org.operaton.bpm.engine.rest.helper.MockProvider;
import org.operaton.bpm.engine.rest.util.container.TestContainerExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.Test;

/**
 * @author Daniel Meyer
 */
public class MetricsRestServiceInteractionTest extends AbstractRestServiceTest {

  @RegisterExtension
  public static TestContainerExtension rule = new TestContainerExtension();

  public static final String METRICS_URL = TEST_RESOURCE_ROOT_PATH + MetricsRestService.PATH;
  public static final String DELETE_UTW_URL = METRICS_URL + "/task-worker";
  public static final String SINGLE_METER_URL = METRICS_URL + "/{name}";
  public static final String SUM_URL = SINGLE_METER_URL + "/sum";

  protected ManagementService managementServiceMock;
  private MetricsQuery meterQueryMock;

  @BeforeEach
  void setUpRuntimeData() {
    managementServiceMock = mock(ManagementService.class);

    when(processEngine.getManagementService()).thenReturn(managementServiceMock);

    meterQueryMock = MockProvider.createMockMeterQuery();
    when(managementServiceMock.createMetricsQuery()).thenReturn(meterQueryMock);

    when(managementServiceMock.getUniqueTaskWorkerCount(any(), any())).thenReturn(10L);
  }

  @Test
  void testGetInterval() {
    when(meterQueryMock.interval()).thenReturn(MockProvider.createMockMetricIntervalResult());

    given()
      .then()
        .expect()
          .body("[0].name", equalTo("metricName"))
          .body("[0].timestamp", equalTo(withTimezone(new Date(15 * 60 * 1000 * 3))))
          .body("[0].reporter", equalTo("REPORTER"))
          .body("[0].value", equalTo(23))

          .body("[1].name", equalTo("metricName"))
          .body("[1].timestamp", equalTo(withTimezone(new Date(15 * 60 * 1000 * 2))))
          .body("[1].reporter", equalTo("REPORTER"))
          .body("[1].value", equalTo(22))

          .body("[2].name", equalTo("metricName"))
          .body("[2].timestamp", equalTo(withTimezone(new Date(15 * 60 * 1000 * 1))))
          .body("[2].reporter", equalTo("REPORTER"))
          .body("[2].value", equalTo(21))
          .statusCode(Status.OK.getStatusCode())
      .when()
        .get(METRICS_URL);

    verify(meterQueryMock).name(null);
    verify(meterQueryMock).reporter(null);
    verify(meterQueryMock, times(1)).interval();
    verifyNoMoreInteractions(meterQueryMock);
  }

  @Test
  void testGetIntervalByName() {
    given()
      .queryParam("name", MockProvider.EXAMPLE_METRICS_NAME)
      .then()
        .expect()
          .statusCode(Status.OK.getStatusCode())
      .when()
        .get(METRICS_URL);

    verify(meterQueryMock).name(MockProvider.EXAMPLE_METRICS_NAME);
    verify(meterQueryMock).reporter(null);
    verify(meterQueryMock, times(1)).interval();
    verifyNoMoreInteractions(meterQueryMock);
  }


  @Test
  void testGetIntervalByReporter() {
    given()
      .queryParam("reporter", MockProvider.EXAMPLE_METRICS_REPORTER)
      .then()
        .expect()
          .statusCode(Status.OK.getStatusCode())
      .when()
        .get(METRICS_URL);

    verify(meterQueryMock).name(null);
    verify(meterQueryMock).reporter(MockProvider.EXAMPLE_METRICS_REPORTER);
    verify(meterQueryMock, times(1)).interval();
    verifyNoMoreInteractions(meterQueryMock);
  }


  @Test
  void testGetIntervalWithOffset() {
    given()
      .queryParam("firstResult", 10)
      .then()
        .expect()
          .statusCode(Status.OK.getStatusCode())
      .when()
        .get(METRICS_URL);

    verify(meterQueryMock).name(null);
    verify(meterQueryMock).reporter(null);
    verify(meterQueryMock).offset(10);
    verify(meterQueryMock, times(1)).interval();
    verifyNoMoreInteractions(meterQueryMock);
  }

  @Test
  void testGetIntervalWithLimit() {
    given()
      .queryParam("maxResults", 10)
      .then()
        .expect()
          .statusCode(Status.OK.getStatusCode())
      .when()
        .get(METRICS_URL);

    verify(meterQueryMock).name(null);
    verify(meterQueryMock).reporter(null);
    verify(meterQueryMock).limit(10);
    verify(meterQueryMock, times(1)).interval();
    verifyNoMoreInteractions(meterQueryMock);
  }

  @Test
  void testGetIntervalAggregation() {
    given()
      .queryParam("aggregateByReporter", true)
      .then()
        .expect()
          .statusCode(Status.OK.getStatusCode())
      .when()
        .get(METRICS_URL);

    verify(meterQueryMock).name(null);
    verify(meterQueryMock).reporter(null);
    verify(meterQueryMock).aggregateByReporter();
    verify(meterQueryMock, times(1)).interval();
    verifyNoMoreInteractions(meterQueryMock);
  }

  @Test
  void testGetIntervalWithStartDate() {

    given()
      .queryParam("startDate", DATE_FORMAT_WITH_TIMEZONE.format(new Date(0)))
      .then()
        .expect()
          .statusCode(Status.OK.getStatusCode())
      .when()
        .get(METRICS_URL);

    verify(meterQueryMock).name(null);
    verify(meterQueryMock).reporter(null);
    verify(meterQueryMock).startDate(new Date(0));
    verify(meterQueryMock, times(1)).interval();
    verifyNoMoreInteractions(meterQueryMock);
  }

  @Test
  void testGetIntervalWithEndDate() {

    given()
      .queryParam("endDate", DATE_FORMAT_WITH_TIMEZONE.format(new Date(15 * 60 * 1000)))
      .then()
        .expect()
          .statusCode(Status.OK.getStatusCode())
      .when()
        .get(METRICS_URL);

    verify(meterQueryMock).name(null);
    verify(meterQueryMock).reporter(null);
    verify(meterQueryMock).endDate(new Date(15 * 60 * 1000));
    verify(meterQueryMock, times(1)).interval();
    verifyNoMoreInteractions(meterQueryMock);
  }

  @Test
  void testGetIntervalWithCustomInterval() {
    given()
      .queryParam("interval", 300)
      .then()
        .expect()
          .statusCode(Status.OK.getStatusCode())
      .when()
        .get(METRICS_URL);

    verify(meterQueryMock).name(null);
    verify(meterQueryMock).reporter(null);
    verify(meterQueryMock, times(1)).interval(300);
    verifyNoMoreInteractions(meterQueryMock);
  }

  @Test
  void testGetIntervalWithAll() {

    given()
      .queryParam("name", MockProvider.EXAMPLE_METRICS_NAME)
      .queryParam("reporter", MockProvider.EXAMPLE_METRICS_REPORTER)
      .queryParam("maxResults", 10)
      .queryParam("firstResult", 10)
      .queryParam("startDate", DATE_FORMAT_WITH_TIMEZONE.format(new Date(0)))
      .queryParam("endDate", DATE_FORMAT_WITH_TIMEZONE.format(new Date(15 * 60 * 1000)))
      .queryParam("aggregateByReporter", true)
      .queryParam("interval", 300)
      .then()
        .expect()
          .statusCode(Status.OK.getStatusCode())
      .when()
        .get(METRICS_URL);

    verify(meterQueryMock).name(MockProvider.EXAMPLE_METRICS_NAME);
    verify(meterQueryMock).reporter(MockProvider.EXAMPLE_METRICS_REPORTER);
    verify(meterQueryMock).offset(10);
    verify(meterQueryMock).limit(10);
    verify(meterQueryMock).startDate(new Date(0));
    verify(meterQueryMock).endDate(new Date(15 * 60 * 1000));
    verify(meterQueryMock).aggregateByReporter();
    verify(meterQueryMock, times(1)).interval(300);
    verifyNoMoreInteractions(meterQueryMock);
  }

  @Test
  void testGetSum() {

    when(meterQueryMock.sum()).thenReturn(10L);

    given()
      .pathParam("name", Metrics.ACTIVTY_INSTANCE_START)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .body("result", equalTo(10))
     .when()
      .get(SUM_URL);

    verify(meterQueryMock).name(Metrics.ACTIVTY_INSTANCE_START);
    verify(meterQueryMock, times(1)).sum();
    verifyNoMoreInteractions(meterQueryMock);
  }

  @Test
  void testGetSumWithTimestamps() {

    when(meterQueryMock.sum()).thenReturn(10L);

    given()
      .pathParam("name", Metrics.ACTIVTY_INSTANCE_START)
      .queryParam("startDate", MockProvider.EXAMPLE_METRICS_START_DATE)
      .queryParam("endDate", MockProvider.EXAMPLE_METRICS_END_DATE)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .body("result", equalTo(10))
     .when()
      .get(SUM_URL);

    verify(meterQueryMock).name(Metrics.ACTIVTY_INSTANCE_START);
    verify(meterQueryMock).startDate(any(Date.class));
    verify(meterQueryMock).endDate(any(Date.class));
    verify(meterQueryMock, times(1)).sum();
    verifyNoMoreInteractions(meterQueryMock);
  }

  @Test
  void testGetSumWithInvalidTimestamp() {

    when(meterQueryMock.sum()).thenReturn(10L);

    given()
      .pathParam("name", Metrics.ACTIVTY_INSTANCE_START)
      .queryParam("startDate", "INVALID-TIME-STAMP")
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
     .when()
      .get(SUM_URL);

  }

  @Test
  void testGetUtw() {

    given()
      .pathParam("name", Metrics.UNIQUE_TASK_WORKERS)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .body("result", equalTo(10))
     .when()
      .get(SUM_URL);

    verify(managementServiceMock, times (1)).getUniqueTaskWorkerCount(null, null);
    verifyNoMoreInteractions(managementServiceMock);
  }

  @Test
  void testGetTaskUsers() {

    given()
      .pathParam("name", Metrics.TASK_USERS)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .body("result", equalTo(10))
     .when()
      .get(SUM_URL);

    verify(managementServiceMock, times (1)).getUniqueTaskWorkerCount(null, null);
    verifyNoMoreInteractions(managementServiceMock);
  }

  @Test
  void testGetUtwWithTimestamps() {

    given()
      .pathParam("name", Metrics.UNIQUE_TASK_WORKERS)
      .queryParam("startDate", MockProvider.EXAMPLE_METRICS_START_DATE)
      .queryParam("endDate", MockProvider.EXAMPLE_METRICS_END_DATE)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .body("result", equalTo(10))
     .when()
      .get(SUM_URL);

    verify(managementServiceMock, times(1)).getUniqueTaskWorkerCount(any(Date.class), any(Date.class));
    verifyNoMoreInteractions(managementServiceMock);
  }

  @Test
  void testGetUtwWithInvalidTimestamp() {

    given()
      .pathParam("name", Metrics.UNIQUE_TASK_WORKERS)
      .queryParam("startDate", "INVALID-TIME-STAMP")
    .then().expect()
      .statusCode(Status.BAD_REQUEST.getStatusCode())
     .when()
      .get(SUM_URL);

    verifyNoMoreInteractions(managementServiceMock);
  }

  @Test
  void testDeleteUtwWithTimestamp() {
    Date date = MockProvider.createMockDuedate();

    given()
      .queryParam("date", DATE_FORMAT_WITH_TIMEZONE.format(date))
    .then().expect()
      .statusCode(Status.NO_CONTENT.getStatusCode())
     .when()
      .delete(DELETE_UTW_URL);

    verify(managementServiceMock, times(1)).deleteTaskMetrics(date);
    verifyNoMoreInteractions(managementServiceMock);
  }

  @Test
  void testDeleteUtwWithoutTimestamp() {
    given()
    .then().expect()
      .statusCode(Status.NO_CONTENT.getStatusCode())
    .when()
      .delete(DELETE_UTW_URL);

    verify(managementServiceMock, times(1)).deleteTaskMetrics(null);
    verifyNoMoreInteractions(managementServiceMock);
  }

  @Test
  void testDeleteUtwThrowsAuthorizationException() {
    String message = "expected exception";
    doThrow(new AuthorizationException(message)).when(managementServiceMock).deleteTaskMetrics(any());

    given()
    .then().expect()
      .statusCode(Status.FORBIDDEN.getStatusCode())
      .body("type", equalTo(AuthorizationException.class.getSimpleName()))
      .body("message", equalTo(message))
    .when()
      .delete(DELETE_UTW_URL);
  }

}
