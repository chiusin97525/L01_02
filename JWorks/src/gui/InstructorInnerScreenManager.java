package gui;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.fxml.FXMLLoader;
import javafx.scene.layout.Pane;

public class InstructorInnerScreenManager {

  public void createNewQuestion(Pane innerPane) {
    showCreateNewQuestionScreen(innerPane);
  }
  
  private void showCreateNewQuestionScreen(Pane innerPane) {
    try {
      // load the new inner pane from the new question screen fxml file
      FXMLLoader loader = new FXMLLoader(
          getClass().getResource("CreateNewQuestionScreen.fxml"));
      // clear the inner pane
      innerPane.getChildren().clear();
      // Add the new pane with question input to the main screen
      innerPane.getChildren().add(loader.load());
    } catch (IOException ex) {
      Logger.getLogger(LoginManager.class.getName()).log(Level.SEVERE, null,
          ex);
    }
  }
}