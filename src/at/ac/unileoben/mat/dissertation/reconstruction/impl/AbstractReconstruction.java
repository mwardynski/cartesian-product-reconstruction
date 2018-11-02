package at.ac.unileoben.mat.dissertation.reconstruction.impl;

import at.ac.unileoben.mat.dissertation.reconstruction.Reconstruction;
import at.ac.unileoben.mat.dissertation.structure.Graph;
import at.ac.unileoben.mat.dissertation.structure.Vertex;

import java.util.List;

/**
 * Created by Marcin on 16.03.2017.
 */
public abstract class AbstractReconstruction implements Reconstruction
{
  @Override
  public Graph reconstruct(List<Vertex> vertices)
  {
    return reconstruct(vertices, vertices.get(0));
  }

  @Override
  public Graph reconstruct(List<Vertex> vertices, Vertex root)
  {
    return reconstruct(vertices);
  }
}
