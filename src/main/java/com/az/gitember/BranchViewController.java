package com.az.gitember;

import com.az.gitember.misc.Const;
import com.az.gitember.misc.GitemberUtil;
import com.az.gitember.misc.ScmBranch;
import com.az.gitember.misc.ScmItem;
import com.az.gitember.ui.AutoCompleteTextField;
import com.az.gitember.ui.PlotCommitRenderer;
import com.sun.javafx.binding.StringConstant;
import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.revplot.PlotCommit;
import org.eclipse.jgit.revplot.PlotCommitList;
import org.eclipse.jgit.revplot.PlotLane;

import java.io.InputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Created by Igor_Azarny on 03 - Dec - 2016
 */
public class BranchViewController implements Initializable {

    private final static Logger log = Logger.getLogger(BranchViewController.class.getName());

    private static final int HEIGH = 30;

    @FXML
    private TableColumn<PlotCommit, Canvas> laneTableColumn;

    @FXML
    private TableColumn<PlotCommit, String> dateTableColumn;

    @FXML
    private TableColumn<PlotCommit, String> messageTableColumn;

    @FXML
    private TableColumn<PlotCommit, String> authorTableColumn;

    @FXML
    private TableView commitsTableView;

    @FXML
    private AnchorPane hostCommitViewPanel;

    private Pane spacerPane;

    private AutoCompleteTextField searchText;

    private PlotCommitRenderer plotCommitRenderer = new PlotCommitRenderer();

    private String treeName;

    private int plotWidth = 5 * HEIGH;

    private PlotCommitList<PlotLane> plotCommits;


    @Override
    @SuppressWarnings("unchecked")
    public void initialize(URL location, ResourceBundle resources) {
        commitsTableView.setFixedCellSize(HEIGH);
        commitsTableView
                .getSelectionModel()
                .selectedItemProperty()
                .addListener(new ChangeListener<PlotCommit>() {

                    @Override
                    public void changed(final ObservableValue<? extends PlotCommit> observable,
                                        final PlotCommit oldValue,
                                        final PlotCommit newValue) {

                        Parent commitView = CommitViewController.openCommitViewWindow(
                                GitemberApp.getRepositoryService().adapt(newValue, null),
                                -1, treeName, null, null, null);
                        hostCommitViewPanel.getChildren().removeAll(hostCommitViewPanel.getChildren());
                        hostCommitViewPanel.getChildren().add(commitView);
                    }

                });

        commitsTableView.setRowFactory(
                tr -> {
                    return new TableRow<PlotCommit>() {

                        private String calculateStyle(final PlotCommit scmItem) {
                            String searchText = BranchViewController.this.searchText.getText();
                            if (scmItem != null && searchText != null && searchText.length() > Const.SEARCH_LIMIT_CHAR) {
                                searchText = searchText.toLowerCase();

                                if (scmItem.getShortMessage().toLowerCase().contains(searchText)
                                        || scmItem.getFullMessage().toLowerCase().contains(searchText)
                                        || scmItem.getName().toLowerCase().contains(searchText)
                                        || prersonIndentContains(scmItem.getCommitterIdent(), searchText)
                                        || prersonIndentContains(scmItem.getAuthorIdent(), searchText)
                                        || GitemberApp.getRepositoryService().getScmItems(scmItem, null)
                                        .stream()
                                        .map(s -> s.getAttribute().getName())
                                        .collect(Collectors.joining(","))
                                        .toLowerCase()
                                        .contains(searchText)) {
                                    return "-fx-font-weight: bold;";
                                }
                            }
                            return "";
                        }

                        @Override
                        protected void updateItem(PlotCommit item, boolean empty) {
                            super.updateItem(item, empty);
                            setStyle(calculateStyle(item));
                        }
                    };
                }
        );

        laneTableColumn.setCellValueFactory(
                c -> {
                    return new ObservableValue<Canvas>() {
                        @Override
                        public Canvas getValue() {
                            Canvas canvas = new Canvas(plotWidth, HEIGH);
                            GraphicsContext gc = canvas.getGraphicsContext2D();
                            plotCommitRenderer.render(gc, c.getValue(), HEIGH);
                            return canvas;
                        }

                        @Override
                        public void addListener(InvalidationListener listener) {
                        }

                        @Override
                        public void removeListener(InvalidationListener listener) {
                        }

                        @Override
                        public void addListener(ChangeListener<? super Canvas> listener) {
                        }

                        @Override
                        public void removeListener(ChangeListener<? super Canvas> listener) {
                        }

                    };
                }
        );
        laneTableColumn.setSortable(false);

        authorTableColumn.setCellValueFactory(
                c -> StringConstant.valueOf(c.getValue().getCommitterIdent().getName())
        );
        authorTableColumn.setSortable(false);

        messageTableColumn.setCellValueFactory(
                c -> StringConstant.valueOf(c.getValue().getShortMessage())
        );
        messageTableColumn.setSortable(false);


        dateTableColumn.setCellValueFactory(
                c -> StringConstant.valueOf(
                        GitemberUtil.formatDate(GitemberUtil.intToDate(c.getValue().getCommitTime()))
                )
        );
        dateTableColumn.setSortable(false);

        spacerPane = new Pane();
        HBox.setHgrow(spacerPane, Priority.ALWAYS);
        spacerPane.setId(Const.MERGED);

        searchText = new AutoCompleteTextField();
        searchText.setId(Const.MERGED);
        searchText.getEntries().addAll(GitemberApp.entries);
        searchText.textProperty().addListener(
                (observable, oldValue, newValue) -> {
                    commitsTableView.refresh();
                    if (oldValue != null && newValue != null && newValue.length() > oldValue.length() && newValue.contains(oldValue)) {
                        GitemberApp.entries.remove(oldValue);
                        GitemberApp.entries.add(newValue);
                    }
                }
        );
    }

    private boolean prersonIndentContains(PersonIdent prersonIndent, String searchString) {
        return prersonIndent != null
                && (prersonIndent.getEmailAddress() != null
                && prersonIndent.getEmailAddress().toLowerCase().contains(searchString.toLowerCase()));

    }


    public void open() throws Exception {
        this.plotCommits = GitemberApp.getRepositoryService().getCommitsByTree(this.treeName);
        commitsTableView.setItems(FXCollections.observableArrayList(plotCommits));
        plotWidth = calculateLineColumnWidth(plotCommits);
        laneTableColumn.setPrefWidth(plotWidth);

    }


    private int calculateLineColumnWidth(PlotCommitList<PlotLane> plotCommits) {
        return 36 + 12 * plotCommits.stream().mapToInt(p -> p.getLane().getPosition()).max().orElse(0);
    }

    public static Parent openBranchHistory(final ScmBranch scmBranch,
                                           ToolBar toolBar) {
        try {
            if (scmBranch != null) {
                final FXMLLoader fxmlLoader = new FXMLLoader();
                try (InputStream is = BranchViewController.class.getResource("/fxml/BranchViewPane.fxml").openStream()) {
                    final Parent branchView = fxmlLoader.load(is);
                    final BranchViewController branchViewController = fxmlLoader.getController();
                    branchViewController.treeName = scmBranch.getFullName();
                    toolBar.getItems().add(branchViewController.spacerPane);
                    toolBar.getItems().add(branchViewController.searchText);
                    branchViewController.open();
                    return branchView;
                }

            }
        } catch (Exception e) {
            log.log(Level.SEVERE, "Cannot open branch view", e);
        }
        return null;
    }
}
