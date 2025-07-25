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
package org.operaton.bpm.engine.cdi.impl.el;

import jakarta.el.ELContext;
import jakarta.el.ELResolver;
import jakarta.el.FunctionMapper;
import jakarta.el.VariableMapper;
import java.util.Locale;

/**
 * @author Daniel Meyer
 *
 */
public class ElContextDelegate extends ELContext {

  protected final jakarta.el.ELContext delegateContext;

  protected final ELResolver elResolver;

  public ElContextDelegate(ELContext delegateContext, ELResolver elResolver) {
    this.delegateContext = delegateContext;
    this.elResolver = elResolver;
  }

  @Override
  public ELResolver getELResolver() {
    return elResolver;
  }

  @Override
  public FunctionMapper getFunctionMapper() {
    return null;
  }

  @Override
  public VariableMapper getVariableMapper() {
    return null;
  }

  // delegate methods ////////////////////////////

  @Override
  public Object getContext(Class key) {
    return delegateContext.getContext(key);
  }

  @Override
  public boolean equals(Object obj) {
    return delegateContext.equals(obj);
  }

  @Override
  public int hashCode() {
    return delegateContext.hashCode();
  }

  @Override
  public Locale getLocale() {
    return delegateContext.getLocale();
  }

  @Override
  public boolean isPropertyResolved() {
    return delegateContext.isPropertyResolved();
  }

  @Override
  public void putContext(Class key, Object contextObject) {
    delegateContext.putContext(key, contextObject);
  }

  @Override
  public void setLocale(Locale locale) {
    delegateContext.setLocale(locale);
  }

  @Override
  public void setPropertyResolved(boolean resolved) {
    delegateContext.setPropertyResolved(resolved);
  }

}
