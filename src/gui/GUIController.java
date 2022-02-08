package gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import overkeys.generator.OverKeysGenerator;

import javax.swing.*;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;


public class GUIController implements Initializable {

    @FXML
    public GridPane mainPane;

    @FXML
    public TextField renderFolder;

    @FXML
    public TextField openscadPath;

    @FXML
    public CheckBox keepScad;
    public Label message;
    public StackPane stackPane;
    public RadioButton shiftXtrue;
    public RadioButton shiftXfalse;
    public ToggleGroup Group1;
    public CheckBox roughRender;
    public Slider shiftX;
    public Slider shiftY;
    public TextField shiftXValue;
    public TextField shiftYValue;
    public CheckBox verticalFlip;

    @FXML
    private TextField blackKeyHeight;

    @FXML
    private TextField blackKeyLength;

    @FXML
    private TextField gamut;

    @FXML
    private TextField halfStepsToGenerator;

    @FXML
    private TextField halfStepsToLargeMOSStep;

    @FXML
    private TextField halfStepsToPeriod;

    @FXML
    private TextField keytopHeightDiff;

    @FXML
    private TextField keytopScale;


    @FXML
    private Button load;

    @FXML
    private TextField metalRoundRadius;

    @FXML
    private TextField octaveWidth;

    @FXML
    private TextField range;

    @FXML
    private Button save;

    @FXML
    private TextField stalkFitXTolerance;

    @FXML
    private TextField stalkFitYTolerance;

    @FXML
    private TextField startingKey;

    @FXML
    private TextField underkeyGap;

    @FXML
    private TextField whiteKeyLength;

    public static final String SEPARATOR = "::";

    private OverKeysGenerator IKG;

    @FXML
    void loadPreset(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("TXT files (*.txt)", "*.txt");
        fileChooser.getExtensionFilters().add(extFilter);

        Node node = (Node) event.getSource();
        Stage currentStage = (Stage) node.getScene().getWindow();

        File file = fileChooser.showOpenDialog(currentStage);
        if (file != null) {
            loadScreenData(file);
        }


    }

    void loadScreenData(File file) {
        Map<String, String> values = readFromFile(file);
        for (String key : values.keySet()) {
            switch (key){
                case "shiftXtrue":
                    if (Integer.parseInt(values.get(key)) == 1)
                        shiftXtrue.setSelected(true);
                    else
                        shiftXfalse.setSelected(true);
                    break;
                case "roughRender":
                    if (Integer.parseInt(values.get(key)) == 1)
                        roughRender.setSelected(true);
                    else
                        roughRender.setSelected(false);
                    break;
                case "verticalFlip":
                    if (Integer.parseInt(values.get(key)) == 1)
                        verticalFlip.setSelected(true);
                    else
                        verticalFlip.setSelected(false);
                    break;
                case "shiftX":
                    break;
                case "shiftY":
                    break;
                default:
                    TextField field = (TextField) mainPane.getScene().lookup("#" + key);
                    field.setText(values.get(key));
            }
        }
        try {
            Files.copy(file.toPath(), (new File("./resources/config.txt").toPath()), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @FXML
    void savePreset(ActionEvent event) {
        Map<String, Double> doubleValues = getDoubleValues();
        Map<String, Integer> intValues = getIntValues();

        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("TXT files (*.txt)", "*.txt");
        fileChooser.getExtensionFilters().add(extFilter);

        Node node = (Node) event.getSource();
        Stage currentStage = (Stage) node.getScene().getWindow();

        File file = fileChooser.showSaveDialog(currentStage);

        if (file != null) {
            saveToFile(doubleValues, intValues, file);
        }

    }

    private Map<String, Integer> getIntValues() {

        Map<String, Integer> intValues = new HashMap<>();
        try {
            intValues.put("halfStepsToPeriod", Integer.parseInt(halfStepsToPeriod.getText()));
            intValues.put("halfStepsToGenerator", Integer.parseInt(halfStepsToGenerator.getText()));
            intValues.put("halfStepsToLargeMOSStep", Integer.parseInt(halfStepsToLargeMOSStep.getText()));
            intValues.put("gamut", Integer.parseInt(gamut.getText()));
            intValues.put("range", Integer.parseInt(range.getText()));
            intValues.put("startingKey", Integer.parseInt(startingKey.getText()));
            if (shiftXtrue.isSelected()) {
                intValues.put("shiftXtrue", 1);
            } else {
                intValues.put("shiftXtrue", 0);
            }
            if (roughRender.isSelected()) {
                intValues.put("roughRender", 1);
            } else {
                intValues.put("roughRender", 0);
            }
            if (verticalFlip.isSelected()) {
                intValues.put("verticalFlip", 1);
            } else {
                intValues.put("verticalFlip", 0);
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, e, "Error", JOptionPane.ERROR_MESSAGE);
        }
        return intValues;
    }

    private Map<String, Double> getDoubleValues() {
        Map<String, Double> doubleValues = new HashMap<>();
        try {
            doubleValues.put("octaveWidth", Double.parseDouble(octaveWidth.getText()));
            doubleValues.put("blackKeyHeight", Double.parseDouble(blackKeyHeight.getText()));
            doubleValues.put("blackKeyLength", Double.parseDouble(blackKeyLength.getText()));
            doubleValues.put("whiteKeyLength", Double.parseDouble(whiteKeyLength.getText()));
            doubleValues.put("keytopHeightDiff", Double.parseDouble(keytopHeightDiff.getText()));
            doubleValues.put("metalRoundRadius", Double.parseDouble(metalRoundRadius.getText()));

            doubleValues.put("stalkFitXTolerance", Double.parseDouble(stalkFitXTolerance.getText()));
            doubleValues.put("stalkFitYTolerance", Double.parseDouble(stalkFitYTolerance.getText()));
            doubleValues.put("keytopScale", Double.parseDouble(keytopScale.getText()));
            doubleValues.put("underkeyGap", Double.parseDouble(underkeyGap.getText()));

            doubleValues.put("shiftXValue",Double.parseDouble(shiftXValue.getText()));
            doubleValues.put("shiftYValue",Double.parseDouble(shiftYValue.getText()));

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, e, "Error", JOptionPane.ERROR_MESSAGE);
        }
        return doubleValues;
    }

    private void saveToFile(Map<String, Double> doubleValues, Map<String, Integer> intValues, File file) {
        try {
            PrintWriter writer;
            writer = new PrintWriter(file);
            for (String key : doubleValues.keySet())
                writer.println(key + SEPARATOR + doubleValues.get(key));
            for (String key : intValues.keySet())
                writer.println(key + SEPARATOR + intValues.get(key));
            writer.close();
            Files.copy(file.toPath(), (new File("./resources/config.txt").toPath()), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, ex, "Error", JOptionPane.ERROR_MESSAGE);
        }

    }

    private void saveToConfig(Map<String, Double> doubleValues, Map<String, Integer> intValues) {
        try {
            PrintWriter writer;
            writer = new PrintWriter(new File("./resources/config.txt"));
            for (String key : doubleValues.keySet())
                writer.println(key + SEPARATOR + doubleValues.get(key));
            for (String key : intValues.keySet())
                writer.println(key + SEPARATOR + intValues.get(key));
            writer.close();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, ex, "Error", JOptionPane.ERROR_MESSAGE);
        }

    }

    private Map<String, String> readFromFile(File file) {
        Map<String, String> values = new HashMap<>();

        try (Scanner sc = new Scanner(file)) {
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                String[] properties = line.split(GUIController.SEPARATOR, -1);
                values.put(properties[0], properties[1]);
            }
        } catch (FileNotFoundException ignored) {

        }
        return values;
    }


    public void browse(ActionEvent actionEvent) {
        DirectoryChooser directoryChooser = new DirectoryChooser();

        Node node = (Node) actionEvent.getSource();
        Stage currentStage = (Stage) node.getScene().getWindow();

        File file = directoryChooser.showDialog(currentStage);
        if (file != null)
            renderFolder.setText(file.getAbsolutePath());
    }

    public void generateFiles(ActionEvent actionEvent) {

        Map<String, Double> doubleValues = getDoubleValues();
        Map<String, Integer> intValues = getIntValues();
        IKG = new OverKeysGenerator();
        IKG.setConstantsFromLists(doubleValues,intValues);
        IKG.getUserInputAndDeriveConstants();



        if (!renderFolder.getText().trim().equals("")) {

            IKG.setRenderPath(renderFolder.getText());
            IKG.setConstantsFromLists(doubleValues,intValues);
            IKG.getUserInputAndDeriveConstants();
            IKG.generateFiles();
            JOptionPane.showMessageDialog(null, ".scad files are generated!!", "Info", JOptionPane.INFORMATION_MESSAGE);

        } else {
            JOptionPane.showMessageDialog(null, "Select render folder!!", "Warning", JOptionPane.WARNING_MESSAGE);
        }

    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {

        BindSliderTextField(shiftXValue, shiftX);

        BindSliderTextField(shiftYValue, shiftY);
    }

    private void BindSliderTextField(TextField shiftYValue, Slider shiftY) {
        shiftYValue.textProperty().bindBidirectional(shiftY.valueProperty(), new StringConverter<Number>()
        {
            @Override
            public String toString(Number t)
            {
                return t.toString();
            }

            @Override
            public Number fromString(String string)
            {
                if (string.equals("")) return 0.0;
                return Double.parseDouble(string);
            }

        });
    }

    public void browseOpenscad(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();

        Node node = (Node) actionEvent.getSource();
        Stage currentStage = (Stage) node.getScene().getWindow();

        fileChooser.setTitle("Where is OpenScad.exe?");
        File file = fileChooser.showOpenDialog(currentStage);
        if (file != null)
            openscadPath.setText(file.getAbsolutePath());
    }

    public void openOutputDirectory(ActionEvent actionEvent) throws IOException {
        Runtime.getRuntime().exec("explorer.exe /open," + renderFolder.getText());
    }

    public void showLog(ActionEvent actionEvent) throws IOException {
        File file = new File("log.txt");
        Runtime.getRuntime().exec("explorer.exe /select, log.txt");
    }

    public void renderFiles(ActionEvent actionEvent) {
        String pathToRender = renderFolder.getText().trim();
        if (!renderFolder.getText().trim().equals("") && !openscadPath.getText().trim().equals("")) {
            String pathToExe = openscadPath.getText().trim();

            boolean keep = keepScad.isSelected();
            File openScad = new File(pathToExe);
            if (openScad.exists()) {
                File[] scadFilesList = new File(pathToRender).listFiles(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String name) {
                        return name.toLowerCase().endsWith(".scad");
                    }
                });

                final GenerateStlService generateStlService = new GenerateStlService(pathToExe, scadFilesList, keep);

                Region veil = new Region();
                veil.setPrefSize(400, 400);
                veil.setStyle("-fx-background-color: rgba(211,211,211,0.50)");
                ProgressIndicator p = new ProgressIndicator();
                p.setMaxSize(150, 150);
                p.progressProperty().bind(generateStlService.progressProperty());
                veil.visibleProperty().bind(generateStlService.runningProperty());
                p.visibleProperty().bind(generateStlService.runningProperty());
                message.textProperty().bind(generateStlService.valueProperty());
                stackPane.getChildren().addAll(veil, p);
                generateStlService.start();
            } else {
                JOptionPane.showMessageDialog(null, "openscad.exe does not exist!!", "Warning", JOptionPane.WARNING_MESSAGE);
            }
        } else {
            JOptionPane.showMessageDialog(null, "Select render folder and OpenScad path!!", "Warning", JOptionPane.WARNING_MESSAGE);
        }
    }


}
