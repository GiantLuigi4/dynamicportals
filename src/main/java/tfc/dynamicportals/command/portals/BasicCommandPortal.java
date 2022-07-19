package tfc.dynamicportals.command.portals;

import tfc.dynamicportals.api.implementation.BasicPortal;

import java.util.UUID;

public class BasicCommandPortal extends BasicPortal implements CommandPortal {
	int id = 0;
	
	public BasicCommandPortal(UUID uuid) {
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
		return "basic";
	}
}
