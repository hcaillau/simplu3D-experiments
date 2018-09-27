package fr.ign.cogit.simplu3d.experiments.numendemo.data;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IFeatureCollection;
import fr.ign.cogit.geoxygene.feature.FT_FeatureCollection;
import fr.ign.cogit.geoxygene.util.attribute.AttributeManager;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileWriter;
import fr.ign.cogit.geoxygene.util.index.Tiling;

public class DataPreparator {

	public final static String ATTRIBUTE_NAME_GROUP = "id_block";
	public final static String ATTRIBUTE_NAME_ID = "IDPAR";

	public static void main(String[] args) throws Exception {
		// File in
		String fileIn = "/home/mbrasebin/Documents/Donnees/demo-numen/municipality/61230/parcelle.json"; // parcelle.json

		// Folder where results are stored
		String folderOut = "/home/mbrasebin/Documents/Donnees/demo-numen/municipality/61230/out/";

		// Reading the features

		IFeatureCollection<IFeature> collectionParcels = DefaultFeatureDeserializer.readJSONFile(fileIn);

		// IFeatureCollection<IFeature> collectionParcels =
		// ShapefileReader.read(fileIn);

		// A temporary collection to store the agregated results
		IFeatureCollection<IFeature> collToExport = new FT_FeatureCollection<>();
		collToExport.addAll(collectionParcels);

		// Creating the groups into a map
		Map<Integer, IFeatureCollection<IFeature>> map = createParcelGroups(collectionParcels);

		// Just checking if there are no double in the results
		int count = 0;
		for (IFeatureCollection<IFeature> coll : map.values()) {
			count = count + coll.size();
		}
		System.out.println("Number of features in map : " + count);
		System.out.println("Number of features in collection : " + collToExport.size());

		// Creating the folder
		exportFolder(map, folderOut);

		// Storing the agregated results
		ShapefileWriter.write(collToExport, folderOut + "agregated_2.shp");

	}

	public static Map<Integer, IFeatureCollection<IFeature>> createParcelGroups(
			IFeatureCollection<IFeature> parcelles) {

		// Map Integer / Features of the group
		Map<Integer, IFeatureCollection<IFeature>> mapResult = new HashMap<>();

		// Initialisation of spatial index with updates
		parcelles.initSpatialIndex(Tiling.class, true);

		// Initializatino of ID attribut to 0
		parcelles.stream().forEach(x -> setIDBlock(x, -1));
		parcelles.stream().forEach(x -> generateIDAttribute(x));

		int attributeCount = 0;

		while (!parcelles.isEmpty()) {
			// We get the first parcel and removes it from the list
			IFeature currentParcel = parcelles.get(0);
			parcelles.remove(0);
			setIDBlock(currentParcel, attributeCount);

			// Preparing a collection with results for the map
			IFeatureCollection<IFeature> grapFeatures = new FT_FeatureCollection<>();
			grapFeatures.add(currentParcel);

			mapResult.put(attributeCount, grapFeatures);

			List<IFeature> candidateParcelles = Arrays.asList(currentParcel);

			// Initialisation of the recursion method that affects ID neighbour by neighbour
			selectByNeighbourdHood(candidateParcelles, parcelles, attributeCount, grapFeatures);

			attributeCount++;
		}

		return mapResult;
	}

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

	public static void exportFolder(Map<Integer, IFeatureCollection<IFeature>> map, String folderIn) {
		// For each key we create a folder with associated features
		map.keySet().parallelStream().forEach(x -> createFolderAndExport(folderIn + x + "/", map.get(x)));
	}

	public static void createFolderAndExport(String path, IFeatureCollection<IFeature> features) {
		// We create the folder and store the collection
		File f = new File(path);
		f.mkdirs();
		ShapefileWriter.write(features, path + "parcelle.shp");
	}

	public static int getIDBlock(IFeature x) {
		return Integer.parseInt(x.getAttribute(ATTRIBUTE_NAME_GROUP).toString());
	}

	public static void setIDBlock(IFeature x, int value) {
		AttributeManager.addAttribute(x, ATTRIBUTE_NAME_GROUP, value, "Integer");
	}

	private static void generateIDAttribute(IFeature x) {
		String commune = x.getAttribute("commune").toString();
		String prefix = x.getAttribute("prefixe").toString();
		String section = x.getAttribute("section").toString();
		String numero = x.getAttribute("numero").toString();
		String contenance = x.getAttribute("contenance").toString();
		String id = x.getAttribute("id").toString();

		String idFinal = commune + prefix + section + numero + contenance + id;
		AttributeManager.addAttribute(x, ATTRIBUTE_NAME_ID, idFinal, "String");
	}

}
