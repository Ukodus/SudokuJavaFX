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
        ResizableCanvas canvas = new ResizableCanvas();
        CanvasPane redPane = new CanvasPane(canvas);
        redPane.setStyle("-fx-background-color: #AA0000;");
        canvas.widthProperty().bind(redPane.widthProperty());
        canvas.heightProperty().bind(redPane.heightProperty());
        mainSplit.getItems().addAll(redPane, nestedSplit);   // add the nested SplitPane into the main SplitPane

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
        primaryStage.setScene(new Scene(borderPane, 600, 450));
        primaryStage.show();
    }

    class ResizableCanvas extends Canvas {

        public ResizableCanvas() {
            // Redraw canvas when size changes.
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
        /*
        @Override
        public double prefWidth(double height) { return getWidth(); }

        @Override
        public double prefHeight(double width) {
            return getHeight();
        }
        */
    }

    public class CanvasPane extends Pane{

        private ResizableCanvas canvas;

        public CanvasPane(ResizableCanvas canvas){
            this.canvas = canvas;
            this.getChildren().add(canvas);
        }

        @Override
        protected void layoutChildren() {
            final int top = (int)snappedTopInset();
            final int right = (int)snappedRightInset();
            final int bottom = (int)snappedBottomInset();
            final int left = (int)snappedLeftInset();
            final int w = (int)getWidth() - left - right;
            final int h = (int)getHeight() - top - bottom;
            canvas.setLayoutX(left);
            canvas.setLayoutY(top);
            if (w != canvas.getWidth() || h != canvas.getHeight()) {
                canvas.setWidth(w);
                canvas.setHeight(h);
                canvas.draw();
            }
        }
    }

}