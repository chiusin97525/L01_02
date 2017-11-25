package gui;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import models.Problem;

import java.util.List;

public class ViewProblemsController extends Controller {
    @FXML
    public TableView<Problem> questionTable;

    @FXML
    private TableColumn<Problem, Integer> idColumn;

    @FXML
    private TableColumn<Problem, String> questionColumn;

    @FXML
    private TableColumn<Problem, String> answerColumn;

    @FXML
    private TextField filterField;

    @FXML
    private Button filterButton;

    @FXML
    private Button clearButton;

    private ViewProblemsManager manager;

    /**
     *
     * @param manager The scene manager to use for this UI controller
     */
    public void start(ViewProblemsManager manager) {
        this.manager = manager;
        questionTable.getItems().setAll(manager.getProblems());
    }

    @Override
    public void initialize() {
        super.initialize();
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        questionColumn.setCellValueFactory(new PropertyValueFactory<>("problem"));
        answerColumn.setCellValueFactory(new PropertyValueFactory<>("answer"));

        // Setup tag filters
        clearButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                filterField.clear();
                questionTable.getItems().setAll(manager.getProblems());
            }
        });

        filterButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                questionTable.getItems().setAll(manager.getProblems(filterField.getText()));
            }
        });
    }
}
