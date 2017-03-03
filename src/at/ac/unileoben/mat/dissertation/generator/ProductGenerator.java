package at.ac.unileoben.mat.dissertation.generator;

/**
 * Created by mwardynski on 21/10/16.
 */
public class ProductGenerator
{

  private static final int[][] G1 = {
          {1, 1},
          {1, 1}
  };

  private static final int[][] G2 = {
          {1, 1, 0, 0, 1, 0, 0, 1},
          {1, 1, 1, 0, 0, 0, 0, 0},
          {0, 1, 1, 1, 0, 0, 0, 0},
          {0, 0, 1, 1, 0, 0, 0, 0},
          {1, 0, 0, 0, 1, 1, 0, 0},
          {0, 0, 0, 0, 1, 1, 1, 0},
          {0, 0, 0, 0, 0, 1, 1, 0},
          {1, 0, 0, 0, 0, 0, 0, 1}
  };

  public static final void main(String... args)
  {
    ProductGenerator productGenerator = new ProductGenerator();
    if (args.length == 0)
    {
      productGenerator.createProduct(G1, G2);
    }
    else
    {
      productGenerator.createProduct(G1, Integer.parseInt(args[0]));
    }

  }

  public ProductGenerator()
  {
  }

  private void createProduct(int[][] factor, int exponent)
  {
    int[][] tmpFactor = factor;
    for (int i = 1; i < exponent; i++)
    {
      tmpFactor = multiplyFactors(factor, tmpFactor);
    }
    printResultGraph(tmpFactor);
  }

  private void createProduct(int[][] factor1, int[][] factor2)
  {
    int[][] productGraph = multiplyFactors(factor1, factor2);
    printResultGraph(productGraph);
  }

  private int[][] multiplyFactors(int[][] factor1, int[][] factor2)
  {
    int resultGraphLength = factor1.length * factor2.length;
    int resultGraph[][] = new int[resultGraphLength][resultGraphLength];
    for (int i = 0; i < factor1.length; i++)
    {
      for (int j = 0; j < factor2.length; j++)
      {
        for (int k = 0; k < factor2.length; k++)
        {
          resultGraph[i * factor2.length + j][i * factor2.length + k] = factor2[j][k];
        }
        for (int k = 0; k < factor1.length; k++)
        {
          resultGraph[i * factor2.length + j][k * factor2.length + j] = factor2[i][k];
        }
      }
    }

    return resultGraph;
  }

  private static void printResultGraph(int[][] resultGraph)
  {
    for (int i = 0; i < resultGraph.length; i++)
    {
      System.out.print(i + " <==> ");
      for (int j = 0; j < resultGraph.length; j++)
      {
        if (resultGraph[i][j] == 1 && i != j)
        {
          System.out.print(j + " ");
        }
      }
      System.out.println("");
    }
  }

}
