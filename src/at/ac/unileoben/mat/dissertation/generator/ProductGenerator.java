package at.ac.unileoben.mat.dissertation.generator;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by mwardynski on 21/10/16.
 */
public class ProductGenerator
{

  private static final int[][] G1 = {
          {1, 1, 0},
          {1, 1, 1},
          {0, 1, 1}
  };

  private static final int[][] G2 = {
          //0  1  2  3  4  5  6  7  8  9  0  1  2  3  4  5  6
/*0*/      {1, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
/*1*/      {1, 1, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0},
/*2*/      {0, 1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
/*3*/      {0, 0, 1, 1, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0},
/*4*/      {0, 0, 0, 1, 1, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0},
/*5*/      {0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0},
/*6*/      {1, 0, 0, 0, 0, 0, 1, 1, 0, 0, 0, 1, 0, 0, 0, 0, 0},
/*7*/      {0, 1, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 1, 0, 0, 0, 0},
/*9*/      {0, 0, 0, 1, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 1, 0, 0},
/*0*/      {0, 0, 0, 0, 1, 0, 0, 0, 1, 1, 1, 0, 0, 0, 0, 1, 0},
/*1*/      {0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 1, 0, 0, 0, 0, 0, 1},
/*2*/      {0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0},
/*3*/      {0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 1, 1, 1, 0, 0, 0},
/*4*/      {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 0, 0},
/*5*/      {0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 1, 1, 0},
/*6*/      {0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 1, 1, 1},
/*7*/      {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 1}
  };

  public static final void main(String... args)
  {
    ProductGenerator productGenerator = new ProductGenerator();
    if (args.length == 0)
    {
      productGenerator.createProduct();
    }
    else
    {
      productGenerator.createProduct(Integer.parseInt(args[0]));
    }

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

  private void createProduct()
  {
    List<int[][]> factors = Arrays.asList(G1, G2);
    multiplyFactorsAndPrintOut(factors);
  }

  private void createProduct(int exponent)
  {
    List<int[][]> factors = IntStream.range(0, exponent)
            .mapToObj(i -> G1)
            .collect(Collectors.toList());
    multiplyFactorsAndPrintOut(factors);
  }

  private void multiplyFactorsAndPrintOut(List<int[][]> factors)
  {
    int[][] productGraph = factors.stream().reduce(this::multiplyTwoFactors).get();
    printResultGraph(productGraph);
  }

  private int[][] multiplyTwoFactors(int[][] factor1, int[][] factor2)
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
          resultGraph[i * factor2.length + j][k * factor2.length + j] = factor1[i][k];
        }
      }
    }

    return resultGraph;
  }

}
