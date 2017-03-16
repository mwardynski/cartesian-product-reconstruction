package at.ac.unileoben.mat.dissertation.reconstruction.impl;

import at.ac.unileoben.mat.dissertation.linearfactorization.LinearFactorization;
import at.ac.unileoben.mat.dissertation.reconstruction.Reconstruction;
import at.ac.unileoben.mat.dissertation.structure.Graph;
import at.ac.unileoben.mat.dissertation.structure.Vertex;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Created by Marcin on 16.03.2017.
 */
@Component
public class InPlaceReconstructionOfCubesImpl implements Reconstruction
{

  @Autowired
  LinearFactorization linearFactorization;

  @Override
  public Graph reconstruct(List<Vertex> vertices)
  {
    Graph graph = linearFactorization.factorize(vertices, vertices.get(0));
    return graph;
  }
}
