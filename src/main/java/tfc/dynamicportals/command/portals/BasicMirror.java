package tfc.dynamicportals.command.portals;

import tfc.dynamicportals.portals.mirror.Mirror;

import java.util.UUID;

public class BasicMirror extends Mirror implements CommandPortal {
	int id = 0;
	
	public BasicMirror(UUID uuid) {
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
		return "mirror";
	}
}
