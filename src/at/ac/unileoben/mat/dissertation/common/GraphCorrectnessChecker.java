package at.ac.unileoben.mat.dissertation.common;

import at.ac.unileoben.mat.dissertation.structure.Vertex;

import java.util.List;


public interface GraphCorrectnessChecker
{
  final static String NOT_HIGH_ENOUGH = "The input graph has not at least 3 layers";
  final static String NOT_SIMPLE = "The input graph is not a simple graph";
  final static String NOT_CONNECTED = "The input graph is not a connected graph";
  final static String NOT_THIN = "The input graph is not a thin graph";
  final static String BIPARTITE = "The input graph is a bipartite graph";

  boolean isSimple(List<Vertex> graph);

  boolean isConnected(List<Vertex> graph);

  boolean isNotBipartite(List<Vertex> graph);
}
