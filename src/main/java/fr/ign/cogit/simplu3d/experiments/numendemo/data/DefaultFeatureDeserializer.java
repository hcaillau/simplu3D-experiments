package fr.ign.cogit.simplu3d.experiments.numendemo.data;

import java.lang.reflect.Type;
import java.util.Map.Entry;
import java.util.Set;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;import com.google.gson.internal.bind.JsonAdapterAnnotationTypeAdapterFactory;

import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IFeatureCollection;
import fr.ign.cogit.geoxygene.feature.DefaultFeature;
import fr.ign.cogit.geoxygene.feature.FT_FeatureCollection;

public class DefaultFeatureDeserializer implements JsonDeserializer<IFeatureCollection<IFeature> > {

	@Override
	public IFeatureCollection<IFeature> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
			throws JsonParseException {
		final IFeatureCollection<IFeature> featColl = new FT_FeatureCollection<IFeature>();

		final JsonObject jsonObject = json.getAsJsonObject();
		Set<Entry<String, JsonElement>> set = jsonObject.entrySet();

		for (Entry<String, JsonElement> e : set) {
			transformToFeature(e);
		}

		return featColl ;
	}
	
	
	public DefaultFeature transformToFeature(Entry<String, JsonElement> e) {
		DefaultFeature defaultFeature = new DefaultFeature();
		String key = e.getKey();
		
		switch (key) {
		case "type":
			System.out.println("Type");
			return null;
		case "features":
			JsonElement elem = e.getValue();
			if(elem instanceof JsonArray) {
				
				JsonArray array = (JsonArray)elem;
				 return transformToFeature(array);
			}else {
				System.out.println("Not JsonArray : " + elem.getClass() );
			}
			break;
		default:
			System.out.println("Key unknown : " + key);
			break;
		}
		
		
		return defaultFeature;
	}
	
	public DefaultFeature transformToFeature(JsonArray array) {
		for(JsonElement e : array) {
			
			
		}
		return null;
	}

}
