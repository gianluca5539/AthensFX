package com.athens.athensfx;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.ValueAxis;
import javafx.scene.control.*;
import javafx.scene.shape.Sphere;
import javafx.scene.text.Text;
import org.controlsfx.control.ToggleSwitch;

import static com.athens.athensfx.Genesis.selectedPopulation;

public class WindowController {

    @FXML
    TextArea console;
    @FXML
    Text aLabel;
    @FXML
    Text bLabel;
    @FXML
    Text cLabel;
    @FXML
    Text selectedPopulationID;
    @FXML
    Text menPercentage;
    @FXML
    Text womenPercentage;
    @FXML
    Button nextPopulation;
    @FXML
    Button previousPopulation;
    @FXML
    Button pause;
    @FXML
    TextField a;
    @FXML
    TextField b;
    @FXML
    TextField c;
    @FXML
    Button createNew;
    @FXML
    ProgressBar menProgressBar;
    @FXML
    ProgressBar womenProgressBar;
    @FXML
    LineChart<Number,Number> ratioChart;
    @FXML
    Slider iterationDelaySlider;
    @FXML
    ToggleSwitch growthSwitch;
    @FXML
    Slider refreshDelaySlider;
    @FXML
    PieChart pieChart;
    @FXML
    Slider womenRatioSlider;
    @FXML
    Slider menRatioSlider;
    @FXML
    TitledPane dropDown;
    @FXML
    Slider aSlider;
    @FXML
    Slider bSlider;
    @FXML
    Slider cSlider;
    @FXML
    Sphere earth;
    double angle = 0.0;
    ValueAxis<Number> xAxis;
    PieChart.Data p1 = new PieChart.Data("Faithful", 0);
    PieChart.Data p2 = new PieChart.Data("Philanderer", 0);
    PieChart.Data p3 = new PieChart.Data("CoyWoman", 0);
    PieChart.Data p4 = new PieChart.Data("FastWoman", 0);


    @FXML
    protected void onCreateNew() {
        Genesis.createPopulation(Integer.parseInt(a.getText()), Integer.parseInt(b.getText()), Integer.parseInt(c.getText()));
        aLabel.setText(a.getText());
        bLabel.setText(b.getText());
        cLabel.setText(c.getText());
        populationChange();
        dropDown.setExpanded(false);
    }

    @FXML
    protected void onPauseToggle() {
        if (selectedPopulation.paused) {
            selectedPopulation.paused = false;
            synchronized (selectedPopulation.pauseLock) {selectedPopulation.pauseLock.notifyAll();}
            pause.setText("PAUSE");
        } else {
            selectedPopulation.paused = true;
            pause.setText("RESUME");
        }
    }

    @FXML
    protected void onPreviousPopulation() {
        if (Genesis.selectedPopulationIndex == 0) return;
        selectedPopulation = Genesis.populations.get(--Genesis.selectedPopulationIndex);
        populationChange();
    }

    @FXML
    protected void onNextPopulation() {
        if (Genesis.selectedPopulationIndex >= Genesis.populations.size() - 1) return;
        selectedPopulation = Genesis.populations.get(++Genesis.selectedPopulationIndex);
        populationChange();
    }

    @FXML
    void setPopulationIterationDelay() {
        selectedPopulation.iterationDelay = (int) iterationDelaySlider.getValue();
    }

    @FXML
    void setPopulationGrowth() {
        selectedPopulation.growth = growthSwitch.isSelected();
    }

    @FXML
    void setRefreshDelay() {
        Genesis.WindowUpdater.refreshDelay = (int) refreshDelaySlider.getValue();
    }

    @FXML
    void setParameters() {
        selectedPopulation.a = (int) aSlider.getValue();
        selectedPopulation.b = (int) bSlider.getValue();
        selectedPopulation.c = (int) cSlider.getValue();
        selectedPopulation.updateParameters();
        aLabel.setText(String.valueOf(selectedPopulation.a));
        bLabel.setText(String.valueOf(selectedPopulation.b));
        cLabel.setText(String.valueOf(selectedPopulation.c));
    }

    void setInfo(float[] values) {
        int size = selectedPopulation.womenHolder.series.getData().size();
        if (size > 100) { // TODO this needs an optimization
            xAxis.setLowerBound(size - 100);
            xAxis.setUpperBound(size);
        }
        if (selectedPopulation.paused) return;
        angle += 0.3;
        earth.setRotate(angle);
        float menRatio = values[4] / (values[3] + values[4]);
        float womenRatio = values[6] / (values[6] + values[5]);
        menProgressBar.setProgress(menRatio);
        menPercentage.setText(Math.round(menRatio * 8) + "/8");
        womenProgressBar.setProgress(womenRatio);
        womenPercentage.setText(Math.round(womenRatio * 6) + "/6");
        seriesUpdate(menRatio, womenRatio);
        pieChartUpdate();
        console.setText("---- iteration " + values[0] + " ----" +
                "\n- Population: " + (int) (values[1] + values[2]) + "(" + (int) values[1] + ", " + (int) values[2] + ")" +
                "\n- Normals(M, F): " + (int) values[4] + ", " + (int) values[6] + " = " + ((int) (values[4] + values[6])) +
                "\n- Hornies(M, F): " + (int) values[3] + ", " + (int) values[5] + " = " + ((int) (values[3] + values[5])) +
                "\n- Ratio: " + menRatio + ", " + womenRatio + "\n");
    }

    private void seriesUpdate(float menRatio, float womenRatio) {
        selectedPopulation.menHolder.series.getData().add(new LineChart.Data<>(selectedPopulation.menHolder.series.getData().size(), menRatio));
        selectedPopulation.womenHolder.series.getData().add(new LineChart.Data<>(selectedPopulation.womenHolder.series.getData().size(), womenRatio));
    }

    void lineChartUpdate() {
        ratioChart.getData().clear();
        ratioChart.getData().add(selectedPopulation.womenHolder.series);
        ratioChart.getData().add(selectedPopulation.menHolder.series);
        int size = selectedPopulation.womenHolder.series.getData().size();
        if (size > 100) {
            xAxis.setLowerBound(size - 100);
            xAxis.setUpperBound(size);
        } else {
            xAxis.setLowerBound(0);
            xAxis.setUpperBound(100);
        }
    }

    void populationChange() {
        console.clear();
        pause.setText((selectedPopulation.paused) ? "RESUME" : "PAUSE");
        lineChartUpdate();
        selectedPopulationID.setText(String.valueOf(Genesis.selectedPopulationIndex));
        iterationDelaySlider.setValue(selectedPopulation.iterationDelay);
        growthSwitch.setSelected(selectedPopulation.growth);
        aSlider.setDisable(false);
        bSlider.setDisable(false);
        cSlider.setDisable(false);
        pause.setDisable(false);
        aSlider.setValue(selectedPopulation.a);
        bSlider.setValue(selectedPopulation.b);
        cSlider.setValue(selectedPopulation.c);
        iterationDelaySlider.setDisable(false);
        growthSwitch.setDisable(false);
    }

    void pieChartUpdate() {
        p1.setPieValue(selectedPopulation.faithfulMen.get());
        p2.setPieValue(selectedPopulation.philanderers.get());
        p3.setPieValue(selectedPopulation.coyWomen.get());
        p4.setPieValue(selectedPopulation.fastWomen.get());
    }


    public void bakeThePie() {
        pieChart.setData(FXCollections.observableArrayList(p1, p2, p3, p4));
    }

}