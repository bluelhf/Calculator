package io.github.bluelhf.calculator;

import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.*;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.LockSupport;

public class Controller {
    private static final List<String> WRITABLE = Arrays.asList("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", ".", "/", "^", "*", "+", "-", "(", ")");
    @FXML Label input;
    @FXML Button answerButton;

    private boolean dirty = false;

    private String previousAnswer = "";


    public void init(Stage stage) {
        KeyCodeCombination combo = new KeyCodeCombination(KeyCode.V, KeyCombination.CONTROL_DOWN);
        stage.requestFocus();
        stage.getScene().setOnKeyTyped(keyEvent -> {

            if (keyEvent.getCharacter().equals("p") && !previousAnswer.equalsIgnoreCase("")) {
                if (dirty || input.getText().length() == 0 || Calculator.isOperatorKey(input.getText().charAt(input.getText().length()-1))) {
                    clean();
                    input.setText(input.getText() + previousAnswer);
                    flashNode(input, 0.4, 500);
                }

            }

            if (WRITABLE.contains(keyEvent.getCharacter())) {
                clean();
                stage.getScene().getRoot().lookupAll(".button").forEach(n -> {
                    if (n instanceof Button && ((Button) n).getText().equals(keyEvent.getCharacter())) {
                        flashNode(n, 0.6, 400);
                    }
                });
                input.setText(input.getText() + keyEvent.getCharacter());
            }
        });
        stage.getScene().setOnKeyReleased(keyEvent -> {
            if (combo.match(keyEvent)) {
                if (dirty || input.getText().length() == 0) {
                    String cb = Clipboard.getSystemClipboard().getString();
                    new Thread(() -> {
                        for (char c : cb.toCharArray()) {
                            if (WRITABLE.contains("" + c)) {
                                clean();
                                Platform.runLater(() -> input.setText(input.getText() + c));
                                LockSupport.parkNanos(5000000L);
                            }
                        }
                    }).start();
                }
            }

            if (keyEvent.getCode().getCode() == 8) {
                clean();
                input.setText(input.getText().substring(0, Math.max(input.getText().length()-1, 0)));
            }
        });

        stage.getScene().getRoot().focusedProperty().addListener(bool -> {
            stage.getScene().getRoot().requestFocus();
        });
    }

    public void postInit() {
        answerButton.getScene().getRoot().requestFocus();

        input.textProperty().addListener(s -> flashNode(input, 0.9, 100));
    }

    private void flashNode(Node target, double start, int millis) {
        FadeTransition fadeTransition = new FadeTransition(Duration.millis(millis), target);
        fadeTransition.setToValue(1);
        fadeTransition.setByValue(start);

        target.setOpacity(start);
        fadeTransition.playFromStart();
    }

    private String tryRound(String s) {
        if (s.endsWith(".0")) return s.substring(0, s.length()-2);
        return s;
    }

    private void answer() {
        try {
            input.setText(tryRound("" + Math.round(Calculator.parse(input.getText() + "") * 1000D) / 1000D));
            previousAnswer = input.getText();
        } catch (NumberFormatException ex) {
            input.setText(ex.getLocalizedMessage());
        }
        dirty = true;
    }

    private CompletableFuture<Void> clean() {
        if (!dirty) return CompletableFuture.completedFuture(null);
        Thread t = new Thread(() -> input.setText(""));
        Platform.runLater(t);
        return CompletableFuture.runAsync(() -> {
            try {
                t.join();
            } catch (InterruptedException ignored) {

            }
            dirty = false;
        });
    }
    @FXML private void onButton(ActionEvent event) {
        if (!(event.getTarget() instanceof Button)) return;
        Button b = (Button) event.getTarget();
        flashNode(b, 0.6, 400);
        if (b.getText().equals("AC")) {
            new Thread(() -> {
                while (input.getText().length() > 0) {
                    Platform.runLater(() -> {
                        if (input.getText().length() > 0) input.setText(input.getText().substring(1));
                    });
                    LockSupport.parkNanos(10000000L);
                }
            }).start();

        } else if (b.getText().equals("=")) {
            clean().thenRun(this::answer);
        } else {
            clean().thenRun(() -> Platform.runLater(() -> {
                input.setText(input.getText() + b.getText());
            }));
        }
    }

}
