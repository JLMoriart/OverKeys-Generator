package gui;

import javafx.concurrent.Task;

import java.io.File;
import java.io.IOException;

public class GenerateStl extends Task<String> {

    private String pathToExe;
    private File[] filesList;
    private boolean keep;

    public GenerateStl(String pathToExe, File[] filesList,boolean keep) {
        this.pathToExe = pathToExe;
        this.filesList = filesList;
        this.keep=keep;
    }

    @Override
    protected String call() throws Exception {
        Runtime rt = Runtime.getRuntime();
        int total=filesList.length;
        int i=1;
        for (File file : this.filesList) {
            try {
                Process p = rt.exec(pathToExe + " --export-format binstl -o " +
                        file.getAbsolutePath().toLowerCase().replace(".scad", ".stl") + " " + file.getAbsolutePath());
                int x = p.waitFor();


                updateProgress(i,total);
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
