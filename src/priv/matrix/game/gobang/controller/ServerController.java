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

	// �������ӵ�����
	int x = 0;
	int y = 0;
	// ����֮ǰ�¹���ȫ�����ӵ�����
	// ������������ 0�� ��ʾ����㲢û�����ӣ�1����ʾ������Ǻ��ӣ�2����ʾ�ǰ���
	int[][] allChess = new int[16][16];//��������һ��һ��
	boolean isBlack = true;// �Լ��Ǻ���
	// ��ʶ��ǰ��Ϸ�Ƿ���Լ���
	boolean canPlay = true;
	// ������ʾ����ʾ��Ϣ
	String message ="";// "�Լ��Ǻڷ�����";

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		initCanvas();
		//commuArea.setBackground(Color.GREEN);
		
	}
	

	public void initCanvas()
	{
		this.gc = chessCanvas.getGraphicsContext2D();	//���GraphicsContext2D������Ⱦ


		//���������ɫ
		gc.setFill(Color.BURLYWOOD);
		gc.fillRect(align - 15, align - 15, width + 30, height + 30);

		for(int y = 1; y < 15; y++) {
			gc.setLineWidth(2);		//����ػ�����ص�����
			//������
			gc.strokeLine(align, y * cell + align, width + align, y * cell + align);
			//������
			gc.strokeLine(y * cell + align, align, y * cell + align, height + align);

		}
		gc.stroke();

		/**
		 * ��Ԫ(7,7),�ĸ���λ(3,3),(3,11),(11,3),(11,11)
		 **/
		for(int i = 3; i <= 14; i += 4)
			for(int j = 3; j <= 14;) {
				gc.setFill(Color.BLACK);
				//����Ԫ
				if(i == 7) {
					j = 7;
					gc.strokeOval(i * cell + align - 4, j * cell + align - 4, 8, 8);
					gc.fillOval(i * cell + align - 4, j * cell + align - 4, 8, 8);
					break;
				}
				//����λ
				else {
					gc.strokeOval(i * cell + align - 4, j * cell + align - 4, 8, 8);
					gc.fillOval(i * cell + align - 4, j * cell + align - 4, 8, 8);
					j += 8;
				}
			}

		//�߿�Ӵ�
		gc.setLineWidth(3.0f);
		gc.strokeRect(align, align, width, height);

		
	}

	private void listenClient(final int port) {// ����
		try {
			if (listenButton.getText().trim().equals("����")) {
				new Thread(new Runnable() {
					public void run() {
						// TODO Auto-generated method stub
						try {
							server = new ServerSocket(port);
							Platform.runLater(() -> {listenButton.setText("��������");});
							//listenButton.setText("��������...");
							socket = server.accept();
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}// �ȴ���һֱ���ͻ������Ӳ�����ִ��
						// this.setTitle("���Ǻڷ�");
						sendData("�Ѿ��ɹ����ӡ�����");
						Platform.runLater(() -> {listenButton.setText("��������");});
						System.out.println("����ˣ����ӳɹ���");
						commuArea.appendText("�ͻ����Ѿ����ӵ�������\n");
						message = "�Լ��Ǻڷ�����";
						//repaint();   //---------------------------------------------------------------
						MyThread t = new MyThread();
						t.start();
					}
				}).start();

			}
		} catch (Exception ex) {
		}
	}
	// �ڲ��߳���
	class MyThread extends Thread {// ���̸߳����������
		public void run() {
			try {
				os = new PrintWriter(socket.getOutputStream());
				instr = new BufferedReader(new InputStreamReader(socket
						.getInputStream()));
				while (true) {
					sleep(100);
					//instr = new BufferedReader(new InputStreamReader(socket.getInputStream()));
					if (instr.ready()) { // ����Ƿ�������
						String cmd = instr.readLine();
						System.out.println("cmd:\t" + cmd);
						Platform.runLater(()-> commuArea.appendText("�ͻ���:  " + cmd + "\n"));
						// ��ÿ��|�ַ������зֽ⡣
						ss = cmd.split("\\|");
						if (cmd.startsWith("move")) {
							message = "�ֵ��Լ�������";
							int x = Integer.parseInt(ss[1]);
							int y = Integer.parseInt(ss[2]);
							allChess[x][y] = 2;
							gc.setFill(Color.WHITE);	//	�׷�
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
							new Alert(AlertType.INFORMATION, "�Է������ϲ�����", ButtonType.OK).showAndWait());
						}
						if (cmd.startsWith("over")) {
							Platform.runLater(() -> 
							new Alert(AlertType.INFORMATION, "��Ϸ������ �Է���ʤ��", ButtonType.OK).showAndWait());
							chessCanvas.setDisable(true);
							canPlay = false;
						}
						if (cmd.startsWith("quit")) {
							Platform.runLater(() -> 
							new Alert(AlertType.INFORMATION, "�Է��뿪��, ��Ϸ���� ����", ButtonType.OK).showAndWait());
							chessCanvas.setDisable(true);
							canPlay = false;
						}
						if (cmd.startsWith("OKtoNew")) {
							Platform.runLater(() -> 
							new Alert(AlertType.INFORMATION, "�Է�ͬ������������", ButtonType.OK).showAndWait());
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
							new Alert(AlertType.INFORMATION, "�Է��ܾ�����������", ButtonType.OK).showAndWait());
						}
						if (cmd.startsWith("restart")) {
							Platform.runLater(() -> {
								Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
								alert.setTitle("ȷ�϶Ի���");
								alert.setHeaderText("�Է������ؿ�һ��!");
								alert.setContentText("�Ƿ�ͬ�⣿");
								Optional<ButtonType> result = alert.showAndWait();
								//�����ȷ�ϰ�ť��ִ��ɾ��������ͬʱ�Ƴ�һ��ģ������
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

	public void sendData(String s) {// ��������
		try {
			os = new PrintWriter(socket.getOutputStream());
			os.println(s);
			os.flush();
			if (!s.equals("�Ѿ��ɹ����ӡ�����"))
				Platform.runLater(() -> commuArea.appendText("Server: " + s + "\n"));
		} catch (Exception ex) {
		}
	}

	private boolean checkWin() {
		boolean flag = false;
		// ���湲����ͬ��ɫ������������
		int count = 1;
		// �жϺ����Ƿ���5�������������ص� ������ ����ͬ�� ��allChess[x][y]��yֵ����ͬ
		int color = allChess[x][y];

		// ͨ��ѭ�����������������ж�
		// ������ж�
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

		// ������ж�
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
		// б������жϣ����� + ���£�
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
		// б������жϣ����� +���ϣ�
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
					// ����
					gc.setFill(Color.BLACK);
					gc.strokeOval(align + i*cell - cell/2, align + j*cell - cell/2, cell - 2, cell - 2);
					gc.fillOval(align + i*cell - cell/2, align + j*cell - cell/2, cell - 2, cell - 2);
				}
				if (allChess[i][j] == 2) {
					// ����
//					int tempX = i * 20 + 10;
//					int tempY = j * 20 + 70;
//					g2.setColor(Color.WHITE);
//					g2.fillOval(tempX - 7, tempY - 7, 14, 14);
//					g2.setColor(Color.BLACK);
//					g2.drawOval(tempX - 7, tempY - 7, 14, 14);
					gc.setFill(Color.WHITE);	//	�׷�
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
		//��λ�������
		x = (int)((event.getX() - align + cell/2)/cell);
		y = (int)((event.getY() - align + cell/2)/cell);

		System.out.println("Black side: mouse(x, y):\t" + x + ", " + y);
		if(canPlay == true) {
			if(event.getX() >= 5 && event.getY() >= 5 && x < 15 && y < 15) { //ûԽ��

				if (allChess[x][y] == 0) {
					// �жϵ�ǰҪ�µ���ʲô��ɫ������
					if (isBlack == true) {
						allChess[x][y] = 1;
						message = "�ֵ��׷�";
						sendData("move|" + String.valueOf(x) + "|"
								+ String.valueOf(y));
						gc.setFill(Color.BLACK);	//	�׷�
						gc.strokeOval(align + x*cell - cell/2, align + y*cell - cell/2, cell - 2, cell - 2);
						gc.fillOval(align + x*cell - cell/2, align + y*cell - cell/2, cell - 2, cell - 2);
						canPlay = false;								
						// ����
					} 
					else {
						allChess[x][y] = 2;
						message = "�ֵ��ڷ�";
						sendData("move|" + String.valueOf(x) + "|"
								+ String.valueOf(y));
						gc.setFill(Color.WHITE);	//	�׷�
						gc.strokeOval(align + x*cell - cell/2, align + y*cell - cell/2, cell - 2, cell - 2);
						gc.fillOval(align + x*cell - cell/2, align + y*cell - cell/2, cell - 2, cell - 2);
						canPlay = false;
						// ����
					}
					// �ж���������Ƿ����������������5�������ж���Ϸ�Ƿ����
					boolean winFlag = this.checkWin();
					if (winFlag == true) {
						message = "��Ϸ����,"
								+ (allChess[x][y] == 1 ? "�ڷ�" : "�׷�")
								+ "ʤ";
						sendData("over|" + message);
						Platform.runLater(() -> 
						new Alert(AlertType.INFORMATION, message, ButtonType.OK).showAndWait());
						System.out.println(message);
						canPlay = false;
					}
				} 
				else {
					message = "��ǰλ���Ѿ������ӣ����������ӣ�";
					System.out.println(message);
				}
			}
		}
		else {
			message = "�öԷ����壡";
			Platform.runLater(() -> 
			new Alert(AlertType.INFORMATION, message, ButtonType.OK).showAndWait());
		}
	}

	public void regret()
	{
		if (canPlay != true) {// �öԷ�����
			allChess[x][y] = 0;
			Platform.runLater(() -> repaint());
			//repaint();
			canPlay = true;
			String s = "undo|" + x + "|" + y;
			sendData(s);
			System.out.print("���ͻ�����Ϣ");
		} else// �Է�������
		{
			message = "�Է������壬���ܻ�����";
			Platform.runLater(() -> 
			new Alert(AlertType.INFORMATION, message, ButtonType.OK).showAndWait());
			System.out.print("�Է������壬���ܻ�����");
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
			System.out.print("����...");
	}

}
