package gui;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;

import java.io.File;

public class CreateNewQuestionScreenController extends Controller {
	@FXML
	private Button submitButton;
	@FXML
	private TextField questionInput;
	@FXML
	private TextField answerInput;
	@FXML
	private TextField tagField;
	@FXML
	private Label questionError;
	@FXML
	private Label answerError;
	@FXML
	private Button importButton;

	/**
	 * Start the handling of actions on screen
	 * 
	 * @param createNewQuestionScreenManager
	 */
	public void start(CreateNewQuestionScreenManager createNewQuestionScreenManager) {

		// when the submitButton is clicked
		submitButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				// check if any text field is empty
				if (questionInput.getText().equals("") || answerInput.getText().equals("")) {
					// set an error message to the user
					if (questionInput.getText().equals("")) {
						questionError.setText("Please enter the question");
					}
					if (answerInput.getText().equals("")) {
						answerError.setText("Please enter the answer");
					}

				} else {
					// Create the new question
					createNewQuestionScreenManager.createNewQuestion(questionInput.getText(), answerInput.getText(),
							tagField.getText());

					// reset the all text fields
					questionInput.setText("");
					answerInput.setText("");
					questionError.setText("");
					answerError.setText("");
					tagField.clear();
				}
			}
		});

		importButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				Node sceneNode = (Node) event.getTarget();
				FileChooser fileChooser = new FileChooser();
				fileChooser.setTitle("Open Students File");
				File targetFile = fileChooser.showOpenDialog(sceneNode.getScene().getWindow());
				if (targetFile != null) {
					createNewQuestionScreenManager.createBulkProblems(targetFile);
				}
			}
		});

	}
}
