package gui;

import javafx.concurrent.Task;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class GenerateStl extends Task<String> {

    private final String pathToExe;
    private final ArrayList<File> filesList;
    private final boolean keep;

    public GenerateStl(String pathToExe, ArrayList<File> filesList,boolean keep) {
        this.pathToExe = pathToExe;
        this.filesList = filesList;
        this.keep=keep;
    }

    @Override
    protected String call() {
        Runtime rt = Runtime.getRuntime();
        int total=filesList.size();
        int i=1;
        for (File file : this.filesList) {

            try {
                if (!file.getName().contains("together")) { // exclude together.scad to be rendered
                    Process p = rt.exec(pathToExe + " --export-format binstl -o " +
                            file.getAbsolutePath().toLowerCase().replace(".scad", ".stl") + " " + file.getAbsolutePath());
                    p.waitFor();


                    updateProgress(i, total);
                }
                i++;
            } catch (IOException | InterruptedException e) {
                return "Error generating files "+e.getMessage();
            }
        }
        if (!keep) {
            for (File file : filesList) {
                file.delete();
            }
        }
        return "Files are generated";
    }
}
