package at.ac.unileoben.mat.dissertation.reconstruction.impl;

import at.ac.unileoben.mat.dissertation.structure.Edge;
import at.ac.unileoben.mat.dissertation.structure.Vertex;

import java.util.Iterator;
import java.util.List;

public class AbstractCartesianProductEdgesReconstructionTest
{
  protected void removeEdges(List<Edge> edgesToRemove, List<Vertex> vertices)
  {
    for (Edge edgeToRemove : edgesToRemove)
    {
      Vertex edgeToRemoveOrigin = vertices.get(edgeToRemove.getOrigin().getVertexNo());
      Iterator<Edge> edgeItertor = edgeToRemoveOrigin.getEdges().iterator();
      while (edgeItertor.hasNext())
      {
        Edge edge = edgeItertor.next();
        if (edge.getEndpoint().getVertexNo() == edgeToRemove.getEndpoint().getVertexNo())
        {
          edgeItertor.remove();
          break;
        }
      }

      Vertex edgeToRemoveEndpoinnt = vertices.get(edgeToRemove.getEndpoint().getVertexNo());
      edgeItertor = edgeToRemoveEndpoinnt.getEdges().iterator();
      while (edgeItertor.hasNext())
      {
        Edge edge = edgeItertor.next();
        if (edge.getEndpoint().getVertexNo() == edgeToRemove.getOrigin().getVertexNo())
        {
          edgeItertor.remove();
          break;
        }
      }
    }
  }

}
