package fr.ign.cogit.simplu3d.experiments.smartplu.data;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;

import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IFeatureCollection;
import fr.ign.cogit.geoxygene.feature.FT_FeatureCollection;
import fr.ign.cogit.geoxygene.util.attribute.AttributeManager;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileWriter;
import fr.ign.cogit.geoxygene.util.index.Tiling;

/**
 * Class for data preparation for SmartPLU experiments
 * 
 * Input : A JSON File that represents parcels with the right format to store
 * the ruels with lon/lat WGS84 coordinates Output : a set of folder that
 * contains a shapefile with parcels corresponding to an ID
 * 
 * 
 * @author mbrasebin
 *
 */
public class DataPreparator {

	// Attribute to store the id_block, it is only used to check if the algorithm
	// works right
	public final static String ATTRIBUTE_NAME_GROUP = "id_block";

	// IDPAR is used during simulation to identify the parcels
	// It will serve during the simulation to make the link between
	// simulation results and parcels
	public final static String ATTRIBUTE_NAME_ID = "IDPAR";

	// ATTRIBUTE THAT STORE THE NUMBER OF BAND (0 here)
	// IT is used during the simulation
	public final static String ATTRIBUTE_NAME_BAND = "B1_T_BANDE";

	public static void main(String[] args) throws Exception {
		// File in
		String fileIn = "/home/mbrasebin/Documents/Donnees/demo-numen/municipality/61230/parcelle.json";

		// Folder where results are stored
		String folderOut = "/home/mbrasebin/Documents/Donnees/demo-numen/municipality/61230/out/";

		// Reading the features
		IFeatureCollection<IFeature> collectionParcels = DefaultFeatureDeserializer.readJSONFile(fileIn);

		// If we want to use a shapefile instead (data has to be in Lambert93)
		// IFeatureCollection<IFeature> collectionParcels =
		// ShapefileReader.read(fileIn);

		// A temporary collection to store the agregated results
		IFeatureCollection<IFeature> collToExport = new FT_FeatureCollection<>();
		collToExport.addAll(collectionParcels);

		// Creating the groups into a map
		Map<Integer, IFeatureCollection<IFeature>> map = createParcelGroups(collectionParcels);

		// Just checking if there are no double in the results
		int count = 0;
		for (Object s : map.keySet().toArray()) {
			IFeatureCollection<IFeature> coll = map.get(s);
			int size = coll.size();
			System.out.println("For group : " + s + "  -  " + size + "  entities");
			count = count + size;
		}

		System.out.println("Number of features in map : " + count);
		System.out.println("Number of features in collection : " + collToExport.size());

		// Creating the folder
		exportFolder(map, folderOut);
		
		
		/////////////////////////////////////////////////////////////////////
		//This code is not useful for a final production and for simulation as it only proposes an aggregated export
		//WARNING !!!!!!!!!
		//Do not forget to remove the aggregated.shp from out folder 
		//If you want to run simulation
		/////////////////////////////
		
		
		// This hint is to ensure that the first item has rules
		// Because the schema of the shapefile export is based on the schema of the
		// first feature
		int nbElem = collToExport.size();
		for (int i = 0; i < nbElem; i++) {
			IFeature feat = collToExport.get(i);
			if (feat.getAttribute("DOCUMENT") != null) {
				collToExport.remove(i);
				collToExport.getElements().add(0, feat);
				break;
			}
		}

		// Storing the agregated results (only for debug and to check if the blocks are
		// correctly generated)
		ShapefileWriter.write(collToExport, folderOut + "agregated.shp",
				CRS.decode(DefaultFeatureDeserializer.SRID_END));

	}

	/**
	 * 
	 * @param parcelles a collection of parcels that will be scattered into groups
	 *                  corresponding to morphologic blocks
	 * @return
	 */
	public static Map<Integer, IFeatureCollection<IFeature>> createParcelGroups(
			IFeatureCollection<IFeature> parcelles) {

		// Map Integer / Features of the group
		Map<Integer, IFeatureCollection<IFeature>> mapResult = new HashMap<>();

		// Initialisation of spatial index with updates
		parcelles.initSpatialIndex(Tiling.class, true);

		// Initializatino of ID attribut to -1
		parcelles.stream().forEach(x -> setIDBlock(x, -1));
		// Adding missin attributes ID and NAME_BAND set by ATTRIBUTE_NAME_ID and
		// ATTRIBUTE_NAME_BAND attribute name
		parcelles.stream().forEach(x -> generateMissingAttributes(x));

		// Current group ID
		int idCurrentGroup = 0;

		while (!parcelles.isEmpty()) {
			// We get the first parcel and removes it from the list
			IFeature currentParcel = parcelles.get(0);
			parcelles.remove(0);
			setIDBlock(currentParcel, idCurrentGroup);

			// Preparing a collection with results for the map
			IFeatureCollection<IFeature> grapFeatures = new FT_FeatureCollection<>();
			grapFeatures.add(currentParcel);

			mapResult.put(idCurrentGroup, grapFeatures);

			List<IFeature> candidateParcelles = Arrays.asList(currentParcel);

			// Initialisation of the recursion method that affects ID neighbour by neighbour
			selectByNeighbourdHood(candidateParcelles, parcelles, idCurrentGroup, grapFeatures);

			idCurrentGroup++;
		}

		return mapResult;
	}

	/**
	 * A method that determine the neighbour parcels from candidates
	 * (featCandidates) and remove them from the general parcel collections
	 * (parcelles) and set the value attributeCount for the group. The result is
	 * stored in grapFeatures that will be reused in the different uses of the
	 * recursive method
	 * 
	 * @param featCandidates
	 * @param parcelles
	 * @param attributeCount
	 * @param grapFeatures
	 */
	public static void selectByNeighbourdHood(List<IFeature> featCandidates, IFeatureCollection<IFeature> parcelles,
			int attributeCount, IFeatureCollection<IFeature> grapFeatures) {

		for (IFeature currentParcel : featCandidates) {
			// Update of the current grap
			grapFeatures.addUnique(currentParcel);
			// We select the surrounding parcels
			Collection<IFeature> surroundingParcels = parcelles.select(currentParcel.getGeom().buffer(0.1));

			// We only keep features where ID is not set
			List<IFeature> listNotSetSurroundingParcels = surroundingParcels.stream().filter(x -> -1 == getIDBlock(x))
					.collect(Collectors.toList());
			// We set the group value to the features
			listNotSetSurroundingParcels.stream().forEach(x -> setIDBlock(x, attributeCount));

			// We remove the list from existing parcels
			parcelles.removeAll(listNotSetSurroundingParcels);

			if (!listNotSetSurroundingParcels.isEmpty()) {
				// We relaunch with the new selected parcels
				selectByNeighbourdHood(listNotSetSurroundingParcels, parcelles, attributeCount, grapFeatures);
			}
		}

	}

	/**
	 * Create a folder for each entry of the map
	 * 
	 * @param map
	 * @param folderIn
	 */
	public static void exportFolder(Map<Integer, IFeatureCollection<IFeature>> map, String folderIn) {
		// For each key we create a folder with associated features
		map.keySet().parallelStream().forEach(x -> createFolderAndExport(folderIn + x + "/", map.get(x)));
	}

	/**
	 * Create a folder for an entry of the map (the name parcelle.shp is used in the
	 * simulator)
	 * 
	 * @param path
	 * @param features
	 */
	public static void createFolderAndExport(String path, IFeatureCollection<IFeature> features) {
		// We create the folder and store the collection
		
		// This hint is to ensure that the first item has rules
		// Because the schema of the shapefile export is based on the schema of the
		// first feature
		int nbElem = features.size();
		for (int i = 0; i < nbElem; i++) {
			IFeature feat = features.get(i);
			if (feat.getAttribute("DOCUMENT") != null) {
				features.remove(i);
				features.getElements().add(0, feat);
				break;
			}
		}

		
		File f = new File(path);
		f.mkdirs();
		try {
			ShapefileWriter.write(features, path + "parcelle.shp", CRS.decode(DefaultFeatureDeserializer.SRID_END));
		} catch (NoSuchAuthorityCodeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Get IDBlock value for a feature
	 * @param x
	 * @return
	 */
	public static int getIDBlock(IFeature x) {
		return Integer.parseInt(x.getAttribute(ATTRIBUTE_NAME_GROUP).toString());
	}

	/**
	 * Set IDBlock value for a feature
	 * @param x
	 * @param value
	 */
	public static void setIDBlock(IFeature x, int value) {
		AttributeManager.addAttribute(x, ATTRIBUTE_NAME_GROUP, value, "Integer");
	}

	/**
	 * Adding missing attributes :
	 * - the ID is generated from a concatenation of several attributes
	 * - the ATTRIBUTE_NAME_BAND that is set to 0 as there is only one band regulation
	 * @param x
	 */
	private static void generateMissingAttributes(IFeature x) {
		String commune = x.getAttribute("commune").toString();
		String prefix = x.getAttribute("prefixe").toString();
		String section = x.getAttribute("section").toString();
		String numero = x.getAttribute("numero").toString();
		String contenance = x.getAttribute("contenance").toString();
		String id = x.getAttribute("id").toString();

		String idFinal = commune + prefix + section + numero + contenance + id;
		AttributeManager.addAttribute(x, ATTRIBUTE_NAME_ID, idFinal, "String");

		AttributeManager.addAttribute(x, ATTRIBUTE_NAME_BAND, 0, "Integer");
	}

}
