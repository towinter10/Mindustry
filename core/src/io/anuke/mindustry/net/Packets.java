package io.anuke.mindustry.net;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Base64Coder;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;
import io.anuke.mindustry.Vars;
import io.anuke.mindustry.entities.Player;
import io.anuke.mindustry.entities.SyncEntity;
import io.anuke.mindustry.io.Version;
import io.anuke.mindustry.net.Packet.ImportantPacket;
import io.anuke.mindustry.net.Packet.UnimportantPacket;
import io.anuke.mindustry.world.Block;
import io.anuke.ucore.entities.Entities;
import io.anuke.ucore.entities.EntityGroup;
import io.anuke.ucore.util.IOUtils;

import java.nio.ByteBuffer;

/**Class for storing all packets.*/
public class Packets {

    public static class Connect implements ImportantPacket{
        public int id;
        public String addressTCP;
    }

    public static class Disconnect implements ImportantPacket{
        public int id;
        public String addressTCP;
    }

    public static class InvokePacket implements Packet{
        public byte type;

        public ByteBuffer writeBuffer;
        public int writeLength;

        @Override
        public void read(ByteBuffer buffer) {
            type = buffer.get();

            if(Net.client()){
                //TODO implement
                //CallClient.readPacket(buffer, type);
            }else{
                byte[] bytes = new byte[writeLength];
                buffer.get(bytes);
                writeBuffer = ByteBuffer.wrap(bytes);
            }
        }

        @Override
        public void write(ByteBuffer buffer) {
            buffer.put(type);
            writeBuffer.position(0);
            for(int i = 0; i < writeLength; i ++){
                buffer.put(writeBuffer.get());
            }
        }
    }

    public static class WorldData extends Streamable{

    }

    public static class SyncPacket implements Packet, UnimportantPacket{
        public byte[] data;

        @Override
        public void write(ByteBuffer buffer) {
            buffer.putShort((short)data.length);
            buffer.put(data);
        }

        @Override
        public void read(ByteBuffer buffer) {
            data = new byte[buffer.getShort()];
            buffer.get(data);
        }
    }

    public static class BlockSyncPacket extends Streamable{

    }

    public static class ConnectPacket implements Packet{
        public int version;
        public int players;
        public String name;
        public boolean mobile;
        public int color;
        public byte[] uuid;

        @Override
        public void write(ByteBuffer buffer) {
            buffer.putInt(Version.build);
            IOUtils.writeString(buffer, name);
            buffer.put(mobile ? (byte)1 : 0);
            buffer.putInt(color);
            buffer.put(uuid);
        }

        @Override
        public void read(ByteBuffer buffer) {
            version = buffer.getInt();
            name = IOUtils.readString(buffer);
            mobile = buffer.get() == 1;
            color = buffer.getInt();
            uuid = new byte[8];
            buffer.get(uuid);
        }
    }

    public static class ConnectConfirmPacket implements Packet{
        @Override
        public void write(ByteBuffer buffer) { }

        @Override
        public void read(ByteBuffer buffer) { }
    }

    public static class DisconnectPacket implements Packet{
        public int playerid;

        @Override
        public void write(ByteBuffer buffer) {
            buffer.putInt(playerid);
        }

        @Override
        public void read(ByteBuffer buffer) {
            playerid = buffer.getInt();
        }
    }

    public static class StateSyncPacket implements Packet, UnimportantPacket{
        //todo fix item syncing
        public float countdown, time;
        public int enemies, wave;
        public long timestamp;

        @Override
        public void write(ByteBuffer buffer) {
            buffer.putFloat(countdown);
            buffer.putFloat(time);
            buffer.putShort((short)enemies);
            buffer.putShort((short)wave);
            buffer.putLong(timestamp);
        }

        @Override
        public void read(ByteBuffer buffer) {
            countdown = buffer.getFloat();
            time = buffer.getFloat();
            enemies = buffer.getShort();
            wave = buffer.getShort();
            timestamp = buffer.getLong();
        }
    }

	public static class BlockLogRequestPacket implements Packet {
		public int x;
		public int y;
		public Array<EditLog> editlogs;

		@Override
		public void write(ByteBuffer buffer) {
			buffer.putShort((short)x);
			buffer.putShort((short)y);
			buffer.putInt(editlogs.size);
			for(EditLog value : editlogs) {
				buffer.put((byte)value.playername.getBytes().length);
				buffer.put(value.playername.getBytes());
				buffer.putInt(value.block.id);
				buffer.put((byte) value.rotation);
				buffer.put((byte) value.action.ordinal());
			}
		}

		@Override
		public void read(ByteBuffer buffer) {
			x = buffer.getShort();
			y = buffer.getShort();
			editlogs = new Array<>();
			int arraySize = buffer.getInt();
			for(int a = 0; a < arraySize; a ++) {
				byte length = buffer.get();
				byte[] bytes = new byte[length];
				buffer.get(bytes);
				String name = new String(bytes);

				int blockid = buffer.getInt();
				int rotation = buffer.get();
				int ordinal = buffer.get();

				editlogs.add(new EditLog(name, Block.getByID(blockid), rotation, EditLog.EditAction.values()[ordinal]));
			}
		}
	}

    public static class RollbackRequestPacket implements Packet {
        public int rollbackTimes;

        @Override
        public void write(ByteBuffer buffer) {
            buffer.putInt(rollbackTimes);
        }

        @Override
        public void read(ByteBuffer buffer) {
            rollbackTimes = buffer.getInt();
        }
    }

    public static class PositionPacket implements Packet{
        public Player player;

        @Override
        public void write(ByteBuffer buffer) {
            buffer.putInt(player.id);
            buffer.putLong(TimeUtils.millis());
            player.write(buffer);
        }

        @Override
        public void read(ByteBuffer buffer) {
            int id = buffer.getInt();
            long time = buffer.getLong();
            player = Vars.playerGroup.getByID(id);
            player.read(buffer, time);
        }
    }

    public static class EntityShootPacket implements Packet, UnimportantPacket{
        public float x, y, rotation;
        public short bulletid;
        public byte groupid;
        public short data;
        public int entityid;

        @Override
        public void write(ByteBuffer buffer) {
            buffer.put(groupid);
            buffer.putInt(entityid);
            buffer.putFloat(x);
            buffer.putFloat(y);
            buffer.putFloat(rotation);
            buffer.putShort(bulletid);
        }

        @Override
        public void read(ByteBuffer buffer) {
            groupid = buffer.get();
            entityid = buffer.getInt();
            x = buffer.getFloat();
            y = buffer.getFloat();
            rotation = buffer.getFloat();
            bulletid = buffer.getShort();
        }
    }

    public static class PlacePacket implements Packet{
        public int playerid;
        public byte rotation;
        public short x, y;
        public byte recipe;

        @Override
        public void write(ByteBuffer buffer) {
            buffer.putInt(playerid);
            buffer.put(rotation);
            buffer.putShort(x);
            buffer.putShort(y);
            buffer.put(recipe);
        }

        @Override
        public void read(ByteBuffer buffer) {
            playerid = buffer.getInt();
            rotation = buffer.get();
            x = buffer.getShort();
            y = buffer.getShort();
            recipe = buffer.get();
        }
    }

    public static class BreakPacket implements Packet{
        public int playerid;
        public short x, y;

        @Override
        public void write(ByteBuffer buffer) {
            buffer.putInt(playerid);
            buffer.putShort(x);
            buffer.putShort(y);
        }

        @Override
        public void read(ByteBuffer buffer) {
            playerid = buffer.getInt();
            x = buffer.getShort();
            y = buffer.getShort();
        }
    }

    public static class EntitySpawnPacket implements Packet{
        public SyncEntity entity;
        public EntityGroup<?> group;

        @Override
        public void write(ByteBuffer buffer){
            buffer.put((byte)group.getID());
            buffer.putInt(entity.id);
            entity.writeSpawn(buffer);
        }

        @Override
        public void read(ByteBuffer buffer) {
            byte groupid = buffer.get();
            int id = buffer.getInt();
            group = Entities.getGroup(groupid);
            try {
                entity = (SyncEntity) ClassReflection.newInstance(group.getType());
                entity.id = id;
                entity.readSpawn(buffer);
                entity.setNet(entity.x, entity.y);
            }catch (ReflectionException e){
                throw new RuntimeException(e);
            }
        }
    }

    public static class EntityDeathPacket implements Packet{
        public byte group;
        public int id;

        @Override
        public void write(ByteBuffer buffer) {
            buffer.put(group);
            buffer.putInt(id);
        }

        @Override
        public void read(ByteBuffer buffer) {
            group = buffer.get();
            id = buffer.getInt();
        }
    }

    public static class BlockDestroyPacket implements Packet{
        public int position;

        @Override
        public void write(ByteBuffer buffer) {
            buffer.putInt(position);
        }

        @Override
        public void read(ByteBuffer buffer) {
            position = buffer.getInt();
        }
    }

    public static class BlockUpdatePacket implements Packet{
        public int health, position;

        @Override
        public void write(ByteBuffer buffer) {
            buffer.putShort((short)health);
            buffer.putInt(position);
        }

        @Override
        public void read(ByteBuffer buffer) {
            health = buffer.getShort();
            position = buffer.getInt();
        }
    }

    public static class ChatPacket implements Packet{
        public String name;
        public String text;
        public int id;

        @Override
        public void write(ByteBuffer buffer) {
            if(name != null) {
                buffer.putShort((short) name.getBytes().length);
                buffer.put(name.getBytes());
            }else{
                buffer.putShort((short)-1);
            }
            IOUtils.writeString(buffer, text);
            buffer.putInt(id);
        }

        @Override
        public void read(ByteBuffer buffer) {
            short nlength = buffer.getShort();
            if(nlength != -1) {
                byte[] n = new byte[nlength];
                buffer.get(n);
                name = new String(n);
            }else{
                name = null;
            }

            text = IOUtils.readString(buffer);
            id = buffer.getInt();
        }
    }

    public static class KickPacket implements Packet, ImportantPacket{
        public KickReason reason;

        @Override
        public void write(ByteBuffer buffer) {
            buffer.put((byte)reason.ordinal());
        }

        @Override
        public void read(ByteBuffer buffer) {
            reason = KickReason.values()[buffer.get()];
        }
    }

    public enum KickReason{
        kick, invalidPassword, clientOutdated, serverOutdated, banned, gameover(true), recentKick, nameInUse, idInUse, fastShoot;
        public final boolean quiet;

        KickReason(){ quiet = false; }

        KickReason(boolean quiet){
            this.quiet = quiet;
        }
    }

    public static class UpgradePacket implements Packet{
        public byte upgradeid; //weapon ID only, currently
        public int playerid;

        @Override
        public void write(ByteBuffer buffer) {
            buffer.put(upgradeid);
            buffer.putInt(playerid);
        }

        @Override
        public void read(ByteBuffer buffer) {
            upgradeid = buffer.get();
            playerid = buffer.getInt();
        }
    }

    public static class WeaponSwitchPacket implements Packet{
        public int playerid;
        public byte weapon;

        @Override
        public void write(ByteBuffer buffer) {
            buffer.putInt(playerid);
            buffer.put(weapon);
        }

        @Override
        public void read(ByteBuffer buffer) {
            playerid = buffer.getInt();
            weapon = buffer.get();
        }
    }

    public static class BlockTapPacket implements Packet{
        public int position, player;
        //todo implement

        @Override
        public void write(ByteBuffer buffer) {
            buffer.putInt(position);
        }

        @Override
        public void read(ByteBuffer buffer) {
            position = buffer.getInt();
        }
    }

    public static class BlockConfigPacket implements Packet{
        public int position;
        public byte data;

        @Override
        public void write(ByteBuffer buffer) {
            buffer.putInt(position);
            buffer.put(data);
        }

        @Override
        public void read(ByteBuffer buffer) {
            position = buffer.getInt();
            data = buffer.get();
        }
    }

    public static class EntityRequestPacket implements Packet{
        public int id;
        public byte group;

        @Override
        public void write(ByteBuffer buffer) {
            buffer.putInt(id);
            buffer.put(group);
        }

        @Override
        public void read(ByteBuffer buffer) {
            id = buffer.getInt();
            group = buffer.get();
        }
    }

    public static class GameOverPacket implements Packet{
        @Override
        public void write(ByteBuffer buffer) { }

        @Override
        public void read(ByteBuffer buffer) { }
    }

    public static class MapAckPacket implements Packet{
        @Override
        public void write(ByteBuffer buffer) { }

        @Override
        public void read(ByteBuffer buffer) { }
    }

    public static class NetErrorPacket implements Packet{
        public String message;

        @Override
        public void write(ByteBuffer buffer) {
            IOUtils.writeString(buffer, message);
        }

        @Override
        public void read(ByteBuffer buffer) {
            message = IOUtils.readString(buffer);
        }
    }

    public static class AdministerRequestPacket implements Packet{
        public AdminAction action;
        public int id;

        @Override
        public void write(ByteBuffer buffer) {
            buffer.put((byte)action.ordinal());
            buffer.putInt(id);
        }

        @Override
        public void read(ByteBuffer buffer) {
            action = AdminAction.values()[buffer.get()];
            id = buffer.getInt();
        }
    }

    public enum AdminAction{
        kick, ban, trace
    }

    public static class TracePacket implements Packet{
        public TraceInfo info;

        @Override
        public void write(ByteBuffer buffer) {
            buffer.putInt(info.playerid);
            IOUtils.writeString(buffer, info.ip);
            buffer.put(info.modclient ? (byte)1 : 0);
            buffer.put(info.android ? (byte)1 : 0);

            buffer.putInt(info.totalBlocksBroken);
            buffer.putInt(info.structureBlocksBroken);
            buffer.putInt(info.lastBlockBroken.id);

            buffer.putInt(info.totalBlocksPlaced);
            buffer.putInt(info.lastBlockPlaced.id);
            buffer.put(Base64Coder.decode(info.uuid));
        }

        @Override
        public void read(ByteBuffer buffer) {
            int id = buffer.getInt();
            String ip = IOUtils.readString(buffer);

            info = new TraceInfo(ip);

            info.playerid = id;
            info.modclient = buffer.get() == 1;
            info.android = buffer.get() == 1;
            info.totalBlocksBroken = buffer.getInt();
            info.structureBlocksBroken = buffer.getInt();
            info.lastBlockBroken = Block.getByID(buffer.getInt());
            info.totalBlocksPlaced = buffer.getInt();
            info.lastBlockPlaced = Block.getByID(buffer.getInt());
            byte[] uuid = new byte[8];
            buffer.get(uuid);

            info.uuid = new String(Base64Coder.encode(uuid));
        }
    }
}
