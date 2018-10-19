package fr.ign.cogit.simplu3d.experiments.smartplu.data;

import java.io.File;

import fr.ign.simplu3d.AggregateResults;

/**
 * Executable that agregates the results of the simulation from the root folder
 * that contains the differents sub folder that were distributed
 * 
 * @author mbrasebin
 *
 */
public class ResultsAgregator {

	/**
	 * Requires two arguments : 1/ the output folder of the simulation and 2/ the output folder of the agregation
	 * The value can be the same
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {

		if (args.length < 2) {
			System.out.println("Two arguments expected : results root folder and folderOut");
		}

		File results = new File(args[0]);
		
		if(! results.exists()) {
			System.out.println("Input folder does not exist");
			return;
		}
		
		//If the output folder does not exist we create it
		File outputFolder = new File(args[1]);
		
		if(! outputFolder.exists() && ! outputFolder.mkdirs()) {
			System.out.println("Cannot create the output folder");
			return;
		}
		
		AggregateResults.aggregateBuildings(results, new File(outputFolder, "buildings.shp"));
	}

}
