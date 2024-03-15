import javafx.collections.ObservableList;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;

import java.util.List;

public class GUICollectionAppView1 extends Pane implements GUIView {
    private TextField queryField;
    private CheckBox pageRankBox;
    private ListView<String> titleList, scoreList;
    private GUIButtonPane buttonPane;


    public ListView<String> getTitleList() {
        return titleList;
    }

    public ListView<String> getScoreList() {
        return scoreList;
    }

    public TextField getQueryField() {
        return queryField;
    }

    public CheckBox getPageRankBox() {
        return pageRankBox;
    }

    public GUIButtonPane getButtonPane() {
        return buttonPane;
    }

    public GUICollectionAppView1() {
        Label label1 = new Label("Query");
        label1.relocate(10, 10);
        Label label2 = new Label("Titles");
        label2.relocate(220, 10);
        Label label3 = new Label("Score");
        label3.relocate(290, 10);

        queryField = new TextField();
        queryField.relocate(10, 40);
        queryField.setPrefSize(200, 300);

        titleList = new ListView<String>();
        titleList.relocate(220, 40);
        titleList.setPrefSize(60, 300);

        scoreList = new ListView<String>();
        scoreList.relocate(290, 40);
        scoreList.setPrefSize(60, 300);

        buttonPane = new GUIButtonPane();
        buttonPane.relocate(30, 400);
        buttonPane.setPrefSize(305, 30);

        pageRankBox = new CheckBox("PageRank");
        pageRankBox.relocate(220, 400);
        pageRankBox.setPrefSize(80, 30);

        getChildren().addAll(label1, label2, label3, queryField, titleList, scoreList, buttonPane, pageRankBox);

        setPrefSize(360, 500);
    }

    @Override
    public void update(List<SearchResult> results) {
        ObservableList<String> titles = titleList.getItems();
        ObservableList<String> scores = scoreList.getItems();
        titles.clear();
        scores.clear();
        for (SearchResult result : results) {
            titles.add(result.getTitle());
            scores.add(String.format("%.3f", result.getScore()));
        }
    }
}