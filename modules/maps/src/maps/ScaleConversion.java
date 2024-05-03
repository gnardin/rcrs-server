package maps;

/**
   A coordinate conversion that scales and translates coordinates.
*/
public class ScaleConversion implements CoordinateConversion {
    private final double xOrigin;
    private final double yOrigin;
    private final double xScale;
    private final double yScale;

    /**
       Construct a ScaleConversion.
       @param xOrigin The x coordinate of the new origin.
       @param yOrigin The y coordinate of the new origin.
       @param xScale The scale factor for x coordinates.
       @param yScale The scale factor for y coordinates.
    */
    public ScaleConversion(double xOrigin, double yOrigin, double xScale, double yScale) {
        this.xOrigin = xOrigin;
        this.yOrigin = yOrigin;
        this.xScale = xScale;
        this.yScale = yScale;
    }

    @Override
    public double convertX(double x) {
        return (x - xOrigin) * xScale;
    }

    @Override
    public double convertY(double y) {
        return (y - yOrigin) * yScale;
    }
}
