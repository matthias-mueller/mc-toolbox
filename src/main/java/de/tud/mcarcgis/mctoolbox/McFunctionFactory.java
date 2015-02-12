package de.tud.mcarcgis.mctoolbox;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import net.opengis.wps.x100.ProcessDescriptionType;

import org.n52.movingcode.runtime.GlobalRepositoryManager;
import org.n52.movingcode.runtime.codepackage.MovingCodePackage;
import org.n52.movingcode.runtime.codepackage.PID;
import org.n52.movingcode.runtime.processors.ProcessorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.esri.arcgis.geodatabase.IEnumGPName;
import com.esri.arcgis.geodatabase.IGPName;
import com.esri.arcgis.geoprocessing.EnumGPName;
import com.esri.arcgis.geoprocessing.GPFunctionName;
import com.esri.arcgis.geoprocessing.IEnumGPEnvironment;
import com.esri.arcgis.geoprocessing.IGPFunction;
import com.esri.arcgis.geoprocessing.IGPFunctionFactory;
import com.esri.arcgis.interop.AutomationException;
import com.esri.arcgis.interop.extn.ArcGISCategories;
import com.esri.arcgis.interop.extn.ArcGISExtension;
import com.esri.arcgis.system.IUID;
import com.esri.arcgis.system.UID;
import com.esri.arcgis.system.esriProductCode;

/**
 * Function factory that creates GPTools from moving code packages.
 * 
 * @author matthias
 *
 */
@ArcGISExtension(categories = { ArcGISCategories.GPFunctionFactories })
public class McFunctionFactory implements IGPFunctionFactory {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(McFunctionFactory.class);

	private static final String FUNCTION_FACTORY_ALIAS = "mcFunctionFactory";
	private static final String FACTORY_NAME = "mcFunctionFactory";

	private final Set<PID> supportedPackages;

	private final Map<String, McTool> tools;

	GlobalRepositoryManager rm;

	public McFunctionFactory() {
		init();
		if (rm == null){
			rm = GlobalRepositoryManager.getInstance();
		}

		if (rm.getRegisteredRepositories().length > 0){
			LOGGER.info("The following repositories have been loaded:\n{}", Arrays.toString(rm.getRegisteredRepositories()));
		} else {
			LOGGER.info("No repositories have been loaded.");
		}

		supportedPackages = new HashSet<PID>();
		tools = new HashMap<String, McTool>();
		
		// verify the content of each package
		// and create GPTools for the supported packages
		PID currentPackageId;
		for (MovingCodePackage currentMCP : rm.getLatestPackages()) {
			if (ProcessorFactory.getInstance().supportsPackage(currentMCP)) {
				currentPackageId = currentMCP.getPackageId();
				supportedPackages.add(currentPackageId);
				try {
					IGPName gpFunctionName = makeFunctionName(currentMCP);
					tools.put(currentPackageId.getCanonicalName(), new McTool(currentMCP, gpFunctionName));
				} catch (Exception e){
					LOGGER.warn("Could not create Tool for packageId {}", currentPackageId.getCanonicalName());
				}
			}
		}

	}

	/**
	 * Returns the appropriate GPFunction object based on specified tool name
	 */
	public IGPFunction getFunction(String canonicalPackageName)
			throws IOException, AutomationException {
		PID packageId = PID.fromString(canonicalPackageName);
		MovingCodePackage mcp = rm.getPackage(packageId);
		if (mcp != null) {
			return tools.get(canonicalPackageName);
		} else {
			return null;
		}
	}

	/**
	 * Returns a GPFunctionName objects based on specified tool name
	 */
	public IGPName getFunctionName(String canonicalPackageName) throws IOException, AutomationException {
		McTool tool = tools.get(canonicalPackageName);
		
		if(tool != null){
			return tool.fullName;
		} else {
			return null;
		}

	}
	
	/**
	 * Creates an {@link IGPName} for a given {@link MovingCodePackage}
	 * 
	 * @param mcp
	 * @return
	 * @throws UnknownHostException
	 * @throws IOException
	 */
	private final IGPName makeFunctionName(MovingCodePackage mcp) throws UnknownHostException, IOException{
		ProcessDescriptionType pd = mcp.getDescriptionAsDocument()
				.getPackageDescription().getFunctionality()
				.getWps100ProcessDescription();
		GPFunctionName functionName = new GPFunctionName();

		String functionId = mcp.getFunctionIdentifier();

		if (pd.isSetAbstract()) {
			functionName.setDescription(pd.getAbstract().getStringValue());
		}

		functionName.setDisplayName(pd.getTitle().getStringValue());
		functionName.setName(functionId);

		int end = functionId.lastIndexOf(".");
		if (end == -1) {
			end = functionId.length();
		}
		
		/**
		 * Category is created from the canonical name:
		 * 
		 * my.domain.toolbox.something.toolname
		 * --------------------------- --------
		 *              |                 |
		 *          category           truncated 
		 */
		functionName.setCategory(functionId.substring(0, end));
		functionName.setMinimumProduct(esriProductCode.esriProductCodeAdvanced);
		
		functionName.setFactoryByRef(this);
		return functionName;
	}

	/**
	 * Returns names of all gp tools created by this function factory
	 * The tool names are the canonical packageIDs
	 */
	public IEnumGPName getFunctionNames() throws IOException, AutomationException {
		EnumGPName nameArray = new EnumGPName();
		for (String functionName : tools.keySet()) {
			nameArray.add(getFunctionName(functionName));
		}
		return nameArray;
	}

	/**
	 * Returns Alias of the function factory {@value #FUNCTION_FACTORY_ALIAS}
	 */
	public String getAlias() throws IOException, AutomationException {
		return FUNCTION_FACTORY_ALIAS;
	}

	/**
	 * Returns Class ID
	 */
	public IUID getCLSID() throws IOException, AutomationException {
		UID uid = new UID();
		uid.setValue("{"
				+ UUID.nameUUIDFromBytes(this.getClass().getName().getBytes())
				+ "}");
		return uid;
	}

	/**
	 * Returns Function Environments
	 */
	public IEnumGPEnvironment getFunctionEnvironments() throws IOException,
	AutomationException {
		return null;
	}

	/**
	 * Returns name of the FunctionFactory {@value #FACTORY_NAME}
	 */
	public String getName() throws IOException, AutomationException {
		return FACTORY_NAME;
	}

	private synchronized void init() {
		Collection<String> remoteRepos = RepoConfig
				.getParameter(RepoConfig.REMOTE_FEED_REPOSITORY_KEY);
		Collection<String> localZipRepos = RepoConfig
				.getParameter(RepoConfig.LOCAL_ZIP_REPOSITORY_KEY);

		GlobalRepositoryManager rm = GlobalRepositoryManager.getInstance();
		for (String directory : localZipRepos) {
			try {
				rm.addLocalZipPackageRepository(directory);
				LOGGER.info("Added MovingCode Repository: " + directory);
			}
			catch (Exception e) {
				// catch any unexpected error; if we get here this is probably an indication for a
				// bug/flaw in mc-runtime ...
				LOGGER.error("Error invoking MovingCode Runtime for feed URL : " + directory);
				e.printStackTrace();
			}
		}

		for (String url : remoteRepos) {

			try {
				URL repoURL = new URL(url);
				rm.addRepository(repoURL);
				LOGGER.info("Added MovingCode Repository: " + url);
			} catch (MalformedURLException e) {
				LOGGER.warn("MovingCode Repository is not a valid URL: " + url);
			} catch (Exception e) {
				// catch any unexpected error; if we get here this is probably
				// an indication for a
				// bug/flaw in mc-runtime ...
				LOGGER.error("Error invoking MovingCode Runtime for feed URL : "
						+ url);
			}
		}

	}

}
