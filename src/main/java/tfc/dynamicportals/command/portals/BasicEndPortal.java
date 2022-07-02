package tfc.dynamicportals.command.portals;

import tfc.dynamicportals.command.CommandPortal;
import tfc.dynamicportals.vanilla.EndPortal;

import java.util.UUID;

public class BasicEndPortal extends EndPortal implements CommandPortal {
	int id = 0;
	
	public BasicEndPortal(UUID uuid) {
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
		return "end";
	}
}
