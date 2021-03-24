package isomorphic.keyboard.generator;

import java.util.*;
import java.io.*;

public class IsomorphicKeyboardGeneratorWithVariablesInOpenSCAD {

    Scanner scan = new Scanner(System.in);
    PrintWriter pw, pwKeyTop, pwClamp, pwValues, togetherPrint;
    double metalRoundRadius, octaveWidth, periodWidth, underKeyWidth, blackKeyHeight, whiteKeyLength, whiteKeyLengthPreShortening, edgeRadius = 3, keytopHeight =10, tolerance=0.05, nutHoleScale,//measurements 
            genh, genhPreShortening, overhead, keyTopSide1, keyTopSide2, shiftX,shiftY,slantCutWidth,clampDepth,//derived stuff
            theta, q, r, a, aPreShortening, b, c, d, dPreShortening, z,//bunch of triangle stuff
            generator, holeScaleX, holeScaleY, stalkScaleX, stalkScaleY, keytopHeightDifference;
    double metalRoundRadiusTolerance=0.1, underKeyGap;
    int periodSteps, generatorSteps, desiredGamut, startingKey, range, genForLargeStep, genForSmallStep, stepsForLarge, stepsForSmall, genForStep1, genForStep1b;
    boolean isKeytop, verticalFlip, neededAbsoluteValue=false, shiftXTrue, roughRender, keytopsInTogether, keytopsInSingleKeyFiles;

    ArrayList<Integer> mosSizes = new ArrayList<>();
    ArrayList<mosScale> mosTracker = new ArrayList<>();

    public static void main(String[] args) {
        IsomorphicKeyboardGeneratorWithVariablesInOpenSCAD IKG = new IsomorphicKeyboardGeneratorWithVariablesInOpenSCAD();
        IKG.getUserInputAndDeriveConstants();
        IKG.generateFiles();
    }

    public void generateFiles() {
        int currentPianoKey, currentGenerator;

        File together = new File("C:\\Users\\FlacidRichard\\Desktop\\OPENSCAD_DUMP\\together.scad");//together is the big collection of keys and keytops that will show if everything worked correctly
        System.out.println(together.getParentFile().mkdirs() + "<-------This is whether the makedirs succeeded or , but it never succeeds, and always succeeds");
        try {
            togetherPrint = new PrintWriter(together, "UTF-8");
        } catch (Exception e) {
            System.out.println(e);
        }
        togetherPrint.println("include<values.scad>;");

        File values = new File("C:\\Users\\FlacidRichard\\Desktop\\OPENSCAD_DUMP\\values.scad");
        values.getParentFile().mkdirs();
        try {
            pwValues = new PrintWriter(values, "UTF-8");
        } catch (Exception e) {
            System.out.println(e);
        }

        createValuesFile();
        pwValues.close();

        for (int i = 0; i < range; i++) {//iterate keys until range is reached.
            //could check for duplicate files, NOT FOR NOWWWW
            currentPianoKey = (i + startingKey) % 12;//started on starting key, have moved i times, keys 12 steps apart are same underlying note
            if (verticalFlip) {
                currentGenerator = (i * genForStep1b) % periodSteps;
            } else {
                currentGenerator = (i * genForStep1) % periodSteps;
            }
            //modulo periodSteps because they share the same topside of the key and I want to start at the lowest one so that I include it, because I only move upwards later one, I think
            int keytopsNeeded = (desiredGamut - currentGenerator - 1) / periodSteps + 1;//-1 then plus one because if desiredGamut-currentGenerator)=periodSteps, I want it to return 1? 

            try {
                File file2 = new File("C:\\Users\\FlacidRichard\\Desktop\\OPENSCAD_DUMP\\" + i + "_" + currentGenerator + ".scad");
                file2.getParentFile().mkdirs();
                pw = new PrintWriter(file2, "UTF-8");

                togetherPrint.println("use<" + i + "_" + currentGenerator + ".scad>;");

                togetherPrint.println("translate([-" + (i * octaveWidth / 12) + ",0,0");

                togetherPrint.println("])");

                togetherPrint.println(i + "_" + currentGenerator + "("+keytopsInTogether+");");

                pw.println("use<keytop.scad>");
                pw.println("include<values.scad>");
                pw.println(i + "_" + currentGenerator + "();");
                pw.println("module " + i + "_" + currentGenerator + "(keytops="+keytopsInSingleKeyFiles+"){");

                createMainBase(currentGenerator, keytopsNeeded, i, currentPianoKey);
                createKeyStalks(currentGenerator, keytopsNeeded, currentPianoKey);
                pw.println("}");

                thinCuts(currentGenerator);
                
                pw.println("}\n}");
                
                pw.close();
            } catch (IOException e) {
                System.out.println(e);
            }
        }
        createKeytop();//after crazy for loop for bases, make keytops file ONCE OH YEAH ONCE
        createClamp();
        togetherPrint.close();
    }
    
    public void createClamp(){
        try {
            File file = new File("C:\\Users\\FlacidRichard\\Desktop\\OPENSCAD_DUMP\\clamp.scad");
            file.getParentFile().mkdirs();
            pwClamp = new PrintWriter(file, "UTF-8");
            
            pwClamp.println("include<values.scad>;");
            pwClamp.println("s=5.57*nutHoleScale;\n" +//m3 measurements
                            "e=6.26*nutHoleScale;\n" +
                            "p0 = [s/2,0];\n" +//hexagon points
                            "p1 = [s, tan(30)*s/2];\n" +
                            "p2 = [s,e-tan(30)*s/2];\n" +
                            "p3 = [s/2,e];\n" +
                            "p4 = [0,e-tan(30)*s/2];\n" +
                            "p5 = [0, tan(30)*s/2];" +
                            "points = [p0, p1, p2, p3, p4, p5];");
            //pwClamp.println("include<polygon.scad>;");
            pwClamp.println("difference(){");
            pwClamp.println("union(){");
            pwClamp.println("translate([-metalRoundRadius-2, -2*metalRoundRadius, -0.5*underKeyWidth])");
            pwClamp.println("cube([metalRoundRadius*2+4, clampDepth, underKeyWidth]);");//rectangular prism body
            pwClamp.println("guard();");
            pwClamp.println("mirror([1,0,0]){");
            pwClamp.println("guard();}");
            pwClamp.println("}");
            
            pwClamp.println("cylinder(underKeyWidth+0.01, metalRoundRadius+4*metalRoundRadiusTolerance, metalRoundRadius+4*metalRoundRadiusTolerance, true);");
            
            pwClamp.println("translate([0, metalRoundRadius*3,0])");//bend Cut
            pwClamp.println("cube([metalRoundRadius*0.75, clampDepth-(metalRoundRadius+2), underKeyWidth+1], true);");
                            
            pwClamp.println("translate([-metalRoundRadius*2,metalRoundRadius*3,0])"//through hole and top triange
                    + "\nrotate([0,90,0])"
                    + "\ncylinder((metalRoundRadius+2)*5,2*nutHoleScale,2*nutHoleScale, true);");
            pwClamp.println("translate([-metalRoundRadius*2,metalRoundRadius*3,0])"
                    + "\nrotate([45,0,0])"
                    + "\ncube([(metalRoundRadius+2)*5,2*nutHoleScale,2*nutHoleScale]);");
            
            pwClamp.println("}");//difference end
            
            pwClamp.println("module guard(){");
            pwClamp.println("difference(){");
            pwClamp.println("translate([+metalRoundRadius+2, -2*metalRoundRadius, -0.5*underKeyWidth])");
            pwClamp.println("cube([metalRoundRadius*2+4, clampDepth, underKeyWidth]);");
            pwClamp.println("translate([metalRoundRadius+2+tolerance, -2*metalRoundRadius+clampDepth*0.1, -0.5*underKeyWidth+underKeyWidth*0.2+tolerance])");
            pwClamp.println("scale([1,0.8,0.8]){");
            pwClamp.println("cube([metalRoundRadius*2+4, clampDepth, underKeyWidth]);");
            pwClamp.println("}");
            pwClamp.println("translate([metalRoundRadius+2+tolerance,-metalRoundRadius*0.25,-underKeyWidth]){");
            pwClamp.println("cube([metalRoundRadius*2+4,metalRoundRadius*0.5,underKeyWidth]);}");
            pwClamp.println("}");//difference end
            pwClamp.println("}");
            pwClamp.close();
            
        } catch (IOException e) {
            System.out.println(e);
        }
    }
    
    public void thinCuts(int currentGenerator) {
        pw.println("//Thin Cuts:");
        pw.println("translate([0,0,-tolerance]){");
        pw.println("linear_extrude(height=blackKeyHeight+metalRoundRadius+sqrt(metalRoundRadius*metalRoundRadius*2)+4+2*tolerance)");
        pw.println("polygon(points=[[-0.1,10],[-0.1,length+0.1],[underKeyWidth/3,length+0.1]]);");
        pw.println("linear_extrude(height=blackKeyHeight+metalRoundRadius+sqrt(metalRoundRadius*metalRoundRadius*2)+4+2*tolerance)");
        pw.println("polygon(points=[[underKeyWidth+0.1,10],[underKeyWidth+0.1,length+0.1],[underKeyWidth/3*2,length+0.1]]);");
        pw.println("\n}");

        pw.println("anglePoints=[\n"
                + "[-underKeyWidth/3,10,blackKeyHeight+metalRoundRadius+sqrt(metalRoundRadius*metalRoundRadius*2)+4],\n"
                + "[0,10,blackKeyHeight+metalRoundRadius+sqrt(metalRoundRadius*metalRoundRadius*2)+4],\n"
                + "[underKeyWidth/3,length+0.1,blackKeyHeight+metalRoundRadius+sqrt(metalRoundRadius*metalRoundRadius*2)+4],\n"
                + "[0,length+0.1,blackKeyHeight+metalRoundRadius+sqrt(metalRoundRadius*metalRoundRadius*2)+4],\n"
                + "[-slantCutWidth-underKeyWidth/3,10,blackKeyHeight+metalRoundRadius+sqrt(metalRoundRadius*metalRoundRadius*2)+4+slantCutWidth],\n"
                + "[-slantCutWidth,10,blackKeyHeight+metalRoundRadius+sqrt(metalRoundRadius*metalRoundRadius*2)+4+slantCutWidth],\n"
                + "[-slantCutWidth+underKeyWidth/3,length+0.1,blackKeyHeight+metalRoundRadius+sqrt(metalRoundRadius*metalRoundRadius*2)+4+slantCutWidth],\n"
                + "[-slantCutWidth,length+0.1,blackKeyHeight+metalRoundRadius+sqrt(metalRoundRadius*metalRoundRadius*2)+4+slantCutWidth]];");

        pw.println("angleFaces=["
                + "[0,1,2,3],"
                + "[0,4,5,1],"
                + "[0,3,7,4],"
                + "[3,2,6,7],"
                + "[1,5,6,2],"
                + "[7,6,5,4]"
                + "];");

        pw.println("translate([0,0,0])polyhedron(anglePoints,angleFaces);");

        pw.println("translate([underKeyWidth,0,0])mirror([1,0,0])polyhedron(anglePoints,angleFaces);");


    }

    public void createKeytop() {
        try {
            File file = new File("C:\\Users\\FlacidRichard\\Desktop\\OPENSCAD_DUMP\\keytop.scad");
            file.getParentFile().mkdirs();
            pwKeyTop = new PrintWriter(file, "UTF-8");

            pwKeyTop.println("include<values.scad>;");
            pwKeyTop.println("keytop();");
            pwKeyTop.println("");

            pwKeyTop.println("module keytopShape(){");//
            pwKeyTop.println("polyhedron(Points,Faces);");
            pwKeyTop.println("}");//

            pwKeyTop.println("module keytop(){");//make it a module so that together.scad can use it
            pwKeyTop.println("difference(){");
            pwKeyTop.println("keytopShape();");//make key

            pwKeyTop.println("translate([1/2*(b+c)-(1/2*(b+c)*holeScaleX),1/2*(d+a)-(1/2*(d+a)*holeScaleY),-0.1])");
            pwKeyTop.println("scale([holeScaleX,holeScaleY,0.5])");
            pwKeyTop.println("keytopShape();");
            
            //edges rounded by subtracting module below
            if(shiftXTrue)
            {
                pwKeyTop.print("edge(11,6,1,");
                if(Math.atan(d/c)>0) pwKeyTop.print("-");
                pwKeyTop.println("1);");

                pwKeyTop.print("edge(7,6,1,");
                if(Math.atan(-a/(b-2*shiftX))>0) pwKeyTop.print("-");
                pwKeyTop.println("1);");

                pwKeyTop.print("edge(7,8,1,");
                pwKeyTop.println("1);");

                pwKeyTop.print("edge(8,9,1,");
                if(Math.atan(d/c)<0) pwKeyTop.print("-");
                pwKeyTop.println("1);");

                pwKeyTop.print("edge(9,10,1,");
                if(Math.atan(-a/(b-2*shiftX))<0) pwKeyTop.print("-");
                pwKeyTop.println("1);");

                pwKeyTop.print("edge(10,11,1,");
                pwKeyTop.println("-1);");
            }
            else
            {
                pwKeyTop.print("edge(11,6,1,");
                if(Math.atan(d/c)>0) pwKeyTop.print("-");
                pwKeyTop.println("1);");

                pwKeyTop.print("edge(7,6,1,");
                pwKeyTop.println("-1);");

                pwKeyTop.print("edge(7,8,1,");
                pwKeyTop.println("1);");

                pwKeyTop.print("edge(8,9,1,");
                if(Math.atan(d/c)<0) pwKeyTop.print("-");
                pwKeyTop.println("1);");

                pwKeyTop.print("edge(9,10,1,");
                pwKeyTop.println("1);");

                pwKeyTop.print("edge(10,11,1,");
                pwKeyTop.println("-1);");
            }
            
            
            pwKeyTop.println("}");

            pwKeyTop.println("module edge (point1,point2,d1,d2){");//annoying combination of cylinders and cubes to round edges
            pwKeyTop.println("difference(){");
            pwKeyTop.println("translate([(Points[point1][0]+Points[point2][0])/2,(Points[point1][1]+Points[point2][1])/2,(Points[point1][2]+Points[point2][2])/2])");
            pwKeyTop.println("rotate([0,90,atan((Points[point2][1]-Points[point1][1])/(Points[point2][0]-Points[point1][0]))])");
            pwKeyTop.println("rotate([0,0,45])");
            pwKeyTop.println("cube([pow(2*(edgeRadius*edgeRadius),1/2),pow(2*(edgeRadius*edgeRadius),1/2),pow(pow(Points[point1][0]-Points[point2][0],2)+pow(Points[point1][1]-Points[point2][1],2),0.5)*2],true);");
            
            pwKeyTop.println("translate([(Points[point1][0]+Points[point2][0])/2,(Points[point1][1]+Points[point2][1])/2,(Points[point1][2]+Points[point2][2])/2])");
            pwKeyTop.println("rotate([0,90,atan((Points[point2][1]-Points[point1][1])/(Points[point2][0]-Points[point1][0]))])");
            pwKeyTop.println("translate([d1*edgeRadius,d2*edgeRadius,0])");
            pwKeyTop.println("cylinder(pow(pow(Points[point1][0]-Points[point2][0],2)+pow(Points[point1][1]-Points[point2][1],2),0.5)*2,edgeRadius+tolerance,edgeRadius+tolerance, true);");//don't need to fill in much here since it's already got the points
            pwKeyTop.println("}");
            pwKeyTop.println("}");
            pwKeyTop.println("}");//DIFFERENCE END
            pwKeyTop.close();
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    public boolean isWhiteKey(int currentPianoKey) {
        return currentPianoKey == 0
                ||//0 is A, if key is a white key...
                currentPianoKey == 2
                || currentPianoKey == 3
                || currentPianoKey == 5
                || currentPianoKey == 7
                || currentPianoKey == 8
                || currentPianoKey == 10;
    }

    public void createKeyStalks(int currentGeneratorIn, int keytopsNeeded, int currentPianoKeyIn) {
        pw.println("//Key Stalks:");
        for (int j = 0; j < keytopsNeeded; j++) {//j changes generator to enharmonically eqauivalent values by being increasing by periodSteps until greater than gamut
            if (isWhiteKey(currentPianoKeyIn)) {
                pw.println("translate([underKeyWidth/2-(b+c)*stalkScaleX/2,genh*" + currentGeneratorIn + "+overhead,0.75*(blackKeyHeight+metalRoundRadius+sqrt(metalRoundRadius*metalRoundRadius*2)+4)]){");
            } else {
                pw.println("translate([underKeyWidth/2-(b+c)*stalkScaleX/2,genh*" + currentGeneratorIn + "+overhead,blackKeyHeight+0.75*(metalRoundRadius+sqrt(metalRoundRadius*metalRoundRadius*2)+4)]){");
            }
            if (isWhiteKey(currentPianoKeyIn)) {
                pw.println("linear_extrude(height=(keytopHeight*0.75+keytopHeightDifference-(" + ((double) currentGeneratorIn / (double) desiredGamut) * keytopHeightDifference + ")+0.25*(blackKeyHeight+metalRoundRadius+sqrt(metalRoundRadius*metalRoundRadius*2)+4))){");//*0.75 to leave 0.25 extra to be taken up by support angle things
            } else {
                pw.println("linear_extrude(height=(keytopHeight*0.75+keytopHeightDifference-(" + ((double) currentGeneratorIn / (double) desiredGamut) * keytopHeightDifference + ")+0.25*(metalRoundRadius+sqrt(metalRoundRadius*metalRoundRadius*2)+4))){");
            }

            pw.println("scale([stalkScaleX,stalkScaleY])");
            pw.println("polygon(points=[");
            pw.println("[Points[0][0],Points[0][1]],[Points[1][0],Points[1][1]],[Points[2][0],Points[2][1]],[Points[3][0],Points[3][1]],[Points[4][0],Points[4][1]],[Points[5][0],Points[5][1]],[Points[6][0],Points[6][1]]");
            pw.println("]);");
            pw.println("}");

            pw.println("if(keytops)");
            pw.println("translate([-0.25*(b+c),(-0.25*(a+d)),60-" + ((double) currentGeneratorIn / (double) desiredGamut) * keytopHeightDifference);
            if(!isWhiteKey(currentPianoKeyIn)){
                pw.println("-((blackKeyHeight+0.75*(metalRoundRadius+sqrt(metalRoundRadius*metalRoundRadius*2)+4))-0.75*(blackKeyHeight+metalRoundRadius+sqrt(metalRoundRadius*metalRoundRadius*2)+4))");//if black key, drop down by the difference bewteen the different stalks' starting heights
            }
            pw.println("])");//
            pw.println("keytop();");
            pw.println("}");
            currentGeneratorIn += periodSteps;
        }
    }

    public void createValuesFile() {
        if(roughRender){
            pwValues.println("$fs=1.5;");
            pwValues.println("$fa=15;");
        }else
        {
            pwValues.println("$fs=0.3675;");
            pwValues.println("$fa=5;");
        }
        pwValues.println("//Constants:");
        pwValues.println("edgeRadius=" + edgeRadius + ";");
        pwValues.println("underKeyWidth=" + underKeyWidth + ";");
        pwValues.println("blackKeyHeight=" + blackKeyHeight + ";");
        pwValues.println("genh=" + genh + ";");
        pwValues.println("a=" + a + ";");
        pwValues.println("b=" + b + ";");
        pwValues.println("c=" + c + ";");
        pwValues.println("d=" + d + ";");
        pwValues.println("overhead=" + overhead + ";");
        pwValues.println("desiredGamut=" + desiredGamut + ";");
        pwValues.println("shiftX=" + shiftX + ";");
        pwValues.println("holeScaleX=" + holeScaleX + ";");
        pwValues.println("holeScaleY=" + holeScaleY + ";");
        pwValues.println("stalkScaleX=" + stalkScaleX + ";");
        pwValues.println("stalkScaleY=" + stalkScaleY + ";");
        pwValues.println("metalRoundRadius=" + metalRoundRadius + ";");
        pwValues.println("metalRoundRadiusTolerance=" + metalRoundRadiusTolerance + ";");
        pwValues.println("keytopHeight=" + keytopHeight + ";");
        pwValues.println("tolerance=" + tolerance + ";");
        pwValues.println("slantCutWidth=" + slantCutWidth + ";");
        pwValues.println("nutHoleScale=" + nutHoleScale + ";");
        pwValues.println("clampDepth=" + clampDepth + ";");
        pwValues.println("shiftY =" + shiftY + ";");
        pwValues.println("keytopHeightDifference =" + keytopHeightDifference + ";");
        
        if(shiftXTrue)
        {
            pwValues.println("");
            pwValues.println("Points = [");//shiftX widens the tips and squishes the middle to make it hexagonal and tall
            pwValues.println("[shiftX,a,0],//0");
            pwValues.println("[(b-shiftX),0,0],//1");
            pwValues.println("[(b+shiftX),0,0],//2");
            pwValues.println("[(b+c-shiftX),d,0],//3");
            pwValues.println("[(c+shiftX),(a+d),0],//4");
            pwValues.println("[(c-shiftX),(a+d),0],//5");

            pwValues.println("[shiftX,a,10],//6");
            pwValues.println("[(b-shiftX),0,keytopHeight],//7");
            pwValues.println("[(b+shiftX),0,keytopHeight],//8");
            pwValues.println("[(b+c-shiftX),d,keytopHeight],//9");
            pwValues.println("[(c+shiftX),(a+d),keytopHeight],//10");
            pwValues.println("[(c-shiftX),(a+d),keytopHeight],//11");
            pwValues.println("];");
            
            pwValues.println("Faces = [");
            pwValues.println("[0,1,2,3,4,5],");
            pwValues.println("[1,0,6,7],");
            pwValues.println("[2,1,7,8],");
            pwValues.println("[3,2,8,9],");
            pwValues.println("[4,3,9,10],");
            pwValues.println("[5,4,10,11],");
            pwValues.println("[0,5,11,6],");
            pwValues.println("[11,10,9,8,7,6] ");
            pwValues.println("];");//faces for polygon in counter clockwise points looking from the inside of the key
            pwValues.println("");
        }
        else
        {
            pwValues.println("");
            pwValues.println("Points = [");//shiftY stretches the side points vertically hexagonal and short
            pwValues.println("[0,a+shiftY,0],//0");
            pwValues.println("[0,a-shiftY,0],//1");
            pwValues.println("[b,shiftY,0],//2");
            pwValues.println("[(b+c),d-shiftY,0],//3");
            pwValues.println("[(b+c),d+shiftY,0],//4");
            pwValues.println("[c,(a+d)-shiftY,0],//5");
            
            pwValues.println("[0,a+shiftY,keytopHeight],//6");
            pwValues.println("[0,a-shiftY,keytopHeight],//7");
            pwValues.println("[b,shiftY,keytopHeight],//8");
            pwValues.println("[(b+c),d-shiftY,keytopHeight],//9");
            pwValues.println("[(b+c),d+shiftY,keytopHeight],//10");
            pwValues.println("[c,(a+d)-shiftY,keytopHeight],//11");
            pwValues.println("];");
            
            pwValues.println("Faces = [");
            pwValues.println("[0,1,2,3,4,5],");
            pwValues.println("[1,0,6,7],");
            pwValues.println("[2,1,7,8],");
            pwValues.println("[3,2,8,9],");
            pwValues.println("[4,3,9,10],");
            pwValues.println("[5,4,10,11],");
            pwValues.println("[0,5,11,6],");
            pwValues.println("[11,10,9,8,7,6] ");
            pwValues.println("];");//faces for polygon in counter clockwise points looking from the inside of the key
            pwValues.println("");
        }
        

    }

    public void createMainBase(int currentGenerator, int keytopsNeeded, int i, int currentPianoKey) {
        double length;//length of main base
        length = genh * (currentGenerator + (keytopsNeeded - 1) * periodSteps) + overhead + stalkScaleY * (a + d);
        System.out.println("length: " +length);
        pw.println("length=" + length + ";");
        pw.println("difference(){"
                + "\nunion(){");
        pw.println("");
        pw.println("//Main Base:");

        if (!isWhiteKey(currentPianoKey)) {
            pw.println("translate([0,0,blackKeyHeight])");
        }
        //raise up black key for easier combination in togetherPrint

        pw.println("difference(){");
        pw.println("union(){");

        if (isWhiteKey(currentPianoKey)) {//first little chunk to hold the metal round, independent of the rest of the main base
            pw.println("cube([underKeyWidth,metalRoundRadius*2+4,blackKeyHeight+metalRoundRadius+sqrt(metalRoundRadius*metalRoundRadius*2)+4],false);");
        } else {
            pw.println("cube([underKeyWidth,metalRoundRadius*2+4,metalRoundRadius+sqrt(metalRoundRadius*metalRoundRadius*2)+4],false);");
        }

        pw.println("translate([0,metalRoundRadius*2+4,0])");
        
        if (isWhiteKey(currentPianoKey)) {//main section, white key is taller
            pw.println("cube([underKeyWidth,length-(metalRoundRadius*2+4),blackKeyHeight+metalRoundRadius+sqrt(metalRoundRadius*metalRoundRadius*2)+4],false);");
        } else {
            pw.println("cube([underKeyWidth,length-(metalRoundRadius*2+4),metalRoundRadius+sqrt(metalRoundRadius*metalRoundRadius*2)+4],false);");
        }
        
        pw.println("translate([0.5*underKeyWidth,length,0])");
        
        if (isWhiteKey(currentPianoKey)) {//rounded tip
            pw.println("cylinder(h=blackKeyHeight+metalRoundRadius+sqrt(metalRoundRadius*metalRoundRadius*2)+4, r=underKeyWidth/6);");
        } else {
            pw.println("cylinder(h=metalRoundRadius+sqrt(metalRoundRadius*metalRoundRadius*2)+4, r=underKeyWidth/6);");
        }

        pw.println("}");

        if (isWhiteKey(currentPianoKey)) {//THIS IS STUPID, SHOULD REWRITE, removes cut from back of white keys
            pw.println("translate([0,blackKeyHeight,0])\n"
                    + "rotate([45,0,0])\n"
                    + "translate([-25,-50,0])\n"
                    + "cube([50,50,50]);");
        }

        if (isWhiteKey(currentPianoKey)) {
            pw.println("translate([-.1,metalRoundRadius+2,blackKeyHeight+metalRoundRadius+2])");
        } else {
            pw.println("translate([-.1,metalRoundRadius+2,metalRoundRadius+2])");
        }

        pw.println("union(){"//round rod hole + square top?
                + "rotate([45,0,0])"
                + "cube([underKeyWidth+0.2,metalRoundRadius+metalRoundRadiusTolerance,metalRoundRadius+metalRoundRadiusTolerance]);"
                + "rotate([0,90,0])");
        //cylinder transform and rotate for metal round, 0.1 to make the extra 0.2 stick out and not be flush with base
        pw.println("cylinder((underKeyWidth+.2),r=metalRoundRadius+metalRoundRadiusTolerance, true);}");//cylinder for metal round, CHANGED FROM +0.5 TO +0.25

        if (isWhiteKey(currentPianoKey)) {
            pw.println("translate([underKeyWidth/10,-tolerance,0.125*blackKeyHeight+blackKeyHeight])");
        } else {
            pw.println("translate([underKeyWidth/10,-tolerance,0.125*(metalRoundRadius*2+8)])");//+8 because of 4 around hole I think I dunno who cares
        }
        
        pw.println("mirror([0,1,0])");//key number label
        pw.println("rotate([90,0,0])");
        pw.println("linear_extrude(height=0.5){");
        pw.println("text(\"" + i + "\",size=underKeyWidth/2);");//(metalRoundRadius*2+4)*0.75)//PROBALBY WANT TO FIGURE OUT HOW TO CENTER TEXT VERTICALLY AND HORIZONTALLY
        pw.println("}");
        
        /*pw.println("//Warp Cuts:");
        
        if (isWhiteKey(currentPianoKey)) {
            pw.println("warpHeight=" + (blackKeyHeight + metalRoundRadius + Math.pow(metalRoundRadius * metalRoundRadius * 2, 0.5) + 4) +";");
            warpCuts(blackKeyHeight + metalRoundRadius + Math.pow(metalRoundRadius * metalRoundRadius * 2, 0.5) + 4, length, 2, true);
        } else {
            pw.println("warpHeight=" + (metalRoundRadius + Math.pow(metalRoundRadius * metalRoundRadius * 2, 0.5) + 4) +";");
            warpCuts(metalRoundRadius + Math.pow(metalRoundRadius * metalRoundRadius * 2, 0.5) + 4, length, 2, false);
        }
        */
        
        pw.println("}");
        
    }

    public void warpCuts(double warpHeight, double length, int row, boolean whiteKey) {
        for (double soFar = 0; soFar < length - warpHeight; soFar += warpHeight) {
            if (row == 2) {
                pw.println("translate([0,0.5*warpHeight,0])");
            }
            pw.println("translate([-1,length-" + soFar + "," + row + "*warpHeight*0.5-warpHeight*0.25])");
            pw.println("rotate([0,90,0])");
            pw.println("linear_extrude(height=underKeyWidth+2)");
            pw.println("polygon(points=[[0,0],[0.25*warpHeight,0.25*warpHeight],[0.5*warpHeight,0],[0.25*warpHeight,-0.25*warpHeight]]);");
        }
    }

    public void getUserInputAndDeriveConstants() {
        octaveWidth = 162.71875;
        blackKeyHeight = 9.525;
        whiteKeyLengthPreShortening = 130.175;
        
        keytopHeightDifference = 10;
        metalRoundRadius = 2.5;
        
        roughRender = false;
        keytopsInSingleKeyFiles = false;
        keytopsInTogether = true;
        
        verticalFlip = false;//don't think I touch this anymore
        shiftXTrue = false;//if not, shift Y. This is terrible variable naming
        
        periodSteps = 2;
        generatorSteps = 1;
        desiredGamut = 5;//2*periodSteps?
        range = 12;
        startingKey = 5;
        stepsForLarge = 1;
       
        //these are sort of broken because instead of setting the distance between edges,
        //it just shrinks the model so that the highest and rightest point are moved these amounts
        
        //Gaps for stalkHole fit
        double xToleranceGap=0.5;
        double yToleranceGap=0.75;
        //for keytop gaps
        double hGap=0.875;
        double vGap=2.125;
        underKeyGap= 0.46875;
        nutHoleScale=1.05;//????????????????

        periodWidth = octaveWidth / 12 * periodSteps;
        underKeyWidth = octaveWidth / 12.0 - underKeyGap;
        genhPreShortening = whiteKeyLengthPreShortening / desiredGamut;
        clampDepth = metalRoundRadius*8;
        
        determineMOS();

        System.out.println(checkCoprime(periodSteps, generatorSteps));
        
        int chosenMosScaleIndex;

        for (int i = 0; i < mosTracker.size(); i++) {
            if (mosTracker.get(i).largeSize == stepsForLarge) {
                chosenMosScaleIndex = i;
                stepsForSmall = mosTracker.get(chosenMosScaleIndex).smallSize;
                break;
            }
        }

        determineGens();//find out what generator values get you to large and small step, as well as 1 step in whole tuning
        
        if ((!verticalFlip && !neededAbsoluteValue) || (verticalFlip && neededAbsoluteValue)) {
                d = genForSmallStep * genhPreShortening -vGap;
                c = stepsForSmall / (periodSteps * 1.0) * periodWidth - hGap;
                b = stepsForLarge / (periodSteps * 1.0) * periodWidth - hGap;
                a = genForLargeStep * genhPreShortening -vGap;
            
        }else{
                a = genForSmallStep * genhPreShortening -vGap;
                b = stepsForSmall / (periodSteps * 1.0) * periodWidth - hGap;
                c = stepsForLarge / (periodSteps * 1.0) * periodWidth - hGap;
                d = genForLargeStep * genhPreShortening -vGap;     
        }

        /*
        this is confusing but I think this is how it's working:
        It calculates a b c and d as if we didn't have to worry about not putting the keytops directly over he pivot point
        then, using those values, it makes the white key shorter (by overhead, and 0.25*(a+d) so that not even the top of the highest key is over the overhead aread)
        Now we need to recalculate a b c and d for this new length. We do that, and it just sticks out by the overhead amount
        
        */
  
        whiteKeyLength = whiteKeyLengthPreShortening-(metalRoundRadius * 2 + 4 + (a + d) * 0.25);//dang haha I think the + 4 thing should be a variable. It's... the extra distance for the metal round/rod hole from the edge I think
        genh = whiteKeyLength / desiredGamut;
        
        if ((!verticalFlip && !neededAbsoluteValue) || (verticalFlip && neededAbsoluteValue)) {
            
                d = genForSmallStep * genh -vGap;
                c = stepsForSmall / (periodSteps * 1.0) * periodWidth - hGap;
                b = stepsForLarge / (periodSteps * 1.0) * periodWidth - hGap;
                a = genForLargeStep * genh -vGap;
            
        }else{
                a = genForSmallStep * genh -vGap;
                b = stepsForSmall / (periodSteps * 1.0) * periodWidth - hGap;
                c = stepsForLarge / (periodSteps * 1.0) * periodWidth - hGap;
                d = genForLargeStep * genh -vGap;     
        }
        
        shiftX = (b+c)/8;
        shiftY = (a+d)/8;
        
        if(shiftXTrue){
            slantCutWidth = b+c-shiftX;
            stalkScaleX = Math.min(underKeyWidth / (b + c - shiftX*2),0.5);
        }
        else{
            slantCutWidth = (b+c)/2-underKeyWidth/6;
            stalkScaleX = Math.min(underKeyWidth / (b + c),0.5);
        }
        
        stalkScaleY = 0.5;
        
        holeScaleX = ((b + c) * stalkScaleX + xToleranceGap) / (b + c);
        holeScaleY = ((a + d) * stalkScaleY + yToleranceGap) / (a + d);
        
        overhead = metalRoundRadius * 2 + 4 + (a + d) * 0.25;
    }

    public void determineMOS() {
        ArrayList<Integer> scaleSteps = new ArrayList<>();

        StepSizeTracker stepSizes;

        scaleSteps.add(0);
        scaleSteps.add(periodSteps);

        mosSizes.add(1);
        mosSizes.add(2);
        mosSizes.add(3);

        int numberLess = 1;
        //steps.add(periodSteps);
        for (int currentGenerator = 1; currentGenerator < periodSteps; currentGenerator++) {
            stepSizes = new StepSizeTracker();
            scaleSteps.add((currentGenerator * generatorSteps) % periodSteps);
            Collections.sort(scaleSteps);
            HashMap<Integer, Integer> tempStepHash;

            for (int h = 1; h < scaleSteps.size() - numberLess; h++) {//need to keep track of h in stepSizes to check for how many times it shows up
                tempStepHash = new HashMap<>();
                for (int i = h; i < scaleSteps.size() + h - numberLess; i++) {//works so far first time through where h=1 and size is 3
                    if (i > scaleSteps.size() - 1) {  
                        stepSizes.addStep(h, scaleSteps.get(i - (scaleSteps.size() - 1)) + periodSteps - scaleSteps.get(i - h));
                    } else {
                        stepSizes.addStep(h, scaleSteps.get(i) - scaleSteps.get(i - h));
                    }
                }
            }

            boolean isMOS = true;
            for (int i = 0; i < stepSizes.stepBags.size(); i++) {
                if (stepSizes.stepBags.get(i).size() > 2) {
                    isMOS = false;
                }
            }

            if (isMOS && isValidMOS(scaleSteps.size() - 1, mosSizes)) {
                mosSizes.add(scaleSteps.size() - 1);
                if (stepSizes.stepBags.get(0).size() == 2) {
                    mosTracker.add(new mosScale(
                            scaleSteps.size() - 1,
                            stepSizes.stepBags.get(0).get(0).size,
                            stepSizes.stepBags.get(0).get(0).amount,
                            stepSizes.stepBags.get(0).get(1).size,
                            stepSizes.stepBags.get(0).get(1).amount));
                } else {
                    mosTracker.add(new mosScale(
                            scaleSteps.size() - 1,
                            stepSizes.stepBags.get(0).get(0).size,
                            stepSizes.stepBags.get(0).get(0).amount));
                }
            }
        }
    }

    public boolean isValidMOS(int test, ArrayList<Integer> mosSizes) {
        for (int i = 0; i < mosSizes.size(); i++) {
            for (int j = i; j < mosSizes.size(); j++) {
                if (test == mosSizes.get(j) + mosSizes.get(i)) {
                    return true;
                }
            }
        }
        return false;
    }

    public void determineGens() {//iterate through stacked generators until you arrive at large step, use to derive small step

        boolean foundLargeGen = false, foundSmallGen = false, foundGenForStep1 = false;
        int currentStepsAboveTonic = generatorSteps;//starting at one generator, so starting at the number of generator steps in the tuning
        int generatorCounter = 1;
        
        while ((foundLargeGen && foundSmallGen && foundGenForStep1) == false /*&& i<periodSteps*/) {

            while (currentStepsAboveTonic > periodSteps - 1) {
                currentStepsAboveTonic -= periodSteps;
            }

            if (currentStepsAboveTonic == stepsForLarge) {
                foundLargeGen = true;
                genForLargeStep = Math.min(generatorCounter, Math.abs(periodSteps - generatorCounter));
                if(generatorCounter > Math.abs(periodSteps - generatorCounter)){
                    neededAbsoluteValue=true;///////////////////////////////////////////////////////////////////////////////////////////////////////////
                }
            }

            if (currentStepsAboveTonic == stepsForSmall) {
                foundSmallGen = true;
                genForSmallStep = Math.min(generatorCounter, Math.abs(periodSteps - generatorCounter));
            }

            if (currentStepsAboveTonic == 1) {
                foundGenForStep1 = true;
                genForStep1 = Math.min(generatorCounter, Math.abs(periodSteps - generatorCounter));
                genForStep1b = Math.abs(genForStep1 - periodSteps);
            }

            if (generatorCounter > periodSteps) {
                System.out.println("I DON'T THINK THAT THOSE TWO NUMBERS ARE COPRIME!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
                break;
            }

            currentStepsAboveTonic += generatorSteps;
            generatorCounter++;
        }
        if (stepsForLarge == 1) {
            genForLargeStep = Math.abs(genForSmallStep - periodSteps);
            neededAbsoluteValue=true;
        }
    }

    public boolean checkCoprime(int per, int gen) {//make sure user input of generator generates entire gamut of periodSteps
        int current = gen;
        boolean areCoprime = true;
        for (int i = 0; i < per - 1; i++) {
            if (current == per) {
                areCoprime = false;
            }
            current += gen;
            if (current > per) {
                current -= per;
            }
        }
        return areCoprime;
    }
}

class mosScale {

    int scaleSize;
    int smallSteps;
    int smallSize;
    int largeSteps;
    int largeSize;

    public mosScale(int scaleSizeIn, int size1In, int steps1In, int size2In, int steps2In) {
        scaleSize = scaleSizeIn;
        if (size1In < size2In) {
            smallSteps = steps1In;
            smallSize = size1In;
            largeSteps = steps2In;
            largeSize = size2In;
        } else {
            smallSteps = steps2In;
            smallSize = size2In;
            largeSteps = steps1In;
            largeSize = size1In;
        }
    }

    public mosScale(int scaleSizeIn, int smallSizeIn, int smallStepsIn) {
        scaleSize = scaleSizeIn;
        smallSteps = smallStepsIn;
        smallSize = smallSizeIn;
        largeSteps = smallStepsIn - scaleSize;
        largeSize = smallSize;
    }

}

class StepSizeTracker {

    ArrayList<ArrayList<StepBag>> stepBags;
    boolean alreadyHere;

    StepSizeTracker() {
        stepBags = new ArrayList<>();
    }

    void addStep(Integer intervalClass, Integer stepSize) {
        if (stepBags.size() < intervalClass) {
            stepBags.add(new ArrayList<StepBag>());
        }
        alreadyHere = false;
        for (int i = 0; i < stepBags.get(intervalClass - 1).size(); i++) {
            if (stepSize == stepBags.get(intervalClass - 1).get(i).size) {
                stepBags.get(intervalClass - 1).get(i).amount++;
                alreadyHere = true;
            }
        }
        if (alreadyHere == false) {
            stepBags.get(intervalClass - 1).add(new StepBag(stepSize, 1));
        }
    }
}

class StepBag {

    int size;
    int amount;

    StepBag(int sizeIn, int amountIn) {
        size = sizeIn;
        amount = amountIn;
    }
}