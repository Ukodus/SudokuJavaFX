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
import javafx.geometry.Bounds;
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
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;



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
    private ArrayList<String> Puzzles;
    public  Position DisplayedPosition;


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

        // create the mouse event handlers
        boardPane.canvas.addEventFilter(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            public void handle(final MouseEvent e) {
                boardPane.canvas.selectCell(e);
            }
        });

        // these event handlers are temporary for development
        boardPane.addEventFilter(MouseEvent.MOUSE_MOVED, new EventHandler<MouseEvent>() {
            public void handle(final MouseEvent e) {
                double width  = boardPane.getWidth();
                double height = boardPane.getHeight();
                double cellWidth  = width / 9.0;
                double cellHeight = height / 9.0;
                int row = (int)(e.getY() / cellHeight);
                int col = (int)(e.getX() / cellWidth);
                statusBar.setText("Event: " + e.getEventType() + "  boardPane width: " + width + "  height: " + height + "  X: " + e.getX() + "  Y:" + e.getY() + "  Row: " + row + "  Col: " + col);
            }
        });
        listPane.addEventFilter(MouseEvent.MOUSE_MOVED, new EventHandler<MouseEvent>() {
            public void handle(final MouseEvent e) {
                statusBar.setText("Event: " + e.getEventType() + "  listPane  X: " + e.getX() + "  Y:" + e.getY());
            }
        });
        infoPane.addEventFilter(MouseEvent.MOUSE_MOVED, new EventHandler<MouseEvent>() {
            public void handle(final MouseEvent e) {
                statusBar.setText("Event: " + e.getEventType() + "  infoPane  X: " + e.getX() + "  Y:" + e.getY());
            }
        });


        InitializePuzzlesList();
        SudokuPuzzle puzzle = new SudokuPuzzle(Puzzles.get(0));     // pick the first puzzle in our list
        boardPane.canvas.displayedPosition = puzzle.lastPosition;   //   and the last position set


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

    private void InitializePuzzlesList() {
        // Initializes the ArrayList, Puzzles, from a text file or, if that fails, from a short hard-coded list.
        Puzzles = new ArrayList<String>();

        String fileSpec = Paths.get("").toAbsolutePath().toString() + "/data/puzzles.txt";
        File file = new File(fileSpec);

        if (file.exists()) {
            try {
                BufferedReader rdr = new BufferedReader(new FileReader(fileSpec));
                String line = null;
                while ((line = rdr.readLine()) != null)
                    Puzzles.add(line);
                rdr.close();
            } catch (Exception ex) {
                System.out.println("IO Exception: " + fileSpec);
                System.out.println(ex.toString());
            }
        }

        // if reading the file did not work, use some 6-star problems from the Mpls. Star-Tribune
        if (Puzzles.size() == 0)
            Puzzles = new ArrayList<String>(Arrays.asList(
             "900000307007010900020037000400350000030060080000072005000590070001080200704000008",
             "090041008000200006000930700860700090000080000040009061002063000900007000100890020",
             "000675091000034070000000600170540060030010050050092014007000000090460000410957000",
             "600009000000007094000580701092058000040000050000190380407025000530900000000400005",
             "050089000004060050700200084410090000009000200000040097890006001040070600000810070",
             "003200000000049102000060409200000065000090000650000008108050000302610000000004200",
             "000040003000019600096030000009003400072000380005200900000020150007190000200080000",
             "080000420020094000900160000006000097000010000390000200000047006000520030034000010",
             "000004000930070000701300209003090000807020605000060800409002701000010038000500000",
             "030500009100074000009020040000000896000060000356000000080090600000430002600002050",
             "900061070040050000100004620000000952000080000634000000059400001000010060060520007",
             "030560800000010600400090020008000003200134006300000500050020004002040000004071050",
             "040090000000038500000007043004000397030040010169000400780900000001780000000060030",
             "090020078000049000060008304400000700900030006003000001502700080000480000780050020",
             "000009570700035094000400003800100600000060000009007005400006000970240008085700000",
             "590700020700028050002510000200000000800050007000000009000049600080270003030005092",
             "600300050000058076000040030020081000300060007000470080070020000540730000080005003",
             "000068200000032085800070040007000500100603002002000900010080009390510000004290000",
             "200070050008050320000200000004600901000030000803002400000008000091060800080020004",
             "000569003090210050000000040900000860005020400032000005010000000070045090300697000",
             "010005400000074000500020070900002706050060030106900002030040001000250000002300090",
             "000036400075001000300500020000000013006020800580000000060007005000300270007460000",
             "000008000850020100034060020020300000600040003000001070010080260005070034000200000",
             "203000008000374000000060500005000010006192800080000400002040000000653000300000904"
            ));
    }

    public static void main(String[] args) {
        launch(args);
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

    public BoardCanvas canvas;

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
    public int selectedCell = -1;   // -1 = no cell selected
    public Position displayedPosition = null;
    public boolean possiblesShown = true;

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

        // if a cell is selected, highlight it
        if (selectedCell >= 0) {
            int row = selectedCell / 9;
            int col = selectedCell % 9;
            double top = cellHeight * row;
            double left = cellWidth * col;

            gc.setStroke( Color.RED );
            gc.setLineWidth(3.0);
            gc.strokeRect(left, top, cellWidth, cellHeight);
        }

        // display the square values for the selected position
        if (displayedPosition != null) {
            double size = Math.min(cellWidth,cellHeight) / 1.5;
            Text t = new Text("8");
            t.setFont(Font.font("SansSerif", FontWeight.BOLD, size));
            Bounds b = t.getBoundsInLocal();
            gc.setFont(t.getFont());
            gc.setFill( Color.BLACK );

            for(square s : displayedPosition.squares) {
                if (s.value != 0) {
                    x = (((double)s.column + 0.5) * cellWidth) - (b.getWidth() / 2.0);
                    y = (((double)s.row + 0.5) * cellHeight) + (b.getHeight() / 3.0);
                    t.setText(s.display);
                    gc.fillText(t.getText(), x, y);
                }
            }
            // display the possible values on blank squares
            if (possiblesShown) {
                size = Math.min(cellWidth, cellHeight) / 6.0;
                if (size >= 8.0) {
                    t = new Text("123456789");
                    t.setFont(Font.font("SansSerif", FontWeight.NORMAL, size));
                    b = t.getBoundsInLocal();
                    gc.setFont(t.getFont());
                    gc.setFill(Color.DIMGRAY);

                    for(square s : displayedPosition.squares) {
                        if (s.value == 0) {
                            x = (((double)s.column + 0.5) * cellWidth) - (b.getWidth() / 2.0);
                            y = ( ((double)s.row * cellHeight) + b.getHeight() );
                            t.setText(s.possible.replace(" ", ""));
                            gc.fillText(t.getText(), x, y);
                        }
                    }
                }
            }
        }
    }

    public void selectCell(MouseEvent e) {
        double cellWidth  = getWidth() / 9.0;
        double cellHeight = getHeight() / 9.0;
        int row  = (int)(e.getY() / cellHeight);
        int col  = (int)(e.getX() / cellWidth);
        int cell = (row * 9 ) + col;
        selectedCell = (cell == selectedCell ? -1 : cell);
        draw();
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



/*
------------------------------------------------------------------------------------------------------------------------
    Everything below is purely computational modelling of a Sudoku puzzle.  Solving a Sudoku puzzle is a process of
    eliminating possible values from the squares until there is only one possible value for each square.  A "move" is
    when a square is filled in with a value which results in a new "position".  A 9x9 Sudoku will therefore have, at
    most, 81 moves and resulting positions.  When a move is made, the value is eliminated from the possible values in
    neighboring squares.  The position is then analyzed to see if additional possible values can be eliminated based on
    more complex logic.  Empty squares with only one possible value create the hint-list of good moves.
------------------------------------------------------------------------------------------------------------------------
*/

class SudokuPuzzle {
    public String     puzzle;               // starting 81-char String of numerals 0-9 where 0 is an empty square
    public Position[] positions;
    public Position   lastPosition;

    public SudokuPuzzle(String puzzle) {
        positions = new Position[81];       // one-time allocation of all positions, so no realloc or garbage cleanup
        for(int p=0; p<81; p++)
            positions[p] = new Position(p);

        this.puzzle = puzzle;
        //System.out.println("Puzzle: " + this.puzzle);

        int moveNumber = -1;
        for(int sqr=0; sqr<81; sqr++) {
            int value = Integer.parseInt(puzzle.substring(sqr,sqr+1));
            if (value > 0) {
                moveNumber++;
                if (moveNumber > 0) {
                    for (int s=0; s<81; s++)
                        if (lastPosition.squares[s].value != 0)
                            positions[moveNumber].setMove(s, lastPosition.squares[s].value);
                }
                positions[moveNumber].setMove(sqr, value);
                lastPosition = positions[moveNumber];
                //System.out.println("moveNumber: "+lastPosition.moveNumber + "  sqr: " + lastPosition.squareNumber + "  value: "+lastPosition.squareValue);
            }
        }
    }

}

class Position {
    public int moveNumber;
    public int squareNumber;
    public int squareValue;
    public square[] squares;

    public Position(int move) {
        moveNumber   = move;
        squareNumber = -1;
        squareValue  = -1;
        squares = new square[81];

        for(int s=0; s<81; s++)
            squares[s] = new square(s);

        for(int s=0; s<81; s++)
            squares[s].setSiblings(squares);
    }

    public void setMove(int square, int value) {
        squareNumber = square;
        squareValue  = value;
        squares[square].setValue(value);
    }
}


class square {
    public int       index;     // 0 - 80, index into squares[]
    public int       row;       // 0 - 8
    public int       column;    // 0 - 8
    public int       value;     // 0 - 9, 0 -> no value
    public String    display;   // "0" - "9", string representation of value
    public int       possCnt;   // 0 - 9 count of possible values
    public String    possible;  // "123456789" impossible values are blanked
    public boolean[] possibool; // indecies 1-9 are true when value possible
    public square[]  siblings;  // squares in the same row, column, box

    public square(int s) {
        // constructor does one-time initializations
        index     = s;
        row       = index / 9;
        column    = index % 9;
        siblings  = new square[20];
        possibool = new boolean[10];    // [0] unused
        reset();
    }

    public final void reset() {
        value    = 0;
        display  = "0";
        possible = "123456789";
        possCnt  = 9;
        for(int i=1; i<10; i++)
            possibool[i] = true;
    }

    public void setSiblings(square[] sqr) {
        int sib = 0;                    // index into siblings[]
        int idx = row * 9;              // index of first square in row
        for(int i=0; i<9; i++, idx++)
            if(idx != index)
                siblings[sib++] = sqr[idx];

        idx = column;                   // index of this column on row 0
        for(int i=0; i<9; i++, idx+=9)
            if(idx != index)
                siblings[sib++] = sqr[idx];

        int boxRow = (row / 3) * 3;
        int boxCol = (column / 3) * 3;
        for(int r = boxRow; r<boxRow+3; r++)
            for(int c = boxCol; c<boxCol+3; c++)
                if(r != row && c != column)
                    siblings[sib++] = sqr[(r * 9) + c];
    }

    public void setValue(int v) {
        value    = v;
        display  = String.valueOf(v);
        possible = "         ";
        possCnt  = 0;
        for(int i=1; i<10; i++)
            possibool[i] = false;

        // value just set is now impossible for the siblings, so...
        for(int i=0; i<20; i++)
            siblings[i].setImpossible(value, display);
    }

    private void setImpossible(int v, String c) {
        if( possibool[v] ) {    // if because it might already be impossible
            possible = possible.replace(c, " ");
            possibool[v] = false;
            possCnt--;
        }
    }

}
