package monitoring.records;

import java.nio.BufferOverflowException;

import kieker.common.exception.RecordInstantiationException;
import kieker.common.record.AbstractMonitoringRecord;
import kieker.common.record.IMonitoringRecord;
import kieker.common.record.io.IValueDeserializer;
import kieker.common.record.io.IValueSerializer;
import kieker.common.util.registry.IRegistry;


/**
 * @author Generic Kieker
 * API compatibility: Kieker 1.13.0
 * 
 * @since 1.13
 */
public class ResponseTimeRecord extends AbstractMonitoringRecord implements IMonitoringRecord.Factory, IMonitoringRecord.BinaryFactory {			
	/** Descriptive definition of the serialization size of the record. */
	public static final int SIZE = TYPE_SIZE_STRING // ResponseTimeRecord.sessionId
			 + TYPE_SIZE_STRING // ResponseTimeRecord.serviceExecutionId
			 + TYPE_SIZE_STRING // ResponseTimeRecord.resourceDemandId
			 + TYPE_SIZE_LONG // ResponseTimeRecord.startTime
			 + TYPE_SIZE_LONG; // ResponseTimeRecord.stopTime
	
	public static final Class<?>[] TYPES = {
		String.class, // ResponseTimeRecord.sessionId
		String.class, // ResponseTimeRecord.serviceExecutionId
		String.class, // ResponseTimeRecord.resourceDemandId
		long.class, // ResponseTimeRecord.startTime
		long.class, // ResponseTimeRecord.stopTime
	};
	
	/** default constants. */
	public static final String SESSION_ID = "<not set>";
	public static final String SERVICE_EXECUTION_ID = "<not set>";
	public static final String RESOURCE_DEMAND_ID = "<not set>";
	private static final long serialVersionUID = -5639455367684007842L;
	
	/** property name array. */
	private static final String[] PROPERTY_NAMES = {
		"sessionId",
		"serviceExecutionId",
		"resourceDemandId",
		"startTime",
		"stopTime",
	};
	
	/** property declarations. */
	private final String sessionId;
	private final String serviceExecutionId;
	private final String resourceDemandId;
	private final long startTime;
	private final long stopTime;
	
	/**
	 * Creates a new instance of this class using the given parameters.
	 * 
	 * @param sessionId
	 *            sessionId
	 * @param serviceExecutionId
	 *            serviceExecutionId
	 * @param resourceDemandId
	 *            resourceDemandId
	 * @param startTime
	 *            startTime
	 * @param stopTime
	 *            stopTime
	 */
	public ResponseTimeRecord(final String sessionId, final String serviceExecutionId, final String resourceDemandId, final long startTime, final long stopTime) {
		this.sessionId = sessionId == null?SESSION_ID:sessionId;
		this.serviceExecutionId = serviceExecutionId == null?SERVICE_EXECUTION_ID:serviceExecutionId;
		this.resourceDemandId = resourceDemandId == null?RESOURCE_DEMAND_ID:resourceDemandId;
		this.startTime = startTime;
		this.stopTime = stopTime;
	}

	/**
	 * This constructor converts the given array into a record.
	 * It is recommended to use the array which is the result of a call to {@link #toArray()}.
	 * 
	 * @param values
	 *            The values for the record.
	 *
	 * @deprecated to be removed 1.15
	 */
	@Deprecated
	public ResponseTimeRecord(final Object[] values) { // NOPMD (direct store of values)
		AbstractMonitoringRecord.checkArray(values, TYPES);
		this.sessionId = (String) values[0];
		this.serviceExecutionId = (String) values[1];
		this.resourceDemandId = (String) values[2];
		this.startTime = (Long) values[3];
		this.stopTime = (Long) values[4];
	}

	/**
	 * This constructor uses the given array to initialize the fields of this record.
	 * 
	 * @param values
	 *            The values for the record.
	 * @param valueTypes
	 *            The types of the elements in the first array.
	 *
	 * @deprecated to be removed 1.15
	 */
	@Deprecated
	protected ResponseTimeRecord(final Object[] values, final Class<?>[] valueTypes) { // NOPMD (values stored directly)
		AbstractMonitoringRecord.checkArray(values, valueTypes);
		this.sessionId = (String) values[0];
		this.serviceExecutionId = (String) values[1];
		this.resourceDemandId = (String) values[2];
		this.startTime = (Long) values[3];
		this.stopTime = (Long) values[4];
	}

	
	/**
	 * @param deserializer
	 *            The deserializer to use
	 * @throws RecordInstantiationException 
	 *            when the record could not be deserialized
	 */
	public ResponseTimeRecord(final IValueDeserializer deserializer) throws RecordInstantiationException {
		this.sessionId = deserializer.getString();
		this.serviceExecutionId = deserializer.getString();
		this.resourceDemandId = deserializer.getString();
		this.startTime = deserializer.getLong();
		this.stopTime = deserializer.getLong();
	}
	
	/**
	 * {@inheritDoc}
	 *
	 * @deprecated to be removed in 1.15
	 */
	@Override
	@Deprecated
	public Object[] toArray() {
		return new Object[] {
			this.getSessionId(),
			this.getServiceExecutionId(),
			this.getResourceDemandId(),
			this.getStartTime(),
			this.getStopTime(),
		};
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void registerStrings(final IRegistry<String> stringRegistry) {	// NOPMD (generated code)
		stringRegistry.get(this.getSessionId());
		stringRegistry.get(this.getServiceExecutionId());
		stringRegistry.get(this.getResourceDemandId());
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void serialize(final IValueSerializer serializer) throws BufferOverflowException {
		//super.serialize(serializer);
		serializer.putString(this.getSessionId());
		serializer.putString(this.getServiceExecutionId());
		serializer.putString(this.getResourceDemandId());
		serializer.putLong(this.getStartTime());
		serializer.putLong(this.getStopTime());
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<?>[] getValueTypes() {
		return TYPES; // NOPMD
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String[] getValueNames() {
		return PROPERTY_NAMES; // NOPMD
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getSize() {
		return SIZE;
	}

	/**
	 * {@inheritDoc}
	 * 
	 * @deprecated to be rmeoved in 1.15
	 */
	@Override
	@Deprecated
	public void initFromArray(final Object[] values) {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(final Object obj) {
		if (obj == null) {
			return false;
		}
		if (obj == this) {
			return true;
		}
		if (obj.getClass() != this.getClass()) {
			return false;
		}
		
		final ResponseTimeRecord castedRecord = (ResponseTimeRecord) obj;
		if (this.getLoggingTimestamp() != castedRecord.getLoggingTimestamp()) {
			return false;
		}
		if (!this.getSessionId().equals(castedRecord.getSessionId())) {
			return false;
		}
		if (!this.getServiceExecutionId().equals(castedRecord.getServiceExecutionId())) {
			return false;
		}
		if (!this.getResourceDemandId().equals(castedRecord.getResourceDemandId())) {
			return false;
		}
		if (this.getStartTime() != castedRecord.getStartTime()) {
			return false;
		}
		if (this.getStopTime() != castedRecord.getStopTime()) {
			return false;
		}
		
		return true;
	}
	
	public final String getSessionId() {
		return this.sessionId;
	}
	
	
	public final String getServiceExecutionId() {
		return this.serviceExecutionId;
	}
	
	
	public final String getResourceDemandId() {
		return this.resourceDemandId;
	}
	
	
	public final long getStartTime() {
		return this.startTime;
	}
	
	
	public final long getStopTime() {
		return this.stopTime;
	}
	
}
