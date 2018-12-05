package at.ac.unileoben.mat.dissertation.structure;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
public class TestCaseContext
{
  private Set<Integer> removedVertexNeighbors;
  private List<Edge> removedEdges;
  private boolean correctResult;

  public Set<Integer> getRemovedVertexNeighbors()
  {
    return removedVertexNeighbors;
  }

  public void setRemovedVertexNeighbors(Set<Integer> removedVertexNeighbors)
  {
    this.removedVertexNeighbors = removedVertexNeighbors;
  }

  public List<Edge> getRemovedEdges()
  {
    return removedEdges;
  }

  public void setRemovedEdges(List<Edge> removedEdges)
  {
    this.removedEdges = removedEdges;
  }

  public boolean isCorrectResult()
  {
    return correctResult;
  }

  public void setCorrectResult(boolean correctResult)
  {
    this.correctResult = correctResult;
  }
}
