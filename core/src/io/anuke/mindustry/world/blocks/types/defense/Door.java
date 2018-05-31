package io.anuke.mindustry.world.blocks.types.defense;

import com.badlogic.gdx.math.Rectangle;
import io.anuke.mindustry.content.fx.BlockFx;
import io.anuke.mindustry.entities.Player;
import io.anuke.mindustry.entities.TileEntity;
import io.anuke.mindustry.entities.Units;
import io.anuke.mindustry.world.Tile;
import io.anuke.mindustry.world.blocks.types.Wall;
import io.anuke.ucore.core.Effects;
import io.anuke.ucore.core.Effects.Effect;
import io.anuke.ucore.graphics.Draw;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class Door extends Wall{
	protected final Rectangle rect = new Rectangle();

	protected Effect openfx = BlockFx.dooropen;
	protected Effect closefx = BlockFx.doorclose;

	public Door(String name) {
		super(name);
		solid = false;
		solidifes = true;
	}
	
	@Override
	public void draw(Tile tile){
		DoorEntity entity = tile.entity();
		
		if(!entity.open){
			Draw.rect(name, tile.drawx(), tile.drawy());
		}else{
			Draw.rect(name + "-open", tile.drawx(), tile.drawy());
		}
	}

	@Override
	public boolean isCursor(Tile tile){
		return true;
	}
	
	@Override
	public boolean isSolidFor(Tile tile){
		DoorEntity entity = tile.entity();
		return !entity.open;
	}

	@Override
	public boolean tapped(Tile tile, Player player){
		DoorEntity entity = tile.entity();
		
		if(Units.anyEntities(tile) && entity.open){
			return true;
		}
		
		entity.open = !entity.open;
		if(!entity.open){
			Effects.effect(closefx, tile.drawx(), tile.drawy());
		}else{
			Effects.effect(openfx, tile.drawx(), tile.drawy());
		}

		return true;
	}
	
	@Override
	public TileEntity getEntity(){
		return new DoorEntity();
	}
	
	public class DoorEntity extends TileEntity{
		public boolean open = false;
		
		@Override
		public void write(DataOutputStream stream) throws IOException{
			super.write(stream);
			stream.writeBoolean(open);
		}
		
		@Override
		public void read(DataInputStream stream) throws IOException{
			super.read(stream);
			open = stream.readBoolean();
		}
	}

}
