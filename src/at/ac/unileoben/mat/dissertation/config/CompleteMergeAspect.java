package at.ac.unileoben.mat.dissertation.config;


import at.ac.unileoben.mat.dissertation.common.GraphHelper;
import at.ac.unileoben.mat.dissertation.structure.Graph;
import at.ac.unileoben.mat.dissertation.structure.exception.CompleteMergeException;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Profile("completeMerge")
public class CompleteMergeAspect
{
  @Autowired
  Graph graph;

  @Autowired
  GraphHelper graphHelper;

  @Pointcut("execution(* at.ac.unileoben.mat.dissertation.linearfactorization.ConsistencyChecker.checkConsistency(..)) && args(currentLayerNo)")
  private void checkConsistencyOperation(int currentLayerNo)
  {
  }

  @Around("checkConsistencyOperation(currentLayerNo)")
  public Object addColorsMergeSnapshot(ProceedingJoinPoint proceedingJoinPoint, int currentLayerNo) throws Throwable
  {
    checkColorsState(currentLayerNo, false);
    Object result = proceedingJoinPoint.proceed();
    checkColorsState(currentLayerNo, true);
    return result;

  }

  private void checkColorsState(int currentLayerNo, boolean afterConsistencyCheck)
  {
    if (!graphHelper.isMoreThanOneColorLeft(graph))
    {
      throw new CompleteMergeException(currentLayerNo, afterConsistencyCheck);
    }
  }
}
