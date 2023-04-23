package com.example.mchatserver;

import com.example.mchatserver.classes.Server;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class StartController implements Initializable {


    @FXML
    private Button btn_start;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        btn_start.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                int port = 7502;
                Server server = new Server(port);

                //demarrer le serveur dans un thread
                Executor executor = Executors.newSingleThreadExecutor();
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            server.startServer();
                        } catch (IOException e) {
                            System.err.println("Erreur lors du démarrage du serveur: " + e.getMessage());
                        }
                    }
                });

                try {
                    // afficher server.fxml
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("server.fxml"));
                    Parent root = loader.load();
                    ServerController controller = loader.getController();
                    controller.setServer(server);
                    Stage stage = (Stage) btn_start.getScene().getWindow();
                    Scene scene = new Scene(root);
                    stage.setScene(scene);
                    //controller.setStage(stage);
                    stage.show();

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });
    }
}
