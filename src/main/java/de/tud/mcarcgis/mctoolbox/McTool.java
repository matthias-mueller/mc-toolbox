package de.tud.mcarcgis.mctoolbox;

import java.awt.GridLayout;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.n52.movingcode.runtime.codepackage.MovingCodePackage;
import org.n52.movingcode.runtime.iodata.IOParameter;
import org.n52.movingcode.runtime.iodata.IOParameterMap;
import org.n52.movingcode.runtime.iodata.ParameterID;
import org.n52.movingcode.runtime.processors.AbstractProcessor;
import org.n52.movingcode.runtime.processors.ProcessorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.esri.arcgis.datasourcesfile.DEFile;
import com.esri.arcgis.datasourcesfile.DEFileType;
import com.esri.arcgis.datasourcesfile.DELayerType;
import com.esri.arcgis.datasourcesfile.DETextFileType;
import com.esri.arcgis.framework.IApplication;
import com.esri.arcgis.geodatabase.DEFeatureClassType;
import com.esri.arcgis.geodatabase.DERasterBandType;
import com.esri.arcgis.geodatabase.DERasterDataset;
import com.esri.arcgis.geodatabase.DERasterDatasetType;
import com.esri.arcgis.geodatabase.GPMessage;
import com.esri.arcgis.geodatabase.IGPCodedValueDomain;
import com.esri.arcgis.geodatabase.IGPDomain;
import com.esri.arcgis.geodatabase.IGPMessage;
import com.esri.arcgis.geodatabase.IGPMessages;
import com.esri.arcgis.geodatabase.IGPName;
import com.esri.arcgis.geodatabase.IGPValue;
import com.esri.arcgis.geodatabase.esriGPMessageSeverity;
import com.esri.arcgis.geoprocessing.BaseGeoprocessingTool;
import com.esri.arcgis.geoprocessing.GPBoolean;
import com.esri.arcgis.geoprocessing.GPBooleanType;
import com.esri.arcgis.geoprocessing.GPCodedValueDomain;
import com.esri.arcgis.geoprocessing.GPCompositeDataType;
import com.esri.arcgis.geoprocessing.GPDataFileType;
import com.esri.arcgis.geoprocessing.GPDate;
import com.esri.arcgis.geoprocessing.GPDateType;
import com.esri.arcgis.geoprocessing.GPDouble;
import com.esri.arcgis.geoprocessing.GPDoubleType;
import com.esri.arcgis.geoprocessing.GPFeatureLayerType;
import com.esri.arcgis.geoprocessing.GPFeatureRecordSetLayerType;
import com.esri.arcgis.geoprocessing.GPLayerType;
import com.esri.arcgis.geoprocessing.GPLong;
import com.esri.arcgis.geoprocessing.GPLongType;
import com.esri.arcgis.geoprocessing.GPParameter;
import com.esri.arcgis.geoprocessing.GPRasterDataLayerType;
import com.esri.arcgis.geoprocessing.GPRasterLayerType;
import com.esri.arcgis.geoprocessing.GPString;
import com.esri.arcgis.geoprocessing.GPStringType;
import com.esri.arcgis.geoprocessing.GPToolName;
import com.esri.arcgis.geoprocessing.IGPEnvironmentManager;
import com.esri.arcgis.geoprocessing.IGPParameter;
import com.esri.arcgis.geoprocessing.esriGPParameterDirection;
import com.esri.arcgis.geoprocessing.esriGPParameterType;
import com.esri.arcgis.interop.AutomationException;
import com.esri.arcgis.system.Array;
import com.esri.arcgis.system.IArray;
import com.esri.arcgis.system.IName;
import com.esri.arcgis.system.ITrackCancel;

public class McTool extends BaseGeoprocessingTool {
	
	private static Logger LOGGER = LoggerFactory.getLogger(McTool.class);
	private final MovingCodePackage mcp;
	final IGPName fullName; 

	public McTool(MovingCodePackage mcp, IGPName fullName) {
		this.mcp = mcp;
		this.fullName = fullName;
	}

	/**
	 * Returns name of the tool This name appears when executing the tool at the
	 * command line or in scripting. This name should be unique to each toolbox
	 * and must not contain spaces.
	 */
	public String getName() throws IOException, AutomationException {
		return mcp.getPackageId().getCanonicalName();
	}

	/**
	 * Returns Display Name of the tool, as seen in ArcToolbox.
	 */
	public String getDisplayName() throws IOException, AutomationException {
		return mcp.getTitle();
	}

	/**
	 * Returns the full name of the tool
	 */
	public IName getFullName() throws IOException, AutomationException {
		return (IName) fullName;
	}
	
	private static final GPParameter makeGpToolParameter(IOParameter param){
		GPParameter gpParam = new GPParameter();
		
		switch (param.getDirection()) {
		case IN:
			gpParam.setDirection(esriGPParameterDirection.esriGPParameterDirectionInput);
			gpParam.setName(param.getMessageInputIdentifier());
			break;
		
		case OUT:
			gpParam.setDirection(esriGPParameterDirection.esriGPParameterDirectionOutput);
			gpParam.setName(param.getMessageOutputIdentifier());
			break;	
		
		case BOTH:
			gpParam.setDirection(esriGPParameterDirection.esriGPParameterDirectionInput);
			gpParam.setName(param.getMessageInputIdentifier());
			break;	
		
		default:
			// ignore and loop to next param
			continue;
		}
		
		gpParam.setDisplayName(param.get);
		
		parameter1.setDisplayName("Input Parameter1");
		parameter1.setParameterType(esriGPParameterType.esriGPParameterTypeRequired);
		parameter1.setDataTypeByRef(new DEFileType());
		parameter1.setValueByRef(new DEFile());
		parameters.add(parameter1);
		
		return gpParam;
		
		
		/**
		 * 
		 
		IArray parameters = new Array();

		GPParameter parameter1 = new GPParameter();
		parameter1.setName("in_param1");
		parameter1.setDirection(esriGPParameterDirection.esriGPParameterDirectionInput);
		parameter1.setDisplayName("Input Parameter1");
		parameter1.setParameterType(esriGPParameterType.esriGPParameterTypeRequired);
		parameter1.setDataTypeByRef(new DEFileType());
		parameter1.setValueByRef(new DEFile());

		GPParameter parameter2 = new GPParameter();
		parameter2.setName("in_param2");
		parameter2.setDirection(esriGPParameterDirection.esriGPParameterDirectionInput);
		parameter2.setDisplayName("Input Parameter2");
		parameter2.setParameterType(esriGPParameterType.esriGPParameterTypeRequired);
		parameter2.setDataTypeByRef(new GPStringType());
		parameter2.setValueByRef(new GPString());
		parameters.add(parameter2);

		GPParameter parameter3 = new GPParameter();
		parameter3.setName("out_param1");
		parameter3.setDirection(esriGPParameterDirection.esriGPParameterDirectionOutput);
		parameter3.setDisplayName("Output Parameter 1");
		parameter3.setParameterType(esriGPParameterType.esriGPParameterTypeRequired);
		parameter3.setDataTypeByRef(new DEFileType());
		parameter3.setValueByRef(new DEFile());
		parameters.add(parameter3);

		return parameters;
		 * 
		 */

		String[] identifierAndURL = toolName.split("@");

		String wpsURL = identifierAndURL[1];

		WPSClientSession session = WPSClientSession.getInstance();

		ProcessDescriptionType descriptionType = null;
		
		JFrame progressFrame = new JFrame();
		try {
			
			progressFrame.setAlwaysOnTop(true);
			
			progressFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
			
			progressFrame.setSize(400, 80);
			
			progressFrame.setLocationRelativeTo(progressFrame.getRootPane());
			
			GridLayout l = new GridLayout(2, 1);
			
			progressFrame.setLayout(l);
			
			JLabel label1 = new JLabel();
			
			label1.setText("Connecting to Server...");		
			
			progressFrame.add(label1);
			
			JProgressBar jbar = new JProgressBar();
			
			jbar.setDoubleBuffered(true);
			
			jbar.setIndeterminate(true);
			
			progressFrame.add(jbar);
			
			if(!session.getLoggedServices().contains(wpsURL)){				
				progressFrame.setVisible(true);				
			}
			
			/**
			 * TODO: do that maybe beforehand, if we want the input and output
			 * descriptions for the process already at the process selection
			 * panel
			 */
			descriptionType = session
					.getProcessDescription(wpsURL, displayName);
			
			progressFrame.setVisible(false);

		} catch (IOException e) {
			LOGGER.error(e);
			JOptionPane.showMessageDialog(null, "Could not connect to: \n"
					+ wpsURL + ".", "52�North WPS ArcMap Client", 1);
			progressFrame.setVisible(false);
			return null;
		}

		DataInputs dataInputs = descriptionType.getDataInputs();
		InputDescriptionType[] inputDescriptions = dataInputs.getInputArray();

		for (int i = 0; i < inputDescriptions.length; i++) {

			final InputDescriptionType currentDescriptionType = inputDescriptions[i];

			String labelText = currentDescriptionType.getIdentifier()
					.getStringValue();

			InputTypeEnum type = checkType(currentDescriptionType);

			switch (type) {
			case Raster:

				GPCompositeDataType composite = new GPCompositeDataType();
				composite.addDataType(new DERasterBandType());
				composite.addDataType(new DERasterDatasetType());
				composite.addDataType(new GPRasterLayerType());
				composite.addDataType(new GPRasterDataLayerType());
				composite.addDataType(new GPLayerType());
				composite.addDataType(new DELayerType());
				composite.addDataType(new DETextFileType());				
				composite.addDataType(new GPDataFileType());
				composite.addDataType(new DEFileType());
				
				GPParameter parameter1 = new GPParameter();
				parameter1.setName(labelText);
				parameter1
						.setDirection(esriGPParameterDirection.esriGPParameterDirectionInput);
				parameter1.setDisplayName(labelText);
				if (currentDescriptionType.getMinOccurs().intValue() > 0) {
					parameter1
							.setParameterType(esriGPParameterType.esriGPParameterTypeRequired);
				} else {
					parameter1
							.setParameterType(esriGPParameterType.esriGPParameterTypeOptional);
				}
				parameter1.setDataTypeByRef(composite);
				parameter1.setValueByRef(new DERasterDataset());
				parameters.add(parameter1);

				break;
			case Vector:
//				GPCompositeDataType compositeVector = new GPCompositeDataType();
//				compositeVector.addDataType(new GPFeatureLayerType());
//				compositeVector.addDataType(new DEFeatureClassType());
//				compositeVector.addDataType(new GPLayerType());
//				compositeVector.addDataType(new DELayerType());
//				compositeVector.addDataType(new GPFeatureRecordSetLayerType());

				GPParameter parameterVector = new GPParameter();
				

				GPCompositeDataType compositeVector = new GPCompositeDataType();
				compositeVector.addDataType(new GPStringType());
				compositeVector.addDataType(new GPFeatureLayerType());
				compositeVector.addDataType(new DEFeatureClassType());
				compositeVector.addDataType(new GPLayerType());
				compositeVector.addDataType(new DELayerType());
				compositeVector.addDataType(new GPFeatureRecordSetLayerType());
				compositeVector.addDataType(new DETextFileType());				
				compositeVector.addDataType(new GPDataFileType());
				compositeVector.addDataType(new DEFileType());
				
				parameterVector.setName(labelText);
				parameterVector
						.setDirection(esriGPParameterDirection.esriGPParameterDirectionInput);
				parameterVector.setDisplayName(labelText);
				if (currentDescriptionType.getMinOccurs().intValue() > 0) {
					parameterVector
							.setParameterType(esriGPParameterType.esriGPParameterTypeRequired);
				} else {
					parameterVector
							.setParameterType(esriGPParameterType.esriGPParameterTypeOptional);
				}
//				parameterVector.setDataTypeByRef(compositeVector);
//				parameterVector.setValueByRef(new GPFeatureLayer());
				parameterVector.setDataTypeByRef(compositeVector);
				parameterVector.setValueByRef(new GPString());
				parameters.add(parameterVector);

				addSchemaMimeTypeEncodingToParameters(parameters,
						currentDescriptionType);
				
				try {
					addReferenceParameter(labelText, parameters);
				} catch (Exception e) {
					LOGGER.error("Could not add reference parameter", e);
				}
				
				break;

			case Literal:
				GPParameter parameter2 = new GPParameter();
				parameter2.setName(labelText);
				parameter2
						.setDirection(esriGPParameterDirection.esriGPParameterDirectionInput);
				parameter2.setDisplayName(labelText);
				if (currentDescriptionType.getMinOccurs().intValue() > 0) {
					parameter2
							.setParameterType(esriGPParameterType.esriGPParameterTypeRequired);
				} else {
					parameter2
							.setParameterType(esriGPParameterType.esriGPParameterTypeOptional);
				}

				DomainMetadataType dataType = currentDescriptionType
						.getLiteralData().getDataType();

				boolean noDataTypeFound = false;

				if (dataType != null) {

					String dataTypeString = currentDescriptionType
							.getLiteralData().getDataType().getReference();

					if (dataTypeString == null) {
						dataTypeString = currentDescriptionType
								.getLiteralData().getDataType()
								.getStringValue();
					}
					if (dataTypeString != null) {

						if (dataTypeString.contains("double")) {
							// parameter2.setDataTypeByRef(new GPDoubleType());
							// parameter2.setValueByRef(new GPDouble());
							/*
							 * try this to avoid decimal point/comma issues
							 */
							parameter2.setDataTypeByRef(new GPStringType());
							parameter2.setValueByRef(new GPString());
						} else if (dataTypeString.contains("string")) {
							parameter2.setDataTypeByRef(new GPStringType());
							parameter2.setValueByRef(new GPString());
						} else if (dataTypeString.contains("integer")) {
							parameter2.setDataTypeByRef(new GPLongType());
							parameter2.setValueByRef(new GPLong());
						} else if (dataTypeString.contains("dateTime")) {
							parameter2.setDataTypeByRef(new GPDateType());
							parameter2.setValueByRef(new GPDate());
						} else if (dataTypeString.contains("boolean")) {
							parameter2.setDataTypeByRef(new GPBooleanType());
							parameter2.setValueByRef(new GPBoolean());
						} else if (dataTypeString.contains("float")) {
							parameter2.setDataTypeByRef(new GPDoubleType());
							parameter2.setValueByRef(new GPDouble());
						} else {
							noDataTypeFound = true;
						}
					} else {
						noDataTypeFound = true;
					}

				} else {
					noDataTypeFound = true;
				}
				
				try {
				
				if(currentDescriptionType.getLiteralData().getAllowedValues() != null){
					
				ValueType[] allowedValues = currentDescriptionType
				.getLiteralData().getAllowedValues().getValueArray();
				
				IGPCodedValueDomain domain = new GPCodedValueDomain();

				for (ValueType allowedValue : allowedValues) {
					
					String string = allowedValue.getStringValue();	
					
					LOGGER.debug("Allowed value " + string);
					
					domain.addStringCode(string, string);
				}
				// Assign the domain to the parameter.
				parameter2.setDomainByRef((IGPDomain) domain);
				
				}
				if (!noDataTypeFound) {
					parameters.add(parameter2);
				} else {
					parameter2.setDataTypeByRef(new GPStringType());
					
					if(!currentDescriptionType.getLiteralData().getDefaultValue().equals("")){
						
						GPString gpString = new GPString();
						
						gpString.setValue(currentDescriptionType.getLiteralData().getDefaultValue());
						
						parameter2.setValueByRef(gpString);						
					}else{						
						parameter2.setValueByRef(new GPString());						
					}
					parameters.add(parameter2);
				}
					
				} catch (Exception e) {
					LOGGER.error("New things dont seem to work", e);
				}
				
				break;
			default:
				break;
			}

		}

		for (OutputDescriptionType outDescType : descriptionType
				.getProcessOutputs().getOutputArray()) {

			boolean supportsRaster = false;
			boolean supportsVector = false;

			String mimeType = outDescType.getComplexOutput().getDefault()
					.getFormat().getMimeType();

			List<String> rasterMimeTypeList = new ArrayList<String>();

			rasterMimeTypeList.add("application/geotiff");
			rasterMimeTypeList.add("image/tiff");
			rasterMimeTypeList.add("image/geotiff");
			rasterMimeTypeList.add("application/x-geotiff");

			if (rasterMimeTypeList.contains(mimeType)) {
				supportsRaster = true;
			}

			List<String> vectocMimeTypeList = new ArrayList<String>();
			vectocMimeTypeList.add("application/x-zipped-shp");
			vectocMimeTypeList.add("text/xml");

			if (vectocMimeTypeList.contains(mimeType)) {
				supportsVector = true;
			}

			if (!supportsRaster && !supportsVector) {

				ComplexDataCombinationsType supportedCombinationTypes = outDescType
						.getComplexOutput().getSupported();

				ComplexDataDescriptionType[] supportedFormats = supportedCombinationTypes
						.getFormatArray();

				for (int i = 0; i < supportedFormats.length; i++) {

					String tmpMimeType = supportedFormats[i].getMimeType();

					if (rasterMimeTypeList.contains(tmpMimeType)) {
						supportsRaster = true;
						break;
					} else if (vectocMimeTypeList.contains(tmpMimeType)) {
						supportsVector = true;
						break;
					}

				}

			}

			if (supportsRaster) {
				GPParameter parameter4 = new GPParameter();
				parameter4.setName(outputPrefix
						+ outDescType.getIdentifier().getStringValue());
				parameter4
						.setDirection(esriGPParameterDirection.esriGPParameterDirectionOutput);
				parameter4.setDisplayName(outDescType.getIdentifier()
						.getStringValue());
				parameter4
						.setParameterType(esriGPParameterType.esriGPParameterTypeOptional);
				parameter4.setDataTypeByRef(new DERasterDatasetType());
				parameter4.setValueByRef(new DERasterDataset());
				parameters.add(parameter4);

			} else if (supportsVector) {
				GPCompositeDataType compositeVector = new GPCompositeDataType();
				compositeVector.addDataType(new GPFeatureLayerType());
				compositeVector.addDataType(new DEFeatureClassType());
				compositeVector.addDataType(new GPLayerType());
				compositeVector.addDataType(new DELayerType());
				compositeVector.addDataType(new GPFeatureRecordSetLayerType());
				compositeVector.addDataType(new DETextFileType());				
				compositeVector.addDataType(new GPDataFileType());
				compositeVector.addDataType(new DEFileType());
				compositeVector.addDataType(new GPStringType());
				GPParameter parameter4 = new GPParameter();
				parameter4.setName(outputPrefix
						+ outDescType.getIdentifier().getStringValue());
				parameter4
						.setDirection(esriGPParameterDirection.esriGPParameterDirectionOutput);
				parameter4.setDisplayName(outDescType.getIdentifier()
						.getStringValue());
				parameter4
						.setParameterType(esriGPParameterType.esriGPParameterTypeOptional);
//				parameter4.setDataTypeByRef(new DEFeatureClassType());
				parameter4.setDataTypeByRef(compositeVector);
				parameter4.setValueByRef(new DEFile());
				parameters.add(parameter4);

				addSchemaMimeTypeEncodingToParameters(parameters, outDescType);

			}
	}

	/**
	 * Returns an array of paramInfo This is the location where the parameters
	 * to the Function Tool are defined. This property returns an IArray of
	 * parameter objects (IGPParameter). These objects define the
	 * characteristics of the input and output parameters.
	 */
	public IArray getParameterInfo() throws IOException, AutomationException {
		
		IOParameterMap params = new IOParameterMap(mcp);
		IArray parameters = new Array();
		
		// TODO: sort identifiers according to their order in the WPS interface description
		
		
		/**
		 * For each parameter create a new GPParameter
		 */
		GPParameter gpParam;
		for (IOParameter param: params.values()){
			gpParam = makeGpToolParameter(param);
			if (gpParam != null){
				parameters.add(gpParam);
			}
		}
		
		return parameters;
	}

	/**
	 * Called each time the user changes a parameter in the tool dialog or
	 * Command Line. This updates the output data of the tool, which extremely
	 * useful for building models. After returning from UpdateParameters(), the
	 * GP framework calls its internal validation routine to check that a given
	 * set of parameter values are of the appropriate number, DataType, and
	 * value.
	 */
	public void updateParameters(IArray paramvalues,
			IGPEnvironmentManager envMgr) {
		
		try {
			for (int i = 0; i < paramvalues.getCount(); i++) {
				IGPParameter tmpParameter = (IGPParameter) paramvalues
						.getElement(i);
				IGPValue tmpParameterValue = gpUtilities
						.unpackGPValue(tmpParameter);
				LOGGER.info("check " + tmpParameter.getName());
				LOGGER.info("Value: " + tmpParameterValue);
			}
		} catch (AutomationException e) {
			LOGGER.error("Error in updating parameters method", e);
		} catch (IOException e) {
			LOGGER.error("Error in updating parameters method", e);
		}
		
	}

	/**
	 * Called after returning from the internal validation routine. You can
	 * examine the messages created from internal validation and change them if
	 * desired.
	 */
	public void updateMessages(IArray paramvalues,
			IGPEnvironmentManager envMgr, IGPMessages gpMessages) {

	}

	/**
	 * Executes the tool
	 */
	public void execute(IArray paramvalues, ITrackCancel trackcancel,
			IGPEnvironmentManager envMgr, IGPMessages messages)
			throws IOException, AutomationException {

		String[] identifierAndURL = toolName.split("@");

		String wpsURL = identifierAndURL[1];

		try {
			messages.addMessage("WPS URL: " + wpsURL);

			WPSClientSession.getInstance().connect(wpsURL);

			Map<String, String> parameterNameValueMap = getParameterNameValueMap(paramvalues);

			ProcessDescriptionType pDescType = WPSClientSession.getInstance()
					.getProcessDescription(wpsURL, displayName);

			ExecuteDocument execDoc = createExecuteDocument(
					parameterNameValueMap, pDescType, messages);

			if (execDoc == null) {
				return;
			}

			LOGGER.debug(execDoc.toString());

			ExecuteResponseDocument responseDoc = (ExecuteResponseDocument) WPSClientSession
					.getInstance().execute(wpsURL, execDoc);

			ExecuteResponse response = responseDoc.getExecuteResponse();
			
			LOGGER.debug(response.toString());
			
			ComplexDataType cData = response.getProcessOutputs()
					.getOutputArray(0).getData().getComplexData();

//			String s = cData.getDomNode().getFirstChild().getNodeValue();
			String s = nodeToString(cData.getDomNode().getFirstChild());
			LOGGER.debug("Test" + s + "test end");
			
			if(!response.toString().contains("application/x-zipped-shp")){
			
			if(s == null || s.trim().equals("") || s.trim().equals(" ") || !s.contains("Feature")) {

				try {
					s = nodeToString(cData.getDomNode().getChildNodes().item(1));
					
					LOGGER.debug("ComplexData content " + s);					
				} catch (Exception e) {
					LOGGER.error("cData.getDomNode().getFirstChild().getChildNodes().item(1).getNodeValue() leads to " + e);
				}
			}else{								
				LOGGER.debug("ComplexData content " + s);				
			}
			}
			String outputPath = "";

			for (String key : parameterNameValueMap.keySet()) {
				if (key.startsWith(outputPrefix)) {
					/*
					 * there should be only one
					 */
					if (parameterNameValueMap.get(key) != null) {
						outputPath = parameterNameValueMap.get(key);
						LOGGER.debug("Found outputPath: " + outputPath);
					}
					break;
				}
			}
			
			messages.addMessage(outputPath);
			
			File outputFile = new File(outputPath);
			
			BufferedWriter bwr = new BufferedWriter(new FileWriter(outputFile));
			
			bwr.write(s);
			
			bwr.close();
			
//			byte[] bytess = Base64.decode(s);
//
//			ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
//
//			byteOut.write(bytess);
//
//			String outputPath = "";
//
//			for (String key : parameterNameValueMap.keySet()) {
//				if (key.startsWith(outputPrefix)) {
//					/*
//					 * there should be only one
//					 */
//					if (parameterNameValueMap.get(key) != null) {
//						outputPath = parameterNameValueMap.get(key);
//						LOGGER.debug("Found outputPath: " + outputPath);
//					}
//					break;
//				}
//			}
//
//			messages.addMessage(outputPath);
//
//			File outputFile = new File(outputPath);
//
//			if (outputFile.getName().endsWith(".shp")) {
//
//				File tmpZipFile = new File(ShapefileExport.getOutputDir()
//						+ File.separator + "52n" + File.separator + "out"
//						+ File.separator + "output.zip");
//				LOGGER.debug("Output ParentFile" + tmpZipFile.getParent());
//				try {
//
//					tmpZipFile.getParentFile().mkdirs();
//				} catch (Exception e) {
//					LOGGER.debug("Exception " + e);
//				}
//				byteOut.writeTo(new FileOutputStream(tmpZipFile));
//
//				byteOut.flush();
//
//				byteOut.close();
//				/*
//				 * path we got from the textfield we need to extract the zipped
//				 * files with the name of the output file
//				 */
//				String shpFileName = outputFile.getName();
//
//				String entryName = shpFileName.substring(0,
//						shpFileName.indexOf("."));
//
//				final int BUFFER = 2048;
//
//				String path = outputFile.getParent();
//
//				try {
//					BufferedOutputStream dest = null;
//					FileInputStream fis = new FileInputStream(tmpZipFile);
//					ZipInputStream zis = new ZipInputStream(
//							new BufferedInputStream(fis));
//					ZipEntry entry;
//					while ((entry = zis.getNextEntry()) != null) {
//
//						String entryFileEnding = entry.getName();
//
//						entryFileEnding = entryFileEnding.substring(
//								entryFileEnding.indexOf("."),
//								entryFileEnding.length());
//
//						String entryOutputName = entryName + entryFileEnding;
//
//						int count;
//						byte data[] = new byte[BUFFER];
//						// write the files to the disk
//						FileOutputStream fos = new FileOutputStream(path
//								+ File.separator + entryOutputName);
//						dest = new BufferedOutputStream(fos, BUFFER);
//						while ((count = zis.read(data, 0, BUFFER)) != -1) {
//							dest.write(data, 0, count);
//						}
//						dest.flush();
//						dest.close();
//					}
//					zis.close();
//				} catch (Exception e5) {
//					e5.printStackTrace();
//				}
//			} else if (outputFile.getName().endsWith("tif")
//					|| outputFile.getName().endsWith("tiff")) {
//
//				FileOutputStream output = new FileOutputStream(outputFile);
//				Base64.decode(s, output);
//				output.close();
//			}
			/**
			 * Create Request with inputs from dialog and base64 encoded
			 * layer(s) Send request to WPS. Output should be zipped shp or
			 * tiff.
			 */

			/**
			 * Wait for response. Handle response write complexdata in temp
			 * directory add new layer to TOC.
			 */

		} catch (Exception e1) {
			LOGGER.error("Something went wrong while executing the WPS process.", e1);
//			IGPMessage errorMessage = new GPMessage();
			try {
//				errorMessage
//						.setErrorCode(esriGPMessageSeverity.esriGPMessageSeverityError);
//				errorMessage
//						.setDescription("Sorry, something went wrong while executing the WPS process.");
//				messages.add(errorMessage);
//				gp.addError("Sorry, something went wrong while executing the WPS process.");
				messages.addError(esriGPMessageSeverity.esriGPMessageSeverityError, "Sorry, something went wrong while executing the WPS process.");
			} catch (Exception e) {
				/*
				 * well...
				 */
			}
		}

	}

	private ExecuteDocument createExecuteDocument(
			Map<String, String> parameterNameValueMap,
			ProcessDescriptionType pDescType, IGPMessages messages) {

		ExecuteDocument result = ExecuteDocument.Factory.newInstance();

		Execute ex = result.addNewExecute();

		ex.setVersion("1.0.0");

		ex.setService("WPS");

		ex.addNewIdentifier().setStringValue(
				pDescType.getIdentifier().getStringValue());

		DataInputsType dataInputs = ex.addNewDataInputs();

		InputDescriptionType[] inputDescTypes = pDescType.getDataInputs()
				.getInputArray();

		for (InputDescriptionType inputDescriptionType : inputDescTypes) {

			String identifier = inputDescriptionType.getIdentifier()
					.getStringValue();

			InputTypeEnum type = checkType(inputDescriptionType);

			String value = parameterNameValueMap.get(identifier);

			if (value == null) {
				continue;
			}

			LOGGER.debug("Value " + value);

			switch (type) {
			case Vector:
				/*
				 * TODO: add strategy for empty schema/mimetype/encoding
				 */
				String schema = parameterNameValueMap.get(identifier + "_schema");				
				LOGGER.debug("Schema = " + schema);
				
				String mimeType = parameterNameValueMap.get(identifier + "_mimetype");
				LOGGER.debug("Mime Type = " + mimeType);
				
				String isReference = parameterNameValueMap.get(identifier + "_reference");
				LOGGER.debug("IsReference = " + isReference);
				
//				if(schema == null || schema.equals("")){
//					schema = pDescType.getDataInputs().getInputArray()
//				}
				
				InputType inTypeVector = dataInputs.addNewInput();

				inTypeVector.addNewIdentifier().setStringValue(identifier);
				
				if(isReference != null && Boolean.parseBoolean(isReference)){
					
					InputReferenceType inHref = inTypeVector.addNewReference();
					
					inHref.setHref(value);
					
					if(mimeType != null && !mimeType.equals("")){
					
						inHref.setMimeType(mimeType);
					}
					
					if(schema != null && !schema.equals("")){
						
						inHref.setSchema(schema);	
					}				
					
				}else{
					
					DocumentBuilderFactory factoryVector = DocumentBuilderFactory
							.newInstance();
					
					
				if(mimeType != null && mimeType.contains("xml")){
				try {

					DataType dataTypeVector = inTypeVector.addNewData();

					ComplexDataType cDataTypeVector = dataTypeVector
							.addNewComplexData();
					
					factoryVector.setNamespaceAware(true);
					
					//TODO: just parse text input??? For vector/raster/whatever??
					DocumentBuilder builder = factoryVector
							.newDocumentBuilder();
					
					Document d = builder.parse(new File(value));

					cDataTypeVector.set(XmlObject.Factory.parse(d));

//					cDataTypeVector.setMimeType(mimeType);
					
					if(mimeType != null && !mimeType.equals("")){
						
						cDataTypeVector.setMimeType(mimeType);	
					}	

					
					if(schema != null && !schema.equals("")){
						
						cDataTypeVector.setSchema(schema);	
					}	
//					cDataTypeVector.setEncoding("UTF-8");//TODO: 

//					cDataTypeVector
//							.setSchema(schema);

				} catch (Exception e) {
					LOGGER.error("Can not parse input XML", e);
				}
				}else{
					
					try{
					
					BufferedReader bread = new BufferedReader(new FileReader(new File(value)));
					
					String line = "";
					
					String content = "";
					
					while((line = bread.readLine()) != null){
						content = content.concat(line);
					}
					
					bread.close();
					
					DataType dataTypeVector = inTypeVector.addNewData();

					ComplexDataType cDataTypeVector = dataTypeVector
							.addNewComplexData();
					
					factoryVector.setNamespaceAware(true);
					
					DocumentBuilder builder = factoryVector
							.newDocumentBuilder();
					
					Document d = builder.newDocument();
					
					Node cdata = d.createCDATASection(content);

					cDataTypeVector.set(XmlObject.Factory.parse(cdata));
					
					if(mimeType != null && !mimeType.equals("")){
						
						cDataTypeVector.setMimeType(mimeType);	
					}
					
					if(schema != null && !schema.equals("")){
						
						cDataTypeVector.setSchema(schema);	
					}
					}catch (Exception e) {
						LOGGER.error(e);
					}
				}
				}

				break;

			case Literal:

				InputType inType1 = dataInputs.addNewInput();

				inType1.addNewIdentifier().setStringValue(identifier);

				DataType dataType1 = inType1.addNewData();

				LiteralDataType lit = dataType1.addNewLiteralData();

				DomainMetadataType literalDatatype = inputDescriptionType
						.getLiteralData().getDataType();

				String datatypeRef = null;

				if (literalDatatype != null) {
					datatypeRef = literalDatatype.getReference();
					if (datatypeRef == null) {
						datatypeRef = literalDatatype.getStringValue();
					}
				}

				if (datatypeRef == null) {
					datatypeRef = "xs:string";
				}

				lit.setDataType(datatypeRef);

				// lit.setDataType(inputDescriptionType.getLiteralData()
				// .getDataType().getReference());

				lit.setStringValue(value);

				break;
			default:
				break;
			}
		}
		try {
			addResponseForm(pDescType, ex, parameterNameValueMap, messages);
		} catch (IllegalArgumentException e) {
			return null;
		}
		return result;
	}

	private void addResponseForm(ProcessDescriptionType pDescType, Execute ex,
			Map<String, String> parameterNameValueMap, IGPMessages messages)
			throws IllegalArgumentException {

		ArrayList<OutputDescriptionType> outputDescTypes = new ArrayList<OutputDescriptionType>(
				pDescType.getProcessOutputs().getOutputArray().length);

		for (OutputDescriptionType outDescType : pDescType.getProcessOutputs()
				.getOutputArray()) {
			if (parameterNameValueMap.get(outputPrefix
					+ outDescType.getIdentifier().getStringValue()) != null) {
				outputDescTypes.add(outDescType);
			}
		}

		if (outputDescTypes.size() > 1) {
			LOGGER.error("More than one output currently not supported.");
			try {
				IGPMessage errorMessage = new GPMessage();
				errorMessage
						.setErrorCode(esriGPMessageSeverity.esriGPMessageSeverityError);
				errorMessage
						.setDescription("Currently only one output is allowed. Found "
								+ outputDescTypes.size() + ".");
				messages.add(errorMessage);
			} catch (Exception e) {
				/*
				 * well...
				 */
			}
			throw new IllegalArgumentException();
		}

		OutputDescriptionType outDescType = outputDescTypes.get(0);

		boolean supportsRaster = false;
		boolean supportsVector = false;

		String mimeType = outDescType.getComplexOutput().getDefault()
				.getFormat().getMimeType();

		List<String> rasterMimeTypeList = new ArrayList<String>();

		rasterMimeTypeList.add("application/geotiff");
		rasterMimeTypeList.add("image/tiff");
		rasterMimeTypeList.add("image/geotiff");
		rasterMimeTypeList.add("application/x-geotiff");

		String supportedRasterFormat = "";

		if (rasterMimeTypeList.contains(mimeType)) {
			supportedRasterFormat = mimeType;
			supportsRaster = true;
		}

		List<String> vectocMimeTypeList = new ArrayList<String>();
		vectocMimeTypeList.add("application/x-zipped-shp");
		vectocMimeTypeList.add("text/xml");

		if (vectocMimeTypeList.contains(mimeType)) {
			supportsVector = true;
		}

		if (!supportsRaster && !supportsVector) {

			ComplexDataCombinationsType supportedCombinationTypes = outDescType
					.getComplexOutput().getSupported();

			ComplexDataDescriptionType[] supportedFormats = supportedCombinationTypes
					.getFormatArray();

			for (int i = 0; i < supportedFormats.length; i++) {

				String tmpMimeType = supportedFormats[i].getMimeType();

				if (rasterMimeTypeList.contains(tmpMimeType)) {
					supportedRasterFormat = tmpMimeType;
					supportsRaster = true;
					break;
				} else if (vectocMimeTypeList.contains(tmpMimeType)) {
					supportsVector = true;
					break;
				}

			}

		}

		ResponseFormType responseForm = ex.addNewResponseForm();

		ResponseDocumentType responseDocument = responseForm
				.addNewResponseDocument();

		DocumentOutputDefinitionType output = responseDocument.addNewOutput();

		String identifier = outputDescTypes.get(0).getIdentifier().getStringValue();
		
		output.addNewIdentifier().setStringValue(identifier);

		if (supportsVector) {
			/*
			 * TODO: add strategy for empty schema/mimetype/encoding
			 */
			String schema = parameterNameValueMap.get(identifier + "_schema");				
			LOGGER.debug("Schema = " + schema);
			
			String mimeType1 = parameterNameValueMap.get(identifier + "_mimetype");
			LOGGER.debug("Mime Type = " + mimeType1);
			
			String encoding = parameterNameValueMap.get(identifier + "_encoding");
			LOGGER.debug("Mime Type = " + encoding);
			
			String isReference = parameterNameValueMap.get(identifier + "_reference");
			LOGGER.debug("IsReference = " + isReference);
			if(mimeType1 != null && !mimeType1.equals("")){
			output.setMimeType(mimeType1);
			}
			if(encoding != null && !encoding.equals("")){
			output.setEncoding(encoding);
			}
			if(schema != null && !schema.equals("")){
				output.setSchema(schema);
			}
		} else if (supportsRaster) {
			output.setMimeType(supportedRasterFormat);
			output.setEncoding("base64");
		}
	}

	private Map<String, String> getParameterNameValueMap(IArray paramvalues) {
		try {
			Map<String, String> result = new HashMap<String, String>(
					paramvalues.getCount());

			for (int i = 0; i < paramvalues.getCount(); i++) {
				IGPParameter tmpParameter = (IGPParameter) paramvalues
						.getElement(i);
				IGPValue tmpParameterValue = gpUtilities
						.unpackGPValue(tmpParameter);
				if (!tmpParameterValue.getAsText().equals("")) {
					LOGGER.info("added " + tmpParameter.getName());
					result.put(tmpParameter.getName(),
							tmpParameterValue.getAsText());
				}else{
					LOGGER.info("Omitted " + tmpParameter.getName());
					LOGGER.info("Value: " + tmpParameterValue);
				}
			}
			return result;
		} catch (Exception e) {
			LOGGER.error(e);
		}
		return null;
	}

	/**
	 * Returns metadata file
	 */
	public String getMetadataFile() throws IOException, AutomationException {
		return metadataFileName;
	}

	/**
	 * Returns status of license
	 */
	public boolean isLicensed() throws IOException, AutomationException {
		// no license checking is done here.
		return true;
	}

	private InputTypeEnum checkType(InputDescriptionType inputDescType) {

		SupportedComplexDataInputType cDataType = inputDescType
				.getComplexData();
		LiteralInputType lDataType = inputDescType.getLiteralData();

		if (cDataType != null) {

			String mimeType = cDataType.getDefault().getFormat().getMimeType();

			List<String> rasterMimeTypeList = new ArrayList<String>();

			rasterMimeTypeList.add("application/geotiff");
			rasterMimeTypeList.add("image/tiff");
			rasterMimeTypeList.add("image/geotiff");
			rasterMimeTypeList.add("application/x-geotiff");

			boolean supportsRaster = false;

			if (rasterMimeTypeList.contains(mimeType)) {
				supportsRaster = true;
			}

			List<String> vectocMimeTypeList = new ArrayList<String>();
			vectocMimeTypeList.add("application/x-zipped-shp");
			vectocMimeTypeList.add("text/xml");
			// vectocMimeTypeList.add("application/x-zipped-shp");
			// vectocMimeTypeList.add("application/x-zipped-shp");

			boolean supportsVector = false;
			if (vectocMimeTypeList.contains(mimeType)) {
				supportsVector = true;
			}

			if (!supportsRaster && !supportsVector) {

				ComplexDataCombinationsType supportedCombinationTypes = cDataType
						.getSupported();

				ComplexDataDescriptionType[] supportedFormats = supportedCombinationTypes
						.getFormatArray();

				for (int i = 0; i < supportedFormats.length; i++) {

					String tmpMimeType = supportedFormats[i].getMimeType();

					if (rasterMimeTypeList.contains(tmpMimeType)) {
						supportsRaster = true;
						break;
					} else if (vectocMimeTypeList.contains(tmpMimeType)) {
						supportsVector = true;
						break;
					}

				}

			}

			if (supportsRaster) {
				return InputTypeEnum.Raster;
			} else if (supportsVector) {
				return InputTypeEnum.Vector;
			}

		} else if (lDataType != null) {
			return InputTypeEnum.Literal;
		}

		return InputTypeEnum.Unknown;

	}

	public String getMetadataFileName() {
		try {
			return this.getDisplayName() + ".xml";
		} catch (AutomationException e) {
			LOGGER.error(e.getMessage());
		} catch (IOException e) {
			LOGGER.error(e.getMessage());
		}
		return null;
	}

	private void addSchemaMimeTypeEncodingToParameters(IArray params,
			InputDescriptionType currentDescriptionType) {

		String labelText = currentDescriptionType.getIdentifier()
				.getStringValue();

		Set<String> schemas = new HashSet<String>();
		Set<String> mimeTypes = new HashSet<String>();

		ComplexDataDescriptionType defaultFormat = currentDescriptionType
				.getComplexData().getDefault().getFormat();

		schemas.add(defaultFormat.getSchema());
		mimeTypes.add(defaultFormat.getMimeType());

		ComplexDataDescriptionType[] supportedFormats = currentDescriptionType
				.getComplexData().getSupported().getFormatArray();

		for (ComplexDataDescriptionType complexDataDescriptionType : supportedFormats) {
			schemas.add(complexDataDescriptionType.getSchema());
			mimeTypes.add(complexDataDescriptionType.getMimeType());
		}

		try {
			parameters.add(createListBoxStringParameter(labelText + "_schema",
					labelText + " schema", schemas, false));
		} catch (AutomationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			parameters.add(createListBoxStringParameter(
					labelText + "_mimetype", labelText + " mime type",
					mimeTypes, false));
		} catch (AutomationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void addSchemaMimeTypeEncodingToParameters(Array parameters2,
			OutputDescriptionType outDescType) {
		String labelText = outDescType.getIdentifier().getStringValue();

		Set<String> schemas = new HashSet<String>();
		Set<String> mimeTypes = new HashSet<String>();
		Set<String> encodings = new HashSet<String>();

		ComplexDataDescriptionType defaultFormat = outDescType
				.getComplexOutput().getDefault().getFormat();

		schemas.add(defaultFormat.getSchema());
		mimeTypes.add(defaultFormat.getMimeType());
		encodings.add(defaultFormat.getEncoding());
		
		ComplexDataDescriptionType[] supportedFormats = outDescType
				.getComplexOutput().getSupported().getFormatArray();

		for (ComplexDataDescriptionType complexDataDescriptionType : supportedFormats) {
			schemas.add(complexDataDescriptionType.getSchema());
			mimeTypes.add(complexDataDescriptionType.getMimeType());
			encodings.add(complexDataDescriptionType.getEncoding());
		}

		try {
			parameters.add(createListBoxStringParameter(labelText + "_schema",
					labelText + " schema", schemas, true));
		} catch (AutomationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			parameters.add(createListBoxStringParameter(
					labelText + "_mimetype", labelText + " mime type",
					mimeTypes, true));
		} catch (AutomationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			parameters.add(createListBoxStringParameter(
					labelText + "_encoding", labelText + " encoding",
					encodings, true));
		} catch (AutomationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Make a parameter from an enum of Strings
	 * 
	 * @param name
	 * @param displayName
	 * @param listBoxElements
	 * @param output
	 * @return
	 * @throws Exception
	 */
	private static final GPParameter createListBoxStringParameter(
			String name,
			String displayName,
			Set<String> listBoxElements,
			boolean output) 
					throws Exception {

		GPParameter parameter = new GPParameter();
		parameter.setName(name);
		
		if (output) {
			parameter.setDirection(esriGPParameterDirection.esriGPParameterDirectionOutput);
		} else {
			parameter.setDirection(esriGPParameterDirection.esriGPParameterDirectionInput);			
		}
		
		parameter.setDisplayName(displayName);
		parameter.setParameterType(esriGPParameterType.esriGPParameterTypeOptional);
		parameter.setDataTypeByRef(new GPStringType());
		parameter.setValueByRef(new GPString());
		IGPCodedValueDomain domain = new GPCodedValueDomain();

		for (String string : listBoxElements) {
			domain.addStringCode(string, string);
		}
		// Assign the domain to the parameter.
		parameter.setDomainByRef((IGPDomain) domain);

		return parameter;
	}

}
