package at.ac.unileoben.mat.dissertation.structure;

import org.springframework.stereotype.Component;

@Component
public class TestCaseContext
{
  private boolean correctResult;

  public boolean isCorrectResult()
  {
    return correctResult;
  }

  public void setCorrectResult(boolean correctResult)
  {
    this.correctResult = correctResult;
  }
}
