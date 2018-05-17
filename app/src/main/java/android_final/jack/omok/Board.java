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
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class Board extends View {

    //To abstract the spot you are in within the (when default) 19x19 board
    public class Spot{
        public int x;
        public int y;
        public boolean occupied;

        public double pixel_x; //The pixel that the spot is located at
        public double pixel_y;

        public Spot(int x, int y){
            this.x = x;
            this.y = y;
            this.occupied = false;

            double pixel_of_x_0 = pixel_per_spot/2.0;
            double pixel_of_y_0 = pixel_per_spot/2.0;

            this.pixel_x = pixel_of_x_0 + (x * pixel_per_spot);
            this.pixel_y = pixel_of_y_0 + (y * pixel_per_spot);
            String loc = "(" + pixel_x+","+ pixel_y+")";
            Log.i("TEST", x + "," + y + " is constructed at " + loc);
        }
    }


    int dimensions = 19; //default is a 19 x 19 board
    double pixel_per_spot = 0; //The amount of pixels between each spot

    Spot[][] board_state;

    boolean your_turn = false;
    boolean isWhite = false;

    Context context;

    public Board(Context context, boolean goesFirst) {
        super(context);

        this.context = context;

        your_turn = goesFirst;
        isWhite = goesFirst; //White goes first, hey I didn't design this game

        //Next 3 lines is to get the width of the screen.
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity)context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int screen_width = displayMetrics.widthPixels;
        this.pixel_per_spot = ((double)screen_width/ ((double)dimensions + 1));

        initialize_board_state();
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
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch(event.getAction()){
            case MotionEvent.ACTION_UP:
                if(your_turn) {
                    Log.i("TEST", "Touched " + event.getX() + " " + event.getY());
                    Spot s = findNearestSpot(event.getX(), event.getY());
                    if (isValidMove(s)) {
                        updateBoardState(s);
                        updateOpponentsBoardState(s);
                        endTurn();
                    }
                }


        }
        //idk why but gotta return something
        return true;
    }

    public void updateOpponentsBoardState(Spot s) {
        //"piece 2 3" means piece was placed at 2,3
        String command = String.valueOf(GameSession.PIECE_PLAYED) + String.valueOf(s.x) + " " + String.valueOf(s.y);
        byte[] bytes = command.getBytes();
        ((GameSession)context).sendReceive.write(bytes);
    }

    public void startTurn(){
        this.your_turn = true;

    }

    public void endTurn() {
        this.your_turn = false;

        String command = String.valueOf(GameSession.NEXT_TURN);
        byte[] bytes = command.getBytes();
        ((GameSession)context).sendReceive.write(bytes);
    }

    @Override
    protected void onDraw(Canvas canvas){
        super.onDraw(canvas);

        Bitmap go_board_img = BitmapFactory.decodeResource(getResources(), R.drawable.go_board);
        //Resize the image to take up the size of the screen width
        go_board_img = Bitmap.createScaledBitmap(go_board_img, canvas.getWidth(), canvas.getWidth(), false);

        //Draw the board
        canvas.drawBitmap(go_board_img, 0, 0, null);

        //Draw all the pieces on the board
        drawBoardState(canvas, this.board_state);
    }

    public void drawBoardState(Canvas canvas, Spot[][] board_state){
        for(int i=0; i<dimensions ;i++){
            for(int j=0; j<dimensions; j++){
                Spot s = board_state[i][j];
                if(s.occupied == true)
                    placePiece(canvas, board_state[i][j]);
            }
        }

    }

    public void placePiece(Canvas canvas, Spot s){
        String loc = "(" + s.pixel_x + " , " + s.pixel_y + ")";
        Log.i("TEST", "Placing piece at " + loc);
        Paint paint = new Paint();
        if(isWhite)
            paint.setColor(Color.WHITE);
        else
            paint.setColor(Color.BLACK);
        canvas.drawCircle((float)s.pixel_x, (float)s.pixel_y, (float)(this.pixel_per_spot/2.0 * 0.9), paint );
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
                String loc = "("+board_state[i][j].pixel_x+","+board_state[i][j].pixel_y+")";
                Log.i("TEST", i + "," + j + " is located at " + loc);
            }
        }
    }

    public void updateBoardState(Spot s){
        s.occupied = true;
        invalidate();
    }

    public void updateBoardState(int x, int y, boolean status){
       board_state[x][y].occupied = status;
       invalidate();
    }

    public void win(){

    }

}
