package org.opendcs.odcsapi.res;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import decodes.db.DecodesScript;
import decodes.db.PlatformConfig;
import decodes.db.PlatformConfigList;
import decodes.db.Poly5Converter;
import decodes.db.ScriptSensor;
import decodes.db.UnitConverter;
import decodes.sql.DbKey;
import org.junit.jupiter.api.Test;
import org.opendcs.odcsapi.beans.ApiConfigRef;
import org.opendcs.odcsapi.beans.ApiConfigScript;
import org.opendcs.odcsapi.beans.ApiConfigScriptSensor;
import org.opendcs.odcsapi.beans.ApiPlatformConfig;
import org.opendcs.odcsapi.beans.ApiUnitConverter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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

		ApiPlatformConfig apiConfig = map(config);

		assertNotNull(apiConfig);
		assertEquals(config.numPlatformsUsing, apiConfig.getNumPlatforms());
		assertEquals(config.configName, apiConfig.getName());
		assertEquals(config.description, apiConfig.getDescription());
		assertEquals(config.getId().getValue(), apiConfig.getConfigId());
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
				assertMatch(apiConfig.getScripts().get(0).getScriptSensors().get(0).getUnitConverter(), (Poly5Converter) sensor.execConverter);
			}
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
			assertMatch(scripts.get(0).getScriptSensors().get(0).getUnitConverter(), (Poly5Converter) sensor.execConverter);
		}
	}

	@Test
	void testApiConfigScriptSensorMap() throws Exception
	{
		ApiConfigScriptSensor sensor = new ApiConfigScriptSensor();
		sensor.setSensorNumber(1);
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
		sensor.setUnitConverter(unitConverter);

		ScriptSensor decodesSensor = map(sensor);

		assertNotNull(decodesSensor);
		assertEquals(sensor.getSensorNumber(), decodesSensor.sensorNumber);
		assertMatch(sensor.getUnitConverter(), (Poly5Converter) decodesSensor.execConverter);
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
		assertEquals(3545706.0, decodesUc.convert(20.0));
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

	private static void assertMatch(ApiUnitConverter apiUc, Poly5Converter decodesUc)
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
		unitConverter.setAlgorithm("None");
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
