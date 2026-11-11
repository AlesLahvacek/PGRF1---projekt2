package graphics.clipping;

import model.Polygon;

public interface PolygonClipper {

    Polygon clip(Polygon subject, Polygon boundary);

}
