package at.ac.unileoben.mat.dissertation.linearfactorization;

import at.ac.unileoben.mat.dissertation.structure.Vertex;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Marcin
 * Date: 20.12.15
 * Time: 19:27
 * To change this template use File | Settings | File Templates.
 */
public interface GraphFactorizationPreparer
{
  void prepareToLinearFactorization(List<Vertex> vertices, Vertex root);

  void finalizeFactorization();

  void removeVertex(List<Vertex> vertices, int vertexIndex);
}
