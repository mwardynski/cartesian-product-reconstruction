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
    int resultGraphLength = G1.length * G2.length;
    int resultGraph[][] = new int[resultGraphLength][resultGraphLength];
    for (int i = 0; i < G1.length; i++)
    {
      for (int j = 0; j < G2.length; j++)
      {
        for (int k = 0; k < G2.length; k++)
        {
          resultGraph[i * G2.length + j][i * G2.length + k] = G2[j][k];
        }
        for (int k = 0; k < G1.length; k++)
        {
          resultGraph[i * G2.length + j][k * G2.length + j] = G2[i][k];
        }
      }
    }

    for (int i = 0; i < resultGraphLength; i++)
    {
      System.out.print(i + " <==> ");
      for (int j = 0; j < resultGraphLength; j++)
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
