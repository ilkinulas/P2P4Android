package com.appsonfire.p2p;

import java.io.IOException;

import com.google.appsonfire.gson.Gson;
import com.google.appsonfire.gson.GsonBuilder;
import com.google.appsonfire.gson.JsonElement;
import com.google.appsonfire.gson.TypeAdapter;
import com.google.appsonfire.gson.internal.Streams;
import com.google.appsonfire.gson.stream.JsonReader;
import com.google.appsonfire.gson.stream.JsonWriter;

public abstract class Message {
	protected String klass = getClass().getCanonicalName();
	protected int type;
	protected long sequenceId;
	protected int playerIndex = -1;
	protected String playerName;
	protected boolean ack;
	protected String sourceAddress;

	protected boolean updateTurn;

	private static Gson gson = null;

	static {
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(Message.class, new MessageTypeAdapter().nullSafe());
		gson = gsonBuilder.create();
	}

	public static Message decode(String s) {
		return gson.fromJson(s, Message.class);

	}

	public String encode() {
		return gson.toJson(this);
	}

	public boolean shouldUpdateTurn() {
		return this.updateTurn;
	}

	public long getSequenceId() {
		return sequenceId;
	}

	public void setSequenceId(long sequenceId) {
		this.sequenceId = sequenceId;
	}

	public boolean isAck() {
		return ack;
	}

	public void setAck(boolean ack) {
		this.ack = ack;
	}

	public String getKlass() {
		return klass;
	}

	public int getPlayerIndex() {
		return playerIndex;
	}

	public String getPlayerName() {
		return playerName;
	}

	public void setPlayerIndex(int playerIndex) {
		this.playerIndex = playerIndex;
	}

	public void setPlayerName(String playerName) {
		this.playerName = playerName;
	}

	public String getSourceAddress() {
		return sourceAddress;
	}

	public void setSourceAddress(String sourceAddress) {
		this.sourceAddress = sourceAddress;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public static class MessageTypeAdapter extends TypeAdapter<Message> {
		private Gson gson = new Gson();

		@Override
		public Message read(JsonReader reader) throws IOException {
			JsonElement jsonElement = Streams.parse(reader);
			String type = jsonElement.getAsJsonObject().get("klass").getAsString();
			// Logger.d("Message type is  " + type);
			try {
				String jsonString = jsonElement.getAsJsonObject().toString();
				// Logger.d("Json String for message is " + jsonString);
				return (Message) gson.fromJson(jsonString, Class.forName(type));
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		public void write(JsonWriter writer, Message message) throws IOException {
			writer.value(gson.toJson(message));
		}

	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Message [klass=");
		builder.append(klass);
		builder.append(", type=");
		builder.append(type);
		builder.append(", sequenceId=");
		builder.append(sequenceId);
		builder.append(", playerIndex=");
		builder.append(playerIndex);
		builder.append(", playerName=");
		builder.append(playerName);
		builder.append(", ack=");
		builder.append(ack);
		builder.append(", sourceAddress=");
		builder.append(sourceAddress);
		builder.append("]");
		return builder.toString();
	}
	
	
	
	
	
}
