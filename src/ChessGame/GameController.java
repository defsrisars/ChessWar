package ChessGame;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Observable;

import ObserverData.MyMove;
import ObserverData.DragData;


/**
 * GameController，負責玩家在遊戲時，對於移動棋子（View）與modal資料的控制
 */

public class GameController extends Observable implements MouseMotionListener, MouseListener{
	
	/* 基本參數 */
	private GameModel model ;
	private ChessMap chessMap ;
	/* 記錄滑鼠移動座標 */
	private int mouse_x; // save mouse point x on button
	private int mouse_y; // save mouse point y on button
	
	public GameController(GameModel model){
		this.model = model;
		this.chessMap = model.getChessMap() ;
		
		Chess[] chess = this.model.getAllChess();
		for(int i=0;i<32;i++){
			this.addObserver(chess[i]);
			chess[i].addMouseMotionListener(this);
			chess[i].addMouseListener(this);
		}
	}
	
	/**
	 * 玩家移動棋子，若為當前玩家拖移自己的棋子，則顯示移動效果
	 * 並以Observer通知Chess做view update
	 * @param e 移動事件MouseEvent
	 */
	@Override
	public void mouseDragged(MouseEvent e) {
		Chess c = (Chess) e.getComponent();
		if (c.getChessSide() == model.getTurn() && !model.isGameOver() && !model.getPauseStatus() && model.isGameStart() ) {
			int x = c.getLocation().x + (e.getX() - mouse_x) ;
			int y = c.getLocation().y + (e.getY() - mouse_y) ;
			setChanged();
			notifyObservers(new DragData(c,x,y));
		}
	}
	
	/**
	 * 紀錄滑鼠移動座標
	 * @param e 移動事件MouseEvent
	 */
	@Override
	public void mouseMoved(MouseEvent e) {
		mouse_x = e.getX();
		mouse_y = e.getY();
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		
	}

	@Override
	public void mouseEntered(MouseEvent e) {

	}

	@Override
	public void mouseExited(MouseEvent e) {

	}

	@Override
	public void mousePressed(MouseEvent e) {

	}
	
	/**
	 * 玩家拖移棋子，釋放後判斷移動是否符合規則
	 * @param e 移動事件MouseEvent
	 */
	@Override
	public void mouseReleased(MouseEvent e) {
		/* 如果滑鼠左鍵釋放，取得棋子 */
		Chess c = (Chess) e.getComponent();
		/* 如果選擇棋子的顏色為當前玩家 && 遊戲未結束 && 不是暫停狀態 && 遊戲已經開始 */
		if ( (c.getChessSide() == model.getTurn()) && (!model.isGameOver()) && (!model.getPauseStatus()) && (model.isGameStart()) ) {
			
			if (e.getButton() == MouseEvent.BUTTON1) {
				
				/* 取得移動後最接近座標位置 */
				Point p = this.chessMap.findNearChessLoc((Chess) e.getComponent());
				ChessRule chessRule = this.model.getChessRule();
				
				// 確認是否顛倒，若顛倒取得顛倒後的p !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
				if(model.isInverse()){
					p = new Point(p.x,9-p.y);
				}
				
				/* 若該座標在棋盤內，且符合該棋子移動規則 */
				if ((p != null) && chessRule.isCouldMove(c, p)) {
					
//					選擇棋子、移動資訊
//					System.out.println("hasChess: " + hasChess(p));
//					System.out.println("isCouldMove: " + isCouldMove(c, p));
//					System.out.println("Obstacle: " + calObstacle(c, p));
					
					/* 合法移動，移除上一手移動軌跡 */
					model.removePreviousMoveBorder();
					
					/* 若該點有棋子 */
					if (chessRule.hasChess(p)) {
						/* 若該點棋子顏色與點選棋子不同，則吃棋 */
						if(chessRule.getChess(p).getChessSide() != c.getChessSide()){
							/* 進行吃棋動作 */
							int beEatedChessIndex = chessRule.getChess(p).getChessIndex();
							chessRule.eatChess(p);
							/* 記錄點選棋子原本的位置，並將此棋步儲存紀錄 */
							Point oldPos = c.getChessLoc();
							model.saveMove(c, p, "eat",beEatedChessIndex);
							/* 將點選的棋子移動到新的位置 */
							model.changeChessViewPosition(c,p);
							model.setChanged();
							model.notifyObservers(new MyMove(c,new Point(p)));
							/* 新增移動軌跡 */
							model.addPreviousMoveBorder(oldPos);
							model.addThisMoveBorder(c);
							/* 換手 */
							model.changeTurn();	
							
							// 如果對方是AI，則通知AI 
							if(model.getTurn() == model.getPlayer1Color() && model.getPlayer1Type() != 1 || model.getTurn() == model.getPlayer2Color() && this.model.getPlayer2Type() != 1){
								model.notifyAI(c.getChessIndex(), new Point(oldPos.x,oldPos.y), new Point(p.x,p.y));
							}
							
						/* 若該點棋子顏色與點選棋子相同，則將點選棋子歸位 */
						}else{
							c.setLocation(this.chessMap.getChessLoc(c.getChessLoc()));
						}
					/* 若該點無棋子，則直接移動 */
					}else{
						/* 記錄點選棋子原本的位置，並將此棋步儲存紀錄 */
						Point oldPos = c.getChessLoc();
						model.saveMove(c, p, "move");
						/* 將點選的棋子移動到新的位置 */
						model.changeChessViewPosition(c,p);
						model.setChanged();
						model.notifyObservers(new MyMove(c,new Point(p)));
						/* 新增移動軌跡 */
						model.addPreviousMoveBorder(oldPos);
						model.addThisMoveBorder(c);
						/* 換手 */
						model.changeTurn();
						
						// 如果對方是AI，則通知AI
						if(model.getTurn() == model.getPlayer1Color() && this.model.getPlayer1Type() != 1 || model.getTurn() == model.getPlayer2Color() && this.model.getPlayer2Type() != 1){
							model.notifyAI(c.getChessIndex(), new Point(oldPos.x,oldPos.y), new Point(p.x,p.y));
						}
					}
					
					/* 移動結束，判斷遊戲是否結束 */
					if (model.isGameOver()) {
						model.gameOver(); // System.out.println("GameOver!");
					}
				
				/* 若該座標不在棋盤內，或不符合該棋子移動規則 */
				} else {
					/* 直接歸位 */
//					c.setLocation(this.chessMap.getChessLoc(c.getChessLoc()));
					model.changeChessViewPosition(c,c.getChessLoc()); //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
				}

			}
		}
	}
	
}
