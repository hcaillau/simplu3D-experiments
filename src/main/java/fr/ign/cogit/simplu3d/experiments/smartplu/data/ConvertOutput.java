package fr.ign.cogit.simplu3d.experiments.smartplu.data;

import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.feature.IFeatureCollection;
import fr.ign.cogit.geoxygene.api.spatial.geomroot.IGeometry;
import fr.ign.cogit.geoxygene.feature.DefaultFeature;
import fr.ign.cogit.geoxygene.feature.FT_FeatureCollection;
import fr.ign.cogit.geoxygene.sig3d.geometry.Box3D;
import fr.ign.cogit.geoxygene.sig3d.semantic.DTM;
import fr.ign.cogit.geoxygene.sig3d.util.ColorShade;
import fr.ign.cogit.geoxygene.util.attribute.AttributeManager;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileReader;
import fr.ign.cogit.geoxygene.util.conversion.ShapefileWriter;
import fr.ign.cogit.simplu3d.rjmcmc.cuboid.geometry.impl.Cuboid;
import fr.ign.cogit.simplu3d.rjmcmc.cuboid.geometry.loader.LoaderCuboid;

public class ConvertOutput {

	public static void main(String[] args) throws Exception {

		String shapefileIn = "/home/mbrasebin/Documents/Donnees/demo-numen/sorties/22278/buildings/buildings.shp";

		String rasterIn = "/home/mbrasebin/Documents/Donnees/demo-numen/sorties/export/22278/DTM.asc";
		
		String shapefileOut = "/home/mbrasebin/Documents/Donnees/demo-numen/sorties/export/22278/buildings.shp";
		
		
		DTM mnt = new DTM(rasterIn, "DTM", false, 1, ColorShade.BLUE_CYAN_GREEN_YELLOW_WHITE);
		IFeatureCollection<IFeature> featColInl = ShapefileReader.read(shapefileIn);
		IFeatureCollection<IFeature> featColl = new FT_FeatureCollection<>();

		for (IFeature featIn : featColInl) {

			Cuboid c = LoaderCuboid.transformFeature(featIn);

			IFeature feat = new DefaultFeature();
			feat.setGeom(c.getFootprint());
			
			
			IGeometry mapGeomed = mnt.mapGeom(feat.getGeom(),0, true,false);
			
			double z = (new Box3D(mapGeomed)).getLLDP().getZ();

			AttributeManager.addAttribute(feat, "height", c.getHeight(), "Double");
			AttributeManager.addAttribute(feat, "area", c.getFootprint().area(), "Double");
			AttributeManager.addAttribute(feat, "volume", c.getHeight() * c.getFootprint().area(), "Double");
			AttributeManager.addAttribute(feat, "idpar", featIn.getAttribute("idpar"), "String");
			AttributeManager.addAttribute(feat, "imu_dir", featIn.getAttribute("imu_dir"), "String");
			
			AttributeManager.addAttribute(feat, "z_min",  z, "Double");

			featColl.add(feat);
		}

		ShapefileWriter.write(featColl, shapefileOut);

	}

}
