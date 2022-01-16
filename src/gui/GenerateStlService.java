package gui;

import javafx.concurrent.Service;
import javafx.concurrent.Task;

import java.io.File;

public class GenerateStlService extends Service<String> {

    private String pathToExe;
    private File[] filesList;
    boolean keep;

    public GenerateStlService(String pathToExe, File[] filesList, boolean keep) {
        this.pathToExe=pathToExe;
        this.filesList=filesList;
        this.keep=keep;
    }

    @Override
    protected Task<String> createTask() {
        return new GenerateStl(pathToExe,filesList,keep);
    }
}
