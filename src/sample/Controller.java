package sample;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.*;
import javafx.scene.control.RadioButton;
import javafx.util.Duration;

import java.net.URL;
import java.util.ResourceBundle;

public class Controller implements Initializable{

    private Timeline tl;
    @FXML
    private AreaChart<Number, Number> AreaChartSpeedX;

    @FXML
    private RadioButton RadioButtonChargePositive;

    @FXML
    private BarChart<Number, String> bc;
    @FXML
    private NumberAxis xAxis;
    @FXML
    private NumberAxis yAxis;
    @FXML
    private LineChart linechart;

    public void updateText(){
        RadioButtonChargePositive.setText("asdas");
    }

    public void initialize(URL url, ResourceBundle a) {

        xAxis = new NumberAxis(1960, 2020, 10);
        xAxis.setLabel("Years");

        //Defining the y axis
        yAxis = new NumberAxis   (0, 350, 50);
        yAxis.setLabel("No.of schools");

        //Creating the line chart
        linechart = new LineChart(xAxis, yAxis);

        //Prepare XYChart.Series objects by setting data
        XYChart.Series series = new XYChart.Series();
        series.setName("No of schools in an year");

        series.getData().add(new XYChart.Data(1970, 15));
        series.getData().add(new XYChart.Data(1980, 30));
        series.getData().add(new XYChart.Data(1990, 60));
        series.getData().add(new XYChart.Data(2000, 120));
        series.getData().add(new XYChart.Data(2013, 240));
        series.getData().add(new XYChart.Data(2014, 300));

        //Setting the data to Line chart
        linechart.getData().add(series);

    }

}

