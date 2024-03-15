import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import javafx.event.*;

import java.util.List;

public class GUICollectionApp1 extends Application {
    List<SearchResult> model;
    ProjectTesterImp tester;

    public GUICollectionApp1() {
        tester = new ProjectTesterImp();
        tester.initialize();
        tester.crawl("https://people.scs.carleton.ca/~davidmckenney/tinyfruits/N-0.html");
        model = tester.search("apple", false, 10);
    }

    public void start(Stage primaryStage) {
        Pane aPane = new Pane();

        GUICollectionAppView1 view = new GUICollectionAppView1();
        aPane.getChildren().add(view);
        view.update(model);
        view.getButtonPane().getSearchButton().setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent actionEvent) {
                model = tester.search(view.getQueryField().getText(), view.getPageRankBox().isSelected(), 10);
                view.update(model);
            }
        });
        primaryStage.setTitle("Search Engine");
        primaryStage.setResizable(false);
        primaryStage.setScene(new Scene(aPane));
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}