package at.ac.unileoben.mat.dissertation.linearfactorization;

import at.ac.unileoben.mat.dissertation.structure.Graph;
import at.ac.unileoben.mat.dissertation.structure.Vertex;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Marcin
 * Date: 20.12.15
 * Time: 19:28
 * To change this template use File | Settings | File Templates.
 */
public interface LinearFactorization
{
  List<Vertex> parseGraph(String graphFilePath);

  Graph factorize(List<Vertex> vertices, Vertex root);
}
