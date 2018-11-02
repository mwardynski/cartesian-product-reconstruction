package at.ac.unileoben.mat.dissertation.structure;

public class IntervalData
{
  Vertex[] intervalVertices;
  Vertex currentIntervalTopVertex;
  boolean[] currentIntervalTopVertexAdjacencyVector;

  public IntervalData(int graphSize)
  {
    this.intervalVertices = new Vertex[graphSize];
  }

  public Vertex[] getIntervalVertices()
  {
    return intervalVertices;
  }

  public Vertex getCurrentIntervalTopVertex()
  {
    return currentIntervalTopVertex;
  }

  public void setCurrentIntervalTopVertex(Vertex currentIntervalTopVertex)
  {
    this.currentIntervalTopVertex = currentIntervalTopVertex;
  }

  public boolean[] getCurrentIntervalTopVertexAdjacencyVector()
  {
    return currentIntervalTopVertexAdjacencyVector;
  }

  public void setCurrentIntervalTopVertexAdjacencyVector(boolean[] currentIntervalTopVertexAdjacencyVector)
  {
    this.currentIntervalTopVertexAdjacencyVector = currentIntervalTopVertexAdjacencyVector;
  }
}
