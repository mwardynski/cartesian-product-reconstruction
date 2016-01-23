package at.ac.unileoben.mat.dissertation.reconstruction.impl;

import at.ac.unileoben.mat.dissertation.common.GraphPreparer;
import at.ac.unileoben.mat.dissertation.linearfactorization.LinearFactorization;
import at.ac.unileoben.mat.dissertation.reconstruction.ProductReconstructor;
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
    linearFactorization.factorize(vertices, null);
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
