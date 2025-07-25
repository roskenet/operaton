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

import static io.restassured.RestAssured.expect;
import static io.restassured.RestAssured.given;
import static io.restassured.path.json.JsonPath.from;
import static org.assertj.core.api.Assertions.assertThat;
import static org.operaton.bpm.engine.rest.util.DateTimeUtils.withTimezone;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.ws.rs.core.Response.Status;

import org.operaton.bpm.engine.impl.calendar.DateTimeUtil;
import org.operaton.bpm.engine.repository.DecisionDefinition;
import org.operaton.bpm.engine.repository.DecisionDefinitionQuery;
import org.operaton.bpm.engine.rest.exception.InvalidRequestException;
import org.operaton.bpm.engine.rest.helper.MockProvider;
import org.operaton.bpm.engine.rest.util.container.TestContainerExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import io.restassured.http.ContentType;
import io.restassured.response.Response;

public class DecisionDefinitionRestServiceQueryTest extends AbstractRestServiceTest {

  @RegisterExtension
  public static TestContainerExtension rule = new TestContainerExtension();

  protected static final String DECISION_DEFINITION_QUERY_URL = TEST_RESOURCE_ROOT_PATH + "/decision-definition";
  protected static final String DECISION_DEFINITION_COUNT_QUERY_URL = DECISION_DEFINITION_QUERY_URL + "/count";

  private DecisionDefinitionQuery mockedQuery;

  @BeforeEach
  void setUpRuntime() {
    mockedQuery = createMockDecisionDefinitionQuery(MockProvider.createMockDecisionDefinitions());
  }

  private DecisionDefinitionQuery createMockDecisionDefinitionQuery(List<DecisionDefinition> mockedDefinitions) {
    DecisionDefinitionQuery sampleDefinitionsQuery = mock(DecisionDefinitionQuery.class);

    when(sampleDefinitionsQuery.list()).thenReturn(mockedDefinitions);
    when(sampleDefinitionsQuery.count()).thenReturn((long) mockedDefinitions.size());
    when(processEngine.getRepositoryService().createDecisionDefinitionQuery()).thenReturn(sampleDefinitionsQuery);

    return sampleDefinitionsQuery;
  }

  @Test
  void testEmptyQuery() {
    given()
      .then()
        .expect()
          .statusCode(Status.OK.getStatusCode())
      .when()
        .get(DECISION_DEFINITION_QUERY_URL);

    verify(mockedQuery).list();
  }

  @Test
  void testInvalidNumericParameter() {
    String anInvalidIntegerQueryParam = "aString";

    given()
      .queryParam("version", anInvalidIntegerQueryParam)
      .then()
        .expect()
          .statusCode(Status.BAD_REQUEST.getStatusCode())
          .contentType(ContentType.JSON)
          .body("type", equalTo(InvalidRequestException.class.getSimpleName()))
          .body("message", equalTo("Cannot set query parameter 'version' to value 'aString': "
            + "Cannot convert value aString to java type java.lang.Integer"))
      .when()
        .get(DECISION_DEFINITION_QUERY_URL);
  }

  /**
   * We assume that boolean query parameters that are not "true"
   * or "false" are evaluated to "false" and don't cause a 400 error.
   */
  @Test
  void testInvalidBooleanParameter() {
    String anInvalidBooleanQueryParam = "neitherTrueNorFalse";
    given()
      .queryParam("active", anInvalidBooleanQueryParam)
      .then()
        .expect()
          .statusCode(Status.OK.getStatusCode())
      .when()
        .get(DECISION_DEFINITION_QUERY_URL);
  }

  @Test
  void testInvalidSortingOptions() {
    executeAndVerifySorting("anInvalidSortByOption", "asc", Status.BAD_REQUEST);
    executeAndVerifySorting("id", "anInvalidSortOrderOption", Status.BAD_REQUEST);
  }

  @Test
  void testSortByParameterOnly() {
    given()
      .queryParam("sortBy", "id")
      .then()
        .expect()
          .statusCode(Status.BAD_REQUEST.getStatusCode())
      .when()
        .get(DECISION_DEFINITION_QUERY_URL);
  }

  @Test
  void testSortOrderParameterOnly() {
    given()
      .queryParam("sortOrder", "asc")
      .then()
        .expect()
          .statusCode(Status.BAD_REQUEST.getStatusCode())
      .when()
        .get(DECISION_DEFINITION_QUERY_URL);
  }

  protected void executeAndVerifySorting(String sortBy, String sortOrder, Status expectedStatus) {
    given()
      .queryParam("sortBy", sortBy)
      .queryParam("sortOrder", sortOrder)
    .then()
      .expect()
        .statusCode(expectedStatus.getStatusCode())
    .when()
      .get(DECISION_DEFINITION_QUERY_URL);
  }

  @Test
  void testSortingParameters() {
    // asc
    InOrder inOrder = inOrder(mockedQuery);
    executeAndVerifySorting("id", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByDecisionDefinitionId();
    inOrder.verify(mockedQuery).asc();

    inOrder = inOrder(mockedQuery);
    executeAndVerifySorting("name", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByDecisionDefinitionName();
    inOrder.verify(mockedQuery).asc();

    inOrder = inOrder(mockedQuery);
    executeAndVerifySorting("version", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByDecisionDefinitionVersion();
    inOrder.verify(mockedQuery).asc();

    inOrder = inOrder(mockedQuery);
    executeAndVerifySorting("key", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByDecisionDefinitionKey();
    inOrder.verify(mockedQuery).asc();

    inOrder = inOrder(mockedQuery);
    executeAndVerifySorting("category", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByDecisionDefinitionCategory();
    inOrder.verify(mockedQuery).asc();

    inOrder = inOrder(mockedQuery);
    executeAndVerifySorting("deploymentId", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByDeploymentId();
    inOrder.verify(mockedQuery).asc();

    inOrder = inOrder(mockedQuery);
    executeAndVerifySorting("deployTime", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByDeploymentTime();
    inOrder.verify(mockedQuery).asc();

    inOrder = inOrder(mockedQuery);
    executeAndVerifySorting("tenantId", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByTenantId();
    inOrder.verify(mockedQuery).asc();

    inOrder = inOrder(mockedQuery);
    executeAndVerifySorting("decisionRequirementsDefinitionKey", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByDecisionRequirementsDefinitionKey();
    inOrder.verify(mockedQuery).asc();

    // desc
    inOrder = inOrder(mockedQuery);
    executeAndVerifySorting("id", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByDecisionDefinitionId();
    inOrder.verify(mockedQuery).desc();

    inOrder = inOrder(mockedQuery);
    executeAndVerifySorting("name", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByDecisionDefinitionName();
    inOrder.verify(mockedQuery).desc();

    inOrder = inOrder(mockedQuery);
    executeAndVerifySorting("version", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByDecisionDefinitionVersion();
    inOrder.verify(mockedQuery).desc();

    inOrder = inOrder(mockedQuery);
    executeAndVerifySorting("key", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByDecisionDefinitionKey();
    inOrder.verify(mockedQuery).desc();

    inOrder = inOrder(mockedQuery);
    executeAndVerifySorting("category", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByDecisionDefinitionCategory();
    inOrder.verify(mockedQuery).desc();

    inOrder = inOrder(mockedQuery);
    executeAndVerifySorting("deploymentId", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByDeploymentId();
    inOrder.verify(mockedQuery).desc();

    inOrder = inOrder(mockedQuery);
    executeAndVerifySorting("deployTime", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByDeploymentTime();
    inOrder.verify(mockedQuery).asc();

    inOrder = inOrder(mockedQuery);
    executeAndVerifySorting("tenantId", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByTenantId();
    inOrder.verify(mockedQuery).desc();

    inOrder = inOrder(mockedQuery);
    executeAndVerifySorting("versionTag", "asc", Status.OK);
    inOrder.verify(mockedQuery).orderByVersionTag();
    inOrder.verify(mockedQuery).asc();

    inOrder = inOrder(mockedQuery);
    executeAndVerifySorting("versionTag", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByVersionTag();
    inOrder.verify(mockedQuery).asc();

    inOrder = inOrder(mockedQuery);
    executeAndVerifySorting("decisionRequirementsDefinitionKey", "desc", Status.OK);
    inOrder.verify(mockedQuery).orderByDecisionRequirementsDefinitionKey();
    inOrder.verify(mockedQuery).desc();
  }

  @Test
  void testSuccessfulPagination() {
    int firstResult = 0;
    int maxResults = 10;

    given()
      .queryParam("firstResult", firstResult)
      .queryParam("maxResults", maxResults)
      .then()
        .expect()
          .statusCode(Status.OK.getStatusCode())
      .when()
        .get(DECISION_DEFINITION_QUERY_URL);

    verify(mockedQuery).listPage(firstResult, maxResults);
  }

  /**
   * If parameter "firstResult" is missing, we expect 0 as default.
   */
  @Test
  void testMissingFirstResultParameter() {
    int maxResults = 10;

    given()
      .queryParam("maxResults", maxResults)
      .then()
        .expect()
          .statusCode(Status.OK.getStatusCode())
      .when()
        .get(DECISION_DEFINITION_QUERY_URL);

    verify(mockedQuery).listPage(0, maxResults);
  }

  /**
   * If parameter "maxResults" is missing, we expect Integer.MAX_VALUE as default.
   */
  @Test
  void testMissingMaxResultsParameter() {
    int firstResult = 10;

    given()
      .queryParam("firstResult", firstResult)
      .then()
        .expect()
          .statusCode(Status.OK.getStatusCode())
      .when()
        .get(DECISION_DEFINITION_QUERY_URL);

    verify(mockedQuery).listPage(firstResult, Integer.MAX_VALUE);
  }

  @Test
  void testDecisionDefinitionRetrieval() {
    Response response = given()
        .then()
          .expect()
            .statusCode(Status.OK.getStatusCode())
        .when()
          .get(DECISION_DEFINITION_QUERY_URL);

    // assert query invocation
    InOrder inOrder = inOrder(mockedQuery);
    inOrder.verify(mockedQuery).list();

    String content = response.asString();
    List<Map<String, String>> decisionDefinitions = from(content).getList("");

    assertThat(decisionDefinitions).hasSize(1);
    assertThat(decisionDefinitions.get(0)).isNotNull();

    String returnedId = from(content).getString("[0].id");
    String returnedKey = from(content).getString("[0].key");
    String returnedCategory = from(content).getString("[0].category");
    String returnedName = from(content).getString("[0].name");
    int returnedVersion = from(content).getInt("[0].version");
    String returnedResource = from(content).getString("[0].resource");
    String returnedDeploymentId = from(content).getString("[0].deploymentId");
    String returnedDecisionRequirementsDefinitionId = from(content).getString("[0].decisionRequirementsDefinitionId");
    String returnedDecisionRequirementsDefinitionKey = from(content).getString("[0].decisionRequirementsDefinitionKey");
    String returnedTenantId = from(content).getString("[0].tenantId");

    assertThat(returnedId).isEqualTo(MockProvider.EXAMPLE_DECISION_DEFINITION_ID);
    assertThat(returnedKey).isEqualTo(MockProvider.EXAMPLE_DECISION_DEFINITION_KEY);
    assertThat(returnedCategory).isEqualTo(MockProvider.EXAMPLE_DECISION_DEFINITION_CATEGORY);
    assertThat(returnedName).isEqualTo(MockProvider.EXAMPLE_DECISION_DEFINITION_NAME);
    assertThat(returnedVersion).isEqualTo(MockProvider.EXAMPLE_DECISION_DEFINITION_VERSION);
    assertThat(returnedResource).isEqualTo(MockProvider.EXAMPLE_DECISION_DEFINITION_RESOURCE_NAME);
    assertThat(returnedDeploymentId).isEqualTo(MockProvider.EXAMPLE_DEPLOYMENT_ID);
    assertThat(returnedDecisionRequirementsDefinitionId).isEqualTo(MockProvider.EXAMPLE_DECISION_REQUIREMENTS_DEFINITION_ID);
    assertThat(returnedDecisionRequirementsDefinitionKey).isEqualTo(MockProvider.EXAMPLE_DECISION_REQUIREMENTS_DEFINITION_KEY);
    assertThat(returnedTenantId).isNull();
  }

  @Test
  void testDecisionDefinitionRetrievalByList() {
    mockedQuery = createMockDecisionDefinitionQuery(MockProvider.createMockTwoDecisionDefinitions());

    Response response = given()
      .queryParam("decisionDefinitionIdIn", MockProvider.EXAMPLE_DECISION_DEFINITION_ID_LIST)
      .then().expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(DECISION_DEFINITION_QUERY_URL);

    // assert query invocation
    InOrder inOrder = inOrder(mockedQuery);
    inOrder.verify(mockedQuery).decisionDefinitionIdIn(MockProvider.EXAMPLE_DECISION_DEFINITION_ID, MockProvider.ANOTHER_EXAMPLE_DECISION_DEFINITION_ID);
    inOrder.verify(mockedQuery).list();

    String content = response.asString();
    List<Map<String, Object>> definitions = from(content).getList("");
    assertThat(definitions).hasSize(2);

    String returnedDefinitionId1 = from(content).getString("[0].id");
    String returnedDefinitionId2 = from(content).getString("[1].id");

    assertThat(returnedDefinitionId1).isEqualTo(MockProvider.EXAMPLE_DECISION_DEFINITION_ID);
    assertThat(returnedDefinitionId2).isEqualTo(MockProvider.ANOTHER_EXAMPLE_DECISION_DEFINITION_ID);
  }

  @Test
  void testDecisionDefinitionRetrievalByEmptyList() {
    given()
      .queryParam("decisionDefinitionIdIn", "")
      .then().expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(DECISION_DEFINITION_QUERY_URL);

    // assert query invocation
    InOrder inOrder = inOrder(mockedQuery);
    inOrder.verify(mockedQuery, never()).decisionDefinitionIdIn(Mockito.any());
    inOrder.verify(mockedQuery).list();
  }

  @Test
  void testAdditionalParameters() {
    Map<String, String> queryParameters = getCompleteQueryParameters();

    given()
      .queryParams(queryParameters)
      .expect()
        .statusCode(Status.OK.getStatusCode())
      .when()
        .get(DECISION_DEFINITION_QUERY_URL);

    // assert query invocation
    verify(mockedQuery).decisionDefinitionId(queryParameters.get("decisionDefinitionId"));
    verify(mockedQuery).decisionDefinitionCategory(queryParameters.get("category"));
    verify(mockedQuery).decisionDefinitionCategoryLike(queryParameters.get("categoryLike"));
    verify(mockedQuery).decisionDefinitionName(queryParameters.get("name"));
    verify(mockedQuery).decisionDefinitionNameLike(queryParameters.get("nameLike"));
    verify(mockedQuery).deploymentId(queryParameters.get("deploymentId"));
    verify(mockedQuery).decisionDefinitionKey(queryParameters.get("key"));
    verify(mockedQuery).decisionDefinitionKeyLike(queryParameters.get("keyLike"));
    verify(mockedQuery).decisionDefinitionVersion(Integer.parseInt(queryParameters.get("version")));
    verify(mockedQuery).latestVersion();
    verify(mockedQuery).decisionDefinitionResourceName(queryParameters.get("resourceName"));
    verify(mockedQuery).decisionDefinitionResourceNameLike(queryParameters.get("resourceNameLike"));
    verify(mockedQuery).decisionRequirementsDefinitionId(queryParameters.get("decisionRequirementsDefinitionId"));
    verify(mockedQuery).decisionRequirementsDefinitionKey(queryParameters.get("decisionRequirementsDefinitionKey"));
    verify(mockedQuery).versionTag(queryParameters.get("versionTag"));
    verify(mockedQuery).versionTagLike(queryParameters.get("versionTagLike"));
    verify(mockedQuery).withoutDecisionRequirementsDefinition();
    verify(mockedQuery).deployedAfter(DateTimeUtil.parseDate(queryParameters.get("deployedAfter")));
    verify(mockedQuery).deployedAt(DateTimeUtil.parseDate(queryParameters.get("deployedAt")));
    verify(mockedQuery).list();
  }

  @Test
  void testDecisionDefinitionTenantIdList() {
    List<DecisionDefinition> decisionDefinitions = Arrays.asList(
        MockProvider.mockDecisionDefinition().tenantId(MockProvider.EXAMPLE_TENANT_ID).build(),
        MockProvider.mockDecisionDefinition().id(MockProvider.ANOTHER_EXAMPLE_CASE_DEFINITION_ID).tenantId(MockProvider.ANOTHER_EXAMPLE_TENANT_ID).build());
    mockedQuery = createMockDecisionDefinitionQuery(decisionDefinitions);

    Response response = given()
      .queryParam("tenantIdIn", MockProvider.EXAMPLE_TENANT_ID_LIST)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .get(DECISION_DEFINITION_QUERY_URL);

    verify(mockedQuery).tenantIdIn(MockProvider.EXAMPLE_TENANT_ID, MockProvider.ANOTHER_EXAMPLE_TENANT_ID);
    verify(mockedQuery).list();

    String content = response.asString();
    List<Map<String, Object>> definitions = from(content).getList("");
    assertThat(definitions).hasSize(2);

    String returnedTenantId1 = from(content).getString("[0].tenantId");
    String returnedTenantId2 = from(content).getString("[1].tenantId");

    assertThat(returnedTenantId1).isEqualTo(MockProvider.EXAMPLE_TENANT_ID);
    assertThat(returnedTenantId2).isEqualTo(MockProvider.ANOTHER_EXAMPLE_TENANT_ID);
  }

  @Test
  void testDecisionDefinitionWithoutTenantId() {
    Response response = given()
      .queryParam("withoutTenantId", true)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .get(DECISION_DEFINITION_QUERY_URL);

    verify(mockedQuery).withoutTenantId();
    verify(mockedQuery).list();

    String content = response.asString();
    List<Map<String, Object>> definitions = from(content).getList("");
    assertThat(definitions).hasSize(1);

    String returnedTenantId1 = from(content).getString("[0].tenantId");
    assertThat(returnedTenantId1).isNull();
  }

  @Test
  void testDecisionDefinitionTenantIdIncludeDefinitionsWithoutTenantid() {
    List<DecisionDefinition> decisionDefinitions = Arrays.asList(
        MockProvider.mockDecisionDefinition().tenantId(null).build(),
        MockProvider.mockDecisionDefinition().tenantId(MockProvider.EXAMPLE_TENANT_ID).build());
    mockedQuery = createMockDecisionDefinitionQuery(decisionDefinitions);

    Response response = given()
      .queryParam("tenantIdIn", MockProvider.EXAMPLE_TENANT_ID)
      .queryParam("includeDecisionDefinitionsWithoutTenantId", true)
    .then().expect()
      .statusCode(Status.OK.getStatusCode())
    .when()
      .get(DECISION_DEFINITION_QUERY_URL);

    verify(mockedQuery).tenantIdIn(MockProvider.EXAMPLE_TENANT_ID);
    verify(mockedQuery).includeDecisionDefinitionsWithoutTenantId();
    verify(mockedQuery).list();

    String content = response.asString();
    List<Map<String, Object>> definitions = from(content).getList("");
    assertThat(definitions).hasSize(2);

    String returnedTenantId1 = from(content).getString("[0].tenantId");
    String returnedTenantId2 = from(content).getString("[1].tenantId");

    assertThat(returnedTenantId1).isNull();
    assertThat(returnedTenantId2).isEqualTo(MockProvider.EXAMPLE_TENANT_ID);
  }

  @Test
  void testQueryCount() {
    expect().statusCode(Status.OK.getStatusCode())
      .body("count", equalTo(1))
      .when().get(DECISION_DEFINITION_COUNT_QUERY_URL);

    verify(mockedQuery).count();
  }

  @Test
  void testDecisionDefinitionVersionTag() {
    List<DecisionDefinition> decisionDefinitions = Arrays.asList(
      MockProvider.mockDecisionDefinition().versionTag(MockProvider.EXAMPLE_VERSION_TAG).build(),
      MockProvider.mockDecisionDefinition().id(MockProvider.ANOTHER_EXAMPLE_DECISION_DEFINITION_ID).versionTag(MockProvider.ANOTHER_EXAMPLE_VERSION_TAG).build());
    mockedQuery = createMockDecisionDefinitionQuery(decisionDefinitions);

    given()
      .queryParam("versionTag", MockProvider.EXAMPLE_VERSION_TAG)
      .then().expect()
      .statusCode(Status.OK.getStatusCode())
      .when()
      .get(DECISION_DEFINITION_QUERY_URL);

    verify(mockedQuery).versionTag(MockProvider.EXAMPLE_VERSION_TAG);
    verify(mockedQuery).list();
  }

  @Test
  void testQueryByDeployTimeAfter() {
    String deployTime = withTimezone("2020-03-27T00:00:00");
    Date date = DateTimeUtil.parseDate(deployTime);

    given().queryParam("deployedAfter", deployTime)
      .then().expect().statusCode(Status.OK.getStatusCode())
      .when().get(DECISION_DEFINITION_QUERY_URL);

    verify(mockedQuery).deployedAfter(date);
    verify(mockedQuery).list();
  }

  @Test
  void testQueryByDeployTimeAt() {
    String deployTime = withTimezone("2020-03-27T00:00:00");
    Date date = DateTimeUtil.parseDate(deployTime);

    given().queryParam("deployedAt", deployTime)
    .then().expect().statusCode(Status.OK.getStatusCode())
    .when().get(DECISION_DEFINITION_QUERY_URL);

    verify(mockedQuery).deployedAt(date);
    verify(mockedQuery).list();
  }

  private Map<String, String> getCompleteQueryParameters() {
    Map<String, String> parameters = new HashMap<>();

    parameters.put("decisionDefinitionId", "anId");
    parameters.put("category", "cat");
    parameters.put("categoryLike", "catlike");
    parameters.put("name", "name");
    parameters.put("nameLike", "namelike");
    parameters.put("deploymentId", "depId");
    parameters.put("deployedAfter", withTimezone("2020-03-27T00:00:00"));
    parameters.put("deployedAt", withTimezone("2020-03-27T00:00:00"));
    parameters.put("key", "key");
    parameters.put("keyLike", "keylike");
    parameters.put("version", "1");
    parameters.put("latestVersion", "true");
    parameters.put("resourceName", "res");
    parameters.put("resourceNameLike", "resLike");
    parameters.put("decisionRequirementsDefinitionId", MockProvider.EXAMPLE_DECISION_REQUIREMENTS_DEFINITION_ID);
    parameters.put("decisionRequirementsDefinitionKey", MockProvider.EXAMPLE_DECISION_REQUIREMENTS_DEFINITION_KEY);
    parameters.put("withoutDecisionRequirementsDefinition", "true");
    parameters.put("versionTag", "semVer");
    parameters.put("versionTagLike", "semVerLike");

    return parameters;
  }

}
