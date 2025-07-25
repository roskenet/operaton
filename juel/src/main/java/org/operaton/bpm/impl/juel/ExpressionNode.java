/*
 * Based on JUEL 2.2.1 code, 2006-2009 Odysseus Software GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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
package org.operaton.bpm.impl.juel;

import jakarta.el.ELContext;
import jakarta.el.MethodInfo;
import jakarta.el.ValueReference;

/**
 * Expression node interface. This interface provides all the methods needed for value expressions
 * and method expressions.
 *
 * @see Tree
 * @author Christoph Beck
 */
public interface ExpressionNode extends Node {
	/**
	 * @return <code>true</code> if this node represents literal text
	 */
	boolean isLiteralText();

	/**
	 * @return <code>true</code> if the subtree rooted at this node could be used as an lvalue
	 *         expression (identifier or property sequence with non-literal prefix).
	 */
	boolean isLeftValue();

	/**
	 * @return <code>true</code> if the subtree rooted at this node is a method invocation.
	 */
	boolean isMethodInvocation();

	/**
	 * Evaluate node.
	 *
	 * @param bindings
	 *            bindings containing variables and functions
	 * @param context
	 *            evaluation context
	 * @param expectedType
	 *            result type
	 * @return evaluated node, coerced to the expected type
	 */
	Object getValue(Bindings bindings, ELContext context, Class<?> expectedType);

	/**
	 * Get value reference.
	 *
	 * @param bindings
	 * @param context
	 * @return value reference
	 */
	ValueReference getValueReference(Bindings bindings, ELContext context);

	/**
	 * Get the value type accepted in {@link #setValue(Bindings, ELContext, Object)}.
	 *
	 * @param bindings
	 *            bindings containing variables and functions
	 * @param context
	 *            evaluation context
	 * @return accepted type or <code>null</code> for non-lvalue nodes
	 */
	Class<?> getType(Bindings bindings, ELContext context);

	/**
	 * Determine whether {@link #setValue(Bindings, ELContext, Object)} will throw a
	 * {@link jakarta.el.PropertyNotWritableException}.
	 *
	 * @param bindings
	 *            bindings containing variables and functions
	 * @param context
	 *            evaluation context
	 * @return <code>true</code> if this a read-only expression node
	 */
	boolean isReadOnly(Bindings bindings, ELContext context);

	/**
	 * Assign value.
	 *
	 * @param bindings
	 *            bindings containing variables and functions
	 * @param context
	 *            evaluation context
	 * @param value
	 *            value to set
	 */
	void setValue(Bindings bindings, ELContext context, Object value);

	/**
	 * Get method information. If this is a non-lvalue node, answer <code>null</code>.
	 *
	 * @param bindings
	 *            bindings containing variables and functions
	 * @param context
	 *            evaluation context
	 * @param returnType
	 *            expected method return type (may be <code>null</code> meaning don't care)
	 * @param paramTypes
	 *            expected method argument types
	 * @return method information or <code>null</code>
	 */
  MethodInfo getMethodInfo(Bindings bindings, ELContext context, Class<?> returnType, Class<?>[] paramTypes);

	/**
	 * Invoke method.
	 *
	 * @param bindings
	 *            bindings containing variables and functions
	 * @param context
	 *            evaluation context
	 * @param returnType
	 *            expected method return type (may be <code>null</code> meaning don't care)
	 * @param paramTypes
	 *            expected method argument types
	 * @param paramValues
	 *            parameter values
	 * @return result of the method invocation
	 */
  Object invoke(Bindings bindings, ELContext context, Class<?> returnType, Class<?>[] paramTypes, Object[] paramValues);

	/**
	 * Get the canonical expression string for this node. Variable and funtion names will be
	 * replaced in a way such that two expression nodes that have the same node structure and
	 * bindings will also answer the same value here.
	 * <p/>
	 * For example, <code>"${foo:bar()+2*foobar}"</code> may lead to
	 * <code>"${&lt;fn>() + 2 * &lt;var>}"</code> if <code>foobar</code> is a bound variable.
	 * Otherwise, the structural id would be <code>"${&lt;fn>() + 2 * foobar}"</code>.
	 * <p/>
	 * If the bindings is <code>null</code>, the full canonical subexpression is returned.
	 */
  String getStructuralId(Bindings bindings);
}
