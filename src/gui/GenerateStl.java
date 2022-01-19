package gui;

import javafx.concurrent.Task;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class GenerateStl extends Task<String> {

    private final String pathToExe;
    private final File[] filesList;
    private final boolean keep;

    public static  String STL_FOLDER="stl_files_";

    public GenerateStl(String pathToExe, File[] filesList,boolean keep) {
        this.pathToExe = pathToExe;
        this.filesList = filesList;
        this.keep=keep;
    }

    @Override
    protected String call() {
        Runtime rt = Runtime.getRuntime();
        int total=filesList.length;
        String renderPath=filesList[0].getParent();
        int i=1;
        for (File file : this.filesList) {

            try {
                if (!file.getName().contains("together")) { // exclude together.scad to be rendered
                    String stlname=file.getAbsolutePath().toLowerCase().replace(".scad",".stl");

                    Process p = rt.exec(pathToExe + " --export-format binstl -o " +
                            stlname+ " " + file.getAbsolutePath());
                    p.waitFor();


                    updateProgress(i, total);
                }
                i++;
            } catch (IOException | InterruptedException e) {
                return "Error generating files "+e.getMessage();
            }
        }

        Date date=new Date();
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMddhhmmss");
        String filepostfix = dateFormat.format(date);
        File newFolder=new File(renderPath+"\\"+filepostfix);
        newFolder.mkdir();

        if (!keep) {
            for (File file : filesList) {
                file.delete();
            }
        }

        File[] fileList= new File(renderPath).listFiles();
        for (File file : fileList) {
            file.renameTo(new File(newFolder.getAbsolutePath()+"\\"+file.getName()));
        }
        return "Files are generated and moved to "+newFolder.getAbsolutePath();
    }
}
