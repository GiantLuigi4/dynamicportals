package tfc.dynamicportals.command.portals;

import tfc.dynamicportals.api.BasicPortal;
import tfc.dynamicportals.command.CommandPortal;
import tfc.dynamicportals.vanilla.NetherPortal;

import java.util.UUID;

public class BasicNetherPortal extends NetherPortal implements CommandPortal {
	int id = 0;
	
	public BasicNetherPortal(UUID uuid) {
		super(uuid);
	}
	
	@Override
	public int myId() {
		return id;
	}
	
	@Override
	public int setId(int val) {
		return id = val;
	}
	
	@Override
	public String type() {
		return "nether";
	}
}
