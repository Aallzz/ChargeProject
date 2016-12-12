package sample;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import sample.Charge.Charge;
import sample.Charge.ChargeCharacteristic.Acceleration;
import sample.Charge.ChargeCharacteristic.Coordinate;
import sample.Charge.ChargeCharacteristic.Speed;

import java.io.*;
import java.lang.String;
import java.text.DecimalFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main extends Application {

    private double time = 0.000000001;
    private int div = 1000000;
    private int radius = 2;

    private GridPane gridpane;
    private LineChart<Number, Number> chartspeedx, chartspeedy;
    private Canvas canvas;
    private VBox vbox, vboxLabel;
    private Label labelCount, labelTime;
    private TextField textDiv;
    private Button buttonClear, buttonSave, buttonLoad;
    private CheckBox checkBoxDeleteClick;

    private ArrayList<Charge> charges;
    private int chargeSign = +1;
    private int chargeSetType = 0;

    private int cntpressed = 0;
    boolean deleteOnCanvasClick = true;
    private Point2D p1, p2;

    private int findCharge(Charge q){
        int id = -1;
        for (Charge qq : charges){
            id++;
            if (Charge.distance(q, qq) <= 2 * radius) return id;
        }
        return -1;
    }
    private void UpdateChargesStates(){
        for (int i = 0; i < charges.size(); ++i){
            charges.get(i).getF().clear();
            for (int j = 0; j < charges.size(); ++j) {
                if (i == j) continue;
                if (charges.get(i).getQ() == 0) continue;
                if (charges.get(j).getQ() == 0) continue;
                charges.get(i).updateForce(charges.get(j));
            }
        }

        for (int i = 0; i < charges.size(); ++i){
            if (charges.get(i).isFixed()) continue;
            charges.get(i).updateAcceleration();
        }

        double lTime = 0.000000000000000000000001, rTime = 0.001;
        for (int ppp = 0; ppp < 800; ++ppp){
            double mid = (lTime + rTime) / 2;
            double dl = 0;
            for (int i = 0; i < charges.size(); ++i){
                if (charges.get(i).isFixed()) continue;
                for (int j = 0; j < 3; ++j)
                    dl = Math.max(dl, Math.abs(charges.get(i).getS().get(j) * mid + charges.get(i).getA().get(j) * mid * mid / 2));
            }
            if (dl > radius / 20.0) rTime = mid;
            else lTime = mid;
        }

        time = lTime;
//        time = 0.0000001;
        System.out.println(time);

        for (int i = 0; i < charges.size(); ++i){
            if (charges.get(i).isFixed()) continue;
            charges.get(i).updateCoordinate(time);
            charges.get(i).updateSpeed(time);
        }
    }
    private void DrawCharges(){
        canvas.getGraphicsContext2D().clearRect(0, 0,  canvas.getWidth(), canvas.getHeight());
        canvas.getGraphicsContext2D().setFill(Color.WHITE);
        canvas.getGraphicsContext2D().fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
        for (Charge charge: charges){
            if (charge.getQ() > 0) canvas.getGraphicsContext2D().setFill(Color.BLUE);
            else if (charge.getQ() < 0) canvas.getGraphicsContext2D().setFill(Color.RED);
            else canvas.getGraphicsContext2D().setFill(Color.BLACK);
            canvas.getGraphicsContext2D().fillOval(charge.getC().get(0) - radius, charge.getC().get(1) - radius, radius + radius, radius + radius);
        }
    }

    private void BuildBorderCircle(Point2D c, Point2D p, boolean fx){
        System.out.println(c.getX() + " " + c.getY());
        System.out.println(p.getX() + " " + p.getY());
        double r = Math.pow((c.getX() - p.getX())*(c.getX() - p.getX()) + (c.getY() - p.getY())*(c.getY() - p.getY()), 0.5);
        double delta = 0.01;
        if (!fx) delta = 1;
        for (double x = -r; x <= r; x += delta){
            double y = Math.pow(r * r - x * x, 0.5);
            Charge charge1 = new Charge((double)chargeSign, new Coordinate(x + c.getX(), y + c.getY(), 0.0), new Speed(0.0, 0.0, 0.0), new Acceleration(0.0, 0.0, 0.0), fx);
            Charge charge2 = new Charge((double)chargeSign, new Coordinate(x + c.getX(), -y + c.getY(), 0.0), new Speed(0.0, 0.0, 0.0), new Acceleration(0.0, 0.0, 0.0), fx);
            if (findCharge(charge1) == -1) charges.add(charge1);
            if (findCharge(charge2) == -1) charges.add(charge2);
        }
    }
    private void BuildBorderLine(Point2D s, Point2D f, boolean fx){
        if (s.getX() > f.getX()){
            Point2D temp = s;
            s = f;
            f = temp;
        }

        double dx = f.getX() - s.getX();
        double dy = f.getY() - s.getY();

//        int cnt = (int)Math.ceil((Math.pow(dx * dx + dy * dy, 0.5) + 1) / (Charge.r * 2));

        dx /= 1000;
        dy /= 1000;

        double x = s.getX();
        double y = s.getY();

        for (; x < f.getX(); ){
            Charge charge = new Charge(chargeSign, new Coordinate(x, y, 0.0), new Speed(0.0, 0.0, 0.0), new Acceleration(0.0, 0.0, 0.0), fx);
            if (findCharge(charge) == -1) charges.add(charge);
            x += dx;
            y += dy;
        }
    }

    private void CreateGridPane(){
        gridpane = new GridPane();
        gridpane.getColumnConstraints().add(new ColumnConstraints(300));
        gridpane.getColumnConstraints().add(new ColumnConstraints(300));
        gridpane.getColumnConstraints().add(new ColumnConstraints(200));
        gridpane.getRowConstraints().add(new RowConstraints(200));
        gridpane.getRowConstraints().add(new RowConstraints(200));
        gridpane.getRowConstraints().add(new RowConstraints(200));
        gridpane.setGridLinesVisible(true);
        gridpane.setHgap(5);
        gridpane.setVgap(5);
        gridpane.setPadding(new Insets(10, 10, 10, 10));
    }
    private void CreateCharts(){
        final NumberAxis xAxis1 = new NumberAxis();
        final NumberAxis yAxis1 = new NumberAxis();

        final NumberAxis xAxis2 = new NumberAxis();
        final NumberAxis yAxis2 = new NumberAxis();

        chartspeedx = new LineChart<Number, Number>(xAxis1, yAxis1);
        chartspeedx.setTitle("Speed X");

        chartspeedy = new LineChart<Number, Number>(xAxis2, yAxis2);
        chartspeedy.setTitle("Speed Y");

        chartspeedx.setAnimated(false);
        chartspeedy.setAnimated(false);

        xAxis1.setLabel("Speed");
        yAxis1.setLabel("Count");

        xAxis2.setLabel("Speed");
        yAxis2.setLabel("Count");

        Timeline tl = new Timeline();
        tl.getKeyFrames().add(new KeyFrame(Duration.millis(500),
                new EventHandler<ActionEvent>() {
                    @Override public void handle(ActionEvent actionEvent) {

                        Collections.sort(charges, new Comparator<Charge>() {
                            @Override
                            public int compare(Charge c1, Charge c2)
                            {

                                return  (c1.getS().get(0) / div < c2.getS().get(0) / div ? 1 : -1);
                            }
                        });



                        if (charges.size() != 0) {
                            XYChart.Series series1 = new XYChart.Series();
                            int pSpeed = (int) (charges.get(0).getS().get(0) / div);
                            int cnt = 0;
                            boolean was = false;
                            for (Charge charge : charges) {
                                if (charge.isFixed()) continue;
                                was = true;
                                if (pSpeed != (int) (charge.getS().get(0) / div)) {
                                    series1.getData().add(new XYChart.Data<>(pSpeed, cnt));
                                    pSpeed = (int) (charge.getS().get(0) / div);
                                    cnt = 1;
                                } else {
                                    cnt++;
                                }
                            }

                            if (cnt != 0) series1.getData().add(new XYChart.Data<>(pSpeed, cnt));
                            if (was) chartspeedx.getData().setAll(series1);
                        }

                        Collections.sort(charges, new Comparator<Charge>() {
                            @Override
                            public int compare(Charge c1, Charge c2)
                            {

                                return  (c1.getS().get(1) / div < c2.getS().get(1) / div ? 1 : -1);
                            }
                        });

                        if (charges.size() != 0) {
                            XYChart.Series series2 = new XYChart.Series();
                            int pSpeed = (int) (charges.get(0).getS().get(1) / div);
                            int cnt = 0;
                            boolean was = false;
                            for (Charge charge : charges) {
                                if (charge.isFixed()) continue;
                                was = true;
                                if (pSpeed != (int) (charge.getS().get(1) / div)) {
                                    series2.getData().add(new XYChart.Data<>(pSpeed, cnt));
                                    pSpeed = (int) (charge.getS().get(1) / div);
                                    cnt = 1;
                                } else {
                                    cnt++;
                                }
                            }

                            if (cnt != 0) series2.getData().add(new XYChart.Data<>(pSpeed, cnt));
                            if (was) chartspeedy.getData().setAll(series2);
                        }
                    }
                }
        ));

        tl.setCycleCount(Animation.INDEFINITE);
        tl.play();
    }  // После Canvas заглянуть сюда, чтобы переделать отрисовку графиков
    private void CreateCanvas(){

        canvas = new Canvas(600, 403);
        p1 = new Point2D(0,0);
        p2 = new Point2D(0,0);

        canvas.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                boolean fixed;
                if (event.getButton() == MouseButton.PRIMARY) fixed = false;
                else fixed = true;
                Charge curCharge = new Charge((double)chargeSign, new Coordinate(event.getSceneX() - 10, event.getSceneY() - 10, 0.0), new Speed(0.0, 0.0, 0.0), new Acceleration(0.0, 0.0, 0.0), fixed);
                if (chargeSetType == 0){
                    System.out.println(curCharge.getC().get(0) + " " + curCharge.getC().get(1));
                    int id = findCharge(curCharge);
                    if (id != -1) {
                        if (deleteOnCanvasClick) charges.remove(id);
                        return ;
                    }
                    charges.add(curCharge);
                    cntpressed = 0;
                }

                if (chargeSetType == 1){
                    if (cntpressed == 0){
                        double x = event.getSceneX() - 10;
                        double y = event.getSceneY() - 10;
                        p1 = p1.subtract(p1);
                        p1 = p1.add(x, y);
                        cntpressed++;
                    } else {
                        double xx = event.getSceneX() - 10;
                        double yy = event.getSceneY() - 10;
                        p2 = p2.subtract(p2);
                        p2 = p2.add(xx, yy);
                        cntpressed = 0;
                        BuildBorderCircle(p1, p2, fixed);
                    }
                }

                if (chargeSetType == 2){
                    if (cntpressed == 0){
                        double x = event.getSceneX() - 10;
                        double y = event.getSceneY() - 10;
                        p1 = p1.subtract(p1);
                        p1 = p1.add(x, y);
                        cntpressed++;
                    } else {
                        double xx = event.getSceneX() - 10;
                        double yy = event.getSceneY() - 10;
                        p2 = p2.subtract(p2);
                        p2 = p2.add(xx, yy);
                        cntpressed = 0;
                        BuildBorderLine(p1, p2, fixed);
                    }
                }

            }
        });

        Timeline tl = new Timeline();
        tl.getKeyFrames().add(new KeyFrame(Duration.millis(400), new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                UpdateChargesStates();
                DrawCharges();
            }
        }));

        tl.setCycleCount(Animation.INDEFINITE);
        tl.play();

    }
    private void CreateRadioButtons(){
        RadioButton rbp, rbm, rbn;
        RadioButton tpl, tpp, tpc;

        rbp = new RadioButton("Positive charge");
        rbm = new RadioButton("Negative charge");
        rbn = new RadioButton("Neutral charge");

        rbp.setUserData(1);
        rbm.setUserData(-1);
        rbn.setUserData(0);

        tpp = new RadioButton("Point");
        tpc = new RadioButton("Cirlce");
        tpl = new RadioButton("Line");

        tpp.setUserData(0);
        tpc.setUserData(1);
        tpl.setUserData(2);

        checkBoxDeleteClick = new CheckBox("Delete on click");
        checkBoxDeleteClick.setSelected(true);
        checkBoxDeleteClick.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                deleteOnCanvasClick = newValue;
            }
        });

        vbox = new VBox();
        HBox hbox = new HBox();
        hbox.setSpacing(30);
        hbox.getChildren().addAll(tpp, checkBoxDeleteClick);
        vbox.getChildren().addAll(rbp, rbm, rbn, (new Separator()), hbox, tpc, tpl);
        vbox.setSpacing(10);
        vbox.setPadding((new Insets(10, 10, 10, 10)));


        ToggleGroup groupSign = new ToggleGroup();
        ToggleGroup groupType = new ToggleGroup();

        rbp.setToggleGroup(groupSign);
        rbm.setToggleGroup(groupSign);
        rbn.setToggleGroup(groupSign);

        tpp.setToggleGroup(groupType);
        tpc.setToggleGroup(groupType);
        tpl.setToggleGroup(groupType);

        rbp.setSelected(true);
        tpp.setSelected(true);

        groupSign.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) {
                if (newValue.getUserData() == null) return ;
                chargeSign = (int)newValue.getUserData();
            }
        });

        groupType.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {
            @Override
            public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) {
                if (newValue.getUserData() == null) return ;
                chargeSetType = (int)newValue.getUserData();
            }
        });
    }
    private void CreateControlInfo(Stage primaryStage) throws  FileNotFoundException{

        textDiv = new TextField();
        textDiv.setPromptText("Enter division range");

        labelCount = new Label();
        labelTime = new Label();

        vboxLabel = new VBox();

        buttonClear = new Button("Delete charges");
        buttonSave = new Button("Save");
        buttonLoad = new Button("Load");

        HBox hbox = new HBox();
        hbox.getChildren().addAll(buttonSave, buttonLoad);
        hbox.setSpacing(30);

        vboxLabel.getChildren().addAll(labelCount, labelTime, textDiv, new Separator(), buttonClear, hbox);
        vboxLabel.setSpacing(10);
        vboxLabel.setPadding((new Insets(10, 10, 10, 10)));

        DecimalFormat four = new DecimalFormat("#0.000000000");

        Timeline tl = new Timeline();
        tl.getKeyFrames().add(new KeyFrame(Duration.millis(100), new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent actionEvent) {
                        Integer cnt = new Integer(charges.size());
                        labelCount.setText("Count of charges: " + cnt.toString());
                        labelTime.setText("Time updation: " + four.format(time * 1000));

                        String s = textDiv.getText();
                        int curDiv = 0;
                        for (int i = 0; i < s.length(); ++i){
                            if (s.charAt(i) >= '0' && s.charAt(i) <= '9') curDiv = curDiv * 10 + s.charAt(i) - '0';
                            else {
                                curDiv = 0;
                                break;
                            }
                        }
                        if (curDiv == 0) curDiv = 1000000;
                        div = curDiv;
                    }
        }));

        tl.setCycleCount(Animation.INDEFINITE);
        tl.play();

        buttonClear.setOnAction(new EventHandler<ActionEvent>(){
            @Override
            public void handle(ActionEvent event) {
                charges.clear();
            }
        });

        buttonSave.setOnAction(new EventHandler<ActionEvent>(){
            @Override
            public void handle(ActionEvent event) {
                FileChooser fileChooser = new FileChooser();

                //Set extension filter
                FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("TXT files (*.txt)", "*.txt");
                fileChooser.getExtensionFilters().add(extFilter);

                //Show save file dialog
                File file = fileChooser.showSaveDialog(primaryStage);

                if(file != null){
                    SaveFile(getText(), file);
                }
            }
        });

        buttonLoad.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event){
                FileChooser fileChooser = new FileChooser();

                //Set extension filter
                FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("TXT files (*.txt)", "*.txt");
                fileChooser.getExtensionFilters().add(extFilter);

                //Show save file dialog
                File file = fileChooser.showOpenDialog(primaryStage);

                if(file != null){
                    LoadFile(file);
                }
            }
        });
    }

    private void SaveFile(String content, File file){
        try {
            FileWriter fileWriter = null;
            fileWriter = new FileWriter(file);
            fileWriter.write(content);
            fileWriter.close();
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    private void LoadFile(File file){

        try {
            Scanner scanner = new Scanner(file);
            scanner.useLocale(Locale.ENGLISH);
            charges.clear();
            System.out.println(scanner.hasNextDouble());
//            System.out.println("line = " + scanner.nextLine());
            while (scanner.hasNextDouble()) {
                double cx, cy, cz, q, sx, sy, sz, ax, ay, az, fx, fy, fz, fix;
                q = scanner.nextFloat();
                cx = scanner.nextFloat();
                cy = scanner.nextFloat();
                cz = scanner.nextFloat();
                sx = scanner.nextFloat();
                sy = scanner.nextFloat();
                sz = scanner.nextFloat();
                ax = scanner.nextFloat();
                ay = scanner.nextFloat();
                az = scanner.nextFloat();
                fx = scanner.nextFloat();
                fy = scanner.nextFloat();
                fz = scanner.nextFloat();
                fix = scanner.nextFloat();
                Charge charge = new Charge(q, new Coordinate(cx, cy, cz), new Speed(sx, sy, sz), new Acceleration(ax, ay, az), (fix == 1.0));
                charge.force.set(0, fx);
                charge.force.set(1, fy);
                charge.force.set(2, fz);
                charges.add(charge);
            }
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private String getText(){
        String res = "";
        for (Charge charge : charges){
            res += charge.getQ() + "\n";
            res +=  charge.getC().get(0) + " " + charge.getC().get(1) + " " + charge.getC().get(2) + "\n";
            res +=  charge.getS().get(0) + " " + charge.getS().get(1) + " " + charge.getS().get(2) + "\n";
            res +=  charge.getA().get(0) + " " + charge.getA().get(1) + " " + charge.getA().get(2) + "\n";
            res +=  charge.getF().get(0) + " " + charge.getF().get(1) + " " + charge.getF().get(2) + "\n";
            res +=  (charge.isFixed() ? 1 : 0) + "\n";
        }
        return res;
    }

    @Override
    public void start(Stage stage) throws Exception {

        charges = new ArrayList<>();

        CreateGridPane();
        CreateCharts();
        CreateCanvas();
        CreateRadioButtons();
        CreateControlInfo(stage);

        gridpane.add(chartspeedx, 0, 2);
        gridpane.add(chartspeedy, 1, 2);
        gridpane.add(canvas, 0, 0, 2, 2);
        gridpane.add(vbox, 2, 0);
        gridpane.add(vboxLabel, 2, 1);

        Scene scene = new Scene(gridpane, 900, 660);

        stage.setScene(scene);

        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
