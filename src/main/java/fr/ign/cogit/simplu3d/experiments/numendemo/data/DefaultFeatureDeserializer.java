package fr.ign.cogit.simplu3d.experiments.numendemo.data;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.util.Map.Entry;
import java.util.Set;

import org.geotools.referencing.CRS;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.io.geojson.GeoJsonReader;

import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IFeatureCollection;
import fr.ign.cogit.geoxygene.api.spatial.geomroot.IGeometry;
import fr.ign.cogit.geoxygene.datatools.CRSConversion;
import fr.ign.cogit.geoxygene.feature.DefaultFeature;
import fr.ign.cogit.geoxygene.feature.FT_FeatureCollection;
import fr.ign.cogit.geoxygene.util.attribute.AttributeManager;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileWriter;

/**
 * Class to make a conversion between a Geojson file and a
 * IFeatureCollection<IFeature>
 * 
 * 
 * @author mbrasebin
 *
 */
public class DefaultFeatureDeserializer implements JsonDeserializer<IFeatureCollection<IFeature>> {

	//The input/output SRID
	//A reprojection is proceeded during the conversion
	public final static String SRID_INI = "urn:ogc:def:crs:EPSG::4326";
	public final static String SRID_END = "EPSG:2154";

	public static void main(String[] args) throws Exception {
		// Transform a json file to a shapefile
		// The idea is afterall to test the conversion between a geohson file and a
		// IFeatureCollection in Geoxygene
		String fileIn = "/home/mbrasebin/Documents/Donnees/demo-numen/municipality/61230/parcelle.json";
		String fileOut = "/home/mbrasebin/Documents/Donnees/demo-numen/municipality/61230/out/parcelle_2.shp";

		// We transform the file
		IFeatureCollection<IFeature> collectionParcels = readJSONFile(fileIn);

		// We write the collection
		ShapefileWriter.write(collectionParcels, fileOut);

	}

	/**
	 * The main class
	 * 
	 * @param file the path to the json file
	 * @return
	 * @throws Exception
	 */
	public static IFeatureCollection<IFeature> readJSONFile(String file) throws Exception {

		// Reading the file with gson
		InputStream intputSream = new FileInputStream(file);
		Reader reader = new InputStreamReader(intputSream, "UTF-8");
		// System.out.println(IOUtils.toString(is)

		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(IFeatureCollection.class, new DefaultFeatureDeserializer());
		Gson gson = gsonBuilder.create();

		IFeatureCollection<IFeature> featureCollection = gson.fromJson(reader, IFeatureCollection.class);

		// This hint is to ensure that the first item has rules
		// Because the schema of the shapefile export is based on the schema of the
		// first feature
		int nbElem = featureCollection.size();
		for (int i = 0; i < nbElem; i++) {
			IFeature feat = featureCollection.get(i);
			if (feat.getAttribute("DOCUMENT") != null) {
				featureCollection.remove(i);
				featureCollection.getElements().add(0, feat);
				break;
			}
		}

		System.out.println("Number of parcels : " + nbElem);

		return featureCollection;

	}

	@Override
	public IFeatureCollection<IFeature> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
			throws JsonParseException {
		IFeatureCollection<IFeature> featColl = new FT_FeatureCollection<IFeature>();

		final JsonObject jsonObject = json.getAsJsonObject();
		Set<Entry<String, JsonElement>> set = jsonObject.entrySet();
		// The top level of the file is composed of 2 item
		for (Entry<String, JsonElement> e : set) {
			try {
				IFeatureCollection<IFeature> featCollTemp = transformToFeature(e);
				if (featCollTemp != null) {
					featColl.addAll(featCollTemp);
				}
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}

		return featColl;
	}

	public IFeatureCollection<IFeature> transformToFeature(Entry<String, JsonElement> e) throws Exception {
		IFeatureCollection<IFeature> featColl = new FT_FeatureCollection<IFeature>();
		String key = e.getKey();

		switch (key) {
		// The top level of the file is composed of 2 item
		case "type":
			return null;

		case "features":

			// The features are stored as an array
			JsonElement elem = e.getValue();
			if (elem instanceof JsonArray) {

				return transformToFeature((JsonArray) elem);
			} else {
				System.out.println("Not JsonArray : " + elem.getClass());
			}
			break;
		default:
			System.out.println("Key unknown : " + key);
			break;
		}

		return featColl;
	}

	public IFeatureCollection<IFeature> transformToFeature(JsonArray array) throws Exception {

		IFeatureCollection<IFeature> featCollection = new FT_FeatureCollection<>();

		for (JsonElement e : array) {
			// For each line of the array we create a new feature to export
			DefaultFeature feat = new DefaultFeature();

			if (e instanceof JsonObject) {
				//We create a feature for each entry
				transformToFeature((JsonObject) e, feat);
				featCollection.add(feat);
			} else {
				System.out.println("transformToFeature : " + e.getClass());
			}

		}
		return featCollection;
	}

	public DefaultFeature transformToFeature(JsonObject jsonPrim, DefaultFeature feat) throws Exception {

		for (Entry<String, JsonElement> e : jsonPrim.entrySet()) {
			// 4 items at this level
			// properties
			// type
			// id
			// geometry
			String key = e.getKey();
			switch (key) {
			case "type":
				AttributeManager.addAttribute(feat, "type", e.getValue(), "String");
				break;
			case "id":
				AttributeManager.addAttribute(feat, "id", e.getValue(), "String");
				break;
			case "properties":
				generateProperties(e.getValue(), feat);
				break;
			case "geometry":
				IGeometry geom = transformGeom(e.getValue());
				feat.setGeom(geom);
				break;
			default:
				System.out.println("Unknown element : " + key);
			}

		}

		return feat;
	}

	private static GeoJsonReader jsonReader = new GeoJsonReader(new GeometryFactory());

	// We convert the geometry with JTS jsonreader
	private IGeometry transformGeom(JsonElement value) throws Exception {
		Geometry geom = jsonReader.read(value.toString());

		CoordinateReferenceSystem sourceCRS = CRS.decode(SRID_INI);
		CoordinateReferenceSystem targetCRS = CRS.decode(SRID_END);

		IGeometry targetGeometry = CRSConversion.changeCRS(geom, sourceCRS, targetCRS);

		return targetGeometry;
	}

	// We generate the attributes
	public void generateProperties(JsonElement properties, DefaultFeature feat) {
		JsonObject jsonPrim = (JsonObject) properties;
		for (Entry<String, JsonElement> e : jsonPrim.entrySet()) {
			JsonElement element = e.getValue();
			if (element instanceof JsonNull) {
				AttributeManager.addAttribute(feat, e.getKey(), "", "String");
			} else if (element instanceof JsonPrimitive) {

				JsonPrimitive primitive = (JsonPrimitive) element;

				//If the primitive is a number we store it as an Integer or Double
				if (primitive.isNumber()) {
					try {

						int value = primitive.getAsInt();
						AttributeManager.addAttribute(feat, e.getKey(), Math.abs(value), "Integer");

					} catch (Exception e2) {
						double value = primitive.getAsDouble();
						AttributeManager.addAttribute(feat, e.getKey(), Math.abs(value), "Double");

					}

				} else {
					//If it is a string, we remove the "
					String value = e.getValue().toString();
					value = value.replaceAll("\"", "");

					AttributeManager.addAttribute(feat, e.getKey(), value, "String");
				}

			}else {
				//If it is an other type of attribute we store it as a String
				String value = e.getValue().toString();
				value = value.replaceAll("\"", "");

				AttributeManager.addAttribute(feat, e.getKey(), value, "String");
	
			}

		}

	}

}
