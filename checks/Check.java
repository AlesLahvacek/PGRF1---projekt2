import checks.*;
import utilities.TestUtility;

public final class Check {

    public static void main(String[] args) {
        runStage("checks.FirstCheck", FirstCheck::run);
        runStage("checks.SecondCheck", SecondCheck::run);
        runStage("checks.ThirdCheck", ThirdCheck::run);
        runStage("checks.FourthCheck", FourthCheck::run);
        runStage("checks.FifthCheck", FifthCheck::run);
        runStage("checks.SixthCheck", SixthCheck::run);
        TestUtility.finish();
    }

    private static void runStage(String name, Runnable stage) {
        System.out.println("\n ======== ======== ======== ======== " + name + " ======== ======== ======== ======== \n");
        stage.run();
    }

}
