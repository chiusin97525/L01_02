package gui;

import javafx.scene.layout.Pane;
import models.Problem;
import models.ProblemSet;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class AddProblemSetScreenManager extends Manager {
    /**
     * Load and display the screen to view all problems.
     *
     * @param innerPane The pane that new contents are loading into
     */
    public void showScreen(Pane innerPane) {
        loader = loadNewPane(loader, innerPane, "AddProblemSetScreen.fxml");
        AddProblemSetScreenController controller =
                loader.getController();
        controller.start(this);
    }

    public void addProblemSet(ProblemSet problemSet) {
        // Create string args array to call the appropriate command from interpreter
        List<String> argList = new ArrayList<>();
        argList.add("AddSimpleProblemSetCommand"); // add problem set command
        SimpleDateFormat dateFormat = new SimpleDateFormat();

        // Start date, end date, max attempts, followed by problem ids
        argList.add(dateFormat.format(problemSet.getStartTime()));
        argList.add(dateFormat.format(problemSet.getEndTime()));
        argList.add(String.valueOf(problemSet.getMaxAttempts()));
        argList.add(String.valueOf(problemSet.getQuestions().size()));

        for (Problem p : problemSet.getQuestions()) {
            argList.add(String.valueOf(p.getId()));
        }

        argList.addAll(problemSet.getTags());

        interpreter.executeAction(argList.toArray(new String[argList.size()]));
    }
}
