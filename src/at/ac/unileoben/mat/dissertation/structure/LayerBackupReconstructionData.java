package at.ac.unileoben.mat.dissertation.structure;


import java.util.LinkedList;
import java.util.List;

public class LayerBackupReconstructionData
{
  GraphColoring graphColoring;
  List<Vertex> newUnitLayerVertices;

  public LayerBackupReconstructionData(GraphColoring graphColoring)
  {
    this.graphColoring = graphColoring;
    newUnitLayerVertices = new LinkedList<>();
  }

  public GraphColoring getGraphColoring()
  {
    return graphColoring;
  }

  public List<Vertex> getNewUnitLayerVertices()
  {
    return newUnitLayerVertices;
  }
}
