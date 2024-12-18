package com.example.tictactoeclient;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.util.Objects;

public class FieldSizeSelectionController extends BaseController {

    @FXML
    private ImageView boardImageView;

    @FXML
    private RadioButton size3x3RadioButton;

    @FXML
    private RadioButton size4x4RadioButton;

    @FXML
    private RadioButton size5x5RadioButton;

    @FXML
    private final ToggleGroup sizeToggleGroup = new ToggleGroup(); // Создание ToggleGroup

    private int selectedSize = 0;

    @FXML
    private void initialize() {
        // Привязка RadioButton к ToggleGroup
        size3x3RadioButton.setToggleGroup(sizeToggleGroup);
        size4x4RadioButton.setToggleGroup(sizeToggleGroup);
        size5x5RadioButton.setToggleGroup(sizeToggleGroup);

        // Устанавливаем первый RadioButton активным по умолчанию
        size3x3RadioButton.setSelected(true);

        // Устанавливаем параметры для начального размера
        updateBoardImage(3);
        selectedSize = 3;
    }

    @FXML
    private void onSizeSelected(ActionEvent event) {
        RadioButton selectedRadioButton = (RadioButton) sizeToggleGroup.getSelectedToggle();
        if (selectedRadioButton != null) {
            String sizeText = selectedRadioButton.getText();
            switch (sizeText) {
                case "3x3":
                    selectedSize = 3;
                    updateBoardImage(3);
                    break;
                case "4x4":
                    selectedSize = 4;
                    updateBoardImage(4);
                    break;
                case "5x5":
                    selectedSize = 5;
                    updateBoardImage(5);
                    break;
            }
        }
    }

    @FXML
    private void onSave(ActionEvent event) {
        if (selectedSize == 0) {
            System.out.println("Выберите размер поля прежде чем начать");
            return;
        }
        closeWindow((Stage) ((Node) event.getSource()).getScene().getWindow());
    }

    @FXML
    private void onCancel(ActionEvent event) {
        selectedSize = 0; // Сбрасываем размер
        closeWindow((Stage) ((Node) event.getSource()).getScene().getWindow());
    }

    public int getSelectedSize() {
        return selectedSize;
    }

    private void updateBoardImage(int size) {
        String imagePath = String.format("/images/board_%dx%d.png", size, size);
        Image boardImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream(imagePath)));
        boardImageView.setImage(boardImage);
    }
}
