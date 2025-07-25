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
package org.operaton.spin.json.mapping;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * @author Svetlana Dorokhova.
 */

@JsonDeserialize(contentAs = RegularCustomer.class)
public class GenericCustomerList<T extends Customer> implements List<T> {

  protected List<T> innerList = new ArrayList<>();

  @Override
  public int size() {
    return innerList.size();
  }

  @Override
  public boolean isEmpty() {
    return innerList.isEmpty();
  }

  @Override
  public boolean contains(Object o) {
    return innerList.contains(o);
  }

  @Override
  public Iterator<T> iterator() {
    return innerList.iterator();
  }

  @Override
  public Object[] toArray() {
    return innerList.toArray();
  }

  @Override
  public <T> T[] toArray(T[] a) {
    return innerList.toArray(a);
  }

  @Override
  public boolean add(T customer) {
    return innerList.add(customer);
  }

  @Override
  public boolean remove(Object o) {
    return innerList.remove(o);
  }

  @Override
  public boolean containsAll(Collection<?> c) {
    return innerList.containsAll(c);
  }

  @Override
  public boolean addAll(Collection<? extends T> c) {
    return innerList.addAll(c);
  }

  @Override
  public boolean addAll(int index, Collection<? extends T> c) {
    return innerList.addAll(index, c);
  }

  @Override
  public boolean removeAll(Collection<?> c) {
    return innerList.removeAll(c);
  }

  @Override
  public boolean retainAll(Collection<?> c) {
    return innerList.retainAll(c);
  }

  @Override
  public void clear() {
    innerList.clear();
  }

  @Override
  public boolean equals(Object o) {
    return innerList.equals(o);
  }

  @Override
  public int hashCode() {
    return innerList.hashCode();
  }

  @Override
  public T get(int index) {
    return innerList.get(index);
  }

  @Override
  public T set(int index, T element) {
    return innerList.set(index, element);
  }

  @Override
  public void add(int index, T element) {
    innerList.add(index, element);
  }

  @Override
  public T remove(int index) {
    return innerList.remove(index);
  }

  @Override
  public int indexOf(Object o) {
    return innerList.indexOf(o);
  }

  @Override
  public int lastIndexOf(Object o) {
    return innerList.lastIndexOf(o);
  }

  @Override
  public ListIterator<T> listIterator() {
    return innerList.listIterator();
  }

  @Override
  public ListIterator<T> listIterator(int index) {
    return innerList.listIterator(index);
  }

  @Override
  public List<T> subList(int fromIndex, int toIndex) {
    return innerList.subList(fromIndex, toIndex);
  }
}
