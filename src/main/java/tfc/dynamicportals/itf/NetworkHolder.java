package tfc.dynamicportals.itf;

import tfc.dynamicportals.api.PortalNet;
import tfc.dynamicportals.level.LevelLoader;

import java.util.ArrayList;

public interface NetworkHolder {
	ArrayList<PortalNet> getPortalNetworks();
	LevelLoader getLoader();
}
