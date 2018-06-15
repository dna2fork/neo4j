/*
 * Copyright (c) 2002-2018 "Neo4j,"
 * Neo4j Sweden AB [http://neo4j.com]
 *
 * This file is part of Neo4j Enterprise Edition. The included source
 * code can be redistributed and/or modified under the terms of the
 * GNU AFFERO GENERAL PUBLIC LICENSE Version 3
 * (http://www.fsf.org/licensing/licenses/agpl-3.0.html) with the
 * Commons Clause, as found in the associated LICENSE.txt file.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * Neo4j object code can be licensed independently from the source
 * under separate terms from the AGPL. Inquiries can be directed to:
 * licensing@neo4j.com
 *
 * More information is also available at:
 * https://neo4j.com/licensing/
 */
package org.neo4j.cypher.internal.runtime.compiled.expressions

import org.neo4j.codegen.MethodReference
import org.neo4j.values.storable.{FloatingPointValue, IntegralValue, TextValue}

import scala.reflect.ClassTag

/**
  * IntermediateRepresentation is an intermediate step between pure byte code and the operator/expression
  *
  * The representation is intended to be quite low level and fairly close to the actual bytecode representation.
  */
sealed trait IntermediateRepresentation

/**
  * Invoke a static method
  *
  * @param method the method to invoke
  * @param params the parameter to the static method
  */
case class InvokeStatic(method: Method, params: Seq[IntermediateRepresentation]) extends IntermediateRepresentation

/**
  * Invoke a method
  *
  * @param target the target to call the method on
  * @param method the method to invoke
  * @param params the parameter to the method
  */
case class Invoke(target: IntermediateRepresentation, method: Method, params: Seq[IntermediateRepresentation])
  extends IntermediateRepresentation

/**
  * Load a local variable by name
  *
  * @param variable the name of the variable
  */
case class Load(variable: String) extends IntermediateRepresentation

/**
  * Loads constant IntegralValue
  *
  * @param value the constant value
  */
case class Integer(value: IntegralValue) extends IntermediateRepresentation

/**
  * Constant FloatingPointValue
  *
  * @param value the constant value
  */
case class Float(value: FloatingPointValue) extends IntermediateRepresentation

/**
  * Constant TextValue
  *
  * @param value the constant value
  */
case class StringLiteral(value: TextValue) extends IntermediateRepresentation

/**
  * Constant java value
  *
  * @param value the constant value
  */
case class Constant(value: Any) extends IntermediateRepresentation

/**
  * Load NO_VALUE
  */
case object NULL extends IntermediateRepresentation

/**
  * Load TRUE
  */
case object TRUE extends IntermediateRepresentation


/**
  * Load FALSE
  */
case object FALSE extends IntermediateRepresentation

/**
  * Loads an array literal of the given inputs
  *
  * @param values the values of the array
  */
case class ArrayLiteral(values: Array[IntermediateRepresentation]) extends IntermediateRepresentation

/**
  * Defines ternary expression, i.e. {{{condition ? onTrue : onFalse}}}
  *
  * @param condition the condition to test
  * @param onTrue    will be evaluted if condition is true
  * @param onFalse   will be evaluated if condition is false
  */
case class Ternary(condition: IntermediateRepresentation,
                   onTrue: IntermediateRepresentation,
                   onFalse: IntermediateRepresentation) extends IntermediateRepresentation

/**
  * Defines equality or identy, i.e. {{{lhs == rhs}}}
  *
  * @param lhs the left-hand side to check
  * @param rhs the right-hand side to check
  */
case class Eq(lhs: IntermediateRepresentation, rhs: IntermediateRepresentation) extends IntermediateRepresentation

/**
  * Defines  {{{lhs != rhs}}}
  *
  * @param lhs the left-hand side to check
  * @param rhs the right-hand side to check
  */
case class NotEq(lhs: IntermediateRepresentation, rhs: IntermediateRepresentation) extends IntermediateRepresentation

/**
  * A block is a sequence of operations where the block evaluates to the last expression
  * @param ops the operations to perform in the block
  */
case class Block(ops: Seq[IntermediateRepresentation]) extends IntermediateRepresentation

/**
  * A conditon executes the operation if the test evaluates to true.
  *
  *  {{{
  *  if (test)
  *  {
  *    onTrue;
  *  }
  *  }}}
  * @param test the condition to check
  * @param onTrue the opertation to perform if the `test` evaluates to true
  */
case class Condition(test: IntermediateRepresentation, onTrue: IntermediateRepresentation)
  extends IntermediateRepresentation

/**
  * Declare a local variable of the given type.
  *
  * {{{
  * typ name;
  * }}}
  * @param typ the type of the variable
  * @param name the name of the variable
  */
case class DeclareLocalVariable(typ: Class[_], name: String) extends IntermediateRepresentation

/**
  * Assign a variable to a value.
  *
  * {{{
  * name = value;
  * }}}
  * @param name the name of the variable
  * @param value the value to assign to the variable
  */
case class AssignToLocalVariable(name: String, value: IntermediateRepresentation) extends IntermediateRepresentation

/**
  * try-catch block
  * {{{
  *   try
  *   {
  *     ops;
  *   }
  *   catch (exception name)
  *   {
  *     onError;
  *   }
  * }}}
  * @param ops the operation to perform in the happy path
  * @param onError the operation to perform if an exception is caught
  * @param exception the type of the exception
  * @param name the name of the caught exception
  */
case class TryCatch(ops: IntermediateRepresentation, onError: IntermediateRepresentation, exception: Class[_], name: String) extends IntermediateRepresentation

/**
  * Throw an error
  * @param error the error to throw
  */
case class Throw(error: IntermediateRepresentation) extends IntermediateRepresentation

/**
  * Boolean && operator
  * {{{
  *   lhs && rhs;
  * }}}
  * @param lhs the left-hand side of and
  * @param rhs the right-hand side of and
  */
case class BooleanAnd(lhs: IntermediateRepresentation, rhs: IntermediateRepresentation) extends IntermediateRepresentation

/**
  * Defines a method
  *
  * @param owner  the owner of the method
  * @param output output type to the method
  * @param name   the name of the method
  * @param params the parameter types of the method
  */
case class Method(owner: Class[_], output: Class[_], name: String, params: Class[_]*) {

  def asReference: MethodReference = MethodReference.methodReference(owner, output, name, params: _*)
}

/**
  * Defines a simple dsl to facilitate constructing intermediate representation
  */
object IntermediateRepresentation {

  def method[OWNER, OUT](name: String)(implicit owner: ClassTag[OWNER], out: ClassTag[OUT]) =
    Method(owner.runtimeClass, out.runtimeClass, name)

  def method[OWNER, OUT, IN](name: String)(implicit owner: ClassTag[OWNER], out: ClassTag[OUT], in: ClassTag[IN]) =
    Method(owner.runtimeClass, out.runtimeClass, name, in.runtimeClass)

  def method[OWNER, OUT, IN1, IN2](name: String)
                                  (implicit owner: ClassTag[OWNER], out: ClassTag[OUT], in1: ClassTag[IN1],
                                   in2: ClassTag[IN2]) =
    Method(owner.runtimeClass, out.runtimeClass, name, in1.runtimeClass, in2.runtimeClass)

  def method[OWNER, OUT, IN1, IN2, IN3](name: String)
                                       (implicit owner: ClassTag[OWNER], out: ClassTag[OUT], in1: ClassTag[IN1],
                                        in2: ClassTag[IN2], in3: ClassTag[IN3]) =
    Method(owner.runtimeClass, out.runtimeClass, name, in1.runtimeClass, in2.runtimeClass, in3.runtimeClass)

  def invokeStatic(method: Method, params: IntermediateRepresentation*): IntermediateRepresentation = InvokeStatic(
    method, params)

  def invoke(owner: IntermediateRepresentation, method: Method,
             params: IntermediateRepresentation*): IntermediateRepresentation =
    Invoke(owner, method, params)

  def load(variable: String): IntermediateRepresentation = Load(variable)

  def noValue: IntermediateRepresentation = NULL

  def truthValue: IntermediateRepresentation = TRUE

  def falseValue: IntermediateRepresentation = FALSE

  def constant(value: Any): IntermediateRepresentation = Constant(value)

  def arrayOf(values: IntermediateRepresentation*): IntermediateRepresentation = ArrayLiteral(values.toArray)

  def ternary(condition: IntermediateRepresentation,
              onTrue: IntermediateRepresentation,
              onFalse: IntermediateRepresentation): IntermediateRepresentation = Ternary(condition, onTrue, onFalse)

  def equal(lhs: IntermediateRepresentation, rhs: IntermediateRepresentation): IntermediateRepresentation =
    Eq(lhs, rhs)

  def notEqual(lhs: IntermediateRepresentation, rhs: IntermediateRepresentation): IntermediateRepresentation =
    NotEq(lhs, rhs)

  def block(ops: IntermediateRepresentation*): IntermediateRepresentation = Block(ops)

  def condition(test: IntermediateRepresentation)
               (onTrue: IntermediateRepresentation): IntermediateRepresentation = Condition(test, onTrue)

  def declare[TYPE](name: String)(implicit typ: ClassTag[TYPE]) = DeclareLocalVariable(typ.runtimeClass, name)

  def assign(name: String, value: IntermediateRepresentation) = AssignToLocalVariable(name, value)

  def tryCatch[E](name: String)(ops: IntermediateRepresentation)(onError: IntermediateRepresentation)
                 (implicit typ: ClassTag[E]) =
    TryCatch(ops, onError, typ.runtimeClass, name)

  def fail(error: IntermediateRepresentation) = Throw(error)

  def and(lhs: IntermediateRepresentation, rhs: IntermediateRepresentation) = BooleanAnd(lhs, rhs)
}