/*
 * Copyright (c) 2002-2017 "Neo Technology,"
 * Network Engine for Objects in Lund AB [http://neotechnology.com]
 *
 * This file is part of Neo4j.
 *
 * Neo4j is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.neo4j.cypher.internal.compatibility.v3_3.runtime.pipes

import org.neo4j.cypher.internal.compatibility.v3_3.runtime.ExecutionContext
import org.neo4j.cypher.internal.compiler.v3_3._
import org.neo4j.cypher.internal.compiler.v3_3.planDescription.Id
import org.neo4j.cypher.internal.frontend.v3_3.ast.{LabelToken, PropertyKeyToken}

case class NodeIndexScanPipe(ident: String,
                             label: LabelToken,
                             propertyKey: PropertyKeyToken)
                            (val id: Id = new Id) extends Pipe {

  private val descriptor = IndexDescriptor(label.nameId.id, propertyKey.nameId.id)

  protected def internalCreateResults(state: QueryState): Iterator[ExecutionContext] = {
    val baseContext = state.createOrGetInitialContext()
    val resultNodes = state.query.indexScan(descriptor)
    resultNodes.map(node => baseContext.newWith1(ident, node))
  }

}
