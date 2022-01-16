package gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import overkeys.generator.OverKeysGenerator;

import javax.swing.*;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Scanner;


public class GUIController implements Initializable{

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
    private TextField keytopXGap;

    @FXML
    private TextField keytopYGap;

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

    public static final String SEPARATOR="::";

    @FXML
    void loadPreset(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("TXT files (*.txt)", "*.txt");
        fileChooser.getExtensionFilters().add(extFilter);

        Node node = (Node) event.getSource();
        Stage currentStage = (Stage) node.getScene().getWindow();

        File file = fileChooser.showOpenDialog(currentStage);
        if (file !=null){
            loadScreenData(file);
        }


    }

    void loadScreenData(File file){
        Map<String,String> values=readFromFile(file);
        for(String key:values.keySet()) {
            TextField field=(TextField) mainPane.getScene().lookup("#"+key);
            field.setText(values.get(key));
        }
        try {
            Files.copy(file.toPath(), (new File("./resources/config.txt").toPath()), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void savePreset(ActionEvent event) {
        Map<String, Double> doubleValues = new HashMap<>();
        Map<String, Integer> intValues = new HashMap<>();
        try {
            doubleValues.put("octaveWidth", Double.parseDouble(octaveWidth.getText()));
            doubleValues.put("blackKeyHeight", Double.parseDouble(blackKeyHeight.getText()));
            doubleValues.put("blackKeyLength", Double.parseDouble(blackKeyLength.getText()));
            doubleValues.put("whiteKeyLength", Double.parseDouble(whiteKeyLength.getText()));
            doubleValues.put("keytopHeightDiff", Double.parseDouble(keytopHeightDiff.getText()));
            doubleValues.put("metalRoundRadius", Double.parseDouble(metalRoundRadius.getText()));

            doubleValues.put("stalkFitXTolerance", Double.parseDouble(stalkFitXTolerance.getText()));
            doubleValues.put("stalkFitYTolerance", Double.parseDouble(stalkFitYTolerance.getText()));
            doubleValues.put("keytopXGap", Double.parseDouble(keytopXGap.getText()));
            doubleValues.put("keytopYGap", Double.parseDouble(keytopYGap.getText()));
            doubleValues.put("underkeyGap", Double.parseDouble(underkeyGap.getText()));

            intValues.put("halfStepsToPeriod",Integer.parseInt(halfStepsToPeriod.getText()));
            intValues.put("halfStepsToGenerator",Integer.parseInt(halfStepsToGenerator.getText()));
            intValues.put("halfStepsToLargeMOSStep",Integer.parseInt(halfStepsToLargeMOSStep.getText()));
            intValues.put("gamut",Integer.parseInt(gamut.getText()));
            intValues.put("range",Integer.parseInt(range.getText()));
            intValues.put("startingKey",Integer.parseInt(startingKey.getText()));

            FileChooser fileChooser = new FileChooser();
            FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("TXT files (*.txt)", "*.txt");
            fileChooser.getExtensionFilters().add(extFilter);

            Node node = (Node) event.getSource();
            Stage currentStage = (Stage) node.getScene().getWindow();

            File file = fileChooser.showSaveDialog(currentStage);

            if (file !=null){
                saveToFile(doubleValues,intValues,file);
            }

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(null, e, "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void saveToFile(Map<String, Double> doubleValues, Map<String, Integer> intValues, File file) {
        try {
            PrintWriter writer;
            writer = new PrintWriter(file);
            for(String key:doubleValues.keySet())
                writer.println(key+SEPARATOR+doubleValues.get(key));
            for(String key:intValues.keySet())
                writer.println(key+SEPARATOR+intValues.get(key));
            writer.close();
            Files.copy(file.toPath(), (new File("./resources/config.txt").toPath()), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(null, ex, "Error", JOptionPane.ERROR_MESSAGE);
        }

    }

    private Map<String, String> readFromFile(File file) {
        Map<String,String> values=new HashMap<>();

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
        if (file!=null)
            renderFolder.setText(file.getAbsolutePath());
    }

    public void renderFiles(ActionEvent actionEvent) {
        OverKeysGenerator IKG = new OverKeysGenerator();

        if (!renderFolder.getText().trim().equals("") || !openscadPath.getText().trim().equals("")){
            String pathToExe=openscadPath.getText().trim();
            String pathToRender=renderFolder.getText().trim();
            File openScad=new File(pathToExe);

            boolean keep=keepScad.isSelected();

            if (openScad.exists()) {
                IKG.setRenderPath(renderFolder.getText());
                if (IKG.setConstantsFromConfig())
                    IKG.setDefaultConstants();
                IKG.getUserInputAndDeriveConstants();
                IKG.generateFiles();

                File[] filesList=new File(pathToRender).listFiles(new FilenameFilter() {
                    @Override
                    public boolean accept(File dir, String name) {
                        return name.toLowerCase().endsWith(".scad");
                    }
                });

                final GenerateStlService generateStlService=new GenerateStlService(pathToExe,filesList,keep);

                Region veil=new Region();
                veil.setPrefSize(400,400);
                veil.setStyle("-fx-background-color: rgba(211,211,211,0.50)");
                ProgressIndicator p=new ProgressIndicator();
                p.setMaxSize(150,150);
                p.progressProperty().bind(generateStlService.progressProperty());
                veil.visibleProperty().bind(generateStlService.runningProperty());
                p.visibleProperty().bind(generateStlService.runningProperty());
                message.textProperty().bind(generateStlService.valueProperty());
                stackPane.getChildren().addAll(veil,p);
                generateStlService.start();

            }
            else {
                JOptionPane.showMessageDialog(null,"openscad.exe does not exist!!" , "Warning", JOptionPane.WARNING_MESSAGE);
            }
        }
        else{
            JOptionPane.showMessageDialog(null,"Select render folder and OpenScad path!!" , "Warning", JOptionPane.WARNING_MESSAGE);
        }

    }

    private void generateStlFiles(File[] filesList,String pathToExe) {
        Runtime rt = Runtime.getRuntime();

        for (File file:filesList) {
            message.setText("Generating "+file.getAbsolutePath().toLowerCase().replace(".scad",".stl"));
            try {
                Process p = rt.exec(pathToExe + " --export-format binstl -o " +
                        file.getAbsolutePath().toLowerCase().replace(".scad",".stl")+ " "+file.getAbsolutePath());
                int x = p.waitFor();

            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    public void browseOpenscad(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();

        Node node = (Node) actionEvent.getSource();
        Stage currentStage = (Stage) node.getScene().getWindow();

        fileChooser.setTitle("Where is OpenScad.exe?");
        File file = fileChooser.showOpenDialog(currentStage);
        if (file!=null)
            openscadPath.setText(file.getAbsolutePath());
    }

    public void openOpuputDirectory(ActionEvent actionEvent) throws IOException {
        Runtime.getRuntime().exec("explorer.exe /open," + renderFolder.getText());
    }

    public void showLog(ActionEvent actionEvent) throws IOException {
        File file=new File("log.txt");
        Runtime.getRuntime().exec("explorer.exe /select, log.txt" );
    }
}
