package android_final.jack.omok;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Debug;
import android.support.v7.app.AlertDialog;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class Board extends View {

    //To abstract the spot you are in within the (when default) 19x19 board
    public class Spot{
        public int x;
        public int y;
        private boolean occupied;

        public double pixel_x; //The pixel that the spot is located at
        public double pixel_y;

        public Paint owner; //The owner of this spot is either black or white

        public Spot(int x, int y){
            this.x = x;
            this.y = y;
            this.occupied = false;

            double pixel_of_x_0 = pixel_per_spot/2.0;
            double pixel_of_y_0 = pixel_per_spot/2.0;

            this.pixel_x = pixel_of_x_0 + (x * pixel_per_spot);
            this.pixel_y = pixel_of_y_0 + (y * pixel_per_spot);

            this.owner = null_color;
        }

        public void setOccupied(boolean status){
            this.occupied = status;
        }

        public boolean isOccupied(){
            return this.occupied;
        }

        public void setOwner(Paint color){
            this.owner = color;
        }
    }


    int dimensions = 19; //default is a 19 x 19 board
    double pixel_per_spot = 0; //The amount of pixels between each spot

    Spot[][] board_state;

    Paint null_color;
    Paint your_color;
    Paint their_color;

    boolean your_turn = false;
    boolean board_initialized = false;

    Context context;

    public Board(Context context, boolean goesFirst) {
        super(context);

        this.context = context;

        null_color = new Paint(Color.RED);
        your_turn = goesFirst;
        if(goesFirst) {    //White goes first, hey I didn't design this game
            your_color = new Paint(Color.WHITE);
            their_color = new Paint(Color.BLACK);
        }
        else {
            your_color = new Paint(Color.BLACK);
            their_color = new Paint(Color.WHITE);
        }
    }

//    public Board(Context context, int dim){
//        super(context);
//        this.dimensions = dim;
//        initialize_board_state();
//        printBoardState();
//
//    }

    private void initialize_board_state(){
        this.board_state = new Spot[this.dimensions][this.dimensions];
        for(int i=0; i < this.dimensions; i++){
            for(int j=0; j<this.dimensions; j++){
                board_state[i][j] = new Spot(i,j);
            }
        }

        this.board_initialized = true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        printBoardState();

        switch(event.getAction()){
            case MotionEvent.ACTION_UP:
                if(your_turn) {
                    Spot s = findNearestSpot(event.getX(), event.getY());
                    if (isValidMove(s)) {
                        s.setOwner(your_color);
                        updateBoardState(s, true);
                        updateOpponentsBoardState(s);
                        if(checkWinCondition())
                            winGame();
                        else
                            endTurn();
                    }
                }

        }
        //idk why but gotta return something
        return true;
    }

    public void updateOpponentsBoardState(Spot s) {
        //"piece 2 3" means piece was placed at 2,3
        String command = String.valueOf(GameSession.PIECE_PLAYED) + " " + String.valueOf(s.x) + " " + String.valueOf(s.y);
        byte[] bytes = command.getBytes();
        ((GameSession)this.context).sendReceive.write(bytes);
    }

    public void startTurn(){
        this.your_turn = true;
//        if(your_turn)
//            ((TextView)findViewById(R.id.status)).setText("Your Turn");
        Toast.makeText(context, "Your Turn", Toast.LENGTH_SHORT).show();
    }

    public Spot getSpot(int i, int j){
        return board_state[i][j];
    }

    public void endTurn() {
        this.your_turn = false;
        Toast.makeText(context, "Their Turn", Toast.LENGTH_SHORT).show();

        String command = String.valueOf(GameSession.NEXT_TURN);
        byte[] bytes = command.getBytes();
        ((GameSession)this.context).sendReceive.write(bytes);
    }

    @Override
    protected void onDraw(Canvas canvas){
        super.onDraw(canvas);


        Bitmap go_board_img = BitmapFactory.decodeResource(getResources(), R.drawable.go_board);
        //Resize the image to take up the size of the screen width
        go_board_img = Bitmap.createScaledBitmap(go_board_img, canvas.getWidth(), canvas.getWidth(), false);

        //Draw the board
        canvas.drawBitmap(go_board_img, 0, 0, null);

        this.pixel_per_spot = (go_board_img.getWidth()/ ((double)dimensions + 1));

        if(!board_initialized)
            initialize_board_state();

        //Draw all the pieces on the board
        drawBoardState(canvas, this.board_state);

    }

    public void drawBoardState(Canvas canvas, Spot[][] board_state){
        for(int i=0; i<dimensions ;i++){
            for(int j=0; j<dimensions; j++){
                Spot s = board_state[i][j];
                if(s.occupied == true)
                    if(s.owner.equals(your_color))
                        placePiece(canvas, board_state[i][j], your_color);
                    else
                        placePiece(canvas, board_state[i][j], their_color);
            }
        }

    }

    public void placePiece(Canvas canvas, Spot s, Paint owner){
//        String loc = "(" + s.pixel_x + " , " + s.pixel_y + ")";
//        Log.i("TEST", "Placing piece at " + loc);
        canvas.drawCircle((float)s.pixel_x, (float)s.pixel_y, (float)(this.pixel_per_spot/2.0 * 0.9), owner );
    }

    public boolean isValidMove(Spot s){
        if(s.occupied == false)
            return true;
        else
            return false;
    }

    //Returns the Spot nearest to where you clicked.
    public Spot findNearestSpot(float x, float y){
        Spot nearest_spot = null;
        double dist = 999999;

        for(int i=0; i<dimensions; i++){
            for(int j=0; j<dimensions; j++){
                Spot itr = board_state[i][j];
                double pixel_x = itr.pixel_x;
                double pixel_y = itr.pixel_y;

                double delta_x = pixel_x - x;
                double delta_y = pixel_y - y;
                double itr_dist = Math.sqrt( (delta_x * delta_x) + (delta_y * delta_y) );
                if(itr_dist < dist){
                    nearest_spot = itr;
                    dist = itr_dist;
                }
            }
        }

        return nearest_spot;
    }



    public void printBoardState(){
        Log.i("TEST", "Printing Board State");
        for(int i=0; i<dimensions; i++){
            for(int j=0; j<dimensions; j++){
                if(board_state[i][j].isOccupied()){
                    Log.i("TEST", "PIECE AT " + i + " " + j);
                }
//                String loc = "("+board_state[i][j].pixel_x+","+board_state[i][j].pixel_y+")";
//                Log.i("TEST", i + "," + j + " is located at " + loc);
            }
        }
    }

    public void updateBoardState(Spot s, boolean status){
        updateBoardState(s.x, s.y, status);
    }
    public void updateBoardState(int x, int y, boolean status){
       board_state[x][y].setOccupied(status);
       invalidate();
    }



    public void winGame(){
        //Create alert dialog that you won

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Lost");
        builder.create();

        Toast.makeText(context, "Winner", Toast.LENGTH_SHORT).show();

        //Let the opponent know they lost
        String command = String.valueOf(GameSession.WINNER);
        byte[] bytes = command.getBytes();
        ((GameSession)context).sendReceive.write(bytes);
    }

    //Returns true if there is a 5 in a row somewhere
    public boolean checkWinCondition(){
        for(int i=0; i< dimensions; i++){
            for(int j=0; j<dimensions; j++){
                Spot s = getSpot(i,j);
                if (checkHorizontalWin(s) || checkVerticalWin(s) || checkDiagonalWin(s) ){
                    return true;
                }
            }

        }

        return false;
    }

    //checks if there is a horizontal win given the point as it's center
    private boolean checkHorizontalWin(Spot s){
        int x = s.x;
        int y = s.y;

        if(!(x - 4 < 0)){
            if(getSpot(x,y).owner.equals(your_color) &&
                    getSpot(x - 1, y).owner.equals(your_color) &&
                    getSpot(x - 2, y).owner.equals(your_color) &&
                    getSpot(x - 3, y).owner.equals(your_color) &&
                    getSpot(x - 4, y).owner.equals(your_color)){
                return true;
            }
            return false;
        }
        if(!(x + 4 >= dimensions)){
            if(getSpot(x,y).owner.equals(your_color) &&
                    getSpot(x + 1, y).owner.equals(your_color) &&
                    getSpot(x + 2, y).owner.equals(your_color) &&
                    getSpot(x + 3, y).owner.equals(your_color) &&
                    getSpot(x + 4, y).owner.equals(your_color)){
                return true;
            }

            return false;
        }
        return false;

    }

    private boolean checkVerticalWin(Spot s){
        int x = s.x;
        int y = s.y;

        if(!(y - 4 < 0)){
            if(getSpot(x,y).owner.equals(your_color) &&
                    getSpot(x, y - 1).owner.equals(your_color) &&
                    getSpot(x, y - 2).owner.equals(your_color) &&
                    getSpot(x, y - 3).owner.equals(your_color) &&
                    getSpot(x, y - 4).owner.equals(your_color)){
                return true;
            }
            return false;
        }
        if(!(y + 4 >= dimensions)){
            if(getSpot(x,y).owner.equals(your_color) &&
                    getSpot(x, y + 1).owner.equals(your_color) &&
                    getSpot(x, y + 2).owner.equals(your_color) &&
                    getSpot(x, y + 3).owner.equals(your_color) &&
                    getSpot(x, y + 4).owner.equals(your_color)){
                return true;
            }

            return false;
        }
        return false;

    }

    private boolean checkDiagonalWin(Spot s){
        int x = s.x;
        int y = s.y;

        if( ( (x + 4) < dimensions) && ( (y + 4) < dimensions)) {
            //Down right diagonal
            if (getSpot(x, y).owner.equals(your_color) &&
                    getSpot(x + 1, y + 1).owner.equals(your_color) &&
                    getSpot(x + 2, y + 2).owner.equals(your_color) &&
                    getSpot(x + 3, y + 3).owner.equals(your_color) &&
                    getSpot(x + 4, y + 4).owner.equals(your_color)) {
                return true;
            }
        }
            //Down left diagonal
        if (((x-4) >= 0) && ((y+4) < dimensions)) {
            if (getSpot(x, y).owner.equals(your_color) &&
                    getSpot(x - 1, y + 1).owner.equals(your_color) &&
                    getSpot(x - 2, y + 2).owner.equals(your_color) &&
                    getSpot(x - 3, y + 3).owner.equals(your_color) &&
                    getSpot(x - 4, y + 4).owner.equals(your_color)) {
                return true;
            }
        }

        if (((x-4) >= 0) && ((y-4) >= 0)) {
            //Upleft diagonal
            if (getSpot(x, y).owner.equals(your_color) &&
                    getSpot(x - 1, y - 1).owner.equals(your_color) &&
                    getSpot(x - 2, y - 2).owner.equals(your_color) &&
                    getSpot(x - 3, y - 3).owner.equals(your_color) &&
                    getSpot(x - 4, y - 4).owner.equals(your_color)) {
                return true;
            }
        }

        if (((x+4) < dimensions) && ((y-4) >= 0)) {
            //up right diagonal
            if (getSpot(x, y).owner.equals(your_color) &&
                    getSpot(x + 1, y - 1).owner.equals(your_color) &&
                    getSpot(x + 2, y - 2).owner.equals(your_color) &&
                    getSpot(x + 3, y - 3).owner.equals(your_color) &&
                    getSpot(x + 4, y - 4).owner.equals(your_color)) {
                return true;
            }
        }
        return false;

    }

}
