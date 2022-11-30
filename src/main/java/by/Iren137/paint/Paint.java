package by.Iren137.paint;

import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Stack;

import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;

import static by.Iren137.paint.Constants.*;

public class Paint extends Application {
    @Override
    public void start(Stage stage) {
        Stack<Shape> undoHistory = new Stack<>();
        Stack<Shape> redoHistory = new Stack<>();

        ToggleButton drawButton = new ToggleButton("Draw");
        ToggleButton rubberButton = new ToggleButton("Rubber");
        ToggleButton lineButton = new ToggleButton("Line");
        ToggleButton rectangleButton = new ToggleButton("Rectangle");
        ToggleButton circleButton = new ToggleButton("Circle");
        ToggleButton ellipseButton = new ToggleButton("Ellipse");
        ToggleButton textButton = new ToggleButton("Text");

        ToggleButton[] toolsArr = {drawButton, rubberButton, lineButton, rectangleButton, circleButton, ellipseButton, textButton};

        ToggleGroup tools = new ToggleGroup();

        for (ToggleButton tool : toolsArr) {
            tool.setMinWidth(minWidth);
            tool.setToggleGroup(tools);
            tool.setCursor(Cursor.HAND);
        }

        ColorPicker cpLine = new ColorPicker(Color.BLACK);
        ColorPicker cpFill = new ColorPicker(Color.TRANSPARENT);

        TextArea text = new TextArea();
        text.setPrefRowCount(1);

        Slider slider = new Slider(sliderMin, sliderMax, sliderValue);
        slider.setShowTickLabels(true);
        slider.setShowTickMarks(true);

        Label lineColorOut = new Label("Line Color");
        Label fillColorOut = new Label("Fill Color");
        Label lineWidthOut = new Label("3.0");

        Button undo = new Button("Undo");
        Button redo = new Button("Redo");
        Button save = new Button("Save");
        Button open = new Button("Open");

        Button[] buttonsArray = {undo, redo, save, open};

        for (Button button : buttonsArray) {
            button.setMinWidth(minWidth);
            button.setCursor(Cursor.HAND);
            button.setTextFill(Color.WHITE);
            button.setStyle("-fx-background-color: #666;");
        }
        save.setStyle("-fx-background-color: #80334d;");
        open.setStyle("-fx-background-color: #80334d;");

        VBox buttons = new VBox(VBoxValue);
        buttons.getChildren().addAll(drawButton, rubberButton, lineButton, rectangleButton, circleButton, ellipseButton,
                textButton, text, lineColorOut, cpLine, fillColorOut, cpFill, lineWidthOut, slider, undo, redo, open, save);
        buttons.setPadding(new Insets(buttonsPadding));
        buttons.setStyle("-fx-background-color: #999");
        buttons.setPrefWidth(prefWidth);

        Canvas canvas = new Canvas(canvasWidth, canvasHeight);
        GraphicsContext gc;
        gc = canvas.getGraphicsContext2D();
        gc.setLineWidth(1);

        Line line = new Line();
        Rectangle rect = new Rectangle();
        Circle circle = new Circle();
        Ellipse ellipse = new Ellipse();

        canvas.setOnMousePressed(e -> {
            if (drawButton.isSelected()) {
                gc.setStroke(cpLine.getValue());
                gc.beginPath();
                gc.lineTo(e.getX(), e.getY());
            } else if (rubberButton.isSelected()) {
                double lineWidth = gc.getLineWidth();
                gc.clearRect(e.getX() - lineWidth / 2, e.getY() - lineWidth / 2, lineWidth, lineWidth);
            } else if (lineButton.isSelected()) {
                gc.setStroke(cpLine.getValue());
                line.setStartX(e.getX());
                line.setStartY(e.getY());
            } else if (rectangleButton.isSelected()) {
                gc.setStroke(cpLine.getValue());
                gc.setFill(cpFill.getValue());
                rect.setX(e.getX());
                rect.setY(e.getY());
            } else if (circleButton.isSelected()) {
                gc.setStroke(cpLine.getValue());
                gc.setFill(cpFill.getValue());
                circle.setCenterX(e.getX());
                circle.setCenterY(e.getY());
            } else if (ellipseButton.isSelected()) {
                gc.setStroke(cpLine.getValue());
                gc.setFill(cpFill.getValue());
                ellipse.setCenterX(e.getX());
                ellipse.setCenterY(e.getY());
            } else if (textButton.isSelected()) {
                gc.setLineWidth(1);
                gc.setFont(Font.font(slider.getValue()));
                gc.setStroke(cpLine.getValue());
                gc.setFill(cpFill.getValue());
                gc.fillText(text.getText(), e.getX(), e.getY());
                gc.strokeText(text.getText(), e.getX(), e.getY());
            }
        });

        canvas.setOnMouseDragged(e -> {
            if (drawButton.isSelected()) {
                gc.lineTo(e.getX(), e.getY());
                gc.stroke();
            } else if (rubberButton.isSelected()) {
                double lineWidth = gc.getLineWidth();
                gc.clearRect(e.getX() - lineWidth / 2, e.getY() - lineWidth / 2, lineWidth, lineWidth);
            }
        });

        canvas.setOnMouseReleased(e -> {
            if (drawButton.isSelected()) {
                gc.lineTo(e.getX(), e.getY());
                gc.stroke();
                gc.closePath();
            } else if (rubberButton.isSelected()) {
                double lineWidth = gc.getLineWidth();
                gc.clearRect(e.getX() - lineWidth / 2, e.getY() - lineWidth / 2, lineWidth, lineWidth);
            } else if (lineButton.isSelected()) {
                line.setEndX(e.getX());
                line.setEndY(e.getY());
                gc.strokeLine(line.getStartX(), line.getStartY(), line.getEndX(), line.getEndY());

                undoHistory.push(new Line(line.getStartX(), line.getStartY(), line.getEndX(), line.getEndY()));
            } else if (rectangleButton.isSelected()) {
                rect.setWidth(Math.abs((e.getX() - rect.getX())));
                rect.setHeight(Math.abs((e.getY() - rect.getY())));
                if (rect.getX() > e.getX()) {
                    rect.setX(e.getX());
                }
                if (rect.getY() > e.getY()) {
                    rect.setY(e.getY());
                }

                gc.fillRect(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
                gc.strokeRect(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());

                undoHistory.push(new Rectangle(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight()));

            } else if (circleButton.isSelected()) {
                circle.setRadius((Math.abs(e.getX() - circle.getCenterX()) + Math.abs(e.getY() - circle.getCenterY())) / 2);

                if (circle.getCenterX() > e.getX()) {
                    circle.setCenterX(e.getX());
                }
                if (circle.getCenterY() > e.getY()) {
                    circle.setCenterY(e.getY());
                }

                gc.fillOval(circle.getCenterX(), circle.getCenterY(), circle.getRadius(), circle.getRadius());
                gc.strokeOval(circle.getCenterX(), circle.getCenterY(), circle.getRadius(), circle.getRadius());

                undoHistory.push(new Circle(circle.getCenterX(), circle.getCenterY(), circle.getRadius()));
            } else if (ellipseButton.isSelected()) {
                ellipse.setRadiusX(Math.abs(e.getX() - ellipse.getCenterX()));
                ellipse.setRadiusY(Math.abs(e.getY() - ellipse.getCenterY()));

                if (ellipse.getCenterX() > e.getX()) {
                    ellipse.setCenterX(e.getX());
                }
                if (ellipse.getCenterY() > e.getY()) {
                    ellipse.setCenterY(e.getY());
                }

                gc.strokeOval(ellipse.getCenterX(), ellipse.getCenterY(), ellipse.getRadiusX(), ellipse.getRadiusY());
                gc.fillOval(ellipse.getCenterX(), ellipse.getCenterY(), ellipse.getRadiusX(), ellipse.getRadiusY());

                undoHistory.push(new Ellipse(ellipse.getCenterX(), ellipse.getCenterY(), ellipse.getRadiusX(), ellipse.getRadiusY()));
            }
            redoHistory.clear();
            if (!undoHistory.empty()) {
                Shape lastUndo = undoHistory.lastElement();
                lastUndo.setFill(gc.getFill());
                lastUndo.setStroke(gc.getStroke());
                lastUndo.setStrokeWidth(gc.getLineWidth());
            }

        });

        cpLine.setOnAction(e -> gc.setStroke(cpLine.getValue()));
        cpFill.setOnAction(e -> gc.setFill(cpFill.getValue()));

        slider.valueProperty().addListener(e -> {
            double width = slider.getValue();
            if (textButton.isSelected()) {
                gc.setLineWidth(1);
                gc.setFont(Font.font(slider.getValue()));
                lineWidthOut.setText(String.format("%.1f", width));
                return;
            }
            lineWidthOut.setText(String.format("%.1f", width));
            gc.setLineWidth(width);
        });

        undo.setOnAction(e -> {
            if (!undoHistory.empty()) {
                gc.clearRect(0, 0, canvasWidth, canvasHeight);
                Shape removedShape = undoHistory.lastElement();
                if (removedShape.getClass() == Line.class) {
                    Line tempLine = (Line) removedShape;
                    tempLine.setFill(gc.getFill());
                    tempLine.setStroke(gc.getStroke());
                    tempLine.setStrokeWidth(gc.getLineWidth());
                    redoHistory.push(new Line(tempLine.getStartX(), tempLine.getStartY(), tempLine.getEndX(), tempLine.getEndY()));

                } else if (removedShape.getClass() == Rectangle.class) {
                    Rectangle tempRect = (Rectangle) removedShape;
                    tempRect.setFill(gc.getFill());
                    tempRect.setStroke(gc.getStroke());
                    tempRect.setStrokeWidth(gc.getLineWidth());
                    redoHistory.push(new Rectangle(tempRect.getX(), tempRect.getY(), tempRect.getWidth(), tempRect.getHeight()));
                } else if (removedShape.getClass() == Circle.class) {
                    Circle tempCircle = (Circle) removedShape;
                    tempCircle.setStrokeWidth(gc.getLineWidth());
                    tempCircle.setFill(gc.getFill());
                    tempCircle.setStroke(gc.getStroke());
                    redoHistory.push(new Circle(tempCircle.getCenterX(), tempCircle.getCenterY(), tempCircle.getRadius()));
                } else if (removedShape.getClass() == Ellipse.class) {
                    Ellipse tempEllipse = (Ellipse) removedShape;
                    tempEllipse.setFill(gc.getFill());
                    tempEllipse.setStroke(gc.getStroke());
                    tempEllipse.setStrokeWidth(gc.getLineWidth());
                    redoHistory.push(new Ellipse(tempEllipse.getCenterX(), tempEllipse.getCenterY(), tempEllipse.getRadiusX(), tempEllipse.getRadiusY()));
                }
                Shape lastRedo = redoHistory.lastElement();
                lastRedo.setFill(removedShape.getFill());
                lastRedo.setStroke(removedShape.getStroke());
                lastRedo.setStrokeWidth(removedShape.getStrokeWidth());
                undoHistory.pop();

                for (int i = 0; i < undoHistory.size(); i++) {
                    Shape shape = undoHistory.elementAt(i);
                    if (shape.getClass() == Line.class) {
                        Line temp = (Line) shape;
                        gc.setLineWidth(temp.getStrokeWidth());
                        gc.setStroke(temp.getStroke());
                        gc.setFill(temp.getFill());
                        gc.strokeLine(temp.getStartX(), temp.getStartY(), temp.getEndX(), temp.getEndY());
                    } else if (shape.getClass() == Rectangle.class) {
                        Rectangle temp = (Rectangle) shape;
                        gc.setLineWidth(temp.getStrokeWidth());
                        gc.setStroke(temp.getStroke());
                        gc.setFill(temp.getFill());
                        gc.fillRect(temp.getX(), temp.getY(), temp.getWidth(), temp.getHeight());
                        gc.strokeRect(temp.getX(), temp.getY(), temp.getWidth(), temp.getHeight());
                    } else if (shape.getClass() == Circle.class) {
                        Circle temp = (Circle) shape;
                        gc.setLineWidth(temp.getStrokeWidth());
                        gc.setStroke(temp.getStroke());
                        gc.setFill(temp.getFill());
                        gc.fillOval(temp.getCenterX(), temp.getCenterY(), temp.getRadius(), temp.getRadius());
                        gc.strokeOval(temp.getCenterX(), temp.getCenterY(), temp.getRadius(), temp.getRadius());
                    } else if (shape.getClass() == Ellipse.class) {
                        Ellipse temp = (Ellipse) shape;
                        gc.setLineWidth(temp.getStrokeWidth());
                        gc.setStroke(temp.getStroke());
                        gc.setFill(temp.getFill());
                        gc.fillOval(temp.getCenterX(), temp.getCenterY(), temp.getRadiusX(), temp.getRadiusY());
                        gc.strokeOval(temp.getCenterX(), temp.getCenterY(), temp.getRadiusX(), temp.getRadiusY());
                    }
                }
            } else {
                System.out.println("There is no action to undo.");
            }
        });

        redo.setOnAction(e -> {
            if (!redoHistory.empty()) {
                Shape shape = redoHistory.lastElement();
                gc.setLineWidth(shape.getStrokeWidth());
                gc.setStroke(shape.getStroke());
                gc.setFill(shape.getFill());

                redoHistory.pop();
                if (shape.getClass() == Line.class) {
                    Line tempLine = (Line) shape;
                    gc.strokeLine(tempLine.getStartX(), tempLine.getStartY(), tempLine.getEndX(), tempLine.getEndY());
                    undoHistory.push(new Line(tempLine.getStartX(), tempLine.getStartY(), tempLine.getEndX(), tempLine.getEndY()));
                } else if (shape.getClass() == Rectangle.class) {
                    Rectangle tempRect = (Rectangle) shape;
                    gc.fillRect(tempRect.getX(), tempRect.getY(), tempRect.getWidth(), tempRect.getHeight());
                    gc.strokeRect(tempRect.getX(), tempRect.getY(), tempRect.getWidth(), tempRect.getHeight());

                    undoHistory.push(new Rectangle(tempRect.getX(), tempRect.getY(), tempRect.getWidth(), tempRect.getHeight()));
                } else if (shape.getClass() == Circle.class) {
                    Circle tempCircle = (Circle) shape;
                    gc.fillOval(tempCircle.getCenterX(), tempCircle.getCenterY(), tempCircle.getRadius(), tempCircle.getRadius());
                    gc.strokeOval(tempCircle.getCenterX(), tempCircle.getCenterY(), tempCircle.getRadius(), tempCircle.getRadius());

                    undoHistory.push(new Circle(tempCircle.getCenterX(), tempCircle.getCenterY(), tempCircle.getRadius()));
                } else if (shape.getClass() == Ellipse.class) {
                    Ellipse tempEllipse = (Ellipse) shape;
                    gc.fillOval(tempEllipse.getCenterX(), tempEllipse.getCenterY(), tempEllipse.getRadiusX(), tempEllipse.getRadiusY());
                    gc.strokeOval(tempEllipse.getCenterX(), tempEllipse.getCenterY(), tempEllipse.getRadiusX(), tempEllipse.getRadiusY());

                    undoHistory.push(new Ellipse(tempEllipse.getCenterX(), tempEllipse.getCenterY(), tempEllipse.getRadiusX(), tempEllipse.getRadiusY()));
                }
                Shape lastUndo = undoHistory.lastElement();
                lastUndo.setFill(gc.getFill());
                lastUndo.setStroke(gc.getStroke());
                lastUndo.setStrokeWidth(gc.getLineWidth());
            } else {
                System.out.println("There is no action to redo.");
            }
        });

        open.setOnAction((e) -> {
            FileChooser openFile = new FileChooser();
            openFile.setTitle("Open File");
            File file = openFile.showOpenDialog(stage);
            if (file != null) {
                try {
                    InputStream io = new FileInputStream(file);
                    Image img = new Image(io);
                    gc.drawImage(img, 0, 0);
                } catch (IOException ex) {
                    System.out.println("Error!");
                }
            }
        });

        save.setOnAction((e) -> {
            FileChooser saveFile = new FileChooser();
            saveFile.setTitle("Save File");

            File file = saveFile.showSaveDialog(stage);
            if (file != null) {
                try {
                    WritableImage writableImage = new WritableImage(canvasWidth, canvasHeight);
                    canvas.snapshot(null, writableImage);
                    RenderedImage renderedImage = SwingFXUtils.fromFXImage(writableImage, null);
                    ImageIO.write(renderedImage, "png", file);
                } catch (IOException ex) {
                    System.out.println("Error!");
                }
            }

        });

        BorderPane pane = new BorderPane();
        pane.setLeft(buttons);
        pane.setCenter(canvas);

        Scene scene = new Scene(pane, sceneWidth, sceneHeight);

        stage.setTitle("Paint");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}