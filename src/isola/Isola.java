package isola;
import com.isola.R;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

public class Isola extends View {
	private Paint paint = new Paint();
	Paint gray = new Paint();
	private Path path = new Path();
	
	private Rect resetButton = new Rect();
	Paint darkGray;
	Paint white;
	int resetButtonWidth = 400;
	int resetButtonHeight = 150;
	int resetButtonTop;
	int resetButtonLeft;
	String buttonText;
	
	boolean inGame;
	int[][] gridGame; // 0 free, -1 blocked, 1-4 players position
	int players = 2; // 2 or 4 players, placed clockwise, starting from top
	int playerTurn; // Clockwise
	
	int cellSize = 120;
	
	int gridSize = cellSize * 9;
	int marginLeft = cellSize;
	int borderGrid = gridSize - marginLeft;
	int marginTop = cellSize;
	
	Paint yellowOutline;
	Rect outline;

	public Isola(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		paint.setAntiAlias(true);
		paint.setStrokeWidth(6f);
		paint.setColor(Color.BLACK);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeJoin(Paint.Join.ROUND);
		
	}
	
	public boolean onOptionsItemSelected(MenuItem item){
		if (item.getItemId() == R.id.reset){
			init();
		}
		Toast.makeText(getContext(), "Yo!", Toast.LENGTH_SHORT).show();

		return true;
	}
	
	public void init(){
		
		// Game
		inGame = true;
		gridGame = new int[7][7];
		playerTurn = 1;
		
		Toast.makeText(getContext(), "Let's start the game!", Toast.LENGTH_SHORT).show();
		
		// gridGame
		gridGame[0][3] = 1; // Player 1 on the top center
		if (players == 2) {
			gridGame[6][3] = 2; // Player 2 on the bottom center
		} else {
			gridGame[3][6] = 2; // Player 2 on the right center
			gridGame[6][3] = 3; // Player 3 on the bottom center
			gridGame[3][0] = 4; // Player 4 on the left center
		}
		gray.setColor(Color.GRAY);
		gray.setStyle(Paint.Style.STROKE);
		
		// resetButton
		resetButtonTop = 1100;
		darkGray = new Paint();
		darkGray.setColor(Color.DKGRAY);
		darkGray.setStyle(Paint.Style.FILL);
		
		white = new Paint(); 
		white.setColor(Color.WHITE); 
		white.setStyle(Paint.Style.FILL); 
		white.setTextSize(80);
		white.setShadowLayer(15.0f, 0.0f, 2.0f, 0xFF000000);
		
		// outline current player
		outline = new Rect();
		yellowOutline = new Paint();
		yellowOutline.setColor(Color.YELLOW);
		yellowOutline.setStyle(Paint.Style.STROKE);
		yellowOutline.setStrokeWidth(8);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		/*
		 * Draw the reset Button
		 */	
		buttonText = inGame?"Reset":"Start !";
		resetButtonLeft = canvas.getWidth()/2-resetButtonWidth/2;
		resetButton.set(resetButtonLeft, resetButtonTop, resetButtonLeft+resetButtonWidth, resetButtonTop+resetButtonHeight);
		canvas.drawRect(resetButton, darkGray);
		canvas.drawText(buttonText, resetButton.exactCenterX()-110, resetButtonTop+100, white);
		
		/*
		 * Draw the static gridGame
		 */

		// Draw Horizontal lines
		path.moveTo(marginLeft, marginTop);
		while (marginTop <= borderGrid) {
			path.lineTo(borderGrid, marginTop);
			marginTop += cellSize;
			path.moveTo(marginLeft, marginTop);
		}
		
		// Draw Vertical lines
		marginTop = cellSize;
		path.moveTo(marginLeft, marginTop);
		while (marginTop <= borderGrid){
			path.lineTo(marginTop, borderGrid);
			marginTop += cellSize;
			path.moveTo(marginTop, marginLeft);
		}
		
		canvas.drawPath(path, gray);
		
		/*
		 * Draw the cells
		 */
		for (int y = 0; y < gridGame.length; y++) {
			for (int x = 0; x < gridGame.length; x++) {
				drawCell(canvas, x, y, gridGame[y][x]);
			}
		}
		invalidate();
		
		/*
		 * Draw the outline of the current Player
		 */
		Tuple pos = getPlayerCell(playerTurn);
		int outlineLeft = getCellPos(pos.x);
		int outlineTop = getCellPos(pos.y);
		outline.set(outlineLeft, outlineTop, outlineLeft + cellSize, outlineTop + cellSize);
		canvas.drawRect(outline, inGame ? yellowOutline : darkGray);
		
		
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		final int action = event.getAction();
		final float x = event.getX();
		final float y = event.getY();
		int cellX, cellY;
		
		if (action == MotionEvent.ACTION_DOWN){
			if (x >= resetButtonLeft && x <= resetButtonLeft+resetButtonWidth &&
				y >= resetButtonTop && y <= resetButtonTop+resetButtonHeight){
				init();
			}
		}
		
		if (inGame) {
			switch (action) {
			case MotionEvent.ACTION_DOWN:
				if (x >= cellSize && x <= borderGrid && y >= cellSize && y <= borderGrid) {
					cellX = Integer.valueOf((int) ((x-cellSize) / cellSize));
					cellY = Integer.valueOf((int) ((y-cellSize) / cellSize));
					touchCell(cellY, cellX);
				}
				return true;
			default:
				return false;
			}
		} else {
			Toast.makeText(getContext(), "The game is over!", Toast.LENGTH_SHORT).show();
			return true;
		}
						
		
		
		
		//return super.onTouchEvent(event);
		
	}
	
	// Get the origin point of the "number"th cell
	public int getCellPos(int number){
		return (number * cellSize) + cellSize;
	}
	
	public void touchCell(int cellY, int cellX){
		if (canGoTo(cellY, cellX)) {
			makeMove(cellY, cellX, playerTurn);
			
			if (!canMakeAMove()) {
				Toast.makeText(getContext(), String.valueOf(playerTurn) + " has lost!", Toast.LENGTH_SHORT).show();
				inGame = false;
				return;
			}
			
			nextPlayer();
			Toast.makeText(getContext(), "Player " + String.valueOf(playerTurn) + " turn", Toast.LENGTH_SHORT).show();
		} else {
			Toast.makeText(getContext(), "You can't go there", Toast.LENGTH_SHORT).show();
		}
	}
	
	public boolean canGoTo(int cellY, int cellX){
		Tuple playerPos = getPlayerCell(playerTurn);
		
		return (cellY >= 0 && cellY <= 6 && cellX >= 0 && cellX <= 6 &&  // If on the board
				Math.abs(cellY-playerPos.y) <= 1 && Math.abs(cellX-playerPos.x) <= 1 &&  // If in range
				gridGame[cellY][cellX] == 0); // If free cell
	}
	
	public boolean canMakeAMove(){		
		Tuple playerPos = getPlayerCell(playerTurn);
		
		// The current player has to be somewhere!
		//if (playerPosX == -1 || playerPosY == -1) throw new IllegalArgumentException();
		
		// Look if the player can make a move
		int tryPosX, tryPosY;
		for (int y = -1; y <= 1; y++) {
			for (int x = -1; x <= 1; x++) {
				tryPosY = playerPos.y + y;
				tryPosX = playerPos.x + x;
				
				if (canGoTo(tryPosY, tryPosX)) 
					return true;
			}
		}
		return false;
	}
	
	public Tuple getPlayerCell(int player){
		int playerPosX = -1; 
		int playerPosY = -1;
		
		// Find current player's position
		for (int y = 0; y < gridGame.length; y++) {
			for (int x = 0; x < gridGame.length; x++) {
				if (gridGame[y][x] == player) {
					playerPosX = x;
					playerPosY = y;
					break;
				}
			}
		}
		return new Tuple(playerPosY, playerPosX);
	}
	
	public void makeMove(int cellY, int cellX, int player){
		Tuple playerPos = getPlayerCell(player);

		try {
			gridGame[cellY][cellX] = player;
			gridGame[playerPos.y][playerPos.x] = -1;
		} catch (Exception e) {
			Toast.makeText(getContext(), "Weird, you can't go there", Toast.LENGTH_SHORT).show();
		}
		
	}
	
	public void nextPlayer(){
		playerTurn = playerTurn%players + 1; // Next player to play
	}
	
	public void swapPlayers(){
		Tuple player1Cell = getPlayerCell(1);
		Tuple player2Cell = getPlayerCell(2);
		makeMove(player2Cell.y, player2Cell.x, 1);
		makeMove(player1Cell.y, player1Cell.x, 2);
	}
	
	public void drawCell(Canvas canvas, int cellX, int cellY, int type){
		Paint color = new Paint();
		
		switch (type) {
		case -1:
			color.setColor(Color.BLACK);
			break;
		case 0:
			color.setColor(Color.WHITE);
			break;
		case 1:
			color.setColor(Color.BLUE);
			break;
		case 2:
			color.setColor(Color.RED);
			break;
		case 3:
			color.setColor(Color.GREEN);
			break;
		case 4:
			color.setColor(Color.YELLOW);
			break;

		default:
			color.setColor(Color.WHITE);
			break;
		}
		
		int cellPosX = getCellPos(cellX);
		int cellPosY = getCellPos(cellY);
		
		Rect cellRect = new Rect();
		cellRect.set(cellPosX+1, cellPosY+1, cellPosX + cellSize, cellPosY + cellSize);
		
		color.setStyle(Paint.Style.FILL);
		canvas.drawRect(cellRect, color);
	}
}
