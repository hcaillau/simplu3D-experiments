package fr.ign.cogit.simplu3d.experiments.smartplu.tools;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.io.FileUtils;

/**
 * Check if all the simulations have been proceeded and copy the missing to a
 * new folder
 * 
 * @author mbrasebin
 *
 */
public class CheckOutPut {

	public static void main(String[] args) throws Exception {

		// Folder with the output of the results
		String folderResults = "/home/mbrasebin/.openmole/ZBOOK-SIGOPT-2016/webui/projects/smartPLU/results/22278/";

		// Folder with the data used for the simulation
		String folderDataIn = "/home/mbrasebin/.openmole/ZBOOK-SIGOPT-2016/webui/projects/smartPLU/dataBasicSimu/22278/";

		// Folder with the data that were not simulated
		String folderOut = "/home/mbrasebin/.openmole/ZBOOK-SIGOPT-2016/webui/projects/smartPLU/dataBasicSimu/22278-temp/";

		// Number of zones
		int nbZoneMax = 1428;

		List<File> lFiles = IntStream.range(0, nbZoneMax).mapToObj(x -> new File(folderResults + x))
				.filter(x -> !x.exists()).map(x -> new File(folderDataIn + x.getName())).collect(Collectors.toList());

		lFiles.stream().forEach(x -> {
			try {
				FileUtils.copyDirectory(x, new File(folderOut + x.getName()));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		});

	}

}
