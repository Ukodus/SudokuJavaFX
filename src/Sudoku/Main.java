package Sudoku;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;       // for ChangeListener
import javafx.beans.value.ObservableValue;      // for ChangeListener
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.ToolBar;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class Main extends Application {

    private Stage     mainStage;
    private SplitPane nestedSplit;
    private SplitPane mainSplit;
    private StackPane listPane;
    private StackPane infoPane;
    private ResizableCanvasPane boardPane;

    private double dividerPos;   // retains divider position when green panel toggled off
    private int paneLayout = 3;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {

        mainStage = primaryStage;

        LayoutPanes();

        // buttons for toolbar
        CheckBox checkboxGreen = new CheckBox("Green");
        checkboxGreen.setSelected(true);
        checkboxGreen.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if (paneLayout == 1 || paneLayout == 2) {
                    if (newValue) {
                        nestedSplit.getItems().add(infoPane);
                        nestedSplit.setDividerPosition(0, dividerPos);
                    } else {
                        dividerPos = nestedSplit.getDividerPositions()[0];
                        nestedSplit.getItems().remove(infoPane);
                    }
                }
                else {
                    if (newValue) {
                        mainSplit.getItems().add(infoPane);
                        mainSplit.setDividerPosition(0, dividerPos);
                    } else {
                        dividerPos = mainSplit.getDividerPositions()[0];
                        mainSplit.getItems().remove(infoPane);
                    }
                }
            }
        });

        // create the BorderPane with a toolbar and statusbar
        BorderPane borderPane = new BorderPane();
        ToolBar toolbar = new ToolBar();
        toolbar.setPrefHeight(30.0);
        toolbar.getItems().addAll(checkboxGreen);
        Label statusBar = new Label();
        borderPane.setTop(toolbar);
        borderPane.setCenter(mainSplit);
        borderPane.setBottom(statusBar);

        // create the mouse event handler that displays events on the statusbar
        borderPane.addEventFilter(MouseEvent.ANY, new EventHandler<MouseEvent>() {
            public void handle(final MouseEvent e) {
                statusBar.setText("Event: " + e.getEventType() +  "  X: " + e.getX() + "  Y:" + e.getY());
            }
        });

        // Given the layout and screen size, we specify a starting size that has approx square cells.
        // MacBook Pro w Retina is 2880x1800 yet command below yields 1440x900... exactly half.
        // Note:   JavaFX 8 introduces automatic coordinate scaling for Mac "Retina" HiDPI mode.
        //         Because retina displays are twice the resolution of non-retina, a scale factor of 2 is used.
        double height = Screen.getPrimary().getBounds().getMaxY() * .80; // initial Scene height is 80% of max
        double width  = height;  // Make the cells ~square based on initial divider positions.
        switch (paneLayout) {    // Note: JavaFX treats the aspect ratio == 1 so we don't need to worry about that.
            case 1: width *= (1.0 / mainSplit.getDividerPositions()[0]); break;
            case 2: width *= (nestedSplit.getDividerPositions()[0] / mainSplit.getDividerPositions()[0]); break;
            case 3: width *= (mainSplit.getDividerPositions()[0] / nestedSplit.getDividerPositions()[0]); break;
        }

        // Show the main Stage
        mainStage.setTitle("Sudoku");
        mainStage.setScene(new Scene(borderPane, width, height));
        mainStage.show();
    }


    public void LayoutPanes() {

        listPane = new StackPane();
        listPane.setStyle("-fx-background-color: blue;");

        infoPane = new StackPane();
        infoPane.setStyle("-fx-background-color: green;");

        BoardCanvas canvas = new BoardCanvas();
        boardPane = new ResizableCanvasPane(canvas);
        boardPane.setStyle("-fx-background-color: #FFFFFF;");
        canvas.widthProperty().bind(boardPane.widthProperty());
        canvas.heightProperty().bind(boardPane.heightProperty());

        if(paneLayout == 1) {
            // nested SplitPane that will go in the main SplitPane
            nestedSplit = new SplitPane();
            nestedSplit.setOrientation(Orientation.VERTICAL);
            nestedSplit.getItems().addAll(listPane, infoPane);
            nestedSplit.setDividerPosition(0,.50);

            // main SplitPain (horizontal) that will go in the BorderPane center section
            mainSplit = new SplitPane();
            mainSplit.setOrientation(Orientation.HORIZONTAL);
            mainSplit.getItems().addAll(boardPane, nestedSplit);   // add the nested SplitPane into the main SplitPane
            mainSplit.setDividerPosition(0, .667);
        }
        else if(paneLayout == 2){
            nestedSplit = new SplitPane();
            nestedSplit.setOrientation(Orientation.VERTICAL);
            nestedSplit.getItems().addAll(boardPane, infoPane);
            nestedSplit.setDividerPosition(0, .85);

            mainSplit = new SplitPane();
            mainSplit.setOrientation(Orientation.HORIZONTAL);
            mainSplit.getItems().addAll(nestedSplit, listPane);
            mainSplit.setDividerPosition(0, .75);
        }
        else if(paneLayout == 3){
            nestedSplit = new SplitPane();
            nestedSplit.setOrientation(Orientation.HORIZONTAL);
            nestedSplit.getItems().addAll(boardPane, listPane);
            nestedSplit.setDividerPosition(0, .75);

            mainSplit = new SplitPane();
            mainSplit.setOrientation(Orientation.VERTICAL);
            mainSplit.getItems().addAll(nestedSplit, infoPane);
            mainSplit.setDividerPosition(0, .85);
        }

    }


    class ResizableCanvas extends Canvas {

        public ResizableCanvas() {
            // Call the draw() method anytime a resize event occurs.
            widthProperty().addListener(evt -> draw());
            heightProperty().addListener(evt -> draw());
        }

        private void draw() {
            double width = getWidth();
            double height = getHeight();

            GraphicsContext gc = getGraphicsContext2D();
            gc.clearRect(0, 0, width, height);
            gc.setStroke(Color.BLACK);
            gc.strokeLine(0, 0, width, height);
            gc.strokeLine(0, height, width, 0);
        }

        @Override
        public boolean isResizable() {
            return true;
        }

        @Override
        public double prefWidth(double height) { return getWidth(); }

        @Override
        public double prefHeight(double width) { return getHeight(); }
    }


    public class ResizableCanvasPane extends Pane{

        private ResizableCanvas canvas;

        public ResizableCanvasPane(ResizableCanvas canvas){
            this.canvas = canvas;
            this.getChildren().add(canvas);
        }

        @Override
        protected void layoutChildren() {
            final int top    = (int)snappedTopInset();
            final int left   = (int)snappedLeftInset();
            final int bottom = (int)snappedBottomInset();
            final int right  = (int)snappedRightInset();
            final int width  = (int)getWidth() - left - right;
            final int height = (int)getHeight() - top - bottom;
            canvas.setLayoutX(left);
            canvas.setLayoutY(top);
            if (width != canvas.getWidth() || height != canvas.getHeight()) {
                canvas.setWidth(width);
                canvas.setHeight(height);
                canvas.draw();
            }
        }
    }


    class BoardCanvas extends ResizableCanvas{

        public BoardCanvas() {
            // Call the draw() method anytime a resize event occurs.
            widthProperty().addListener(evt -> draw());
            heightProperty().addListener(evt -> draw());
        }

        private void draw() {
            double boardWidth  = getWidth();
            double boardHeight = getHeight();
            double boxWidth    = boardWidth / 3.0;
            double boxHeight   = boardHeight / 3.0;
            double cellWidth   = boxWidth / 3.0;
            double cellHeight  = boxHeight / 3.0;

            Color lightColor = Color.ANTIQUEWHITE;
            Color darkColor  = Color.LIGHTGREY;
            Color lineColor  = Color.BLACK;

            GraphicsContext gc = getGraphicsContext2D();
            gc.setFill( lightColor );
            gc.fillRect(0,0, boardWidth, boardHeight);

            gc.setFill( darkColor );
            gc.fillRect( boxWidth, 0.0, boxWidth, boxHeight);
            gc.fillRect( boxWidth, boxHeight+boxHeight, boxWidth, boxHeight);
            gc.fillRect( 0, boxHeight, boxWidth, boxHeight);
            gc.fillRect( boxWidth+boxWidth, boxHeight, boxWidth, boxHeight);

            gc.setLineWidth(2.0);
            gc.setStroke(lineColor);
            gc.strokeLine(boxWidth, 0, boxWidth, boardHeight);
            gc.strokeLine(boxWidth+boxWidth, 0, boxWidth+boxWidth, boardHeight);
            gc.strokeLine(0, boxHeight, boardWidth, boxHeight);
            gc.strokeLine(0, boxHeight+boxHeight, boardWidth, boxHeight+boxHeight);

            double x = cellWidth;
            gc.setLineWidth(1.0);
            gc.strokeLine(x, 0, x, boardHeight);
            x += cellWidth;
            gc.strokeLine(x, 0, x, boardHeight);
            x += cellWidth + cellWidth;
            gc.strokeLine(x, 0, x, boardHeight);
            x += cellWidth;
            gc.strokeLine(x, 0, x, boardHeight);
            x += cellWidth + cellWidth;
            gc.strokeLine(x, 0, x, boardHeight);
            x += cellWidth;
            gc.strokeLine(x, 0, x, boardHeight);

            double y = cellHeight;
            gc.strokeLine(0, y, boardWidth, y);
            y += cellHeight;
            gc.strokeLine(0, y, boardWidth, y);
            y += cellHeight + cellHeight;
            gc.strokeLine(0, y, boardWidth, y);
            y += cellHeight;
            gc.strokeLine(0, y, boardWidth, y);
            y += cellHeight + cellHeight;
            gc.strokeLine(0, y, boardWidth, y);
            y += cellHeight;
            gc.strokeLine(0, y, boardWidth, y);
        }
    }

}