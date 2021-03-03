package priv.matrix.game.gobang;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import priv.matrix.game.gobang.controller.ClientController;

public class Client extends Application{

	@Override
	public void start(Stage primaryStage) throws Exception {
		
		primaryStage.setTitle("Gobang Client");
		FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/Client.fxml"));
		Parent root = fxmlLoader.load();
		Scene scene = new Scene(root);
        scene.getStylesheets().add("/css/style.css");
		primaryStage.setScene(scene);
		primaryStage.show();
    	primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {

			@Override
			public void handle(WindowEvent arg0) {
				ClientController cc = fxmlLoader.getController();
				cc.sendData("quit|I shall leave you alone now.");
				System.exit(0);
			}
    	});
		
	}
	
	public static void main(String[] args)
	{
		launch(args);
	}

}
