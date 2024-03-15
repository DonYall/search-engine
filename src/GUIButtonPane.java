import javafx.scene.control.Button;
import javafx.scene.layout.Pane;

public class GUIButtonPane extends Pane {
    private     Button  searchButton;

    public Button getSearchButton() { return searchButton; }

    public GUIButtonPane() {
        Pane innerPane = new Pane();

        searchButton = new Button("Search");
        searchButton.setStyle("-fx-font: 12 arial; -fx-base: rgb(0,100,0); -fx-text-fill: rgb(255,255,255);");
        searchButton.relocate(0, 0);
        searchButton.setPrefSize(90,30);

        innerPane.getChildren().add(searchButton);

        getChildren().addAll(innerPane);
    }
}

