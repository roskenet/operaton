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

'use strict';

var Table = require('./../../table');

module.exports = Table.extend({
  tabRepeater: 'tabProvider in processDefinitionTabs',
  tabIndex: 0,
  tabLabel: 'Process Instances',
  tableRepeater: 'processInstance in processInstances',

  isInstanceSuspended: function(idx) {
    return this.tableItem(idx, '.state .badge-suspended').isDisplayed();
  },

  selectInstanceId: function(idx) {
    this.tableItem(idx, by.binding('processInstance.id')).click();
  },

  instanceId: function(idx) {
    return this.tableItem(idx, '[title]').getAttribute('title');
  },

  startTime: function(idx) {
    return this.tableItem(idx, '.start-time');
  },

  businessKey: function(idx) {
    return this.tableItem(idx, '.business-key');
  }
});
