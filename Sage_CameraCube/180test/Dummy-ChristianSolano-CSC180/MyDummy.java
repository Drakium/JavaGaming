//Christian Solano
//Dummy
//Project 2 CSC 180

import java.util.*;
import java.util.concurrent.TimeUnit;

public class MyDummy {
    private static final String RESET = "\u001B[0m";
    private static final String BRIGHT = "\u001B[1m";
    private static final String DIM = "\u001B[2m";
    private static final String UNDERLINE = "\u001B[3m";
    private static final String BLINK = "\u001B[4m";
    private static final String REVERSE = "\u001B[7m";
    private static final String BLACK = "\u001B[30m";
    private static final String RED = "\u001B[31m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String BLUE = "\u001B[34m";
    private static final String MAGENTA = "\u001B[35m";
    private static final String CYAN = "\u001B[36m";
    private static final String WHITE = "\u001B[37m";

    private int[][] myBoard = new int[8][6];
    
    private Stack<int[]> availableMoves = new Stack<>();
    private int alphaBeta = 0;
   
    private long TimeStart;
    long TimeLimit = TimeUnit.SECONDS.toNanos(4);

    private int Winner = 0; 
    private int MaxDepth = 5;
    private boolean GameOver = false;
    private void setupBoard(){
         
         for (int i = 0; i < 8; i ++){
            for(int j = 0; j <6; j++){
             myBoard[i][j]= 0;
            }
         }
         myBoard[0][4] = 1;
         myBoard[1][0] = 2;
         myBoard[1][1] = 3;
         myBoard[1][2] = 4;
         myBoard[1][3] = 4;
         myBoard[1][4] = 3;
         myBoard[1][5] = 2;
         myBoard[2][2] = 5;
         myBoard[2][3] = 5;
         myBoard[5][2] = 10;
         myBoard[5][3] = 10;
         myBoard[6][0] = 7;
         myBoard[6][1] = 8;
         myBoard[6][2] = 9;
         myBoard[6][3] = 9;
         myBoard[6][4] = 8;
         myBoard[6][5] = 7;
         myBoard[7][1] = 6;

      
         
    }
    public void startGame(){
    
        Scanner input = new Scanner(System.in);
        System.out.print("Will the human or computer make the first move? " );
        String whoMoves = input.next();
        printGame();
        if (whoMoves.equals("human")){
            
            humansTurn();
            
        }

        while (GameOver != true){         
            computersTurn();
            if(GameOver != true){
                humansTurn();
            }
        }

        //determine winner
        String winner;
        switch (Winner){
            case 1:
                winner = "Human";
                break;
            case 2:
                winner = "Computer";
                break;
            default:
                winner = "error";
                break;
        }

        System.out.println(winner + "is the winner!");
    }

    
    private ComputersMoves computerDecideAMove() {
		  TimeStart = System.nanoTime();
        int i = 0;
        int depthSet = 0;
        ComputerHumanScores moveAndScore = null;
        while((System.nanoTime() - TimeStart) < TimeLimit){
            ComputerHumanScores temp = miniMax(MaxDepth+i);
            if(temp.getScore() != -10000){
                if((MaxDepth+i) > depthSet){
                    if(moveAndScore == null || (temp.getScore() > moveAndScore.getScore())){
                        moveAndScore = temp;
                    }
                }
            }
            i++;
        }
        int[] move = moveAndScore.getMove();
       

        return new ComputersMoves(String.valueOf(new char[]{checkInts(move[0]),
           Character.forDigit(move[1]+1, 10),checkInts(move[2]),Character.forDigit(move[3]+1, 10)}),
           String.valueOf(new char[]{checkInts(convertIntToFlippedBoard(move[0]+1)),
           Character.forDigit(convertIntToFlippedBoard(move[1])+2, 10),checkInts(convertIntToFlippedBoard(move[2]+1)),
           Character.forDigit(convertIntToFlippedBoard(move[3])+2, 10)}));
    }

    private ComputerHumanScores miniMax(int maxDepth) {
        ComputerHumanScores best = new ComputerHumanScores(-10000, null);
        int bestScore = best.getScore();
        int moveScore;
        int i = 0;
        int[][] moves = checkComputerMoves().getMoves();
        boolean bestMoveSet = false;
        while(moves[i][0] != 0 || moves[i][1] != 0 || moves[i][2] != 0 || moves[i][3] != 0){
            long temp = System.nanoTime();
            long temp1 = temp - TimeStart;
            if(temp1 > TimeLimit){
                return new ComputerHumanScores(-10000, null);
            }
            makeTempMove(moves[i], false);
            moveScore = min(0, maxDepth, 10000, bestScore);
            if(moveScore != -10000) {
                if (moveScore > bestScore) {
                    bestScore = moveScore;
                    best.setMove(moves[i]);
                    bestMoveSet = true;
                
                }
                if (!bestMoveSet) {
                    best.setMove(moves[i]);
                }
            }
            undoTempMove();
            i++;
        }

        best.setScore(bestScore);
        return best;
    }
    

    private int min(int depth, int maxDepth, int bestMinSoFar, int bestMaxSoFar) {
        if (isGameOver(true)){
            return 10000;
        }
        if (depth == maxDepth){
            return posCheck(true);
        }
        ComputerHumanScores best = new ComputerHumanScores(10000, null);
        int bestScore = best.getScore();
        int moveScore;
        int[][] moves = checkHumanMoves().getMoves();

       int i = 0;
        while(moves[i][0] != 0 || moves[i][1] != 0 || moves[i][2] != 0 || moves[i][3] != 0){
            if((System.nanoTime() - TimeStart) > TimeLimit){
                return -10000;
            }
            makeTempMove(moves[i], true);
            moveScore = max(depth+1, maxDepth, bestMinSoFar, bestMaxSoFar);
			if(moveScore < bestMaxSoFar){
                undoTempMove();
                alphaBeta++;
                return moveScore;
            }
            if(moveScore != -10000) {
                if (moveScore < bestScore) {
                    bestScore = moveScore;
                    best.setMove(moves[i]);
					bestMinSoFar = moveScore;
                }
            }
            undoTempMove();
            i++;
        }

        return bestScore;
    }
  // checks the max score of the minimax with max depth. 
    private int max(int depth, int maxDepth, int bestMinSoFar, int bestMaxSoFar) {
        if (isGameOver(true)){
            return -10000;
        }
        if (depth == maxDepth){
            return posCheck(false);
        }
        ComputerHumanScores best = new ComputerHumanScores(-10000, null);
        int bestScore = best.getScore();
        int moveScore;
        int[][] moves = checkComputerMoves().getMoves();

        int i = 0;
        while(moves[i][0] != 0 || moves[i][1] != 0 || moves[i][2] != 0 || moves[i][3] != 0){
            if((System.nanoTime() - TimeStart) > TimeLimit){
                return -10000;
            }
            makeTempMove(moves[i], false);
            moveScore = min(depth+1, maxDepth, bestMinSoFar, bestMaxSoFar);
            if(moveScore > bestMinSoFar){
                undoTempMove();
                alphaBeta++;
                return moveScore;
            }
            if(moveScore != -10000) {
                if (moveScore > bestScore) {
                    bestScore = moveScore;
                    best.setMove(moves[i]);
					bestMaxSoFar = moveScore;
                }
            }
            undoTempMove();
            i++;
        }
        
        return bestScore;
    }
    //Sets a temporary move for the computer to calculate the next moves
    private void undoTempMove() {
        int[] move = availableMoves.pop();
        int w = move[0], up = move[1], y = move[2], z = move[3], piece = move[4];
   

        myBoard[z][y] = myBoard[up][w];
        myBoard[up][w] = piece;
    }

    private void makeTempMove(int[] move, boolean humanMove) {
        int w = move[0], up = move[1], y = move[2], z = move[3];
        availableMoves.push(new int[]{y, z, w, up, myBoard[z][y]});
        myBoard[z][y] = myBoard[up][w];
        myBoard[up][w] = 0;

       
    }


    private int posCheck(boolean humanMove){
        int score = 0;
        int[][] movesC;
        int[][] computerMpieces;
        int[][] humanMpieces;
        myPieces moveAndMovablePiecesHolderC;
        myPieces moveAndMovablePiecesHolderH;
      
        moveAndMovablePiecesHolderC = checkComputerMoves();
        moveAndMovablePiecesHolderH = checkHumanMoves();
        movesC = moveAndMovablePiecesHolderC.getMoves();
        computerMpieces = moveAndMovablePiecesHolderC.getMovablePieces();
        humanMpieces = moveAndMovablePiecesHolderH.getMovablePieces();

        for(int i=0;i<humanMpieces.length;i++){
            if(myBoard[humanMpieces[i][0]][humanMpieces[i][1]] == 1){
                score -= 50;
            }
            if(myBoard[humanMpieces[i][0]][humanMpieces[i][1]] == 2){
                score -= 40;
            }
            if(myBoard[humanMpieces[i][0]][humanMpieces[i][1]] == 3){
                score -= 30;
            }
            if(myBoard[humanMpieces[i][0]][humanMpieces[i][1]] == 4){
                score -= 20;
            }
             if(myBoard[humanMpieces[i][0]][humanMpieces[i][1]] == 5){
                score -= 10;
            }


        }

        for(int i=0;i<computerMpieces.length;i++){
            if(myBoard[computerMpieces[i][0]][computerMpieces[i][1]] == 6){
                score += 50;
            }
            if(myBoard[computerMpieces[i][0]][computerMpieces[i][1]] == 7){
                score += 40;
            }
            if(myBoard[computerMpieces[i][0]][computerMpieces[i][1]] == 8){
                score += 30;
            }
            if(myBoard[computerMpieces[i][0]][computerMpieces[i][1]] == 9){
                score += 20;
            }
            if(myBoard[computerMpieces[i][0]][computerMpieces[i][1]] == 10){
                score += 10;
            }

        }
        

        return score;
    }

    private void printMoves(int[][] moves) {
        for (int[] move1 : moves) {
            String move = "";
            if (move1[0] != 0 || move1[1] != 0 || move1[2] != 0 || move1[3] != 0) {
                for (int j = 0; j < move1.length; j++) {
                    if (j == 0 || j == 2) {
                        move += checkInts(move1[j]);
                    } else {
                        move += move1[j] + 1 + " ";
                    }
                }
                System.out.println(move);
            }
        }
    }
    private void computersTurn() {
        ComputersMoves finalComputerMoveHolder = computerDecideAMove();
        String move = finalComputerMoveHolder.getMove();
        String convertedMove = finalComputerMoveHolder.getConvertedMove();
        makeMove(move, false);

        System.out.println("COMPUTER MOVES " + move + "   (" + convertedMove + ")\n");

        //Display Board
        printGame();

        GameOver = isGameOver(true);
    }

    private myPieces checkComputerMoves() {
        int[][] movablePieces = new int[9][2];
        int[][] moves = new int[100][4];
        int k = 0, l = 0;

        //Get computer pieces from board
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 6; j++) {
                 if(myBoard[i][j] == 6 || myBoard[i][j] == 7 || myBoard[i][j] == 8 || myBoard[i][j] == 9 || myBoard[i][j] == 10){
                    movablePieces[k][0] = i;
                    movablePieces[k][1] = j;
                    k++;
                }
            }
        }
         
        for(int i=0; i<k; i++){
            //Kings movement
            if(myBoard[movablePieces[i][0]][movablePieces[i][1]] == 6){
                 int up = movablePieces[i][0];
                int y = movablePieces[i][1]+1;
                                
                if(up < 8 && y < 6 && (myBoard[up][y] == 0 || myBoard[up][y] == 2 || myBoard[up][y] == 3
                || myBoard[up][y] == 4 || myBoard[up][y] == 5)){
                    moves[l][0] = movablePieces[i][1];
                    moves[l][1] = movablePieces[i][0];
                    moves[l][2] = y;
                    moves[l][3] = up;
                    l++;
                    if(myBoard[up][y] != 0){
                        break;
                    }
            
                    y++;
                }
            
            }else if(myBoard[movablePieces[i][0]][movablePieces[i][1]] == 7){
                //Knights movement
                int up = movablePieces[i][0]-2;
                int y = movablePieces[i][1]+1;
                                
                if(up > -1 && y < 6 && (myBoard[up][y] == 0 || myBoard[up][y] == 1 || myBoard[up][y] == 2 
                || myBoard[up][y] == 3 || myBoard[up][y] == 4 || myBoard[up][y] == 5)){
                    moves[l][0] = movablePieces[i][1];
                    moves[l][1] = movablePieces[i][0];
                    moves[l][2] = y;
                    moves[l][3] = up;
                    l++;
                    if(myBoard[up][y] != 0){
                        break;
                    }
                    up= up -2;
                    y++;
                }
                
                up = movablePieces[i][0]-2;
                y = movablePieces[i][1]-1;
                
                if(up > -1 && y > -1 && (myBoard[up][y] == 0 || myBoard[up][y] == 1 || myBoard[up][y] == 2 
                || myBoard[up][y] == 3 || myBoard[up][y] == 4 || myBoard[up][y] == 5)){
                    moves[l][0] = movablePieces[i][1];
                    moves[l][1] = movablePieces[i][0];
                    moves[l][2] = y;
                    moves[l][3] = up;
                    l++;
                    if(myBoard[up][y] != 0){
                        break;
                    }
                    up= up-2;
                    y--;
                }
                up = movablePieces[i][0]-1;
                y = movablePieces[i][1]+2;
                if(up > -1 && y < 6 && (myBoard[up][y] == 0 || myBoard[up][y] == 1 || myBoard[up][y] == 2 
                || myBoard[up][y] == 3 || myBoard[up][y] == 4 || myBoard[up][y] == 5)){
                    moves[l][0] = movablePieces[i][1];
                    moves[l][1] = movablePieces[i][0];
                    moves[l][2] = y;
                    moves[l][3] = up;
                    l++;
                    if(myBoard[up][y] != 0){
                        break;
                    }
                    up--;
                    y = y +2;
                }
                
                up = movablePieces[i][0]-1;
                y = movablePieces[i][1]-2;
                
                if(up > -1 && y > -1 && (myBoard[up][y] == 0 || myBoard[up][y] == 1 || myBoard[up][y] == 2 
                || myBoard[up][y] == 3 || myBoard[up][y] == 4 || myBoard[up][y] == 5)){
                    moves[l][0] = movablePieces[i][1];
                    moves[l][1] = movablePieces[i][0];
                    moves[l][2] = y;
                    moves[l][3] = up;
                    l++;
                    if(myBoard[up][y] != 0){
                        break;
                    }
                    up--;
                    y = y -2;
                }
                
             
                
                
            }else if(myBoard[movablePieces[i][0]][movablePieces[i][1]] == 8){
                //Bishop Movement
          
                int up = movablePieces[i][0]-1;
                int y = movablePieces[i][1]-1;
                while(up > -1 && y > -1 && (myBoard[up][y] == 0 || myBoard[up][y] == 1 || myBoard[up][y] == 2 
                || myBoard[up][y] == 3 || myBoard[up][y] == 4 || myBoard[up][y] == 5)){
                    moves[l][0] = movablePieces[i][1];
                    moves[l][1] = movablePieces[i][0];
                    moves[l][2] = y;
                    moves[l][3] = up;
                    l++;
                    if(myBoard[up][y] != 0){
                        break;
                    }
                    up--;
                    y--;
                }

      
                up = movablePieces[i][0]-1;
                y = movablePieces[i][1]+1;
                while(up > -1 && y < 6 && (myBoard[up][y] == 0 || myBoard[up][y] == 1 || myBoard[up][y] == 2 
                || myBoard[up][y] == 3 || myBoard[up][y] == 4 || myBoard[up][y] == 5)){
                    moves[l][0] = movablePieces[i][1];
                    moves[l][1] = movablePieces[i][0];
                    moves[l][2] = y;
                    moves[l][3] = up;
                    l++;
                    if(myBoard[up][y] != 0){
                        break;
                    }
                    up--;
                    y++;
                }

                //Bishop Backward Capture
                up = movablePieces[i][0]+1;
                y = movablePieces[i][1]-1;
                while(up < 8 && y > -1 && (myBoard[up][y] == 0 || myBoard[up][y] == 1 || myBoard[up][y] == 2 
                || myBoard[up][y] == 3 || myBoard[up][y] == 4 || myBoard[up][y] == 5)){
                    if(myBoard[up][y] == 1 || myBoard[up][y] == 2 
                || myBoard[up][y] == 3 || myBoard[up][y] == 4 || myBoard[up][y] == 5){
                        moves[l][0] = movablePieces[i][1];
                        moves[l][1] = movablePieces[i][0];
                        moves[l][2] = y;
                        moves[l][3] = up;
                        l++;
                    }
                    if(myBoard[up][y] != 0){
                        break;
                    }
                    up++;
                    y--;
                }

                //Bishop Backward Capture
                up = movablePieces[i][0]+1;
                y = movablePieces[i][1]+1;
                while(up < 8 && y < 6 && (myBoard[up][y] == 0 || myBoard[up][y] == 1 || myBoard[up][y] == 2 
                || myBoard[up][y] == 3 || myBoard[up][y] == 4 || myBoard[up][y] == 5)){
                    if(myBoard[up][y] == 1 || myBoard[up][y] == 2 
                || myBoard[up][y] == 3 || myBoard[up][y] == 4 || myBoard[up][y] == 5){
                        moves[l][0] = movablePieces[i][1];
                        moves[l][1] = movablePieces[i][0];
                        moves[l][2] = y;
                        moves[l][3] = up;
                        l++;
                    }
                    if(myBoard[up][y] != 0){
                        break;
                    }
                    up++;
                    y++;
                }
                
               
            }else if(myBoard[movablePieces[i][0]][movablePieces[i][1]] == 9){
                //Rook Movement
                int up = movablePieces[i][0]-1;
                int y = movablePieces[i][1];
                //Rook Forward
                while(up > -1 && (myBoard[up][y] == 0 || myBoard[up][y] == 1 || myBoard[up][y] == 2 
                || myBoard[up][y] == 3 || myBoard[up][y] == 4 || myBoard[up][y] == 5)){
                    moves[l][0] = movablePieces[i][1];
                    moves[l][1] = movablePieces[i][0];
                    moves[l][2] = y;
                    moves[l][3] = up;
                    l++;
                    if(myBoard[up][y] != 0){
                        break;
                    }
                    up--;
                }
                up = movablePieces[i][0];
                y = movablePieces[i][1]+1;
                while(y < 6 && (myBoard[up][y] == 0 || myBoard[up][y] == 1 || myBoard[up][y] == 2 
                || myBoard[up][y] == 3 || myBoard[up][y] == 4 || myBoard[up][y] == 5)){
                    moves[l][0] = movablePieces[i][1];
                    moves[l][1] = movablePieces[i][0];
                    moves[l][2] = y;
                    moves[l][3] = up;
                    l++;
                    if(myBoard[up][y] != 0){
                        break;
                    }
                    y++;
                }
                up = movablePieces[i][0];
                y = movablePieces[i][1]-1;
                while(y > -1 && (myBoard[up][y] == 0 || myBoard[up][y] == 1 || myBoard[up][y] == 2 
                || myBoard[up][y] == 3 || myBoard[up][y] == 4 || myBoard[up][y] == 5)){
                    moves[l][0] = movablePieces[i][1];
                    moves[l][1] = movablePieces[i][0];
                    moves[l][2] = y;
                    moves[l][3] = up;
                    l++;
                    if(myBoard[up][y] != 0){
                        break;
                    }
                    y--;
                }

                
                up = movablePieces[i][0]+1;
                y = movablePieces[i][1];
                //Rook Back
                while(up < 8 && (myBoard[up][y] == 0 || myBoard[up][y] == 1 || myBoard[up][y] == 2 
                || myBoard[up][y] == 3 || myBoard[up][y] == 4 || myBoard[up][y] == 5)){
                    if(myBoard[up][y] == 1 || myBoard[up][y] == 2 
                || myBoard[up][y] == 3 || myBoard[up][y] == 4 || myBoard[up][y] == 5){
                        moves[l][0] = movablePieces[i][1];
                        moves[l][1] = movablePieces[i][0];
                        moves[l][2] = y;
                        moves[l][3] = up;
                        l++;
                    }
                    if(myBoard[up][y] != 0){
                        break;
                    }
                    up++;
                }
                
                    
                
                
            }else if(myBoard[movablePieces[i][0]][movablePieces[i][1]] == 10){
                //Pawn Movement
                int up = movablePieces[i][0]-1;
                int y = movablePieces[i][1];
                                
                if(up > -1 && y > -1 && (myBoard[up][y] == 0)){
                    moves[l][0] = movablePieces[i][1];
                    moves[l][1] = movablePieces[i][0];
                    moves[l][2] = y;
                    moves[l][3] = up;
                    l++;
                    if(myBoard[up][y] != 0){
                        break;
                    }
                    
                    up--;
                }
                up = movablePieces[i][0]-1;
                y = movablePieces[i][1]-1;
                if(up > -1 && y > -1 && (myBoard[up][y] == 1 || myBoard[up][y] == 2 
                || myBoard[up][y] == 3 || myBoard[up][y] == 4 || myBoard[up][y] == 5)){
                    moves[l][0] = movablePieces[i][1];
                    moves[l][1] = movablePieces[i][0];
                    moves[l][2] = y;
                    moves[l][3] = up;
                    l++;
                    if(myBoard[up][y] != 0){
                        break;
                    }
                    
                    up--;
                    y--;
                }
                up = movablePieces[i][0] -1;
                y = movablePieces[i][1] +1 ;
                if(up > -1 && y < 6 && (myBoard[up][y] == 1 || myBoard[up][y] == 2 
                || myBoard[up][y] == 3 || myBoard[up][y] == 4 || myBoard[up][y] == 5)){
                    moves[l][0] = movablePieces[i][1];
                    moves[l][1] = movablePieces[i][0];
                    moves[l][2] = y;
                    moves[l][3] = up;
                    l++;
                    if(myBoard[up][y] != 0){
                        break;
                    }
                    
                    up--;
                    y++;
                }

                
               
                
                
            }
        }

        return new myPieces(moves, movablePieces);
    }

    private myPieces checkHumanMoves(){
        int[][] movablePieces = new int[9][2];
        int[][] moves = new int[100][4];
        int k = 0, l = 0;

        //Get human pieces from board
        for(int i=0;i<8; i+=1){
            for(int j=0; j<6;j++){
                if(myBoard[i][j] == 1 || myBoard[i][j] == 2 || myBoard[i][j] == 3 || myBoard[i][j] == 4 || myBoard[i][j] == 5){
                    movablePieces[k][0] = i;
                    movablePieces[k][1] = j;
                    k++;
                }
            }
        }
         
        //Determine all moves for pieces
        for(int i=0; i<k; i++){
            //Kings movement
            if(myBoard[movablePieces[i][0]][movablePieces[i][1]] == 1){
                int up = movablePieces[i][0];
                int y = movablePieces[i][1]-1;
                                
                if(up < 8 && y > -1 && (myBoard[up][y] == 0 || myBoard[up][y] == 6 || myBoard[up][y] == 7
                || myBoard[up][y] == 8 || myBoard[up][y] == 9)){
                    moves[l][0] = movablePieces[i][1];
                    moves[l][1] = movablePieces[i][0];
                    moves[l][2] = y;
                    moves[l][3] = up;
                    l++;
                    if(myBoard[up][y] != 0){
                        break;
                    }
            
                    y--;
                }
            
            }else if(myBoard[movablePieces[i][0]][movablePieces[i][1]] == 2){
                //Knights movement
                int up = movablePieces[i][0]+2;
                int y = movablePieces[i][1]+1;
                                
                if(up < 8 && y < 6 && (myBoard[up][y] == 0 || myBoard[up][y] == 6 
                || myBoard[up][y] == 7 || myBoard[up][y] == 8 || myBoard[up][y] == 9 || myBoard[up][y] == 10)){
                    moves[l][0] = movablePieces[i][1];
                    moves[l][1] = movablePieces[i][0];
                    moves[l][2] = y;
                    moves[l][3] = up;
                    l++;
                    if(myBoard[up][y] != 0){
                        break;
                    }
                    up= up +2;
                    y++;
                }
                
                up = movablePieces[i][0]+2;
                y = movablePieces[i][1]-1;
                
                if(up < 8 && y > -1 && (myBoard[up][y] == 0|| myBoard[up][y] == 6 
                || myBoard[up][y] == 7 || myBoard[up][y] == 8 || myBoard[up][y] == 9 || myBoard[up][y] == 10)){
                    moves[l][0] = movablePieces[i][1];
                    moves[l][1] = movablePieces[i][0];
                    moves[l][2] = y;
                    moves[l][3] = up;
                    l++;
                    if(myBoard[up][y] != 0){
                        break;
                    }
                    up= up+2;
                    y--;
                }
                up = movablePieces[i][0]+1;
                y = movablePieces[i][1]+2;
                if(up < 8 && y < 6 && (myBoard[up][y] == 0|| myBoard[up][y] == 6 
                || myBoard[up][y] == 7 || myBoard[up][y] == 8 || myBoard[up][y] == 9 || myBoard[up][y] == 10)){
                    moves[l][0] = movablePieces[i][1];
                    moves[l][1] = movablePieces[i][0];
                    moves[l][2] = y;
                    moves[l][3] = up;
                    l++;
                    if(myBoard[up][y] != 0){
                        break;
                    }
                    up++;
                    y = y +2;
                }
                
                up = movablePieces[i][0]+1;
                y = movablePieces[i][1]-2;
                
                if(up < 8 && y > -1 && (myBoard[up][y] == 0|| myBoard[up][y] == 6 
                || myBoard[up][y] == 7 || myBoard[up][y] == 8 || myBoard[up][y] == 9 || myBoard[up][y] == 10)){
                    moves[l][0] = movablePieces[i][1];
                    moves[l][1] = movablePieces[i][0];
                    moves[l][2] = y;
                    moves[l][3] = up;
                    l++;
                    if(myBoard[up][y] != 0){
                        break;
                    }
                    up++;
                    y = y -2;
                }
                
               
                
                
            }else if(myBoard[movablePieces[i][0]][movablePieces[i][1]] == 3){
                //Bishop Movement
          
                int up = movablePieces[i][0]+1;
                int y = movablePieces[i][1]+1;
                while(up < 8 && y < 6 && (myBoard[up][y] == 0|| myBoard[up][y] == 6 
                || myBoard[up][y] == 7 || myBoard[up][y] == 8 || myBoard[up][y] == 9 || myBoard[up][y] == 10)){
                    moves[l][0] = movablePieces[i][1];
                    moves[l][1] = movablePieces[i][0];
                    moves[l][2] = y;
                    moves[l][3] = up;
                    l++;
                    if(myBoard[up][y] != 0){
                        break;
                    }
                    up++;
                    y++;
                }

      
                up = movablePieces[i][0]+1;
                y = movablePieces[i][1]-1;
                while(up < 8 && y > -1 && (myBoard[up][y] == 0 || myBoard[up][y] == 6 
                || myBoard[up][y] == 7 || myBoard[up][y] == 8 || myBoard[up][y] == 9 || myBoard[up][y] == 10)){
                    moves[l][0] = movablePieces[i][1];
                    moves[l][1] = movablePieces[i][0];
                    moves[l][2] = y;
                    moves[l][3] = up;
                    l++;
                    if(myBoard[up][y] != 0){
                        break;
                    }
                    up++;
                    y--;
                }

                //Bishop Backward Capture
                up = movablePieces[i][0]-1;
                y = movablePieces[i][1]+1;
                while(up > -1 && y < 6 && (myBoard[up][y] == 0 || myBoard[up][y] == 6 || myBoard[up][y] == 7)){
                    if(myBoard[up][y] == 5 || myBoard[up][y] == 6 || myBoard[up][y] == 7 || myBoard[up][y] == 8
                    || myBoard[up][y] == 9 || myBoard[up][y] == 10){
                        moves[l][0] = movablePieces[i][1];
                        moves[l][1] = movablePieces[i][0];
                        moves[l][2] = y;
                        moves[l][3] = up;
                        l++;
                    }
                    if(myBoard[up][y] != 0){
                        break;
                    }
                    up--;
                    y++;
                }

                //Bishop Backward Capture
                up = movablePieces[i][0]-1;
                y = movablePieces[i][1]-1;
                while(up > -1 && y > -1 && (myBoard[up][y] == 0 || myBoard[up][y] == 6 || myBoard[up][y] == 7)){
                    if(myBoard[up][y] == 5 || myBoard[up][y] == 6 || myBoard[up][y] == 7 || myBoard[up][y] == 8
                    || myBoard[up][y] == 9 || myBoard[up][y] == 10){
                        moves[l][0] = movablePieces[i][1];
                        moves[l][1] = movablePieces[i][0];
                        moves[l][2] = y;
                        moves[l][3] = up;
                        l++;
                    }
                    if(myBoard[up][y] != 0){
                        break;
                    }
                    up--;
                    y--;
                }
                
                
            }else if(myBoard[movablePieces[i][0]][movablePieces[i][1]] == 4){
                //Rook Movement
                int up = movablePieces[i][0]+1;
                int y = movablePieces[i][1];
                //Rook Forward
                while(up < 8 && (myBoard[up][y] == 0 || myBoard[up][y] == 6 || myBoard[up][y] == 7 ||
                myBoard[up][y] == 8 || myBoard[up][y] == 9 || myBoard[up][y] == 10)){
                    moves[l][0] = movablePieces[i][1];
                    moves[l][1] = movablePieces[i][0];
                    moves[l][2] = y;
                    moves[l][3] = up;
                    l++;
                    if(myBoard[up][y] != 0){
                        break;
                    }
                    up++;
                }
                up = movablePieces[i][0];
                y = movablePieces[i][1]+1;
                while(y < 6 && (myBoard[up][y] == 0 || myBoard[up][y] == 6 || myBoard[up][y] == 7 ||
                myBoard[up][y] == 8 || myBoard[up][y] == 9 || myBoard[up][y] == 10)){
                    moves[l][0] = movablePieces[i][1];
                    moves[l][1] = movablePieces[i][0];
                    moves[l][2] = y;
                    moves[l][3] = up;
                    l++;
                    if(myBoard[up][y] != 0){
                        break;
                    }
                    y++;
                }
                up = movablePieces[i][0];
                y = movablePieces[i][1]-1;
                while(y > -1 && (myBoard[up][y] == 0 || myBoard[up][y] == 6 || myBoard[up][y] == 7 ||
                myBoard[up][y] == 8 || myBoard[up][y] == 9 || myBoard[up][y] == 10)){
                    moves[l][0] = movablePieces[i][1];
                    moves[l][1] = movablePieces[i][0];
                    moves[l][2] = y;
                    moves[l][3] = up;
                    l++;
                    if(myBoard[up][y] != 0){
                        break;
                    }
                    y--;
                }
                
                up = movablePieces[i][0]-1;
                y = movablePieces[i][1];
                //Rook Back
                while(up > -1 && (myBoard[up][y] == 0 || myBoard[up][y] == 6 || myBoard[up][y] == 7 ||
                myBoard[up][y] == 8 || myBoard[up][y] == 9 || myBoard[up][y] == 10)){
                    if(myBoard[up][y] == 0 || myBoard[up][y] == 5 || myBoard[up][y] == 6 || myBoard[up][y] == 7 ||
                myBoard[up][y] == 8 || myBoard[up][y] == 9 || myBoard[up][y] == 10){
                        moves[l][0] = movablePieces[i][1];
                        moves[l][1] = movablePieces[i][0];
                        moves[l][2] = y;
                        moves[l][3] = up;
                        l++;
                    }
                    if(myBoard[up][y] != 0){
                        break;
                    }
                    up--;
                }
                
                    
              
                
            }else if(myBoard[movablePieces[i][0]][movablePieces[i][1]] == 5){
                //Pawn Movement
                int up = movablePieces[i][0]+1;
                int y = movablePieces[i][1];
                                
                if(up < 8 && y < 6 && (myBoard[up][y] == 0 || myBoard[up][y] == 6 
                || myBoard[up][y] == 7 || myBoard[up][y] == 8 || myBoard[up][y] == 9 || myBoard[up][y] == 10)){
                    moves[l][0] = movablePieces[i][1];
                    moves[l][1] = movablePieces[i][0];
                    moves[l][2] = y;
                    moves[l][3] = up;
                    l++;
                    if(myBoard[up][y] != 0){
                        break;
                    }
                    
                    up++;
                }
                up = movablePieces[i][0]+1;
                y = movablePieces[i][1]+1;
                if(up < 8 && y < 6 && (myBoard[up][y] == 6 
                || myBoard[up][y] == 7 || myBoard[up][y] == 8 || myBoard[up][y] == 9 || myBoard[up][y] == 10)){
                    moves[l][0] = movablePieces[i][1];
                    moves[l][1] = movablePieces[i][0];
                    moves[l][2] = y;
                    moves[l][3] = up;
                    l++;
                    if(myBoard[up][y] != 0){
                        break;
                    }
                    
                    up++;
                    y++;
                }
                up = movablePieces[i][0] +1;
                y = movablePieces[i][1] -1 ;
                if(up < 8 && y > -1 && (myBoard[up][y] == 6 
                || myBoard[up][y] == 7 || myBoard[up][y] == 8 || myBoard[up][y] == 9 || myBoard[up][y] == 10)){
                    moves[l][0] = movablePieces[i][1];
                    moves[l][1] = movablePieces[i][0];
                    moves[l][2] = y;
                    moves[l][3] = up;
                    l++;
                    if(myBoard[up][y] != 0){
                        break;
                    }
                    
                    up++;
                    y--;
                }

                
               
                
                
            }
        }

        return new myPieces(moves, movablePieces);
    }

    private int convertIntToFlippedBoard(int i){
        switch (i){
            case 0:
                return 6;
            case 1:
                return 5;
            case 2:
                return 4;
            case 3:
                return 3;
            case 4:
                return 2;
            case 5:
                return 1;
            case 6:
                return 0;
            default:
                return -1;
        }
    }

    private boolean isGameOver(boolean humanMove) {
        int[][] moves;
        
        /* if(myBoard[0][0] != 1 && myBoard[0][1] != 6 && myBoard[0][2] != 1 && 
        myBoard[0][3] != 1 && myBoard[0][4] != 1 && myBoard[0][5] != 1){
            Winner = 1;
            return true;
        }

         if(myBoard[7][0] != 6 && myBoard[7][1] != 6 && myBoard[7][2] != 6 && 
        myBoard[7][3] != 6 && myBoard[7][4] != 6 && myBoard[7][5] != 6){
            Winner = 2;
            return true;
        }*/
       

        return false;
    }

    private void humansTurn(){
        Scanner input = new Scanner(System.in);
        System.out.print("Enter your move in format A1B1: ");
        String move = input.next();

        //Check legal
        while(!legalMove(move)){
            System.out.print("Enter your move in format A1B1: ");
            input.nextLine();
            move = input.next();
        }

        makeMove(move, true);

        printGame();

        GameOver = isGameOver(false);
    }

    private boolean legalMove(String move) {

        if(move.length() != 4){
            System.out.print("Try a different input following the format.");
            return false;
        }
        int w = checkLetters(move.charAt(0));
        int up = Character.getNumericValue(move.charAt(1))-1;
        int y = checkLetters(move.charAt(2));
        int z = Character.getNumericValue(move.charAt(3))-1;

        int[][] moves = checkHumanMoves().getMoves();
        int i = 0;
        while(moves[i][0] != 0 || moves[i][1] != 0 || moves[i][2] != 0 || moves[i][3] != 0){
            if(moves[i][0] == w && moves[i][1] == up && moves[i][2] == y && moves[i][3] == z){
                return true;
            }
            i++;
        }

        System.out.print("Illegal, try a different move.");
        return false;
    }

    private void makeMove(String move, boolean humanMove) {
        while(!availableMoves.empty()){
            undoTempMove();
        }

        int w = checkLetters(move.charAt(0));
        int up = Character.getNumericValue(move.charAt(1))-1;
        int y = checkLetters(move.charAt(2));
        int z = Character.getNumericValue(move.charAt(3))-1;

        myBoard[z][y] = myBoard[up][w];
        if (myBoard[z][y] == 2){
            myBoard[z][y] = 4;
        }else
        if (myBoard[z][y] == 4){
            myBoard[z][y] = 3;
        }else 
        if (myBoard[z][y] == 3){
            myBoard[z][y] = 2;
        }else
        if (myBoard[z][y] == 7){
            myBoard[z][y] = 9;
        }else 
        if (myBoard[z][y] == 9){
            myBoard[z][y] = 8;
        }else 
        if (myBoard[z][y] == 8){
            myBoard[z][y] = 7;
        }
        myBoard[up][w] = 0;

        

    }

    private int checkLetters(char z) {
        switch (z){
            case 'A':case 'a':
                return 0;
            case 'B':case 'b':
                return 1;
            case 'C':case 'c':
                return 2;
            case 'D':case 'd':
                return 3;
            case 'E':case 'e':
                return 4;
            case 'F':case 'f':
                return 5;
            default:
                return -1;
        }
    }

    private char checkInts(int z) {
        switch (z){
            case 0:
                return 'A';
            case 1:
                return 'B';
            case 2:
                return 'C';
            case 3:
                return 'D';
            case 4:
                return 'E';
            case 5:
                return 'F';
            default:
                return 'X';
        }
    }

    
    private void printGame(){
       for(int i = 7; i>=0; i--){
            System.out.print(i+1+"  ");
            String line = "";
            for(int j = 0; j<6; j++){
             if (i == 6 && j == 5){
                  line += boardPiecesPrint(myBoard[i][j]) + "  computer";

             }else if (i == 0 && j == 5){
                  line += boardPiecesPrint(myBoard[i][j]) + "  human";
             
             }else{
                line += boardPiecesPrint(myBoard[i][j]) + " ";}
            }
            System.out.println(line);
        }
        System.out.println("   -----------");
        System.out.println("   A B C D E F");
        
        
        
    }

    private String boardPiecesPrint(int i){
            switch (i){
               case 0:
                    return "-";
                case 1:
                    return "k";
                case 2:
                    return "n";
                case 3:
                    return "b";
                case 4:
                    return "r";
                case 5:
                    return "p";
                case 6:
                    return "K";
                case 7:
                    return "N";
                case 8:
                    return "B";
                case 9:
                    return "R";
                case 10:
                    return "P";
                default:
                    return "-";
            }
       
    }
   public static void main(String[] args){
        MyDummy game = new MyDummy();
        game.setupBoard();
        game.startGame();
    }

}

class ComputersMoves{
    private String move;
    private String convertedMove;

    public ComputersMoves(String move, String convertedMove) {
        this.move = move;
        this.convertedMove = convertedMove;
    }

    public String getMove() {
        return move;
    }

    public String getConvertedMove() {
        return convertedMove;
    }
}

class ComputerHumanScores {
    private int score;
    private int[] move;

    ComputerHumanScores(int score, int[] move) {
        this.score = score;
        this.move = move;
    }

    int getScore() {
        return score;
    }

    void setScore(int score) {
        this.score = score;
    }

    int[] getMove() {
        return move;
    }

    void setMove(int[] move) {
        this.move = move;
    }
}

class myPieces{
    private int[][] moves;
    int[][] movablePieces;
    
    int[][] getMovablePieces() {
        return movablePieces;
    }

    myPieces(int[][] moves, int[][] movablePieces) {
        this.moves = moves;
        this.movablePieces = movablePieces;
    }

    int[][] getMoves() {
        return moves;
    }

    }
