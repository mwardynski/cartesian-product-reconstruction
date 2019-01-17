package at.ac.unileoben.mat.dissertation.structure;

import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class TestCaseContext
{
  private Set<Integer> removedVertexNeighbors;
  private boolean correctResult;

  public Set<Integer> getRemovedVertexNeighbors()
  {
    return removedVertexNeighbors;
  }

  public void setRemovedVertexNeighbors(Set<Integer> removedVertexNeighbors)
  {
    this.removedVertexNeighbors = removedVertexNeighbors;
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
