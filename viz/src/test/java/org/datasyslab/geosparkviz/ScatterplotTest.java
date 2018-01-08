/**
 * FILE: ScatterplotTest.java
 * PATH: org.datasyslab.geosparkviz.ScatterplotTest.java
 * Copyright (c) 2015-2017 GeoSpark Development Team
 * All rights reserved.
 */
package org.datasyslab.geosparkviz;

import java.awt.Color;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.storage.StorageLevel;
import org.datasyslab.geosparkviz.core.ImageGenerator;
import org.datasyslab.geosparkviz.core.ImageStitcher;
import org.datasyslab.geosparkviz.extension.visualizationEffect.ScatterPlot;
import org.datasyslab.geosparkviz.utils.ColorizeOption;
import org.datasyslab.geosparkviz.utils.ImageType;
import org.datasyslab.geosparkviz.utils.RasterizationUtils;
import org.datasyslab.geospark.enums.FileDataSplitter;

import org.datasyslab.geospark.spatialRDD.LineStringRDD;
import org.datasyslab.geospark.spatialRDD.PointRDD;
import org.datasyslab.geospark.spatialRDD.PolygonRDD;
import org.datasyslab.geospark.spatialRDD.RectangleRDD;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.vividsolutions.jts.geom.Envelope;


import scala.Tuple2;


// TODO: Auto-generated Javadoc
/**
 * The Class ScatterplotTest.
 */
public class ScatterplotTest extends GeoSparkVizTestBase{
	


	/**
	 * Sets the up before class.
	 *
	 * @throws Exception the exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		initialize(ScatterplotTest.class.getSimpleName());
	}

	/**
	 * Tear down.
	 *
	 * @throws Exception the exception
	 */
	@AfterClass
	public static void tearDown() throws Exception {
		sparkContext.stop();
	}

	/**
	 * Test encode decode id.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void testEncodeDecodeId() throws Exception {
		int pixelX = 33;
		int pixelY = 44;
		int resolutionX = 1000;
		int resolutionY = 600;
		int serialId = RasterizationUtils.Encode2DTo1DId(resolutionX, resolutionY, pixelX, pixelY);
		Tuple2<Integer, Integer> pixelCoordinate = RasterizationUtils.Decode1DTo2DId(resolutionX, resolutionY, serialId);
		assert pixelCoordinate._1 == pixelX;
		assert pixelCoordinate._2 == pixelY;
		
		pixelX = 1000;
		pixelY = 2;
		resolutionX = 2000;
		resolutionY = 1200;
		serialId = RasterizationUtils.Encode2DTo1DId(resolutionX, resolutionY, pixelX, pixelY);
		pixelCoordinate = RasterizationUtils.Decode1DTo2DId(resolutionX, resolutionY, serialId);
		assert pixelCoordinate._1 == pixelX;
		assert pixelCoordinate._2 == pixelY;
	}
	
	/**
	 * Test point RDD visualization.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void testPointRDDVisualization() throws Exception {
		PointRDD spatialRDD = new PointRDD(sparkContext, PointInputLocation, PointOffset, PointSplitter, false, PointNumPartitions, StorageLevel.MEMORY_ONLY());
		ScatterPlot visualizationOperator = new ScatterPlot(1000,600,USMainLandBoundary,false);
		visualizationOperator.CustomizeColor(255, 255, 255, 255, Color.GREEN, true);
		visualizationOperator.Visualize(sparkContext, spatialRDD);
		ImageGenerator imageGenerator = new  ImageGenerator();
		imageGenerator.SaveRasterImageAsLocalFile(visualizationOperator.rasterImage, "./target/scatterplot/PointRDD",ImageType.PNG);
		
		visualizationOperator = new ScatterPlot(1000,600,USMainLandBoundary,false,true);
		visualizationOperator.CustomizeColor(255, 255, 255, 255, Color.GREEN, true);
		visualizationOperator.Visualize(sparkContext, spatialRDD);
		imageGenerator = new  ImageGenerator();
		imageGenerator.SaveVectorImageAsLocalFile(visualizationOperator.vectorImage, "./target/scatterplot/PointRDD",ImageType.SVG);
	}

	/**
	 * Test point RDD visualization with parallel rendering.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void testPointRDDVisualizationWithParallelRendering() throws Exception {
		PointRDD spatialRDD = new PointRDD(sparkContext, PointInputLocation, PointOffset, PointSplitter, false, PointNumPartitions, StorageLevel.MEMORY_ONLY());
		ScatterPlot visualizationOperator = new ScatterPlot(1000,600,USMainLandBoundary, ColorizeOption.NORMAL,
				false, 4, 4, true, false);
		visualizationOperator.CustomizeColor(255, 255, 255, 255, Color.GREEN, true);
		visualizationOperator.Visualize(sparkContext, spatialRDD);

		ImageGenerator imageGenerator = new ImageGenerator();
		imageGenerator.SaveRasterImageAsLocalFile(visualizationOperator.distributedRasterImage, "./target/heatmap/PointRDD-parallelrender",ImageType.PNG, 0, 4, 4);
		ImageStitcher.stitchImagePartitionsFromLocalFile("./target/scatterplot/PointRDD-parallelrender", 1000, 600,0,4,4);
	}
	
	/**
	 * Test save as distributed file.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void testSaveAsDistributedFile() throws Exception {
		PointRDD spatialRDD = new PointRDD(sparkContext, PointInputLocation, PointOffset, PointSplitter, false, PointNumPartitions, StorageLevel.MEMORY_ONLY());
		ScatterPlot visualizationOperator = new ScatterPlot(1000,600,USMainLandBoundary,false,2,2,true,false);
		visualizationOperator.CustomizeColor(255, 255, 255, 255, Color.GREEN, true);
		visualizationOperator.Visualize(sparkContext, spatialRDD);
		ImageGenerator imageGenerator = new  ImageGenerator();
		
        String scatterPlotOutputPath = System.getProperty("user.dir") + "/target/scatterplot/";

		imageGenerator.SaveRasterImageAsLocalFile(visualizationOperator.distributedRasterImage, scatterPlotOutputPath+"PointRDD-parallel-raster",ImageType.PNG);
		
		visualizationOperator = new ScatterPlot(1000,600,USMainLandBoundary,false,-1,-1,true,true);
		visualizationOperator.CustomizeColor(255, 255, 255, 255, Color.GREEN, true);
		visualizationOperator.Visualize(sparkContext, spatialRDD);
		imageGenerator = new  ImageGenerator();
		imageGenerator.SaveVectorImageAsLocalFile(visualizationOperator.distributedVectorImage, scatterPlotOutputPath+"PointRDD-parallel-vector",ImageType.SVG);
	}
	
	/**
	 * Test rectangle RDD visualization.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void testRectangleRDDVisualization() throws Exception {
		RectangleRDD spatialRDD = new RectangleRDD(sparkContext, RectangleInputLocation, RectangleSplitter, false, RectangleNumPartitions, StorageLevel.MEMORY_ONLY());
		ScatterPlot visualizationOperator = new ScatterPlot(1000,600,USMainLandBoundary,false);
		visualizationOperator.CustomizeColor(255, 255, 255, 255, Color.RED, true);
		visualizationOperator.Visualize(sparkContext, spatialRDD);
		ImageGenerator imageGenerator = new  ImageGenerator();
		imageGenerator.SaveRasterImageAsLocalFile(visualizationOperator.rasterImage, "./target/scatterplot/RectangleRDD",ImageType.GIF);
		
		visualizationOperator = new ScatterPlot(1000,600,USMainLandBoundary,false,true);
		visualizationOperator.CustomizeColor(255, 255, 255, 255, Color.RED, true);
		visualizationOperator.Visualize(sparkContext, spatialRDD);
		imageGenerator = new  ImageGenerator();
		imageGenerator.SaveVectorImageAsLocalFile(visualizationOperator.vectorImage, "./target/scatterplot/RectangleRDD",ImageType.SVG);	
	}
	
	/**
	 * Test polygon RDD visualization.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void testPolygonRDDVisualization() throws Exception {
		//UserSuppliedPolygonMapper userSuppliedPolygonMapper = new UserSuppliedPolygonMapper();
		PolygonRDD spatialRDD = new PolygonRDD(sparkContext, PolygonInputLocation, PolygonSplitter, false, PolygonNumPartitions, StorageLevel.MEMORY_ONLY());
		ScatterPlot visualizationOperator = new ScatterPlot(1000,600,USMainLandBoundary,false);
		visualizationOperator.CustomizeColor(255, 255, 255, 255, Color.GREEN, true);
		visualizationOperator.Visualize(sparkContext, spatialRDD);
		ImageGenerator imageGenerator = new  ImageGenerator();
		imageGenerator.SaveRasterImageAsLocalFile(visualizationOperator.rasterImage, "./target/scatterplot/PolygonRDD",ImageType.GIF);
		
		visualizationOperator = new ScatterPlot(1000,600,USMainLandBoundary,false,true);
		visualizationOperator.CustomizeColor(255, 255, 255, 255, Color.GREEN, true);
		visualizationOperator.Visualize(sparkContext, spatialRDD);
		imageGenerator = new  ImageGenerator();
		imageGenerator.SaveVectorImageAsLocalFile(visualizationOperator.vectorImage, "./target/scatterplot/PolygonRDD",ImageType.SVG);
	}
	
	/**
	 * Test line string RDD visualization.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void testLineStringRDDVisualization() throws Exception {
		int resolutionY = 800;
		int resolutionX = RasterizationUtils.GetWidthFromHeight(resolutionY, USMainLandBoundary);
		//UserSuppliedLineStringMapper userSuppliedLineStringMapper = new UserSuppliedLineStringMapper();
		LineStringRDD spatialRDD = new LineStringRDD(sparkContext, LineStringInputLocation, LineStringSplitter, false, LineStringNumPartitions, StorageLevel.MEMORY_ONLY());
		ScatterPlot visualizationOperator = new ScatterPlot(resolutionX,resolutionY,USMainLandBoundary,false);
		visualizationOperator.CustomizeColor(255, 255, 255, 255, Color.GREEN, true);
		visualizationOperator.Visualize(sparkContext, spatialRDD);
		ImageGenerator imageGenerator = new  ImageGenerator();
		imageGenerator.SaveRasterImageAsLocalFile(visualizationOperator.rasterImage, "./target/scatterplot/LineStringRDD",ImageType.GIF);
		
		visualizationOperator = new ScatterPlot(resolutionX,resolutionY,USMainLandBoundary,false,true);
		visualizationOperator.CustomizeColor(255, 255, 255, 255, Color.GREEN, true);
		visualizationOperator.Visualize(sparkContext, spatialRDD);
		imageGenerator = new  ImageGenerator();
		imageGenerator.SaveVectorImageAsLocalFile(visualizationOperator.vectorImage, "./target/scatterplot/LineStringRDD",ImageType.SVG);
	}
}
