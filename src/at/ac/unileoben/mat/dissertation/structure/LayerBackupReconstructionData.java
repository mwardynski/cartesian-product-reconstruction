package at.ac.unileoben.mat.dissertation.structure;


import java.util.LinkedList;
import java.util.List;

public class LayerBackupReconstructionData
{
  int layerNo;
  GraphColoring graphColoring;
  List<Vertex> newUnitLayerVertices;

  public LayerBackupReconstructionData(int layerNo, GraphColoring graphColoring)
  {
    this.layerNo = layerNo;
    this.graphColoring = graphColoring;
    newUnitLayerVertices = new LinkedList<>();
  }

  public int getLayerNo()
  {
    return layerNo;
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
