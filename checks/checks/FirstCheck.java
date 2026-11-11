package checks;

import graphics.clipping.SutherlandHodgmanClipper;
import model.Point;
import utilities.TestUtility;

public final class FirstCheck {

    public static void main(String[] args) {
        run();
        TestUtility.finish();
    }

    public static void run() {
        SutherlandHodgmanClipper clipper = new SutherlandHodgmanClipper();
        Point inside = new Point(2, 1);
        Point outside = new Point(2, -1);
        Point onEdge = new Point(2, 0);
        Point edgeStart = new Point(0, 0);
        Point edgeEnd = new Point(4, 0);

        TestUtility.check("inside, outside, and boundary points for positive orientation", () -> {
            TestUtility.yes(clipper.isInside(inside, edgeStart, edgeEnd, true), "inside point rejected");
            TestUtility.yes(!clipper.isInside(outside, edgeStart, edgeEnd, true), "outside point accepted");
            TestUtility.yes(clipper.isInside(onEdge, edgeStart, edgeEnd, true), "boundary point rejected");
        });

        TestUtility.check("reversed edge uses the opposite orientation", () -> {
            TestUtility.yes(clipper.isInside(inside, edgeEnd, edgeStart, false), "inside point rejected");
            TestUtility.yes(!clipper.isInside(outside, edgeEnd, edgeStart, false), "outside point accepted");
            TestUtility.yes(clipper.isInside(onEdge, edgeEnd, edgeStart, false), "boundary point rejected");
        });
    }

}
