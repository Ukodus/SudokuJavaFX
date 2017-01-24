package Sudoku;
/*
========================================================================================================================

                                *****  THIS APP IS UNDER CONSTRUCTION  *****

    The SudokuJavaFX app allows the user to 'play' Sudoku, maintain a collection of Sudoku puzzles, and analyze/solve
    any of the puzzles in the collection.  JavaFX is used throughout the GUI for hardware acceleration.

    This is my first Java app so expect a lot of experimental crap as I learn.  -- Ukodus

------------------------------------------------------------------------------------------------------------------------

    The app is a single dialog split into three panes: puzzle, move list, and informational text.  The user can change
    the layout of the panes or hide the info pane at any time.  The dialog is resizable using the mouse to drag the
    borders and pane dividers.

    2017-01-20:  At this time, only an empty board is displayed as I figure out UI issues with JavaFX.

========================================================================================================================
*/

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;


public class Main extends Application {

    private Stage      mainStage;       // Rather than recreating UI elements when the layout changes, I opted to
    private Scene      mainScene;       //   save references so the elements can be manipulated easily.
    private BorderPane borderPane;
    private SplitPane  nestedSplit;
    private SplitPane  mainSplit;
    private StackPane  listPane;
    private StackPane  infoPane;
    private BoardPane  boardPane;


    private int paneLayout = 3;         // eventually pull from retained user settings


    public static void main(String[] args) {
        launch(args);
    }


    @Override
    public void start(Stage primaryStage) {

        // one-time creation of panes
        listPane    = new StackPane();
        infoPane    = new StackPane();
        boardPane   = new BoardPane();
        nestedSplit = new SplitPane();
        mainSplit   = new SplitPane();

        // buttons for toolbar
        // layout button: toggles between the three possible layouts
        Button layoutButton = new Button("Layout");
        layoutButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent t) {
                paneLayout = paneLayout % 3 + 1;    // cycles values 1-3 endlessly
                LayoutPanes();
            }
        });

        // Info button: toggles between Show Info and Hide Info
        Button infoButton = new Button("Hide Info");
        infoButton.setOnAction(new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent t) {
                if (infoButton.getText() == "Hide Info") {
                    if (paneLayout == 1 || paneLayout == 2)
                        nestedSplit.getItems().remove(infoPane);
                    else
                        mainSplit.getItems().remove(infoPane);
                    infoButton.setText("Show Info");
                }
                else {
                    if (paneLayout == 1 || paneLayout == 2)
                        nestedSplit.getItems().add(infoPane);
                    else
                        mainSplit.getItems().add(infoPane);
                    infoButton.setText("Hide Info");
                }
                LayoutPanes();  // will resize to 'square' the cells
            }
        });

        // create the BorderPane with a toolbar and statusbar
        borderPane = new BorderPane();
        ToolBar toolbar = new ToolBar();
        toolbar.setPrefHeight(30.0);
        toolbar.getItems().addAll(layoutButton, infoButton);
        Label statusBar = new Label();
        borderPane.setTop(toolbar);
        borderPane.setCenter(mainSplit);
        borderPane.setBottom(statusBar);

        // create the mouse event handler that displays events on the statusbar
        borderPane.addEventFilter(MouseEvent.ANY, new EventHandler<MouseEvent>() {
            public void handle(final MouseEvent e) {
                statusBar.setText("Event: " + e.getEventType() + "  X: " + e.getX() + "  Y:" + e.getY());
            }
        });

        mainStage = primaryStage;
        LayoutPanes();
    }


    public void LayoutPanes() {
        mainStage.hide();

        if (paneLayout == 1) {
            nestedSplit.setOrientation(Orientation.VERTICAL);
            nestedSplit.getItems().clear();
            nestedSplit.getItems().addAll(listPane, infoPane);
            nestedSplit.setDividerPosition(0, .50);

            mainSplit.setOrientation(Orientation.HORIZONTAL);
            mainSplit.getItems().clear();
            mainSplit.getItems().addAll(boardPane, nestedSplit);   // add the nested SplitPane into the main SplitPane
            mainSplit.setDividerPosition(0, .667);
        } else if (paneLayout == 2) {
            nestedSplit.setOrientation(Orientation.VERTICAL);
            nestedSplit.getItems().clear();
            nestedSplit.getItems().addAll(boardPane, infoPane);
            nestedSplit.setDividerPosition(0, .85);

            mainSplit.setOrientation(Orientation.HORIZONTAL);
            mainSplit.getItems().clear();
            mainSplit.getItems().addAll(nestedSplit, listPane);
            mainSplit.setDividerPosition(0, .75);
        } else if (paneLayout == 3) {
            nestedSplit.setOrientation(Orientation.HORIZONTAL);
            nestedSplit.getItems().clear();
            nestedSplit.getItems().addAll(boardPane, listPane);
            nestedSplit.setDividerPosition(0, .75);

            mainSplit.setOrientation(Orientation.VERTICAL);
            mainSplit.getItems().clear();
            mainSplit.getItems().addAll(nestedSplit, infoPane);
            mainSplit.setDividerPosition(0, .85);
        }

        ToolBar tb = (ToolBar) borderPane.getTop();          // seems clumsy to ref the checkbox this way
        Button ib = (Button)(tb.getItems().get(1));
        boolean infoPaneRemoved = ib.getText() == "Show Info";

        // Given the layout and screen size, we specify a starting size that has approx square cells.
        // MacBook Pro w Retina is 2880x1800 yet command below yields 1440x900... exactly half.
        // Note:   JavaFX 8 introduces automatic coordinate scaling for Mac "Retina" HiDPI mode.
        //         Because retina displays are twice the resolution of non-retina, a scale factor of 2 is used.
        double height = Screen.getPrimary().getBounds().getMaxY() * .80; // initial Scene height is 80% of max
        double width = height;   // Make the cells ~square based on initial divider positions.
        switch (paneLayout) {    // Note: JavaFX treats the aspect ratio == 1 so we don't need to worry about that.
            case 1:
                width *= (1.0 / mainSplit.getDividerPositions()[0]);
                break;
            case 2:
                if (infoPaneRemoved)
                    width *= (1.0 / mainSplit.getDividerPositions()[0]);
                else
                    width *= (nestedSplit.getDividerPositions()[0] / mainSplit.getDividerPositions()[0]);
                break;
            case 3:
                if (infoPaneRemoved)
                    width *= (1.0 / nestedSplit.getDividerPositions()[0]);
                else
                    width *= (mainSplit.getDividerPositions()[0] / nestedSplit.getDividerPositions()[0]);
                break;
        }

        if (infoPaneRemoved) {
            if (paneLayout == 1 || paneLayout == 2)
                nestedSplit.getItems().remove(infoPane);
            else
                mainSplit.getItems().remove(infoPane);
        }

        if (mainScene == null) {
            mainScene = new Scene(borderPane, width, height);
            mainStage.setTitle("Sudoku");
            mainStage.setScene(mainScene);
        } else {
            mainStage.setWidth(width);
            mainStage.setHeight(height);
        }

        mainStage.show();
    }
}   // public class Main



/*
------------------------------------------------------------------------------------------------------------------------
    Standard JavaFX behavior when a Pane contains a Canvas, is to make the Canvas bigger as the Pane is resized, but
    not smaller so the Canvas can extend beyond the Pane boundries.  For this app we want the Canvas to exactly fit the
    Pane at all times so we can continuously draw() the board as the user resizes it and know we are going to see the
    entire board on screen at all times.
------------------------------------------------------------------------------------------------------------------------
*/

class BoardPane extends Pane{

    private BoardCanvas canvas;

    public BoardPane(){
        this.canvas = new BoardCanvas();
        this.getChildren().add(canvas);
        canvas.widthProperty().bind(this.widthProperty());      // Causes layoutChildren() to fire when the BoardPane
        canvas.heightProperty().bind(this.heightProperty());    //   width or height changes.  Must bind both or
    }                                                           //   layoutChildren will not fire!  Why?

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
        canvas.draw();
    }
}


class BoardCanvas extends Canvas {

    public void draw() {
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

    // these may be used internally by the base class Canvas, set as precaution.  Needed?  Are there more?
    @Override
    public boolean isResizable() {
        return true;
    }

    @Override
    public double prefWidth(double height) { return getWidth(); }

    @Override
    public double prefHeight(double width) { return getHeight(); }
}

