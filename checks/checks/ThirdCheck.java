package checks;

import graphics.clipping.PolygonClipper;
import graphics.clipping.SutherlandHodgmanClipper;
import model.Polygon;
import utilities.TestUtility;

public final class ThirdCheck {

    public static void main(String[] args) {
        run();
        TestUtility.finish();
    }

    public static void run() {
        int color = 0xffffffff;
        PolygonClipper clipper = new SutherlandHodgmanClipper();
        Polygon halfPlaneWindow = TestUtility.polygon(color, TestUtility.points(0, -10, 10, -10, 10, 10, 0, 10));

        TestUtility.check("left boundary handles all four transitions", () -> {
            Polygon subject = TestUtility.polygon(color, TestUtility.points(-1, 1, 2, 1, 2, 2, -1, 2));
            Polygon expected = TestUtility.polygon(color, TestUtility.points(0, 1, 2, 1, 2, 2, 0, 2));
            TestUtility.sameVertices(TestUtility.vertices(expected), clipper.clip(subject, halfPlaneWindow));
        });

        TestUtility.check("polygon inside the half-plane window is preserved", () -> {
            Polygon subject = TestUtility.polygon(color, TestUtility.points(1, 1, 2, 1, 2, 2, 1, 2));
            TestUtility.exactVertices(TestUtility.vertices(subject), clipper.clip(subject, halfPlaneWindow));
        });

        TestUtility.check("polygon outside the half-plane window is removed", () -> {
            Polygon subject = TestUtility.polygon(color, TestUtility.points(-3, 1, -1, 1, -1, 2, -3, 2));
            TestUtility.yes(clipper.clip(subject, halfPlaneWindow).getLines().isEmpty(),
                "outside polygon was retained");
        });
    }

}
