package at.ac.unileoben.mat.dissertation.common;

import at.ac.unileoben.mat.dissertation.structure.Vertex;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: Marcin
 * Date: 23.01.16
 * Time: 11:47
 * To change this template use File | Settings | File Templates.
 */
public interface GraphPreparer
{
  List<Vertex> parseGraph(String graphFilePath);

  void addVertex(List<Vertex> allVertices, List<Vertex> neighbors);
}