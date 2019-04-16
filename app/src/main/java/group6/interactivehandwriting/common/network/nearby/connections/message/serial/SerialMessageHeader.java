package group6.interactivehandwriting.common.network.nearby.connections.message.serial;

import java.nio.ByteBuffer;

import group6.interactivehandwriting.common.network.nearby.connections.message.NetworkMessageType;

/**
 * Created by JakeL on 10/19/18.
 */

public class SerialMessageHeader implements NetworkSerializable<SerialMessageHeader> {
    public static final int BYTE_SIZE = 22;
    private NetworkMessageType type;
    private int roomNumber;
    private int sequenceNumber;
    private long deviceId;
    private byte bigData;
    private byte pageCount;

    private static int globalSequenceNumber;

    static {
        globalSequenceNumber = 0;
    }


    public static int getNextSequenceNumber() {
        globalSequenceNumber += 1;
        return globalSequenceNumber;
    }

    public SerialMessageHeader withId(long deviceId) {
        this.deviceId = deviceId;
        return this;
    }

    public SerialMessageHeader withType(NetworkMessageType type) {
        this.type = type;
        return this;
    }

    public SerialMessageHeader withRoomNumber(int roomNumber) {
        this.roomNumber = roomNumber;
        return this;
    }

    public SerialMessageHeader withSequenceNumber(int sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
        return this;
    }

    // 0 - array size is less than max
    // 1 - part of larger set of data
    // 2 - last piece of larger data set
    public SerialMessageHeader withBigData(byte bigData) {
        this.bigData = bigData;
        return this;
    }

    public SerialMessageHeader withPageCount(byte pageCount) {
        this.pageCount = pageCount;
        return this;
    }

    @Override
    public byte[] toBytes() {
        ByteBuffer buffer = ByteBuffer.allocate(getByteBufferSize());
        buffer.putInt(type.getValue());
        buffer.putInt(roomNumber);
        buffer.putInt(SerialMessageHeader.getNextSequenceNumber());
        buffer.putLong(deviceId);
        buffer.put(bigData);
        return buffer.array();
    }

    @Override
    public SerialMessageHeader fromBytes(byte[] header) {
        ByteBuffer buffer = ByteBuffer.wrap(header);
        type = NetworkMessageType.get(buffer.getInt());
        roomNumber = buffer.getInt();
        sequenceNumber = buffer.getInt();
        deviceId = buffer.getLong();
        bigData = buffer.get();
        return this;
    }

    @Override
    public int getByteBufferSize() {
        return BYTE_SIZE;
    }

    @Override
    public NetworkMessageType getType() {
        return type;
    }

    public long getDeviceId() {
        return deviceId;
    }

    public int getRoomNumber() { return roomNumber; }

    public byte getBigData() {
        return bigData;
    }
}
