package at.ac.unileoben.mat.dissertation.common;

import at.ac.unileoben.mat.dissertation.structure.FactorizationUnitLayerSpecData;
import at.ac.unileoben.mat.dissertation.structure.Vertex;

import java.util.List;
import java.util.Optional;

/**
 * Created with IntelliJ IDEA.
 * User: Marcin
 * Date: 23.01.16
 * Time: 11:47
 * To change this template use File | Settings | File Templates.
 */
public interface GraphHelper
{
  List<Vertex> parseGraph(String graphFilePath);

  void addVertex(List<Vertex> allVertices, List<Vertex> neighbors);

  List<Vertex> copySubgraph(List<Vertex> allVertices, Optional<Vertex> vertexToRemoveOptional);

  List<List<Vertex>> getGraphConnectedComponents(List<Vertex> vertices);

  int getConnectedComponentSizeForColor(List<Vertex> topVertices, List<Vertex> vertices, FactorizationUnitLayerSpecData[] unitLayerSpecs, int color);

  List<Vertex> getSubgraphForTopVertices(List<Vertex> topVertices, List<Vertex> vertices);

  void createEdgeBetweenVertices(Vertex factorCurrentVertex, Vertex factorPreviousVertex);

  <T> T[] createGraphColoringArray(List<Vertex> vertices, T defaultColor);

  boolean isGraphK1(List<Vertex> vertices);

  boolean isGraphK2(List<Vertex> vertices);

  boolean isGraphC8(List<Vertex> vertices);

  void prepareGraphBfsStructure(List<Vertex> vertices, Vertex root);

  void revertGraphBfsStructure();
}