import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Random;
import tester.*;
import javalib.impworld.*;
import java.awt.Color;
import javalib.worldimages.*;

class LightEmAll extends World {
  // a list of columns of GamePieces,
  // i.e., represents the board in column-major order
  ArrayList<ArrayList<GamePiece>> board;
  // a list of all nodes
  ArrayList<GamePiece> nodes;
  // a list of edges of the minimum spanning tree
  ArrayList<Edge> mst;
  // the width and height of the board
  int width;
  int height;
  // the current location of the power station,
  // as well as its effective radius
  int powerRow;
  int powerCol;
  int radius;
  int cellSize;
  Random rand;
  int numMoves;
  int timePassed;

  // constructor
  LightEmAll(ArrayList<ArrayList<GamePiece>> board, ArrayList<GamePiece> nodes, ArrayList<Edge> mst,
      int height, int width, int powerRow, int powerCol, int radius, int cellSize, Random rand) {
    this.board = board;
    this.nodes = nodes;
    this.mst = mst;
    this.width = width;
    this.height = height;
    this.powerRow = powerRow;
    this.powerCol = powerCol;
    this.radius = radius;
    this.cellSize = cellSize;
    this.rand = rand;
    this.numMoves = numMoves;
    this.timePassed = timePassed;
  }

  // constructor for game
  LightEmAll() {
    this.nodes = new ArrayList<GamePiece>();
    this.mst = new ArrayList<Edge>();
    this.width = 4;
    this.height = 4;
    this.powerRow = 0;
    this.powerCol = 0;
    this.cellSize = 200;
    this.board = new ArrayList<ArrayList<GamePiece>>();
    this.boardStartValues();
    this.initNodes();
    this.initNeighbors();
    this.initEdges();
    this.kruskalAlgo();
    this.initWires();
    this.radius = 0;
    this.rand = new Random();
    this.shuffleBoard();
    this.numMoves = 0;
    this.timePassed = 0;
  }

  // constructor for testing
  LightEmAll(Random rand) {
    this.nodes = new ArrayList<GamePiece>();
    this.mst = new ArrayList<Edge>();
    this.width = 8;
    this.height = 8;
    this.powerRow = 0;
    this.powerCol = 0;
    this.cellSize = 200;
    this.board = new ArrayList<ArrayList<GamePiece>>();
    this.boardStartValues();
    this.initNodes();
    this.initNeighbors();
    this.initEdges();
    this.kruskalAlgo();
    this.initWires();
    this.radius = 0;
    this.rand = rand;
    this.shuffleBoard();
    this.numMoves = 0;
    this.timePassed = 0;
  }

  // initializes a board with basic cells, and initializes the powerStation
  void boardStartValues() {
    for (int x = 0; x < this.height; x++) {
      ArrayList<GamePiece> row = new ArrayList<GamePiece>();
      for (int y = 0; y < this.width; y++) {
        row.add(new GamePiece(x, y));
      }
      this.board.add(row);
    }
    GamePiece powerStation = this.board.get(this.powerCol).get(this.powerRow);
    powerStation.powered = true;
    powerStation.powerStation = true;
  }

  // initializes the list of nodes
  void initNodes() {
    for (int x = 0; x < this.height; x++) {
      for (int y = 0; y < this.width; y++) {
        GamePiece currentGamePiece = this.board.get(x).get(y);
        this.nodes.add(currentGamePiece);
      }
    }
  }

  // initializes the edges using kruskal's algorithm
  void kruskalAlgo() {
    HashMap<GamePiece, GamePiece> representatives = new HashMap<GamePiece, GamePiece>();
    ArrayList<Edge> edgesInTree = new ArrayList<Edge>();
    this.mst.sort(new HighestDepth());
    ArrayList<Edge> worklist = this.mst;
    // initialize every node's representative to itself
    for (int x = 0; x < this.height; x++) {
      for (int y = 0; y < this.width; y++) {
        GamePiece currentGamePiece = this.board.get(x).get(y);
        representatives.put(currentGamePiece, currentGamePiece);
      }
    }
    while (!worklist.isEmpty()) {
      Edge currentEdge = worklist.remove(0);
      if (this.find(representatives, 
          currentEdge.fromNode).equals(this.find(representatives, 
              currentEdge.toNode))) {
        // do nothing as the worklist has already had it removed
      }
      else {
        edgesInTree.add(currentEdge);
        representatives.put(this.find(representatives, currentEdge.toNode),
            this.find(representatives, currentEdge.fromNode));
      }
    }
    this.mst = edgesInTree;
  }

  // finds a piece using the hashmap
  GamePiece find(HashMap<GamePiece, GamePiece> representatives, GamePiece p) {
    if (p.equals(representatives.get(p))) {
      return representatives.get(p);
    }
    else {
      return find(representatives, representatives.get(p));
    }
  }

  // Generates the wiring of the pieces
  void initWires() {
    for (Edge e : this.mst) {
      if (e.fromNode.row < e.toNode.row) {
        e.fromNode.bottom = true;
        e.toNode.top = true;
      }
      if (e.fromNode.col < e.toNode.col) {
        e.fromNode.right = true;
        e.toNode.left = true;
      }
      if (e.fromNode.row > e.toNode.row) {
        e.fromNode.top = true;
        e.toNode.bottom = true;
      }
      if (e.fromNode.col > e.toNode.col) {
        e.fromNode.left = true;
        e.toNode.right = true;
      }
    }
  }

  // Randomly rotates all pieces
  void shuffleBoard() {
    for (GamePiece current: this.nodes) {
      current.rotateRandomly(this.rand.nextInt(4));
    }
  }

  // initializes the neighbors for every GamePiece
  void initNeighbors() {
    GamePiece currentGamePiece;
    for (int x = 0; x < this.height; x++) {
      for (int y = 0; y < this.width; y++) {
        currentGamePiece = this.board.get(x).get(y);
        currentGamePiece.findNeighbors(this.board);
      }
    }
  }

  // initializes the edges of the board
  void initEdges() {
    for (GamePiece currentNode : this.nodes) {
      for (GamePiece currentNeighbor : currentNode.neighbors) {
        Edge currentEdge = new Edge(currentNode, currentNeighbor);
        this.mst.add(currentEdge);
        for (Edge currentEdgeInList : this.mst) {
          if ((currentEdgeInList.fromNode == currentNode
              && currentEdgeInList.toNode == currentNeighbor)
              && (currentEdgeInList.fromNode == currentNeighbor
              && currentEdgeInList.toNode == currentNode)) {
            this.mst.remove(currentEdge);
          }
        }
      }
    }
  }
  
  public void onTick() {
    int timeSoFar = 0;
    if (timeSoFar % 28 == 0) {
    this.timePassed += 1;
  }
    timeSoFar ++;
  }

  // draws scene
  public WorldScene makeScene() {
    this.updatePowered();
    if (this.didWin()) {
      return this.lastScene("You win!");
    }
    
    else {
      WorldScene image = new WorldScene(this.cellSize * this.width, this.cellSize * this.height);
      WorldImage cell;
      WorldImage movesAndTime = new TextImage(Integer.toString(this.numMoves) + Integer.toString(this.timePassed), 10, Color.BLUE);

      for (int x = 0; x < this.height; x++) {
        for (int y = 0; y < this.width; y++) {
          GamePiece currentCell = this.board.get(x).get(y);
          cell = currentCell.drawPiece(this.cellSize);
          image.placeImageXY(cell, y * this.cellSize + this.cellSize / 2,
              x * this.cellSize + this.cellSize / 2);
        }
        image.placeImageXY(
            new RectangleImage(this.height * this.width, 10, OutlineMode.SOLID, Color.LIGHT_GRAY),
            this.width * this.height / 2, this.height * this.height);
            image.placeImageXY(new TextImage("Steps: " + Integer.toString(this.numMoves), 10, Color.BLACK), 50,
            this.height * this.height);
      }
      return image;
    }
  }

  // checks if the game has been won
  boolean didWin() {
    for (int x = 0; x < this.height; x++) {
      for (int y = 0; y < this.width; y++) {
        GamePiece currentCell = this.board.get(x).get(y);
        if (!currentCell.powered) {
          return false;
        }
      }
    }
    return true;
  }

  // draws the game in the won state
  // draws the last scene
  public WorldScene lastScene(String text) {
    TextImage endText = new TextImage(text, 30, Color.GREEN);
    int height = this.cellSize * this.height;
    int width = this.cellSize * this.width;
    WorldScene background = new WorldScene(height, width);
    background.placeImageXY(endText, height / 2, width / 2);
    return background;
  }

  // updates if cells are powered or not
  void updatePowered() {
    GamePiece powerCell = this.board.get(powerCol).get(powerRow);
    for (int x = 0; x < this.height; x++) {
      for (int y = 0; y < this.width; y++) {
        GamePiece currentCell = this.board.get(x).get(y);
        currentCell.powered = false;
      }
    }
    powerCell.powered = true;
    this.radius = this.initRadius();
    powerCell.lightUp(this.radius);
  }

  // finds radius using breath-first-search, using the farthest piece from the
  // powerStation
  int initRadius() {
    ArrayList<GamePiece> que = new ArrayList<GamePiece>();
    que.add(this.getFurthest());
    ArrayList<GamePiece> visited = new ArrayList<GamePiece>();
    while (!que.isEmpty()) {
      GamePiece p = que.get(0);
      visited.add(p);
      que.remove(p);
      for (GamePiece n : p.neighbors) {
        if (!visited.contains(n)) {
          n.depth = p.depth + 1;
          que.add(n);
        }
      }
    }
    return (this.findMaxDepth(visited).depth + 1) / 2;
  }

  // finds the GamePiece with the largest depth in a list
  GamePiece findMaxDepth(ArrayList<GamePiece> visited) {
    GamePiece ans = null;
    int max = 0;
    for (int i = 0; i < visited.size(); i++) {
      GamePiece currentPiece = visited.get(i);
      if (currentPiece.depth >= max) {
        max = currentPiece.depth;
        ans = currentPiece;
      }
    }
    return ans;
  }

  // returns the farthest GamePiece from powerStation in this game, using
  // breath-first search
  GamePiece getFurthest() {
    GamePiece furthest = null;
    ArrayList<GamePiece> que = new ArrayList<GamePiece>(
        Arrays.asList(this.board.get(powerCol).get(powerRow)));
    ArrayList<GamePiece> visited = new ArrayList<GamePiece>();
    while (!que.isEmpty()) {
      GamePiece p = que.get(0);
      visited.add(p);
      furthest = p;
      que.remove(p);
      for (GamePiece n : p.neighbors) {
        if (!visited.contains(n)) {
          que.add(n);
        }
      }
    }
    return furthest;
  }

  // handles the arrow key presses
  public void onKeyEvent(String key) {
    GamePiece makePower;
    GamePiece changePower = this.board.get(powerCol).get(powerRow);
    if (key.equals("right") && this.powerRow < this.width - 1) {
      makePower = this.board.get(powerCol).get(powerRow + 1);
      
      if (makePower.isConnected(changePower)) {
        makePower.powerStation = true;
        changePower.powerStation = false;
        powerRow += 1;
        this.numMoves += 1;
      }
    }

    if (key.equals("left") && this.powerRow > 0) {
      makePower = this.board.get(powerCol).get(powerRow - 1);
      

      if (makePower.isConnected(changePower)) {
        makePower.powerStation = true;
        changePower.powerStation = false;
        powerRow -= 1;
        this.numMoves += 1;
      }
    }

    if (key.equals("down") && powerCol < this.height - 1) {
      makePower = this.board.get(powerCol + 1).get(powerRow);
     
      if (makePower.isConnected(changePower)) {
        makePower.powerStation = true;
        changePower.powerStation = false;
        powerCol += 1;
        this.numMoves += 1;
      }
    }

    if (key.equals("up") && powerCol > 0) {
      makePower = this.board.get(powerCol - 1).get(powerRow);

      if (makePower.isConnected(changePower)) {
        makePower.powerStation = true;
        changePower.powerStation = false;
        powerCol -= 1;
        this.numMoves += 1;
      }
    }

  }

  // handles mouse clicks on the game
  public void onMouseClicked(Posn p, String buttonName) {
    if (buttonName.equals("LeftButton")) {
      GamePiece clicked = this.board.get(p.y / this.cellSize).get(p.x / this.cellSize);
      clicked.rotateHelp();
      this.numMoves += 1;
    }
  }
}

class GamePiece {
  // in logical coordinates, with the origin
  // at the top-left corner of the screen
  int row;
  int col;
  // whether this GamePiece is isConnected to the
  // adjacent left, right, top, or bottom pieces
  boolean left;
  boolean right;
  boolean top;
  boolean bottom;
  // whether the power station is on this piece
  boolean powerStation;

  boolean powered;
  int depth;
  ArrayList<GamePiece> neighbors;

  // constructor
  GamePiece(int row, int col, boolean left, boolean right, boolean top, boolean bottom,
      boolean powerStation, boolean powered, int depth) {
    this.row = row;
    this.col = col;
    this.left = left;
    this.right = right;
    this.top = top;
    this.bottom = bottom;
    this.powerStation = powerStation;
    this.powered = powered;
    this.depth = depth;
    this.neighbors = new ArrayList<GamePiece>();
  }

  public void rotateRandomly(int nextInt) {
    for (int i = 0; i < nextInt; i++) {
      this.rotateHelp();
    }
  }

  // Convince constructor
  GamePiece(int row, int col) {
    this.row = row;
    this.col = col;
    this.left = false;
    this.right = false;
    this.top = false;
    this.bottom = false;
    this.powerStation = false;
    this.powered = false;
    this.depth = 1;
    this.neighbors = new ArrayList<GamePiece>();
  }

  // draws a piece based on a pieces attributes
  WorldImage drawPiece(int sizeOfCell) {
    WorldImage answer = new RectangleImage(sizeOfCell - 1, sizeOfCell - 1, OutlineMode.SOLID,
        Color.DARK_GRAY);
    Color colorForPiece = Color.LIGHT_GRAY;
    if (this.powered) {
      colorForPiece = Color.ORANGE;
    }
    if (this.left) {
      answer = new OverlayOffsetAlign(AlignModeX.LEFT, AlignModeY.MIDDLE,
          new RectangleImage(sizeOfCell / 2, 5, OutlineMode.SOLID, colorForPiece), 0, 0, answer);
    }
    if (this.right) {
      answer = new OverlayOffsetAlign(AlignModeX.RIGHT, AlignModeY.MIDDLE,
          new RectangleImage(sizeOfCell / 2, 5, OutlineMode.SOLID, colorForPiece), 0, 0, answer);
    }
    if (this.top) {
      answer = new OverlayOffsetAlign(AlignModeX.CENTER, AlignModeY.TOP,
          new RectangleImage(5, sizeOfCell / 2, OutlineMode.SOLID, colorForPiece), 0, 0, answer);
    }
    if (this.bottom) {
      answer = new OverlayOffsetAlign(AlignModeX.CENTER, AlignModeY.BOTTOM,
          new RectangleImage(5, sizeOfCell / 2, OutlineMode.SOLID, colorForPiece), 0, 0, answer);
    }
    if (this.powerStation) {
      answer = new OverlayOffsetAlign(AlignModeX.CENTER, AlignModeY.MIDDLE,
          new StarImage(sizeOfCell / 2, 7, OutlineMode.SOLID, Color.BLUE), 0, 0, answer);
    }
    return answer;
  }

  // rotates the given game piece by 90 degrees
  void rotateHelp() {
    boolean left = this.left;
    boolean right = this.right;
    boolean top = this.top;
    boolean bottom = this.bottom;

    this.bottom = right;
    this.left = bottom;
    this.top = left;
    this.right = top;
  }

  // finds the neighbors for a piece
  void findNeighbors(ArrayList<ArrayList<GamePiece>> board) {
    int width = board.size();

    int height = board.get(0).size();

    if (this.row < width - 1) {
      GamePiece toAdd = board.get(this.row + 1).get(this.col);
      this.neighbors.add(toAdd);
    }
    if (this.col < height - 1) {
      GamePiece toAdd = board.get(this.row).get(this.col + 1);
      this.neighbors.add(toAdd);
    }
    if (this.row > 0) {
      GamePiece toAdd = board.get(this.row - 1).get(this.col);
      this.neighbors.add(toAdd);
    }
    if (this.col > 0) {
      GamePiece toAdd = board.get(this.row).get(this.col - 1);
      this.neighbors.add(toAdd);
    }
  }

  // lights up a cell if it is on
  void lightUp(int radius) {
    if (radius > 0) {
      if (this.powered) {
        for (GamePiece currentNeighbor : this.neighbors) {
          if (currentNeighbor.isConnected(this) && !currentNeighbor.powered) {
            currentNeighbor.powered = true;
            currentNeighbor.lightUp(radius - 1);
          }
        }
      }
    }
  }

  // checks if a game piece is connected to another game piece
  boolean isConnected(GamePiece other) {
    if (this.row == other.row + 1 && this.col == other.col) {
      return this.top && other.bottom;
    }

    if (this.row == other.row - 1 && this.col == other.col) {
      return this.bottom && other.top;
    }

    if (this.col == other.col + 1 && this.row == other.row) {
      return this.left && other.right;
    }

    if (this.col == other.col - 1 && this.row == other.row) {
      return this.right && other.left;
    }
    return false;
  }
}

class Edge {
  GamePiece fromNode;
  GamePiece toNode;
  int weight;
  Random rand = new Random();

  // constructor
  Edge(GamePiece f, GamePiece t) {
    this.fromNode = f;
    this.toNode = t;
    this.weight = this.rand.nextInt(100);
  }

  //convenience constructor for testing
  Edge(GamePiece f, GamePiece t, int weight) {
    this.fromNode = f;
    this.toNode = t;
    this.weight = weight;
  }

}

class HighestDepth implements Comparator<Edge> {

  // compares the weight of two edges
  public int compare(Edge t1, Edge t2) {
    if (t1.weight > t2.weight) {
      return 1;
    }
    else if (t1.weight < t2.weight) {
      return -1;
    }
    else {
      return 0;
    }
  }

}

class ExamplesLightEmAll {

  void testGame(Tester t) {
    double tickRate = 1.0 / 28.0;
    LightEmAll game = new LightEmAll();
    game.bigBang(1000, 1000, tickRate);
  }

  GamePiece gp1;
  GamePiece gp2;
  GamePiece gp3;
  GamePiece gp4;
  GamePiece gp5;
  GamePiece gp6;
  LightEmAll test;
  ArrayList<ArrayList<GamePiece>> boardExample;
  ArrayList<GamePiece> row1;
  ArrayList<GamePiece> row2;
  WorldImage answer = new RectangleImage(20 - 1, 20 - 1, OutlineMode.SOLID, Color.DARK_GRAY);
  Edge e1;
  Edge e2;
  Edge e3;

  void initData() {
    this.gp1 = new GamePiece(0, 0, true, false, false, false, false, true, 1);
    this.gp2 = new GamePiece(0, 1);
    this.gp3 = new GamePiece(0, 2, true, false, true, false, false, true, 1);
    this.gp4 = new GamePiece(1, 0, false, false, false, true, true, true, 1);
    this.gp5 = new GamePiece(1, 1);
    this.gp6 = new GamePiece(1, 2);
    this.test = new LightEmAll();
    this.row1 = new ArrayList<GamePiece>();
    this.row2 = new ArrayList<GamePiece>();
    this.row1.add(gp1);
    this.row1.add(gp2);
    this.row1.add(gp3);
    this.row2.add(gp4);
    this.row2.add(gp5);
    this.row2.add(gp6);
    this.boardExample = new ArrayList<ArrayList<GamePiece>>();
    this.boardExample.add(row1);
    this.boardExample.add(row2);
    this.test = new LightEmAll(new Random(3));
    this.test.board = this.boardExample;
    e1 = new Edge(gp1, gp2, 32);
    e2 = new Edge(gp3, gp4, 67);
    e3 = new Edge(gp5, gp6, 67);

  }

  void initData2() {
    this.gp1 = new GamePiece(0, 0, true, false, false, false, false, true, 1);
    this.gp2 = new GamePiece(0, 1);
    this.gp3 = new GamePiece(0, 2, true, false, true, false, false, true, 1);
    this.gp4 = new GamePiece(1, 0, false, false, false, true, true, true, 1);
    this.gp5 = new GamePiece(1, 1);
    this.gp6 = new GamePiece(1, 2);
    this.row1 = new ArrayList<GamePiece>();
    this.row2 = new ArrayList<GamePiece>();
    this.row1.add(gp1);
    this.row1.add(gp2);
    this.row1.add(gp3);
    this.row2.add(gp4);
    this.row2.add(gp5);
    this.row2.add(gp6);
    this.boardExample = new ArrayList<ArrayList<GamePiece>>();
    this.boardExample.add(row1);
    this.boardExample.add(row2);
    this.test = new LightEmAll(new Random(3));
  }

  void testRotateHelp(Tester t) {
    initData();
    t.checkExpect(this.gp1.bottom, this.gp1.bottom);
    this.gp1.rotateHelp();
    t.checkExpect(this.gp1.bottom, this.gp1.right);
    initData();
    t.checkExpect(this.gp3.left, this.gp3.left);
    this.gp3.rotateHelp();
    t.checkExpect(this.gp3.left, this.gp3.bottom);
  }

  void testOnMouseClicked(Tester t) {
    initData();
    t.checkExpect(this.gp1.bottom, this.gp1.bottom);
    this.test.onMouseClicked(new Posn(0, 0), "LeftButton");
    t.checkExpect(this.gp1.bottom, this.gp1.right);
    initData();
    t.checkExpect(this.gp3.left, this.gp3.left);
    this.test.onMouseClicked(new Posn(100, 0), "LeftButton");
    t.checkExpect(this.gp3.left, this.gp3.bottom);
    initData();
    t.checkExpect(this.gp2.bottom, this.gp2.bottom);
    this.test.onMouseClicked(new Posn(50, 0), "LeftButton");
    t.checkExpect(this.gp2.bottom, this.gp2.bottom);
  }

  void testDrawPiece(Tester t) {
    initData();
    t.checkExpect(this.gp1.drawPiece(20), new OverlayOffsetAlign(AlignModeX.LEFT, AlignModeY.MIDDLE,
        new RectangleImage(20 / 2, 5, OutlineMode.SOLID, Color.ORANGE), 0, 0, answer));
    t.checkExpect(this.gp3.drawPiece(20),
        new OverlayOffsetAlign(AlignModeX.CENTER, AlignModeY.TOP,
            new RectangleImage(5, 20 / 2, OutlineMode.SOLID, Color.ORANGE), 0, 0,
            new OverlayOffsetAlign(AlignModeX.LEFT, AlignModeY.MIDDLE,
                new RectangleImage(20 / 2, 5, OutlineMode.SOLID, Color.ORANGE), 0, 0, answer)));
    t.checkExpect(this.gp4.drawPiece(20),
        new OverlayOffsetAlign(AlignModeX.CENTER, AlignModeY.MIDDLE,
            new StarImage(20 / 2, 7, OutlineMode.SOLID, Color.BLUE), 0, 0,
            new OverlayOffsetAlign(AlignModeX.CENTER, AlignModeY.BOTTOM,
                new RectangleImage(5, 20 / 2, OutlineMode.SOLID, Color.ORANGE), 0, 0, answer)));
  }

  void testBoardStartValues(Tester t) {
    initData();
    this.test.boardStartValues();
    t.checkExpect(this.test.board.get(0).get(0).bottom, false);
    t.checkExpect(this.test.board.get(0).get(0).top, false);
    t.checkExpect(this.test.board.get(0).get(1).bottom, false);
    t.checkExpect(this.test.board.get(0).get(1).top, false);
    t.checkExpect(this.test.board.get(0).get(2).powerStation, false);
  }

  /*
   * void testFractalInit(Tester t) { initData2(); this.test.boardStartValues();
   * this.test.fractalInit(0, 3, 0, 3);
   * t.checkExpect(this.test.board.get(0).get(0).top, false);
   * t.checkExpect(this.test.board.get(0).get(0).bottom, true);
   * t.checkExpect(this.test.board.get(1).get(0).right, true);
   * t.checkExpect(this.test.board.get(0).get(2).powerStation, true);
   * t.checkExpect(this.test.board.get(2).get(2).powerStation, false);
   * t.checkExpect(this.test.board.get(1).get(2).right, true);
   * t.checkExpect(this.test.board.get(2).get(2).left, false); }
   */

  void testIsConnected(Tester t) {
    initData2();
    this.test = new LightEmAll(new Random(3));
    this.test.initNeighbors();
    t.checkExpect(this.test.board.get(0).get(1).isConnected(this.test.board.get(0).get(2)), false);
    t.checkExpect(this.test.board.get(1).get(1).isConnected(this.test.board.get(2).get(2)), false);
    t.checkExpect(this.test.board.get(2).get(3).isConnected(this.test.board.get(0).get(2)), false);
    t.checkExpect(this.test.board.get(3).get(3).isConnected(this.test.board.get(3).get(2)), true);
    t.checkExpect(this.test.board.get(1).get(1).isConnected(this.test.board.get(0).get(2)), false);
    t.checkExpect(this.test.board.get(0).get(1).isConnected(this.test.board.get(3).get(2)), false);
    //Tests don't always pass because of random seed
  }

  void testLightUp(Tester t) {
    initData2();
    this.test.initNeighbors();
    t.checkExpect(this.test.board.get(3).get(2).powered, false);
    t.checkExpect(this.test.board.get(1).get(3).powered, false);
    this.test.board.get(3).get(3).powered = true;
    this.test.board.get(3).get(3).lightUp(2);
    t.checkExpect(this.test.board.get(3).get(2).powered, false);
    t.checkExpect(this.test.board.get(1).get(3).powered, false);
    //Tests don't always pass because of random seed
  }

  void testFindNeihgbors(Tester t) {
    initData2();
    t.checkExpect(this.gp1.neighbors.isEmpty(), true);
    this.gp1.findNeighbors(boardExample);
    t.checkExpect(this.gp1.neighbors.isEmpty(), false);
    t.checkExpect(this.gp2.neighbors.isEmpty(), true);
    this.gp2.findNeighbors(boardExample);
    t.checkExpect(this.gp2.neighbors.isEmpty(), false);
  }

  void testUpdatePowered(Tester t) {
    initData2();
    t.checkExpect(this.test.board.get(3).get(2).powered, false);
    this.test.board.get(3).get(2).powered = true;
    this.test.board.get(3).get(2).powered = false;
    this.test.updatePowered();
    t.checkExpect(this.test.board.get(3).get(2).powered, false);
  }

  void testInitNeighbors(Tester t) {
    initData2();
    t.checkExpect(this.gp1.neighbors.isEmpty(), true);
    t.checkExpect(this.gp2.neighbors.isEmpty(), true);
    this.test.initNeighbors();
    t.checkExpect(this.gp1.neighbors.isEmpty(), true);
    t.checkExpect(this.gp2.neighbors.isEmpty(), true);
  }

  void testOnKey(Tester t) {
    initData2();
    t.checkExpect(this.test.powerCol, 0);
    this.test.onKeyEvent("down");
    t.checkExpect(this.test.powerCol, 0);
    this.test.onKeyEvent("down");
    t.checkExpect(this.test.powerRow, 0);
    this.test.onKeyEvent("right");
    t.checkExpect(this.test.powerRow, 0);
  }

  

  void testDidWin(Tester t) {
    initData();
    t.checkExpect(this.test.didWin(), false);
    this.gp1.powered = true;
    this.gp2.powered = true;
    this.gp3.powered = true;
    this.gp4.powered = true;
    this.gp5.powered = true;
    this.gp6.powered = true;
    t.checkExpect(this.test.didWin(), true);
  }

  void testLastScene(Tester t) {
    TextImage endText = new TextImage("You Won", 30, Color.GREEN);
    int height = this.test.cellSize * this.test.height;
    int width = this.test.cellSize * this.test.width;
    WorldScene background = new WorldScene(height, width);
    background.placeImageXY(endText, height / 2, width / 2);
    t.checkExpect(this.test.lastScene("You Won"), background);
  }

  void testGetFurthest(Tester t) {
    initData();
    t.checkExpect(this.test.getFurthest(), this.gp3);
    initData2();
    t.checkExpect(this.test.getFurthest(), this.gp5);
    //Tests don't always pass because of random seed
  }

  void testFindMaxDepth(Tester t) {
    initData();
    ArrayList<GamePiece> testVisited = new ArrayList<GamePiece>();
    t.checkExpect(this.test.findMaxDepth(testVisited), null);
    initData();
    testVisited.add(this.gp1);
    t.checkExpect(this.test.findMaxDepth(testVisited), this.gp1);
  }

  void testInitNodes(Tester t) {
    initData();
    t.checkExpect(this.test.nodes.isEmpty(), false);
    ArrayList<GamePiece> nList = new ArrayList<GamePiece>(Arrays.asList(gp1, 
        gp2, gp3, gp4, gp5, gp6));
    this.test.initNodes();
    t.checkExpect(this.test.nodes.isEmpty(), false);
    t.checkExpect(this.test.nodes, nList);
  }

  void testRotateRandomly(Tester t) {
    initData();
    t.checkExpect(this.gp1.bottom, this.gp1.bottom);
    this.gp1.rotateRandomly(0);
    t.checkExpect(this.gp1.bottom, this.gp1.bottom);
    initData();
    t.checkExpect(this.gp1.bottom, this.gp1.bottom);
    this.gp1.rotateRandomly(1);
    t.checkExpect(this.gp1.bottom, this.gp1.right);
    initData();
    t.checkExpect(this.gp3.left, this.gp3.left);
    this.gp3.rotateRandomly(2);
    t.checkExpect(this.gp3.left, this.gp3.top);
  }

  void testShuffleBoard(Tester t) {
    initData();
    t.checkExpect(this.gp1.bottom, this.gp1.bottom);
    t.checkExpect(this.gp2.bottom, this.gp2.bottom);
    t.checkExpect(this.gp3.bottom, this.gp3.bottom);
    t.checkExpect(this.gp4.bottom, this.gp4.bottom);
    t.checkExpect(this.gp5.bottom, this.gp5.bottom);
    t.checkExpect(this.gp6.bottom, this.gp6.bottom);
    this.test.shuffleBoard();
    t.checkExpect(this.gp1.bottom, this.gp1.right);
    t.checkExpect(this.gp2.bottom, this.gp2.left);
    t.checkExpect(this.gp3.bottom, this.gp3.right);
    t.checkExpect(this.gp4.bottom, this.gp4.bottom);
    t.checkExpect(this.gp5.bottom, this.gp5.left);
    t.checkExpect(this.gp6.bottom, this.gp6.left);
  }

  void testInitRadius(Tester t) {
    initData();
    t.checkExpect(this.test.initRadius(), 1);
    initData2();
    t.checkExpect(this.test.initRadius(), 8);
  }

  void testKruskalAlgo(Tester t) {
    LightEmAll test = new LightEmAll(new Random(5));
    test.rand = new Random();
    test.board = new ArrayList<ArrayList<GamePiece>>();
    test.nodes = new ArrayList<GamePiece>();
    test.mst = new ArrayList<Edge>();
    test.boardStartValues();
    test.initNodes();
    test.initNeighbors();
    test.initEdges();
    t.checkExpect(test.mst.size() == test.nodes.size() - 1, false);
    test.kruskalAlgo();
    t.checkExpect(test.mst.size() == test.nodes.size() - 1, true);
  }

  void testInitEdges(Tester t) {
    LightEmAll test = new LightEmAll(new Random(5));
    test.rand = new Random();
    test.board = new ArrayList<ArrayList<GamePiece>>();
    test.nodes = new ArrayList<GamePiece>();
    test.mst = new ArrayList<Edge>();
    test.boardStartValues();
    test.initNodes();
    test.initNeighbors();
    t.checkExpect(test.mst.isEmpty(), true);
    test.initEdges();
    t.checkExpect(test.mst.isEmpty(), false);
  }

  void testComparitor(Tester t) {
    initData();
    HighestDepth hidepth = new HighestDepth();
    t.checkExpect(hidepth.compare(this.e1, this.e2), -1);
    t.checkExpect(hidepth.compare(this.e2,  this.e1), 1);
    t.checkExpect(hidepth.compare(this.e2, this.e3), 0);
  }

  void testInitWires(Tester t) {
    LightEmAll test = new LightEmAll(new Random(5));
    test.rand = new Random();
    test.board = new ArrayList<ArrayList<GamePiece>>();
    test.nodes = new ArrayList<GamePiece>();
    test.mst = new ArrayList<Edge>();
    test.boardStartValues();
    test.initNodes();
    test.initNeighbors();
    test.initEdges();
    test.kruskalAlgo();
    t.checkExpect(test.mst.get(1).fromNode.bottom, false);
    test.initWires();
    t.checkExpect(test.mst.get(1).fromNode.bottom, true);
  }
  
  void testFind(Tester t) {
    initData();
    HashMap<GamePiece, GamePiece> hi = new HashMap<GamePiece, GamePiece>();
    t.checkExpect(this.test.find(hi, gp1), gp1);
    t.checkExpect(this.test.find(hi, gp2), gp2);
    t.checkExpect(this.test.find(hi, gp3), gp3);
  }
  
  void testMoves(Tester t) {
    initData();
    t.checkExpect(this.test.numMoves,  0);
    this.test.onMouseClicked(new Posn(0, 1), "LeftButton");
    t.checkExpect(this.test.numMoves,  1);
  }
  
  void testTime(Tester t) {
    initData();
  }
}
