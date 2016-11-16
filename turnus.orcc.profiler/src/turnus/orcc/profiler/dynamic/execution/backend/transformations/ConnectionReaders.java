/* 
 * TURNUS - www.turnus.co
 * 
 * Copyright (C) 2010-2016 EPFL SCI STI MM
 *
 * This file is part of TURNUS.
 *
 * TURNUS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * TURNUS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with TURNUS.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Additional permission under GNU GPL version 3 section 7
 * 
 * If you modify this Program, or any covered work, by linking or combining it
 * with Eclipse (or a modified version of Eclipse or an Eclipse plugin or 
 * an Eclipse library), containing parts covered by the terms of the 
 * Eclipse Public License (EPL), the licensors of this Program grant you 
 * additional permission to convey the resulting work.  Corresponding Source 
 * for a non-source form of such a combination shall include the source code 
 * for the parts of Eclipse libraries used as well as that of the  covered work.
 * 
 */
package turnus.orcc.profiler.dynamic.execution.backend.transformations;

import java.util.List;
import java.util.Map;

import net.sf.orcc.df.Connection;
import net.sf.orcc.df.Entity;
import net.sf.orcc.df.Network;
import net.sf.orcc.df.Port;
import net.sf.orcc.df.util.DfVisitor;
import net.sf.orcc.graph.Edge;
import net.sf.orcc.graph.Vertex;

/**
 * 
 * @author Endri Bezati
 * @author Simone Casale Brunet
 *
 */
public class ConnectionReaders extends DfVisitor<Void> {

	@Override
	public Void caseNetwork(Network network) {

		// -- Actor Output Ports
		for (Vertex vertex : network.getChildren()) {
			Entity entity = vertex.getAdapter(Entity.class);
			Map<Port, List<Connection>> map = entity.getOutgoingPortMap();
			for (List<Connection> connections : map.values()) {
				for (Connection connection : connections) {
					connection.setAttribute("nbReaders", connections.size());

					Vertex src = connection.getSource();
					Port srcPort = connection.getSourcePort();
					Vertex tgt = connection.getTarget();
					Vertex tgtPort = connection.getTargetPort();

					String fifoName = "";

					if (srcPort != null && tgtPort != null) {
						fifoName = src.getLabel() + "_" + srcPort.getLabel() + "_" + tgt.getLabel() + "_"
								+ tgtPort.getLabel();
					} else if (srcPort != null && tgtPort == null) {
						fifoName = src.getLabel() + "_" + srcPort.getLabel() + "_" + tgt.getLabel();
					} else if (srcPort == null && tgtPort != null) {
						fifoName = src.getLabel() + "_" + tgt.getLabel() + "_" + tgtPort.getLabel();
					}
					connection.setAttribute("fifoName", fifoName);
				}
			}
		}

		// -- Network Input Ports
		for (Port port : network.getInputs()) {
			port.setAttribute("nbReaders", port.getOutgoing().size());
			int j = 0;
			for (Edge edge : port.getOutgoing()) {
				edge.setAttribute("nbReaders", port.getOutgoing().size());
				edge.setAttribute("fifoId", j);
				if (edge instanceof Connection) {
					Connection connection = (Connection) edge;
					Vertex src = connection.getSource();
					Vertex tgt = connection.getTarget();
					Vertex tgtPort = connection.getTargetPort();
					String fifoName = src.getLabel() + "_" + tgt.getLabel() + "_" + tgtPort.getLabel();
					connection.setAttribute("fifoName", fifoName);
				}
				j++;
			}
		}

		return null;
	}

}
