package com.caplin.brjs.plugins.appcache;

import org.bladerunnerjs.model.BRJSNode;
import org.bladerunnerjs.model.exception.ConfigException;
import org.bladerunnerjs.utility.ConfigValidationChecker;
import org.bladerunnerjs.yaml.AbstractYamlConfFile;

public class YamlAppcacheConf extends AbstractYamlConfFile
{

	public String version;
	
	@Override
	public void verify() throws ConfigException
	{
		ConfigValidationChecker.validate(this);
	}

	@Override
	public void initialize(BRJSNode node) {
		// TODO Auto-generated method stub
		
	}

}
