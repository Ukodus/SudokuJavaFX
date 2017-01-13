package Sudoku;

import javafx.application.Application;
import javafx.scene.input.MouseEvent;
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
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    private double dividerPos;   // retains divider position when green panel toggled off

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {

        // nested SplitPane that will go in the main SplitPane
        SplitPane nestedSplit = new SplitPane();
        nestedSplit.setOrientation(Orientation.VERTICAL);
        StackPane bluePane = new StackPane();
        bluePane.setStyle("-fx-background-color: blue;");
        StackPane greenPane = new StackPane();
        greenPane.setStyle("-fx-background-color: green;");
        nestedSplit.getItems().addAll(bluePane, greenPane);

        // main SplitPain (horizontal) that will go in the BorderPane center section
        SplitPane mainSplit = new SplitPane();
        BoardCanvas canvas = new BoardCanvas();
        ResizableCanvasPane redPane = new ResizableCanvasPane(canvas);
        redPane.setStyle("-fx-background-color: #FFFFFF;");
        canvas.widthProperty().bind(redPane.widthProperty());
        canvas.heightProperty().bind(redPane.heightProperty());
        mainSplit.getItems().addAll(redPane, nestedSplit);   // add the nested SplitPane into the main SplitPane
        mainSplit.setDividerPosition(0, .65);

        // buttons for toolbar
        CheckBox checkboxGreen = new CheckBox("Green");
        checkboxGreen.setSelected(true);
        checkboxGreen.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if(newValue) {
                    nestedSplit.getItems().add(greenPane);
                    nestedSplit.setDividerPosition(0, dividerPos);
                }
                else {
                    dividerPos = nestedSplit.getDividerPositions()[0];
                    nestedSplit.getItems().remove(greenPane);
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

        primaryStage.setTitle("Three Section Form");
        primaryStage.setScene(new Scene(borderPane, 1440, 940));
        primaryStage.show();
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