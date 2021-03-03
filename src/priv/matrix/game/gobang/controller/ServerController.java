package priv.matrix.game.gobang.controller;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class ServerController implements Initializable{

	@FXML
	TextField messageField, portField;
	
	@FXML
	TextArea commuArea;
	
	@FXML
	Canvas chessCanvas;
	
	@FXML
	Button listenButton;

	private GraphicsContext gc;
	private double cell = 40;
	private double align = 35;
	private double width = 560;
	private double height = 560;
	
	ServerSocket server = null;
	Socket socket = null;
	BufferedReader instr = null;
	PrintWriter os = null;
	public static String[] ss = new String[10];

	// 保存棋子的坐标
	int x = 0;
	int y = 0;
	// 保存之前下过的全部棋子的坐标
	// 其中数据内容 0： 表示这个点并没有棋子，1：表示这个点是黑子，2：表示是白子
	int[][] allChess = new int[16][16];//多于棋盘一行一列
	boolean isBlack = true;// 自己是黑棋
	// 标识当前游戏是否可以继续
	boolean canPlay = true;
	// 保存显示的提示信息
	String message ="";// "自己是黑方先行";

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		initCanvas();
		//commuArea.setBackground(Color.GREEN);
		
	}
	

	public void initCanvas()
	{
		this.gc = chessCanvas.getGraphicsContext2D();	//获得GraphicsContext2D进行渲染


		//填充棋盘颜色
		gc.setFill(Color.BURLYWOOD);
		gc.fillRect(align - 15, align - 15, width + 30, height + 30);

		for(int y = 1; y < 15; y++) {
			gc.setLineWidth(2);		//解决重绘出现重叠问题
			//画横线
			gc.strokeLine(align, y * cell + align, width + align, y * cell + align);
			//画竖线
			gc.strokeLine(y * cell + align, align, y * cell + align, height + align);

		}
		gc.stroke();

		/**
		 * 天元(7,7),四个星位(3,3),(3,11),(11,3),(11,11)
		 **/
		for(int i = 3; i <= 14; i += 4)
			for(int j = 3; j <= 14;) {
				gc.setFill(Color.BLACK);
				//画天元
				if(i == 7) {
					j = 7;
					gc.strokeOval(i * cell + align - 4, j * cell + align - 4, 8, 8);
					gc.fillOval(i * cell + align - 4, j * cell + align - 4, 8, 8);
					break;
				}
				//画星位
				else {
					gc.strokeOval(i * cell + align - 4, j * cell + align - 4, 8, 8);
					gc.fillOval(i * cell + align - 4, j * cell + align - 4, 8, 8);
					j += 8;
				}
			}

		//边框加粗
		gc.setLineWidth(3.0f);
		gc.strokeRect(align, align, width, height);

		
	}

	private void listenClient(final int port) {// 侦听
		try {
			if (listenButton.getText().trim().equals("侦听")) {
				new Thread(new Runnable() {
					public void run() {
						// TODO Auto-generated method stub
						try {
							server = new ServerSocket(port);
							Platform.runLater(() -> {listenButton.setText("正在侦听");});
							//listenButton.setText("正在侦听...");
							socket = server.accept();
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}// 等待，一直到客户端连接才望下执行
						// this.setTitle("你是黑方");
						sendData("已经成功连接。。。");
						Platform.runLater(() -> {listenButton.setText("正在聊天");});
						System.out.println("服务端：连接成功！");
						commuArea.appendText("客户端已经连接到服务器\n");
						message = "自己是黑方先行";
						//repaint();   //---------------------------------------------------------------
						MyThread t = new MyThread();
						t.start();
					}
				}).start();

			}
		} catch (Exception ex) {
		}
	}
	// 内部线程类
	class MyThread extends Thread {// 该线程负责接受数据
		public void run() {
			try {
				os = new PrintWriter(socket.getOutputStream());
				instr = new BufferedReader(new InputStreamReader(socket
						.getInputStream()));
				while (true) {
					sleep(100);
					//instr = new BufferedReader(new InputStreamReader(socket.getInputStream()));
					if (instr.ready()) { // 检查是否有数据
						String cmd = instr.readLine();
						System.out.println("cmd:\t" + cmd);
						Platform.runLater(()-> commuArea.appendText("客户端:  " + cmd + "\n"));
						// 在每个|字符处进行分解。
						ss = cmd.split("\\|");
						if (cmd.startsWith("move")) {
							message = "轮到自己下棋子";
							int x = Integer.parseInt(ss[1]);
							int y = Integer.parseInt(ss[2]);
							allChess[x][y] = 2;
							gc.setFill(Color.WHITE);	//	白方
							gc.strokeOval(align + x*cell - cell/2, align + y*cell - cell/2, cell - 2, cell - 2);
							gc.fillOval(align + x*cell - cell/2, align + y*cell - cell/2, cell - 2, cell - 2);
							//repaint();   //---------------------------------------------------------------
							canPlay = true;
						}
						if (cmd.startsWith("undo")) {
							int x = Integer.parseInt(ss[1]);
							int y = Integer.parseInt(ss[2]);
							allChess[x][y] = 0;
							Platform.runLater(() -> repaint());
							//repaint();   //---------------------------------------------------------------
							canPlay = false;
							chessCanvas.setDisable(false);
							Platform.runLater(() -> 
							new Alert(AlertType.INFORMATION, "对方撤销上步祺！", ButtonType.OK).showAndWait());
						}
						if (cmd.startsWith("over")) {
							Platform.runLater(() -> 
							new Alert(AlertType.INFORMATION, "游戏结束， 对方获胜！", ButtonType.OK).showAndWait());
							chessCanvas.setDisable(true);
							canPlay = false;
						}
						if (cmd.startsWith("quit")) {
							Platform.runLater(() -> 
							new Alert(AlertType.INFORMATION, "对方离开了, 游戏结束 ！！", ButtonType.OK).showAndWait());
							chessCanvas.setDisable(true);
							canPlay = false;
						}
						if (cmd.startsWith("OKtoNew")) {
							Platform.runLater(() -> 
							new Alert(AlertType.INFORMATION, "对方同意了您的请求！", ButtonType.OK).showAndWait());
							for(int i = 0; i < 15; i++)
								for(int j = 0; j < 15; j++)
								{
									allChess[i][j] = 0;
								}
							repaint();
							chessCanvas.setDisable(false);
							canPlay = false;
						}
						if (cmd.startsWith("NotoNew")) {
							Platform.runLater(() -> 
							new Alert(AlertType.INFORMATION, "对方拒绝了您的请求！", ButtonType.OK).showAndWait());
						}
						if (cmd.startsWith("restart")) {
							Platform.runLater(() -> {
								Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
								alert.setTitle("确认对话框");
								alert.setHeaderText("对方请求重开一局!");
								alert.setContentText("是否同意？");
								Optional<ButtonType> result = alert.showAndWait();
								//点击了确认按钮，执行删除操作，同时移除一行模型数据
								if (result.get() == ButtonType.OK) {
									for(int i = 0; i < 15; i++)
										for(int j = 0; j < 15; j++)
										{
											allChess[i][j] = 0;
										}
									repaint();
									sendData("OKtoNew|OK, starting anew is fine!");
								}
								else
								{
									sendData("NotoNew|Let me think about it!");
								}
								});
							chessCanvas.setDisable(false);
							canPlay = true;
						}
					}
				}
			} catch (Exception ex) {
				System.out.print("error:  " + ex);
			}
		}
	}

	public void sendData(String s) {// 发送数据
		try {
			os = new PrintWriter(socket.getOutputStream());
			os.println(s);
			os.flush();
			if (!s.equals("已经成功连接。。。"))
				Platform.runLater(() -> commuArea.appendText("Server: " + s + "\n"));
		} catch (Exception ex) {
		}
	}

	private boolean checkWin() {
		boolean flag = false;
		// 保存共有相同颜色多少棋子相连
		int count = 1;
		// 判断横向是否有5个棋子相连，特点 纵坐标 是相同， 即allChess[x][y]中y值是相同
		int color = allChess[x][y];

		// 通过循环来做棋子相连的判断
		// 横向的判断
		int i = 1;
		System.out.println("x, y:\t" + x + "," + y);
		while (color == allChess[x + i][y + 0]) {
			System.out.println("x, y:\t" + (x + i) + "," + y);
			count++;
			i++;
		}
		i = 1;
		while (color == allChess[x - i][y - 0]) {
			count++;
			i++;
		}
		if (count >= 5) {
			flag = true;
		}

		// 纵向的判断
		int i2 = 1;
		int count2 = 1;
		while (color == allChess[x + 0][y + i2]) {
			count2++;
			i2++;
		}
		i2 = 1;
		while (color == allChess[x - 0][y - i2]) {
			count2++;
			i2++;
		}
		if (count2 >= 5) {
			flag = true;
		}
		// 斜方向的判断（右上 + 左下）
		int i3 = 1;
		int count3 = 1;
		while (color == allChess[x + i3][y - i3]) {
			count3++;
			i3++;
		}
		i3 = 1;
		while (color == allChess[x - i3][y + i3]) {
			count3++;
			i3++;
		}
		if (count3 >= 5) {
			flag = true;
		}
		// 斜方向的判断（右下 +左上）
		int i4 = 1;
		int count4 = 1;
		while (color == allChess[x + i4][y + i4]) {
			count4++;
			i4++;
		}
		i4 = 1;
		while (color == allChess[x - i4][y - i4]) {
			count4++;
			i4++;
		}
		if (count4 >= 5) {
			flag = true;
		}
		return flag;
	}
	
	public void repaint()
	{
		gc.clearRect(0, 0, 650, 650);
		initCanvas();
		
		for (int i = 0; i < 15; i++) {
			for (int j = 0; j < 15; j++) {
				if (allChess[i][j] == 1) {
					// 黑子
					gc.setFill(Color.BLACK);
					gc.strokeOval(align + i*cell - cell/2, align + j*cell - cell/2, cell - 2, cell - 2);
					gc.fillOval(align + i*cell - cell/2, align + j*cell - cell/2, cell - 2, cell - 2);
				}
				if (allChess[i][j] == 2) {
					// 白子
//					int tempX = i * 20 + 10;
//					int tempY = j * 20 + 70;
//					g2.setColor(Color.WHITE);
//					g2.fillOval(tempX - 7, tempY - 7, 14, 14);
//					g2.setColor(Color.BLACK);
//					g2.drawOval(tempX - 7, tempY - 7, 14, 14);
					gc.setFill(Color.WHITE);	//	白方
					gc.strokeOval(align + i*cell - cell/2, align + j*cell - cell/2, cell - 2, cell - 2);
					gc.fillOval(align + i*cell - cell/2, align + j*cell - cell/2, cell - 2, cell - 2);

				}
			}
		}
	
		System.out.println("repaint completed!");
		
	}
	public void mouseClicked(MouseEvent event)
	{
		System.out.println("input:\t" + event.getX() + ", " + event.getY());
		//定位棋子落点
		x = (int)((event.getX() - align + cell/2)/cell);
		y = (int)((event.getY() - align + cell/2)/cell);

		System.out.println("Black side: mouse(x, y):\t" + x + ", " + y);
		if(canPlay == true) {
			if(event.getX() >= 5 && event.getY() >= 5 && x < 15 && y < 15) { //没越界

				if (allChess[x][y] == 0) {
					// 判断当前要下的是什么颜色的棋子
					if (isBlack == true) {
						allChess[x][y] = 1;
						message = "轮到白方";
						sendData("move|" + String.valueOf(x) + "|"
								+ String.valueOf(y));
						gc.setFill(Color.BLACK);	//	白方
						gc.strokeOval(align + x*cell - cell/2, align + y*cell - cell/2, cell - 2, cell - 2);
						gc.fillOval(align + x*cell - cell/2, align + y*cell - cell/2, cell - 2, cell - 2);
						canPlay = false;								
						// 黑子
					} 
					else {
						allChess[x][y] = 2;
						message = "轮到黑方";
						sendData("move|" + String.valueOf(x) + "|"
								+ String.valueOf(y));
						gc.setFill(Color.WHITE);	//	白方
						gc.strokeOval(align + x*cell - cell/2, align + y*cell - cell/2, cell - 2, cell - 2);
						gc.fillOval(align + x*cell - cell/2, align + y*cell - cell/2, cell - 2, cell - 2);
						canPlay = false;
						// 白子
					}
					// 判断这个棋子是否和其他的棋子连成5连，即判断游戏是否结束
					boolean winFlag = this.checkWin();
					if (winFlag == true) {
						message = "游戏结束,"
								+ (allChess[x][y] == 1 ? "黑方" : "白方")
								+ "胜";
						sendData("over|" + message);
						Platform.runLater(() -> 
						new Alert(AlertType.INFORMATION, message, ButtonType.OK).showAndWait());
						System.out.println(message);
						canPlay = false;
					}
				} 
				else {
					message = "当前位置已经有棋子，请重新落子！";
					System.out.println(message);
				}
			}
		}
		else {
			message = "该对方走棋！";
			Platform.runLater(() -> 
			new Alert(AlertType.INFORMATION, message, ButtonType.OK).showAndWait());
		}
	}

	public void regret()
	{
		if (canPlay != true) {// 该对方走棋
			allChess[x][y] = 0;
			Platform.runLater(() -> repaint());
			//repaint();
			canPlay = true;
			String s = "undo|" + x + "|" + y;
			sendData(s);
			System.out.print("发送悔棋信息");
		} else// 对方已走棋
		{
			message = "对方已走棋，不能悔棋了";
			Platform.runLater(() -> 
			new Alert(AlertType.INFORMATION, message, ButtonType.OK).showAndWait());
			System.out.print("对方已走棋，不能悔棋了");
		}

	}
	
	public void restart()
	{
		sendData("restart|Can we start the game anew?");
	}

	public void sent()
	{
		String s = ":) " + this.messageField.getText().trim();
		sendData(s);
	}

	public void listen()
	{
			int port = Integer.parseInt(portField.getText().trim());
			listenClient(port);
			System.out.print("侦听...");
	}

}
