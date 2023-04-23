package com.example.mchatserver;

import com.example.mchatserver.classes.Server;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class ServerController implements Initializable {
    private Server server;
    private Stage stage;

    @FXML
    private Button btn_stop;
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        btn_stop.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Confirmation");
                alert.setHeaderText("Voulez-vous vraiment arrêter le serveur?");
                Optional<ButtonType> result = alert.showAndWait();
                if (result.get() == ButtonType.OK) {
                    try {
                        server.stopServer();
                    } catch (IOException e) {
                        System.err.println("Erreur lors de la fermeture du serveur: " + e.getMessage());
                    }
                    try {
                        // afficher start.fxml
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("start.fxml"));
                        Parent root = loader.load();
                        Stage stage = (Stage) btn_stop.getScene().getWindow();
                        Scene scene = new Scene(root);
                        stage.setScene(scene);
                        stage.show();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        if(stage != null) {
            stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
                @Override
                public void handle(WindowEvent windowEvent) {
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Confirmation");
                    alert.setHeaderText("Voulez-vous vraiment fermer l'application?");
                    alert.setContentText("Cliquez sur OK pour fermer l'application et arrêter le serveur.");
                    Optional<ButtonType> result = alert.showAndWait();
                    if (result.isPresent() && result.get() == ButtonType.OK) {
                        try {
                            server.stopServer();
                        } catch (IOException e) {
                            System.err.println("Erreur lors de la fermeture du serveur: " + e.getMessage());
                        }
                        stage.close();
                    } else {
                        windowEvent.consume();
                    }
                }
            });
        }
    }

    public void setServer(Server server) {
         this.server = server;
    }


}
