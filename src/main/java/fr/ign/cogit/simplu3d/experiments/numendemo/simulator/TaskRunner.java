package fr.ign.cogit.simplu3d.experiments.numendemo.simulator;

import java.io.File;

import fr.ign.cogit.simplu3d.io.feature.AttribNames;
import fr.ign.simplu3d.iauidf.openmole.EPFIFTask;
import fr.ign.simplu3d.iauidf.tool.ParcelAttributeTransfert;

public class TaskRunner {
  public static String run(File folder, String dirName, File folderOut, File parameterFile, long seed) {

    EPFIFTask.USE_DEMO_SAMPLER = false;
    EPFIFTask.INTERSECTION = true;
    EPFIFTask.FLOOR_HEIGHT = 3;
    EPFIFTask.MAX_PARCEL_AREA = 10000; // now obsolete
    EPFIFTask.PARCEL_NAME = "parcelle.shp";
    EPFIFTask.DEBUG_MODE = true;
    
	  
    EPFIFTask.ATT_SIMUL = "has_rules";
	ParcelAttributeTransfert.att_libelle_zone = "libelle";
	ParcelAttributeTransfert.att_insee = "insee";
	ParcelAttributeTransfert.att_libelle_de_base  = "libelle"; //same attribute used 2 times
	ParcelAttributeTransfert.att_libelle_de_dul  = "libelle"; //same attribute used 2 times
	ParcelAttributeTransfert.att_fonctions  = "destdomi"; //same attribute used 2 times
	ParcelAttributeTransfert.att_top_zac = "has_rules"; //Random attribute
	ParcelAttributeTransfert.att_zonage_coherent = "has_rules";  //Random attribute
	ParcelAttributeTransfert.att_correction_zonage = "has_rules"; //Random attribute
	ParcelAttributeTransfert.att_typ_bande = "hasError"; //Random attribute with 0 as value
	ParcelAttributeTransfert.att_bande = "hasError"; //Random attribute with 0 as value
	ParcelAttributeTransfert.att_art_5 = "B1_ART_5";
	ParcelAttributeTransfert.att_art_6 = "B1_ART_6"; 
	ParcelAttributeTransfert.att_art_71 = "B1_ART_71";  
	ParcelAttributeTransfert.att_art_72 = "B1_ART_72";  
	ParcelAttributeTransfert.att_art_73 = "B1_ART_73";  
	ParcelAttributeTransfert.att_art_74 = "B1_ART_74";  
	
	
	ParcelAttributeTransfert.att_art_8 = "B1_ART_8"; 
	ParcelAttributeTransfert.att_art_9 = "B1_ART_9"; 
	ParcelAttributeTransfert.att_art_10 = "B1_ART_10";
	ParcelAttributeTransfert.att_art_10_m = "B1_ART_10";

	ParcelAttributeTransfert.att_art_12 = "B1_ART_14"; //Art 12 is not used
	ParcelAttributeTransfert.att_art_10_top = "B1_ART_10";
	ParcelAttributeTransfert.att_art_13 = "B1_ART_13";
	
	ParcelAttributeTransfert.att_art_14 = "B1_ART_14";
	
	
    AttribNames.setATT_CODE_PARC("IDPAR");
  

		
															

    // String[] folderSplit = folder.getAbsolutePath().split(File.separator);
    String imu = dirName;// folderSplit[folderSplit.length - 1];
  
    String result = "";
    	 
    try {
    	result = EPFIFTask.run(folder, dirName, folderOut, parameterFile, seed);
    } catch (Exception e) {
      result = "# " + imu + " #\n";
      result += "# "+ e.toString() + "\n";
//      for (StackTraceElement s : e.getStackTrace())
//        result += "# " + s.toString() + "\n";
      result += "# " + imu + " #\n";
      e.printStackTrace();
    }
    return result;
  }
  
  
  public static ClassLoader getClassLoader() {
	return TaskRunner.class.getClassLoader();
}

  public static void main(String[] args) {
																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																									

    String numrep = "22";
    String foldName ="/home/mbrasebin/Documents/Donnees/demo-numen/municipality/61230/out/";
    
    
    File folder = new File(foldName + numrep + "/");
    File folderOut = new File("/tmp/tmp/" + numrep + "/");
    File parameterFile = new File("/home/mbrasebin/Documents/Donnees/IAUIDF/Input/Input1/dep_94_connected_openmole/dataBasicSimu/scenario/parameters_iauidf.json");
    

    
    
    long seed = 42L;
    String res = "";
    res = run(folder, numrep, folderOut, parameterFile, seed);
    System.out.println("result : " + res);
  }
}

