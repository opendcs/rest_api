package org.opendcs.odcsapi.res;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import decodes.db.ConfigSensor;
import decodes.db.DecodesScript;
import decodes.db.PlatformConfig;
import decodes.db.PlatformConfigList;
import decodes.db.ScriptSensor;
import decodes.db.UnitConverter;
import decodes.sql.DbKey;
import decodes.xml.DecodesScriptParser;
import org.junit.jupiter.api.Test;
import org.opendcs.odcsapi.beans.ApiConfigRef;
import org.opendcs.odcsapi.beans.ApiConfigScript;
import org.opendcs.odcsapi.beans.ApiConfigScriptSensor;
import org.opendcs.odcsapi.beans.ApiPlatformConfig;
import org.opendcs.odcsapi.beans.ApiUnitConverter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.opendcs.odcsapi.res.ConfigResources.coefficientMap;
import static org.opendcs.odcsapi.res.ConfigResources.map;

final class ConfigResourcesTest
{
	@Test
	void testPlatformConfigListMap() throws Exception
	{
		PlatformConfigList pcl = new PlatformConfigList();
		PlatformConfig config = new PlatformConfig();
		config.numPlatformsUsing = 11;
		config.configName = "Test config";
		config.description = "Platform description";
		config.setId(DbKey.createDbKey(5899L));
		pcl.add(config);

		List<ApiConfigRef> configRefs = map(pcl);

		assertNotNull(configRefs);
		assertEquals(config.numPlatformsUsing, configRefs.get(0).getNumPlatforms());
		assertEquals(config.configName, configRefs.get(0).getName());
		assertEquals(config.description, configRefs.get(0).getDescription());
		assertEquals(config.getId().getValue(), configRefs.get(0).getConfigId());
	}

	@Test
	void testPlatformConfigMap() throws Exception
	{
		PlatformConfig config = new PlatformConfig();
		config.numPlatformsUsing = 11;
		config.configName = "Test config";
		config.description = "Platform description";
		config.setId(DbKey.createDbKey(5899L));
		Vector<DecodesScript> scripts = new Vector<>();
		PlatformConfig scriptConfig = new PlatformConfig();
		scriptConfig.numPlatformsUsing = 1;
		scriptConfig.configName = "Test script config";
		scriptConfig.description = "Script config description";
		DecodesScript.DecodesScriptBuilder builder = new DecodesScript.DecodesScriptBuilder(new DecodesScriptParser());
		builder.platformConfig(scriptConfig);
		DecodesScript script = builder.build();
		script.scriptName = "Test script";
		script.scriptType = "Test";
		scripts.add(script);
		config.decodesScripts = scripts;
		PlatformConfig sensor = new PlatformConfig();
		sensor.numPlatformsUsing = 1;
		sensor.configName = "Test sensor";
		sensor.description = "Sensor description";
		ConfigSensor sensorConfig = new ConfigSensor(sensor, 12);
		sensorConfig.absoluteMax = 100.0;
		sensorConfig.absoluteMin = 0.0;
		sensorConfig.recordingInterval = 15;
		sensorConfig.recordingMode = 'C';
		sensorConfig.sensorName = "Test sensor";
		sensorConfig.setUsgsStatCode("00000");
		config.addSensor(sensorConfig);

		ApiPlatformConfig apiConfig = map(config);

		assertNotNull(apiConfig);
		assertEquals(config.numPlatformsUsing, apiConfig.getNumPlatforms());
		assertEquals(config.configName, apiConfig.getName());
		assertEquals(config.description, apiConfig.getDescription());
		assertEquals(config.getId().getValue(), apiConfig.getConfigId());
		assertEquals(config.decodesScripts.size(), apiConfig.getScripts().size());
		assertEquals(config.decodesScripts.get(0).scriptName, apiConfig.getScripts().get(0).getName());
		assertEquals(config.getSensorVec().get(0).absoluteMax, apiConfig.getConfigSensors().get(0).getAbsoluteMax());
		assertEquals(config.getSensorVec().get(0).absoluteMin, apiConfig.getConfigSensors().get(0).getAbsoluteMin());
		assertEquals(config.getSensorVec().get(0).recordingInterval, apiConfig.getConfigSensors().get(0).getRecordingInterval());
		assertEquals(config.getSensorVec().get(0).recordingMode, apiConfig.getConfigSensors().get(0).getRecordingMode());
		assertEquals(config.getSensorVec().get(0).sensorName, apiConfig.getConfigSensors().get(0).getSensorName());
	}

	@Test
	void testApiPlatformConfigMap() throws Exception
	{
		ApiPlatformConfig apiConfig = new ApiPlatformConfig();
		apiConfig.setNumPlatforms(11);
		apiConfig.setName("Test config");
		apiConfig.setDescription("Platform description");
		apiConfig.setScripts(scriptListBuilder());
		apiConfig.setConfigId(5899L);

		PlatformConfig config = map(apiConfig);

		assertNotNull(config);
		assertEquals(apiConfig.getNumPlatforms(), config.numPlatformsUsing);
		assertEquals(apiConfig.getName(), config.configName);
		assertEquals(apiConfig.getDescription(), config.description);
		assertEquals(apiConfig.getConfigId(), config.getId().getValue());
		for (Iterator<DecodesScript> scriptName = config.getScripts(); scriptName.hasNext(); )
		{
			DecodesScript script = scriptName.next();
			assertEquals(apiConfig.getScripts().get(0).getName(), script.scriptName);
			for (ScriptSensor sensor : script.scriptSensors)
			{
				assertEquals(apiConfig.getScripts().get(0).getScriptSensors().get(0).getSensorNumber(), sensor.sensorNumber);
				assertMatch(apiConfig.getScripts().get(0).getScriptSensors().get(0).getUnitConverter(), sensor.execConverter);
			}
		}
		for (Iterator<ConfigSensor> sensor = config.getSensors(); sensor.hasNext(); )
		{
			ConfigSensor sensorConfig = sensor.next();
			assertEquals(apiConfig.getConfigSensors().get(0).getAbsoluteMax(), sensorConfig.absoluteMax);
			assertEquals(apiConfig.getConfigSensors().get(0).getAbsoluteMin(), sensorConfig.absoluteMin);
			assertEquals(apiConfig.getConfigSensors().get(0).getRecordingInterval(), sensorConfig.recordingInterval);
			assertEquals(apiConfig.getConfigSensors().get(0).getRecordingMode(), sensorConfig.recordingMode);
			assertEquals(apiConfig.getConfigSensors().get(0).getSensorName(), sensorConfig.sensorName);
			assertEquals(apiConfig.getConfigSensors().get(0).getSensorNumber(), sensorConfig.sensorNumber);
			assertEquals(apiConfig.getConfigSensors().get(0).getUsgsStatCode(), sensorConfig.getUsgsStatCode());
			assertEquals(apiConfig.getConfigSensors().get(0).getDataTypes(), sensorConfig.getDataType());
			assertEquals(apiConfig.getConfigSensors().get(0).getProperties(), sensorConfig.getProperties());
		}
	}

	@Test
	void testApiConfigScriptMap() throws Exception
	{
		List<ApiConfigScript> scripts = scriptListBuilder();

		Vector<DecodesScript> decodesScripts = map(scripts, new PlatformConfig());

		assertNotNull(decodesScripts);
		assertEquals(scripts.size(), decodesScripts.size());
		assertEquals(scripts.get(0).getName(), decodesScripts.get(0).scriptName);
		for (ScriptSensor sensor : decodesScripts.get(0).scriptSensors)
		{
			assertEquals(scripts.get(0).getScriptSensors().get(0).getSensorNumber(), sensor.sensorNumber);
			assertMatch(scripts.get(0).getScriptSensors().get(0).getUnitConverter(), sensor.execConverter);
		}
	}

	@Test
	void testApiConfigScriptSensorMap() throws Exception
	{
		ApiConfigScriptSensor sensor = new ApiConfigScriptSensor();
		sensor.setSensorNumber(1);
		ApiUnitConverter unitConverter = new ApiUnitConverter();
		unitConverter.setUcId(1234L);
		unitConverter.setAlgorithm("Poly-5");
		unitConverter.setFromAbbr("ft");
		unitConverter.setToAbbr("m");
		unitConverter.setA(1.0);
		unitConverter.setB(2.0);
		unitConverter.setC(3.0);
		unitConverter.setD(4.0);
		unitConverter.setE(5.0);
		unitConverter.setF(6.0);
		sensor.setUnitConverter(unitConverter);

		ScriptSensor decodesSensor = map(sensor);

		assertNotNull(decodesSensor);
		assertEquals(sensor.getSensorNumber(), decodesSensor.sensorNumber);
		assertMatch(sensor.getUnitConverter(), decodesSensor.execConverter);
		assertEquals(sensor.getUnitConverter().getFromAbbr(), decodesSensor.rawConverter.fromAbbr);
		assertEquals(sensor.getUnitConverter().getToAbbr(), decodesSensor.rawConverter.toAbbr);
		assertEquals(sensor.getUnitConverter().getA(), decodesSensor.rawConverter.coefficients[0]);
		assertEquals(sensor.getUnitConverter().getB(), decodesSensor.rawConverter.coefficients[1]);
		assertEquals(sensor.getUnitConverter().getC(), decodesSensor.rawConverter.coefficients[2]);
		assertEquals(sensor.getUnitConverter().getD(), decodesSensor.rawConverter.coefficients[3]);
		assertEquals(sensor.getUnitConverter().getE(), decodesSensor.rawConverter.coefficients[4]);
		assertEquals(sensor.getUnitConverter().getF(), decodesSensor.rawConverter.coefficients[5]);
		assertEquals(sensor.getUnitConverter().getUcId(), decodesSensor.rawConverter.getId().getValue());
		assertEquals(sensor.getUnitConverter().getAlgorithm(), decodesSensor.rawConverter.algorithm);
	}

	@Test
	void testApiUnitConverterMap() throws Exception
	{
		// Test with NullConverter
		ApiUnitConverter unitConverter = new ApiUnitConverter();
		unitConverter.setUcId(1234L);
		unitConverter.setAlgorithm("None");
		unitConverter.setFromAbbr("ft");
		unitConverter.setToAbbr("m");
		unitConverter.setA(1.0);
		unitConverter.setB(2.0);
		unitConverter.setC(3.0);
		unitConverter.setD(4.0);
		unitConverter.setE(5.0);
		unitConverter.setF(6.0);

		UnitConverter decodesUc = map(unitConverter);

		assertNotNull(decodesUc);
		assertEquals(unitConverter.getFromAbbr(), decodesUc.getFromAbbr());
		assertEquals(unitConverter.getToAbbr(), decodesUc.getToAbbr());
		assertEquals(20.0, decodesUc.convert(20.0));

		// Test with Poly5Converter
		unitConverter.setAlgorithm("Poly-5");

		decodesUc = map(unitConverter);

		assertNotNull(decodesUc);
		assertEquals(unitConverter.getFromAbbr(), decodesUc.getFromAbbr());
		assertEquals(unitConverter.getToAbbr(), decodesUc.getToAbbr());
		assertEquals(3545706.0, decodesUc.convert(20.0));

		// Test with CompositeConverter
		unitConverter.setAlgorithm("Composite");
		unitConverter.setFromAbbr("in");

		assertThrows(IllegalArgumentException.class, () -> map(unitConverter));

		// Test with LinearConverter
		unitConverter.setAlgorithm("Linear");
		unitConverter.setFromAbbr("ft");

		decodesUc = map(unitConverter);

		assertNotNull(decodesUc);
		assertEquals(unitConverter.getFromAbbr(), decodesUc.getFromAbbr());
		assertEquals(unitConverter.getToAbbr(), decodesUc.getToAbbr());
		assertEquals(22.0, decodesUc.convert(20.0));

		// Test with USGSStandardConverter
		unitConverter.setAlgorithm("USGS-Standard");
		decodesUc = map(unitConverter);

		assertNotNull(decodesUc);
		assertEquals(unitConverter.getFromAbbr(), decodesUc.getFromAbbr());
		assertEquals(unitConverter.getToAbbr(), decodesUc.getToAbbr());
		assertEquals(10652.0, decodesUc.convert(20.0));
	}

	@Test
	void testCoefficientMap()
	{
		ApiUnitConverter unitConverter = new ApiUnitConverter();
		unitConverter.setA(1.0);
		unitConverter.setB(2.0);
		unitConverter.setC(3.0);
		unitConverter.setD(4.0);
		unitConverter.setE(5.0);
		unitConverter.setF(6.0);

		double[] coefficients = coefficientMap(unitConverter);

		assertEquals(unitConverter.getA(), coefficients[0]);
		assertEquals(unitConverter.getB(), coefficients[1]);
		assertEquals(unitConverter.getC(), coefficients[2]);
		assertEquals(unitConverter.getD(), coefficients[3]);
		assertEquals(unitConverter.getE(), coefficients[4]);
		assertEquals(unitConverter.getF(), coefficients[5]);
	}

	private static void assertMatch(ApiUnitConverter apiUc, UnitConverter decodesUc) throws Exception
	{
		assertEquals(apiUc.getFromAbbr(), decodesUc.getFromAbbr());
		assertEquals(apiUc.getToAbbr(), decodesUc.getToAbbr());
		assertEquals(123456.0, decodesUc.convert(10.0));
	}

	private static ArrayList<ApiConfigScript> scriptListBuilder()
	{
		ArrayList<ApiConfigScript> configScriptList = new ArrayList<>();
		ApiConfigScript script = new ApiConfigScript();
		script.setName("Test script");
		script.setScriptSensors(buildScriptSensorList());
		configScriptList.add(script);
		return configScriptList;
	}

	private static ArrayList<ApiConfigScriptSensor> buildScriptSensorList()
	{
		ArrayList<ApiConfigScriptSensor> scriptSensorList = new ArrayList<>();
		ApiConfigScriptSensor scriptSensor = new ApiConfigScriptSensor();
		scriptSensor.setSensorNumber(1);
		ApiUnitConverter unitConverter = new ApiUnitConverter();
		unitConverter.setUcId(1234L);
		unitConverter.setAlgorithm("Poly-5");
		unitConverter.setFromAbbr("ft");
		unitConverter.setToAbbr("m");
		unitConverter.setA(1.0);
		unitConverter.setB(2.0);
		unitConverter.setC(3.0);
		unitConverter.setD(4.0);
		unitConverter.setE(5.0);
		unitConverter.setF(6.0);
		scriptSensor.setUnitConverter(unitConverter);
		scriptSensorList.add(scriptSensor);
		return scriptSensorList;
	}
}
