package gui;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;

public class InstructorInnerScreenController extends Controller {
  @FXML
  private Button createNewQuestionButton;
  @FXML
  private Button viewQuestionsButton;
  @FXML
  private Button createStudentAccount;
  @FXML
  private Button addProblemSetButton;
  @FXML
  private Button viewProblemSetButton;
  @FXML
  private Pane innerScreen;

  /**
   * Start the handling of actions on screen
   * 
   * @param instructorInnerScreenManager
   */
  public void start(InstructorInnerScreenManager instructorInnerScreenManager) {

    createNewQuestionButton.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent event) {
        instructorInnerScreenManager.createNewQuestion(innerScreen);
      }
    });
    
    
    createStudentAccount.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent event) {
        instructorInnerScreenManager.createNewStudentAccount(innerScreen);
      }
    });

    viewQuestionsButton.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent event) {
        instructorInnerScreenManager.viewAllProblems(innerScreen);
      }
    });

    addProblemSetButton.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent event) {
        instructorInnerScreenManager.addProblemSet(innerScreen);
      }
    });

    viewProblemSetButton.setOnAction(new EventHandler<ActionEvent>() {
      @Override
      public void handle(ActionEvent event) {
        instructorInnerScreenManager.viewProblemSets(innerScreen);
      }
    });
  }
}