--
-- Copyright Camunda Services GmbH and/or licensed to Camunda Services GmbH
-- under one or more contributor license agreements. See the NOTICE file
-- distributed with this work for additional information regarding copyright
-- ownership. Camunda licenses this file to you under the Apache License,
-- Version 2.0; you may not use this file except in compliance with the License.
-- You may obtain a copy of the License at
--
--     https://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.
--

insert into ACT_GE_SCHEMA_LOG
values ('103', CURRENT_TIMESTAMP, '7.12.12');


-- insert telemetry.lock in property table - https://jira.camunda.com/browse/CAM-12023  --
insert into ACT_GE_PROPERTY
values ('telemetry.lock', '0', 1);

-- insert installationId.lock in property table - https://jira.camunda.com/browse/CAM-12031  --
insert into ACT_GE_PROPERTY
values ('installationId.lock', '0', 1);
