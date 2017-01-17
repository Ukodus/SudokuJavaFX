package Sudoku;

import javafx.application.Application;
import javafx.beans.value.ChangeListener;       // for ChangeListener
import javafx.beans.value.ObservableValue;      // for ChangeListener
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
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
import javafx.stage.Stage;

public class Main extends Application {

    private Stage     mainStage;
    private SplitPane nestedSplit;
    private SplitPane mainSplit;
    private StackPane listPane;
    private StackPane infoPane;
    private ResizableCanvasPane boardPane;

    private double dividerPos;   // retains divider position when green panel toggled off
    private int paneLayout = 2;

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

        double width  = (paneLayout == 1 ? 1440.0 : 1300.0);
        double height = (paneLayout == 1 ?  940.0 : 1200.0);
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

            // main SplitPain (horizontal) that will go in the BorderPane center section
            mainSplit = new SplitPane();
            mainSplit.getItems().addAll(boardPane, nestedSplit);   // add the nested SplitPane into the main SplitPane
            mainSplit.setDividerPosition(0, .65);
        }
        else if(paneLayout == 2){
            nestedSplit = new SplitPane();
            nestedSplit.setOrientation(Orientation.HORIZONTAL);
            nestedSplit.getItems().addAll(boardPane, infoPane);
            nestedSplit.setDividerPosition(0, .75);

            mainSplit = new SplitPane();
            nestedSplit.setOrientation(Orientation.VERTICAL);
            mainSplit.getItems().addAll(nestedSplit, listPane);
            mainSplit.setDividerPosition(0, .75);
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