package at.ac.unileoben.mat.dissertation.printout;

import at.ac.unileoben.mat.dissertation.printout.data.RgbColor;
import at.ac.unileoben.mat.dissertation.printout.impl.EdgeColorsGeneratorImpl;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class EdgeColorsGeneratorTest
{

  @Test
  public void generate6EdgeColorsFor6Colors()
  {
    EdgeColorsGenerator edgeColorsGenerator = new EdgeColorsGeneratorImpl();

    List<RgbColor> rgbColors = edgeColorsGenerator.generateColors(6);

    assertThat(rgbColors.size(), is(6));
  }

  @Test
  public void generate24EdgeColorsFor20Colors()
  {
    EdgeColorsGenerator edgeColorsGenerator = new EdgeColorsGeneratorImpl();

    List<RgbColor> rgbColors = edgeColorsGenerator.generateColors(20);

    assertThat(rgbColors.size(), is(24));
  }

  @Test
  public void generateCorrectBitwiseSetsFor13Colors()
  {
    EdgeColorsGenerator edgeColorsGenerator = new EdgeColorsGeneratorImpl();

    List<RgbColor> actualRgbColors = edgeColorsGenerator.generateColors(13);

    List<RgbColor> expectedRgbColors = Arrays.asList(
            new RgbColor(0, 0, 255),
            new RgbColor(0, 255, 0),
            new RgbColor(0, 255, 255),
            new RgbColor(255, 0, 0),
            new RgbColor(255, 0, 255),
            new RgbColor(255, 255, 0),
            new RgbColor(0, 0, 100),
            new RgbColor(0, 100, 0),
            new RgbColor(0, 100, 100),
            new RgbColor(100, 0, 0),
            new RgbColor(100, 0, 100),
            new RgbColor(100, 100, 0),
            new RgbColor(0, 0, 177),
            new RgbColor(0, 177, 0),
            new RgbColor(0, 177, 177),
            new RgbColor(177, 0, 0),
            new RgbColor(177, 0, 177),
            new RgbColor(177, 177, 0)
    );
    assertThat(actualRgbColors, is(expectedRgbColors));
  }

  @Test
  public void generateCorrectBitwiseSetsFor3Colors()
  {
    EdgeColorsGenerator edgeColorsGenerator = new EdgeColorsGeneratorImpl();

    List<RgbColor> actualRgbColors = edgeColorsGenerator.generateColors(3);

    List<RgbColor> expectedRgbColors = Arrays.asList(
            new RgbColor(0, 0, 255),
            new RgbColor(0, 255, 0),
            new RgbColor(0, 255, 255),
            new RgbColor(255, 0, 0),
            new RgbColor(255, 0, 255),
            new RgbColor(255, 255, 0)
    );
    assertThat(actualRgbColors, is(expectedRgbColors));
  }
}
