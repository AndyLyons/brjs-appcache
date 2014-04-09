package com.caplin.brjs.plugins.appcache;

import java.util.List;

import org.bladerunnerjs.model.BRJS;
import org.bladerunnerjs.model.BundleSet;
import org.bladerunnerjs.model.exception.PropertiesException;
import org.bladerunnerjs.model.exception.request.ContentProcessingException;
import org.bladerunnerjs.plugin.ContentPlugin;

/**
 * Builds manifest file strings based on a given set of parameters.
 */
public class AppcacheManifestBuilder
{
	private BRJS brjs;
	private BundleSet bundleSet;
	private AppcacheConf config;
	private boolean isDev;

	/**
	 * Creates an {@link AppcacheManifestBuilder} instance for generating prod manifest files.
	 */
	public AppcacheManifestBuilder(BRJS brjs, BundleSet bundleSet, AppcacheConf config)
	{
		this(brjs, bundleSet, config, false);
	}

	/**
	 * Creates an {@link AppcacheManifestBuilder} instance for generating prod or dev manifest files,
	 * depending on the value of the isDev parameter.
	 */
	public AppcacheManifestBuilder(BRJS brjs, BundleSet bundleSet, AppcacheConf config, boolean isDev)
	{
		this.brjs = brjs;
		this.bundleSet = bundleSet;
		this.config = config;
		this.isDev = isDev;
	}

	/**
	 * Generates the manifest file as a string.
	 * @return The manifest file
	 * @throws PropertiesException
	 * @throws ContentProcessingException
	 */
	public String getManifest() throws PropertiesException, ContentProcessingException
	{
		String manifest = getManifestHeader();
		manifest += getManifestCacheFiles();
		manifest += getManifestNetworkFiles();
		return manifest;
	}

	/**************************************
	 * 
	 * Private
	 * 
	 **************************************/

	/**
	 * Generates the manifest file header
	 */
	private String getManifestHeader() throws PropertiesException
	{
		String version = getManifestVersion();
		return "CACHE MANIFEST\n# v" + version + "\n\n";
	}

	/**
	 * Generates a version number to use for the manifest. The version is generated from either:
	 * 1) The config file
 	 * 2) The node properties
	 * 3) Empty string fallback
	 */
	private String getManifestVersion() throws PropertiesException
	{
		String version = config.getVersion();
		if (version == null || version.trim().isEmpty())
		{
			version = bundleSet.getBundlableNode().nodeProperties("appcache").getPersisentProperty("version");
		}
		if (version == null)
		{
			version = "";
		}

		return version;
	}

	/**
	 * Generates the "CACHE" part of the manifest file
	 */
	private String getManifestCacheFiles() throws ContentProcessingException
	{
		StringBuilder cacheFiles = new StringBuilder();
		cacheFiles.append("CACHE:\n");

		// See #getContentPaths for an explanation on why we need configured languages
		String[] languages = getConfiguredLanguages();
		for (ContentPlugin plugin : brjs.plugins().contentProviders())
		{
			// Do not specify the manifest itself in the cache manifest file, otherwise it
			// will be nearly impossible to inform the browser a new manifest is available
			if (plugin.instanceOf(AppcacheContentPlugin.class))
			{
				continue;
			}

			for (String path : getContentPaths(plugin, languages))
			{
				// Path begins with .. as paths are relative to the manifest file,
				// and the manifest is in the "appcache/" directory
				cacheFiles.append("../" + path + "\n");
			}
		}

		cacheFiles.append("\n");

		return cacheFiles.toString();
	}

	/**
	 * Gets the list of configured languages from the config file.
	 * If none are configured a default of "en" will be used
	 */
	private String[] getConfiguredLanguages()
	{
		String[] languages = config.getLanguages();
		if (languages.length == 0)
		{
			languages = new String[] { "en" };
		}
		return languages;
	}

	/**
	 * Gets the content paths provided by a particular plugin, for a given set of locales.
	 */
	private List<String> getContentPaths(ContentPlugin plugin, String[] languages) throws ContentProcessingException
	{
		// TODO Any language-specific files requested by browsers set to a language not in the locale list
		// will not appear in the appcache manifest, and will not be cached. The application will still work,
		// it's just that some files will have to be requested over the network every time.
		// This should be fixable when flat-file export is added to BRJS, as the mechanism with which language
		// specific files are generated will have changed.
		// In the meantime we let the developer specify required languages in the appcache.conf file, or assume
		// "en" as a default if none are provided.

		List<String> contentPaths;
		if (isDev)
		{
			contentPaths = plugin.getValidDevContentPaths(bundleSet, languages);
		}
		else
		{
			contentPaths = plugin.getValidProdContentPaths(bundleSet, languages);
		}
		return contentPaths;
	}

	/**
	 * Generates the manifest NETWORK section string
	 */
	private String getManifestNetworkFiles()
	{
		return "\nNETWORK:\n*";
	}
}