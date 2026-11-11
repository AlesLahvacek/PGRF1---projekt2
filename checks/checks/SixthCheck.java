package checks;

import graphics.clipping.PolygonClipper;
import graphics.clipping.SutherlandHodgmanClipper;
import model.Line;
import model.Polygon;
import utilities.TestUtility;

public final class SixthCheck {

    public static void main(String[] args) {
        run();
        TestUtility.finish();
    }

    public static void run() {
        PolygonClipper clipper = new SutherlandHodgmanClipper();
        int color = 0xffffffff;
        Polygon boundary = TestUtility.polygon(color, TestUtility.points(0, 0, 4, 0, 2, 4));

        TestUtility.check("model interface returns the clipped triangle as an open edge chain", () -> {
            Polygon subject = TestUtility.polygon(color, TestUtility.points(-1, -1, 5, -1, 5, 5, -1, 5));
            Polygon result = clipper.clip(subject, boundary);
            TestUtility.openEdgeChain(result, 3);
            for (Line line : result.getLines()) {
                TestUtility.yes(line.getColor() == color, "line color was not preserved");
            }
            TestUtility.sameVertices(TestUtility.vertices(boundary), result);
        });
    }

}
