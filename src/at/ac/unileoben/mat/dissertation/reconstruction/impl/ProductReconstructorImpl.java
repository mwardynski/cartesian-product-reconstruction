package at.ac.unileoben.mat.dissertation.reconstruction.impl;

import at.ac.unileoben.mat.dissertation.common.GraphPreparer;
import at.ac.unileoben.mat.dissertation.linearfactorization.LinearFactorization;
import at.ac.unileoben.mat.dissertation.linearfactorization.services.ColoringService;
import at.ac.unileoben.mat.dissertation.linearfactorization.services.EdgeService;
import at.ac.unileoben.mat.dissertation.reconstruction.ProductReconstructor;
import at.ac.unileoben.mat.dissertation.structure.Edge;
import at.ac.unileoben.mat.dissertation.structure.Graph;
import at.ac.unileoben.mat.dissertation.structure.Vertex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.LinkedList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Marcin
 * Date: 23.01.16
 * Time: 11:43
 * To change this template use File | Settings | File Templates.
 */
@Component
public class ProductReconstructorImpl implements ProductReconstructor
{

  @Autowired
  GraphPreparer graphPreparer;

  @Autowired
  LinearFactorization linearFactorization;

  @Autowired
  ColoringService coloringService;

  @Autowired
  EdgeService edgeService;


  @Override
  public void reconstructProduct(List<Vertex> vertices)
  {
    if (isGraphC8(vertices))
    {
      List<Vertex> reconstructedVertexNeighbors = new LinkedList<>();
      for (int i = 0; i < vertices.size(); i += 2)
      {
        reconstructedVertexNeighbors.add(vertices.get(i));
      }
      graphPreparer.addVertex(vertices, reconstructedVertexNeighbors);
    }
    else
    {
      List<Vertex> subgraph;
      List<List<Vertex>> subgraphConnectedComponents;
      List<List<Vertex>> factorizedComponents = null;
      List<Integer> factorizedComponentsK2Color = null;
      Vertex additionallyRemovedVertex = null;
      for (Vertex v : vertices)
      {
        subgraph = graphPreparer.copySubgraph(vertices, v);
        subgraphConnectedComponents = graphPreparer.getGraphConnectedComponents(subgraph);
        factorizedComponents = new LinkedList<>();
        factorizedComponentsK2Color = new LinkedList<>();

        for (List<Vertex> connectedComponentVertices : subgraphConnectedComponents)
        {
          if (isGraphK1(connectedComponentVertices))
          {
            break;
          }
          else if (isGraphK2(connectedComponentVertices))
          {
            factorizedComponents.add(connectedComponentVertices);
            factorizedComponentsK2Color.add(null);
          }
          else
          {
            Graph factorizedGraph = linearFactorization.factorize(connectedComponentVertices, null);
            if (factorizedGraph.getGraphColoring().getActualColors().size() == 1)
            {
              break;
            }
            else
            {
              Vertex root = factorizedGraph.getRoot();
              for (Edge upEdge : root.getUpEdges().getEdges())
              {
                boolean factorizationContainsK2Factor = true;

                int edgeColor = coloringService.getCurrentColorMapping(factorizedGraph.getGraphColoring(), upEdge.getLabel().getColor());
                Vertex upNeighborVertex = upEdge.getEndpoint();
                List<Edge> relevantK2CheckEdges = new LinkedList<>();
                relevantK2CheckEdges.addAll(upNeighborVertex.getUpEdges().getEdges());
                relevantK2CheckEdges.addAll(upNeighborVertex.getCrossEdges().getEdges());
                for (Edge nextEdge : relevantK2CheckEdges)
                {
                  int nextEdgeColor = coloringService.getCurrentColorMapping(factorizedGraph.getGraphColoring(), nextEdge.getLabel().getColor());
                  if (nextEdgeColor == edgeColor)
                  {
                    factorizationContainsK2Factor = false;
                  }
                }
                if (factorizationContainsK2Factor)
                {
                  factorizedComponents.add(factorizedGraph.getVertices());
                  factorizedComponentsK2Color.add(upEdge.getLabel().getColor());
                  break;
                }
              }
            }
          }
        }
        if (subgraphConnectedComponents.size() == factorizedComponents.size())
        {
          additionallyRemovedVertex = v;
          break;
        }
      }
      if (additionallyRemovedVertex != null)
      {
        List<Vertex> factorizedRemovedVertexNeighbors = new LinkedList<>();
        for (int i = 0; i < factorizedComponents.size(); i++)
        {
          List<Vertex> factorizedComponentVertices = factorizedComponents.get(i);
          Integer componentK2Color = factorizedComponentsK2Color.get(i);
          if (componentK2Color == null)
          {
            for (Edge e : additionallyRemovedVertex.getEdges())
            {
              if (e.getEndpoint().getVertexNo() == factorizedComponentVertices.get(0).getVertexNo())
              {
                factorizedRemovedVertexNeighbors.add(factorizedComponentVertices.get(1));
                break;
              }
              else if (e.getEndpoint().getVertexNo() == factorizedComponentVertices.get(1).getVertexNo())
              {
                factorizedRemovedVertexNeighbors.add(factorizedComponentVertices.get(0));
                break;
              }
            }
          }
          else
          {
            boolean[] additionallyRemovedVertexNeighborFlags = new boolean[vertices.size()];
            List<Vertex> additionallyRemovedVertexNeighbors = new LinkedList<>();
            for (Edge e : additionallyRemovedVertex.getEdges())
            {
              additionallyRemovedVertexNeighborFlags[e.getEndpoint().getVertexNo()] = true;
            }
            for (Vertex potentialNeighborVertex : factorizedComponentVertices)
            {
              if (additionallyRemovedVertexNeighborFlags[potentialNeighborVertex.getVertexNo()])
              {
                additionallyRemovedVertexNeighbors.add(potentialNeighborVertex);
              }
            }
            for (Vertex additionallyRemovedVertexNeighbor : additionallyRemovedVertexNeighbors)
            {
              List<Edge> edgesCorrespondingToK2Component = edgeService.getAllEdgesOfColor(additionallyRemovedVertexNeighbor, componentK2Color);
              assert edgesCorrespondingToK2Component.size() == 1;
              Vertex removedVertexNeighbor = edgesCorrespondingToK2Component.iterator().next().getEndpoint();
              factorizedRemovedVertexNeighbors.add(removedVertexNeighbor);
            }

          }
        }
        List<Vertex> removedVertexNeighbors = new LinkedList<>();
        for (Vertex v : factorizedRemovedVertexNeighbors)
        {
          removedVertexNeighbors.add(vertices.get(v.getVertexNo()));
        }
        removedVertexNeighbors.add(additionallyRemovedVertex);
        graphPreparer.addVertex(vertices, removedVertexNeighbors);
      }
    }
    linearFactorization.factorize(vertices, null);
  }

  private boolean isGraphK1(List<Vertex> vertices)
  {
    return vertices.size() == 1;
  }

  private boolean isGraphK2(List<Vertex> vertices)
  {
    return vertices.size() == 2;
  }

  private boolean isGraphC8(List<Vertex> vertices)
  {
    boolean isC8 = true;
    if (vertices.size() == 8)
    {
      for (Vertex v : vertices)
      {
        if (v.getEdges().size() != 2)
        {
          isC8 = false;
        }
      }
    }
    else
    {
      isC8 = false;
    }

    return isC8;
  }
}
